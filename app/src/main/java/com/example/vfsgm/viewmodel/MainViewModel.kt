package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.core.CfCookieCheckManager
import com.example.vfsgm.core.FirebaseDataService
import com.example.vfsgm.core.FirebaseLogService
import com.example.vfsgm.core.JitterService
import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.core.TurnstileService
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.data.api.CalenderApi
import com.example.vfsgm.data.api.LeasedAccountApi
import com.example.vfsgm.data.api.SlotApi
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.data.dto.Entry
import com.example.vfsgm.data.dto.JobState
import com.example.vfsgm.data.dto.SessionData
import com.example.vfsgm.data.network.PublicIpManager
import com.example.vfsgm.data.repository.AppConfigRepository
import com.example.vfsgm.data.repository.DataRepository
import com.example.vfsgm.data.repository.EntryRepository
import com.example.vfsgm.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MainViewModel(application: Application) : BaseViewModel(application) {
    init {
        viewModelScope.launch {
            // load ip
            PublicIpManager.init { ip ->
                println("IP loaded: $ip")
                FirebaseLogService.log(
                    appConfigState.value.deviceIndex, "IP loaded: $ip"
                )
            }
        }

        viewModelScope.launch {
            // Load/reload entry whenever entryIndex changes.
            appConfigState
                .map { it.entryIndex }
                .distinctUntilChanged()
                .collect { entryIndex ->
                    try {
                        entryRepository.loadEntry(entryIndex = entryIndex)
                        val loadedEntry = entryState.value
                        println("Loaded entry for entryIndex=$entryIndex: $loadedEntry")
                        FirebaseLogService.log(
                            appConfigState.value.deviceIndex,
                            "Loaded entry for entryIndex=$entryIndex: $loadedEntry"
                        )
                    } catch (e: Exception) {
                        println("Failed to load entry for entryIndex=$entryIndex: ${e.message}")
                        FirebaseLogService.log(
                            appConfigState.value.deviceIndex,
                            "Failed to load entry for entryIndex=$entryIndex: ${e.message}"
                        )
                    }
                }
        }
    }


    fun stopAllChildJob() {
        checkSlotJob?.cancel()
        checkSlotJob = null
    }

    fun startPeriodicReLogin() {
        FirebaseLogService.log(
            appConfigState.value.deviceIndex, "startPeriodicReLogin called"
        )
        if (reLoginJob?.isActive == true) {
            FirebaseLogService.log(
                appConfigState.value.deviceIndex,
                "startPeriodicReLogin returning job already active"
            )
            return
        }

        dataRepository.updateReLoginJobState(JobState.IN_PROGRESS)

        reLoginJob = viewModelScope.launch {
            while (isActive) {
                try {
                    FirebaseLogService.log(
                        appConfigState.value.deviceIndex, "ReLoginJob Logout called"
                    )
                    logout()

                    FirebaseLogService.log(
                        appConfigState.value.deviceIndex, "ReLoginJob Starting wait for CF cookie"
                    )
                    CfCookieCheckManager.waitUntilCfCookieKeyExists()

                    FirebaseLogService.log(
                        appConfigState.value.deviceIndex, "ReLoginJob Starting Login"
                    )
                    login {
                        addApplicant()

                        loadTimeSlot()

                        startCheckIsSlotAvailable()
                    }
                } catch (e: Exception) {
                    FirebaseLogService.log(
                        appConfigState.value.deviceIndex, "ReLoginJob catch block ${e.message}"
                    )
                    e.printStackTrace()
                }

                delay(25 * 60 * 1000L) // 15 minutes
            }
        }
    }

    fun stopPeriodicReLogin() {
        FirebaseLogService.log(
            appConfigState.value.deviceIndex, "stopPeriodicReLogin called"
        )

        reLoginJob?.cancel()
        reLoginJob = null
        dataRepository.updateReLoginJobState(JobState.STOPPED)
    }

    private suspend fun attemptLoginOnce(entry: Entry): Boolean {
        FirebaseLogService.log(
            appConfigState.value.deviceIndex, "attemptLoginOnce called -> entry: $entry"
        )

        val leasedAccount = when (val res = leasedAccountApi.leaseAccount(entry)) {
            is SealedResult.Success -> res.data
            is SealedResult.Error -> null
        } ?: run {
            println("Lease failed: no account found")
            FirebaseLogService.log(
                appConfigState.value.deviceIndex, "Lease failed: no account found"
            )
            return false
        }

        println("LeasedAccount: $leasedAccount")
        FirebaseLogService.log(
            appConfigState.value.deviceIndex, "LeasedAccount: $leasedAccount"
        )

        val cloudflareToken = TurnstileService.solveTurnstile() ?: run {
            println("Cloudflare token load failed")
            FirebaseLogService.log(
                appConfigState.value.deviceIndex, "Cloudflare token load failed"
            )
            return false
        }

        val accessToken = authApi.login(
            username = leasedAccount.email,
            password = leasedAccount.password,
            cloudflareToken = cloudflareToken,
            appConfig = appConfigState.value
        )

        if (accessToken.isNullOrEmpty()) {
            println("Login failed for ${leasedAccount.email}")
            FirebaseLogService.log(
                appConfigState.value.deviceIndex,
                "Login failed for ${leasedAccount.email}. Reporting Block"
            )
            leasedAccountApi.reportBlock(leasedAccount.email, entry = entry)

            return false
        }


        FirebaseLogService.log(
            appConfigState.value.deviceIndex, "Save session data -> email: ${leasedAccount.email}"
        )

        sessionRepository.saveSessionData(
            SessionData(
                accessToken = accessToken, username = leasedAccount.email
            )
        )
        return true
    }

    fun login(onLoginComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = entryState.value

            val maxAttempts = 5
            var delayMs = 1000L

            repeat(maxAttempts) { attemptIndex ->
                if (!isActive) return@launch

                val ok = try {
                    attemptLoginOnce(entry)
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
            // TODO sent a critical alert that a device failed to login
            FirebaseLogService.log(
                appConfigState.value.deviceIndex, "Login failed after $maxAttempts attempts."
            )
        }
    }

    fun loadApplicants() {
        val sessionData = sessionState.value ?: return
        val entry = entryState.value

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.loadApplicants(
                sessionData = sessionData, entry = entry, appConfig = appConfigState.value
            )
        }
    }

    fun addApplicant() {
        val sessionData = sessionState.value ?: return
        val entry = entryState.value

        viewModelScope.launch(Dispatchers.IO) {
            val urn = applicantApi.addApplicant(
                sessionData = sessionData, entry = entry, appConfig = appConfigState.value
            )

            dataRepository.saveUrn(urn = urn)
        }
    }

    fun loadCalender() {
        val sessionData = sessionState.value ?: return
        val entry = entryState.value

        viewModelScope.launch(Dispatchers.IO) {
            val result = calenderApi.loadCalender(
                sessionData = sessionData, entry = entry, urn = dataState.value.urn
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
        // Prevent double-start
        if (checkSlotJob?.isActive == true) return


        println("startCheckIsSlotAvailable")
        FirebaseLogService.log(
            appConfigState.value.deviceIndex, "startCheckIsSlotAvailable"
        )
        val sessionData = sessionState.value ?: return
        val entry = entryState.value


        // change the job state
        dataRepository.updateCheckSlotJobState(JobState.IN_PROGRESS)

        checkSlotJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val result = calenderApi.checkIsSlotAvailable(
                    sessionData = sessionData,
                    entry = entryState.value,
                    appConfig = appConfigState.value
                )

                when (result) {
                    is SealedResult.Success -> {
                        println("earliest Date Available at: ${result.data}")
                        result.data?.let {
                            dataRepository.saveEarliestSlotDates(it)

                            FirebaseDataService.saveEarliestSlotDate(
                                date = result.data, entry = entry
                            )
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


    fun loadTimeSlot() {
        // Prevent double-start
        if (loadSlotSlob?.isActive == true) return

        println("loadTimeSlot")
        FirebaseLogService.log(
            appConfigState.value.deviceIndex, "loadTimeSlot"
        )

        val sessionData = sessionState.value ?: return
        val entry = entryState.value

        loadSlotSlob = viewModelScope.launch(Dispatchers.IO) {
            val earliestSlotDate = FirebaseDataService.readEarliestSlotDate(
                entry = entry,
            )

            val loadSlotResult = slotApi.loadSlots(
                sessionData = sessionData,
                entry = entry,
                urn = dataState.value.urn,
                slotDate = earliestSlotDate,
                appConfig = appConfigState.value
            )

            when (loadSlotResult) {
                is SealedResult.Success -> {
                    dataRepository.saveAllocationIds(loadSlotResult.data)
                    startSchedule(loadSlotResult.data)


                    FirebaseLogService.log(
                        appConfigState.value.deviceIndex,
                        "Earliest Slot Date received via firebase: $earliestSlotDate"
                    )
                }

                is SealedResult.Error -> {
                    println(loadSlotResult.exception.message)
                }
            }


            println("Earliest Slot Date received via firebase: $earliestSlotDate")
        }
    }

    fun stopLoadTimeSlot() {
        loadSlotSlob?.cancel()
        // change the job state
        dataRepository.updateLoadSlotJobState(JobState.STOPPED)
    }


    fun startSchedule(allocationIds: List<String>) {
        println("startSchedule: $allocationIds")
    }


    fun logout() {
        println("Logout")
        viewModelScope.launch(Dispatchers.IO) {
            stopAllChildJob()
            sessionRepository.clearSession()
        }
    }

    fun updateAppConfig(appConfig: AppConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            appConfigRepository.updateAppConfig(appConfig = appConfig)
        }
    }
}
