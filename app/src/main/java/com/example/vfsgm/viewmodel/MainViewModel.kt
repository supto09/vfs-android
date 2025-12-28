package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.core.CfCookieCheckManager
import com.example.vfsgm.core.JitterService
import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.core.TurnstileService
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.data.api.CalenderApi
import com.example.vfsgm.data.api.SubjectApi
import com.example.vfsgm.data.dto.JobState
import com.example.vfsgm.data.dto.SessionData
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.data.network.PublicIpManager
import com.example.vfsgm.data.repository.DataRepository
import com.example.vfsgm.data.repository.SessionRepository
import com.example.vfsgm.data.repository.SubjectRepository
import com.example.vfsgm.data.store.TurnstileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionRepository = SessionRepository(application.applicationContext)
    val sessionState = sessionRepository.state

    private val subjectRepository = SubjectRepository()
    val subjectState = subjectRepository.state

    private val dataRepository = DataRepository()
    val dataState = dataRepository.state


    private val subjectApi = SubjectApi()
    private val authApi = AuthApi()
    private val applicantApi = ApplicantApi()
    private val calenderApi = CalenderApi()
    private val jitterService = JitterService()


    // jobs
    private var reLoginJob: Job? = null
    private var checkSlotJob: Job? = null

    init {
        viewModelScope.launch {
            // load ip
            PublicIpManager.init { ip ->
                println("IP loaded: $ip")
            }
        }

        viewModelScope.launch {
            // load subject data
            subjectRepository.loadSubject()
        }

        startPeriodicReLogin()
    }


    fun stopAllJob() {
        checkSlotJob?.cancel()
        checkSlotJob = null
    }

    fun startPeriodicReLogin() {
        if (reLoginJob?.isActive == true) return

        reLoginJob = viewModelScope.launch {
            while (isActive) {
                try {
                    logout()
                    CfCookieCheckManager.waitUntilCfCookieKeyExists()
                    login {
//                        startCheckIsSlotAvailable()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(25 * 60 * 1000L) // 15 minutes
            }
        }
    }

    fun stopPeriodicReLogin() {
        reLoginJob?.cancel()
        reLoginJob = null
    }


    private suspend fun attemptLoginOnce(subject: Subject): Boolean {
        val leasedAccount = when (val res = subjectApi.leaseAccount(subject)) {
            is SealedResult.Success -> res.data
            is SealedResult.Error -> null
        } ?: run {
            println("Lease failed: no account found")
            return false
        }

        println("LeasedAccount: $leasedAccount")

        val cloudflareToken = TurnstileService.solveTurnstile() ?: run {
            println("Cloudflare token load failed")
            return false
        }

        val accessToken = authApi.login(
            username = leasedAccount.email,
            password = leasedAccount.password,
            cloudflareToken = cloudflareToken
        )

        if (accessToken.isNullOrEmpty()) {
            println("Login failed for ${leasedAccount.email}")
            subjectApi.reportBlock(leasedAccount.email, subject = subject)

            return false
        }

        sessionRepository.saveSessionData(
            SessionData(
                accessToken = accessToken,
                username = leasedAccount.email
            )
        )
        return true
    }

    fun login(onLoginComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val subject = subjectState.value

            val maxAttempts = 5
            var delayMs = 1000L

            repeat(maxAttempts) { attemptIndex ->
                if (!isActive) return@launch

                val ok = try {
                    attemptLoginOnce(subject)
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }

                if (ok) {
                    delay(1000L)
                    withContext(Dispatchers.Main) { onLoginComplete?.invoke() }
                    return@launch
                }

                val isLast = attemptIndex == maxAttempts - 1
                if (!isLast) {
                    // simple backoff
                    delay(delayMs)
                    delayMs = (delayMs * 2).coerceAtMost(15_000L)
                }
            }

            println("Login failed after $maxAttempts attempts.")
        }
    }

    fun loadApplicants() {
        val sessionData = sessionState.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.loadApplicants(
                accessToken = sessionData.accessToken,
                username = sessionData.username,
            )
        }
    }

    fun addApplicant() {
        val sessionData = sessionState.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val urn = applicantApi.addApplicant(
                accessToken = sessionData.accessToken,
                username = sessionData.username,
                subject = subjectState.value
            )

            dataRepository.saveUrn(urn = urn)
        }
    }


    fun loadCalender() {
        val sessionData = sessionState.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val result = calenderApi.loadCalender(
                accessToken = sessionData.accessToken,
                username = sessionData.username,
                subject = subjectState.value,
                urn = dataState.value.urn
            )

            when (result) {
                is SealedResult.Success -> dataRepository.saveAvailableDates(dates = result.data)
                is SealedResult.Error -> {
                    println(result.exception.message)
                }
            }
        }
    }


    fun startCheckIsSlotAvailable() {
        println("startCheckIsSlotAvailable")
        val sessionData = sessionState.value ?: return

        // Prevent double-start
        if (checkSlotJob?.isActive == true) return

        // change the job state
        dataRepository.updateCheckSlotJobState(JobState.IN_PROGRESS)

        checkSlotJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val result = calenderApi.checkIsSlotAvailable(
                    accessToken = sessionData.accessToken,
                    username = sessionData.username,
                    subject = subjectState.value,
                )

                when (result) {
                    is SealedResult.Success -> {
                        println("earliest Date Available at: ${result.data}")
                        result.data?.let {
                            dataRepository.saveEarliestSlotDates(it)
                        }
                    }

                    is SealedResult.Error -> {
                        println(result.exception.message)
                    }
                }

                // ⏱ wait 3 min AFTER completion
                delay((3 * 60_000L) + jitterService.nextDelayMillis())
            }
        }
    }


    fun stopCheckIsSlotAvailable() {
        checkSlotJob?.cancel()
        // change the job state
        dataRepository.updateCheckSlotJobState(JobState.STOPPED)
    }


    fun logout() {
        println("Logout")
        viewModelScope.launch(Dispatchers.IO) {
            stopAllJob()
            sessionRepository.clearSession()
        }
    }
}
