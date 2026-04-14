package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.core.CfCookieCheckManager
import com.example.vfsgm.core.FirebaseDataService
import com.example.vfsgm.core.logging.AppLogService
import com.example.vfsgm.core.logging.DeviceIndexContext
import com.example.vfsgm.core.logging.LogType
import com.example.vfsgm.core.JitterService
import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.core.TurnstileService
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.data.api.CalenderApi
import com.example.vfsgm.data.api.LeasedAccountApi
import com.example.vfsgm.data.api.LoginOutcome
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MainViewModel(application: Application) : BaseViewModel(application) {
    private companion object {
        const val ADD_APPLICANT_MAX_ATTEMPTS = 5
        const val ADD_APPLICANT_RETRY_DELAY_MS = 10_000L
        const val FIXED_LOGIN_OTP = "658992"
        const val TOTAL_FOLLOWER_APPS = 5
        const val FOLLOWER_PRIORITY_COUNT = 3
        const val FOLLOWER_MAX_DATES = 3
        const val FOLLOWER_SLOT_ROUNDS = 2
        const val FINDER_SLOT_RETRY_COUNT = 3
        const val SLOT_LOAD_RETRY_DELAY_MS = 3_000L
    }

    private sealed interface LoginAttemptOutcome {
        data object Success : LoginAttemptOutcome
        data object Failure : LoginAttemptOutcome
    }

    private sealed interface SlotLoadAttemptOutcome {
        data class Success(val allocationIds: List<String>) : SlotLoadAttemptOutcome
        data class Failure(val reason: String) : SlotLoadAttemptOutcome
    }

    @Volatile
    private var isCurrentCycleFinder: Boolean = false

    @Volatile
    private var currentCycleEarliestDate: String? = null

    @Volatile
    private var followerPriorityDates: List<String> = emptyList()

    private fun resetCycleCoordinationState(reason: String) {
        isCurrentCycleFinder = false
        currentCycleEarliestDate = null
        followerPriorityDates = emptyList()
        vmLog(
            "Cycle coordination state reset",
            LogType.DEBUG,
            metadata = mapOf("reason" to reason)
        )
    }

    private fun computeFollowerPriorityDates(
        availableDates: List<String>,
        deviceIndex: Int
    ): List<String> {
        val uniqueDates = availableDates.distinct()
        val dateCount = uniqueDates.size
        if (dateCount == 0) return emptyList()

        val bucketSize = minOf(TOTAL_FOLLOWER_APPS, dateCount)
        val startOffset = ((deviceIndex - 1).coerceAtLeast(0)) % bucketSize
        val targetCount = minOf(FOLLOWER_PRIORITY_COUNT, dateCount)

        val prioritized = ArrayList<String>(targetCount)
        var step = 0
        while (prioritized.size < targetCount && step < dateCount) {
            val idx = (startOffset + step) % dateCount
            val date = uniqueDates[idx]
            if (prioritized.none { it == date }) prioritized.add(date)
            step += 1
        }
        return prioritized
    }

    private fun buildFollowerSlotAttemptDates(priorityDates: List<String>): List<String> {
        val baseDates = priorityDates
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(FOLLOWER_MAX_DATES)
            .toList()
        if (baseDates.isEmpty()) return emptyList()

        return buildList(baseDates.size * FOLLOWER_SLOT_ROUNDS) {
            repeat(FOLLOWER_SLOT_ROUNDS) {
                addAll(baseDates)
            }
        }
    }

    private suspend fun loadSlotsOnce(
        sessionData: SessionData,
        entry: Entry,
        slotDate: String
    ): SlotLoadAttemptOutcome {
        val loadSlotResult = slotApi.loadSlots(
            sessionData = sessionData,
            entry = entry,
            urn = dataState.value.urn,
            slotDate = slotDate,
            appConfig = appConfigState.value
        )
        return when (loadSlotResult) {
            is SealedResult.Success -> {
                if (loadSlotResult.data.isEmpty()) {
                    SlotLoadAttemptOutcome.Failure("empty allocations")
                } else {
                    SlotLoadAttemptOutcome.Success(loadSlotResult.data)
                }
            }

            is SealedResult.Error -> {
                SlotLoadAttemptOutcome.Failure(loadSlotResult.exception.message ?: "unknown")
            }
        }
    }

    private suspend fun runLoadSlotAttempts(
        sessionData: SessionData,
        entry: Entry,
        attemptDates: List<String>,
        baseDateCount: Int
    ) {
        var succeeded = false

        for ((index, slotDate) in attemptDates.withIndex()) {
            val attemptNo = index + 1
            val roundNo = if (baseDateCount > 0) (index / baseDateCount) + 1 else 1
            vmLog(
                "loadTimeSlot attempt started",
                LogType.DEBUG,
                metadata = mapOf(
                    "attempt" to attemptNo.toString(),
                    "round" to roundNo.toString(),
                    "slotDate" to slotDate
                )
            )

            val outcome = loadSlotsOnce(
                sessionData = sessionData,
                entry = entry,
                slotDate = slotDate
            )
            when (outcome) {
                is SlotLoadAttemptOutcome.Success -> {
                    dataRepository.saveAllocationIds(outcome.allocationIds)
                    startSchedule(outcome.allocationIds)
                    vmLog(
                        "loadTimeSlot success",
                        LogType.SUCCESS,
                        metadata = mapOf(
                            "attempt" to attemptNo.toString(),
                            "round" to roundNo.toString(),
                            "slotDate" to slotDate,
                            "allocationCount" to outcome.allocationIds.size.toString()
                        )
                    )
                    succeeded = true
                    return
                }

                is SlotLoadAttemptOutcome.Failure -> {
                    vmLog(
                        "loadTimeSlot attempt failed",
                        LogType.WARNING,
                        metadata = mapOf(
                            "attempt" to attemptNo.toString(),
                            "round" to roundNo.toString(),
                            "slotDate" to slotDate,
                            "reason" to outcome.reason
                        )
                    )
                    if (attemptNo < attemptDates.size) {
                        delay(SLOT_LOAD_RETRY_DELAY_MS)
                    }
                }
            }
        }

        if (!succeeded) {
            vmLog(
                "loadTimeSlot exhausted all attempts",
                LogType.ERROR,
                metadata = mapOf("attemptCount" to attemptDates.size.toString())
            )
        }
    }

    @Synchronized
    private fun startLoadSlotJob(
        sessionData: SessionData,
        entry: Entry,
        attemptDates: List<String>,
        baseDateCount: Int
    ) {
        if (loadSlotSlob?.isActive == true) {
            vmLog("loadTimeSlot ignored because job is already active", LogType.WARNING)
            return
        }

        if (attemptDates.isEmpty()) {
            vmLog("loadTimeSlot skipped: no attempt dates", LogType.WARNING)
            return
        }

        dataRepository.updateLoadSlotJobState(JobState.IN_PROGRESS)
        vmLog("Load slot job state set to IN_PROGRESS", LogType.DEBUG)

        loadSlotSlob = viewModelScope.launch(Dispatchers.IO) {
            try {
                runLoadSlotAttempts(
                    sessionData = sessionData,
                    entry = entry,
                    attemptDates = attemptDates,
                    baseDateCount = baseDateCount
                )
            } catch (e: CancellationException) {
                vmLog("loadSlotJob cancelled", LogType.DEBUG)
                throw e
            } finally {
                loadSlotSlob = null
                dataRepository.updateLoadSlotJobState(JobState.STOPPED)
                vmLog("Load slot job state set to STOPPED", LogType.DEBUG)
            }
        }
    }

    private fun vmLog(
        message: String,
        type: LogType = LogType.INFO,
        critical: Boolean = false,
        metadata: Map<String, String> = emptyMap()
    ) {
        AppLogService.log(
            deviceIndex = appConfigState.value.deviceIndex,
            message = message,
            logType = type,
            tag = "MainViewModel",
            metadata = metadata,
            critical = critical
        )
    }

    init {
        viewModelScope.launch {
            vmLog("Initializing public IP loader", LogType.DEBUG)
            // load ip
            PublicIpManager.init { ip ->
                println("IP loaded: $ip")
                vmLog("IP loaded", LogType.SUCCESS, metadata = mapOf("ip" to ip))
            }
        }

        viewModelScope.launch {
            appConfigState
                .map { it.deviceIndex }
                .distinctUntilChanged()
                .collect { deviceIndex ->
                    DeviceIndexContext.set(deviceIndex)
                    vmLog(
                        "Device index context updated",
                        LogType.DEBUG,
                        metadata = mapOf("deviceIndex" to deviceIndex.toString())
                    )
                }
        }

        viewModelScope.launch {
            vmLog("Started appConfig entryIndex observer", LogType.DEBUG)
            // Load/reload entry whenever entryIndex changes.
            appConfigState
                .map { it.entryIndex }
                .distinctUntilChanged()
                .collect { entryIndex ->
                    vmLog(
                        "Loading entry for changed entryIndex",
                        metadata = mapOf("entryIndex" to entryIndex.toString())
                    )
                    try {
                        entryRepository.loadEntry(entryIndex = entryIndex)
                        val loadedEntry = entryState.value
                        println("Loaded entry for entryIndex=$entryIndex: $loadedEntry")
                        vmLog(
                            "Entry loaded",
                            LogType.SUCCESS,
                            metadata = mapOf("entryIndex" to entryIndex.toString())
                        )
                    } catch (e: Exception) {
                        println("Failed to load entry for entryIndex=$entryIndex: ${e.message}")
                        vmLog(
                            "Failed to load entry",
                            LogType.ERROR,
                            critical = true,
                            metadata = mapOf(
                                "entryIndex" to entryIndex.toString(),
                                "error" to (e.message ?: "unknown")
                            )
                        )
                    }
                }
        }
    }


    fun stopAllChildJob() {
        vmLog("Stopping child jobs", LogType.WARNING)
        resetCycleCoordinationState("stopAllChildJob")
        loginJob?.cancel()
        loginJob = null
        verifyOtpJob?.cancel()
        verifyOtpJob = null
        loadApplicantsJob?.cancel()
        loadApplicantsJob = null
        addApplicantJob?.cancel()
        addApplicantJob = null
        loadCalenderJob?.cancel()
        loadCalenderJob = null
        dataRepository.updateLoginJobState(JobState.STOPPED)
        dataRepository.updateVerifyOtpJobState(JobState.STOPPED)
        dataRepository.updateLoadApplicantsJobState(JobState.STOPPED)
        dataRepository.updateAddApplicantJobState(JobState.STOPPED)
        dataRepository.updateLoadCalenderJobState(JobState.STOPPED)
        dataRepository.updateOtpVerificationRequired(false)
        checkSlotJob?.cancel()
        checkSlotJob = null
        loadSlotSlob?.cancel()
        loadSlotSlob = null
    }

    fun startPeriodicReLogin() {
        vmLog("startPeriodicReLogin called")
        if (reLoginJob?.isActive == true) {
            vmLog("startPeriodicReLogin ignored because job is already active", LogType.WARNING)
            return
        }

        dataRepository.updateReLoginJobState(JobState.IN_PROGRESS)
        vmLog("ReLogin job state set to IN_PROGRESS", LogType.DEBUG)

        reLoginJob = viewModelScope.launch {
            vmLog("ReLogin loop started")
            while (isActive) {
                try {
                    resetCycleCoordinationState("reLogin cycle start")
                    vmLog("ReLogin cycle: logout start", LogType.DEBUG)
                    stopAllChildJob()
                    sessionRepository.clearSession()
                    vmLog("ReLogin cycle: session cleared", LogType.DEBUG)

                    vmLog("ReLogin cycle: waiting for CF cookie", LogType.DEBUG)
                    CfCookieCheckManager.waitUntilCfCookieKeyExists()

                    vmLog("ReLogin cycle: login start", LogType.DEBUG)
                    login {
                        vmLog(
                            "ReLogin callback: login complete, triggering applicant+slot jobs",
                            LogType.SUCCESS
                        )
                        viewModelScope.launch(Dispatchers.IO) {
                            addApplicant(triggerSlotFlowOnSuccess = true)
                        }
                    }
                } catch (e: Exception) {
                    vmLog(
                        "ReLogin cycle failed",
                        LogType.ERROR,
                        critical = true,
                        metadata = mapOf("error" to (e.message ?: "unknown"))
                    )
                    e.printStackTrace()
                }

                delay(25 * 60 * 1000L) // 15 minutes
            }
        }
    }

    fun stopPeriodicReLogin() {
        vmLog("stopPeriodicReLogin called", LogType.WARNING)

        reLoginJob?.cancel()
        reLoginJob = null
        dataRepository.updateReLoginJobState(JobState.STOPPED)
        vmLog("ReLogin job state set to STOPPED", LogType.DEBUG)
    }

    private suspend fun attemptLoginOnce(entry: Entry): LoginAttemptOutcome {
        vmLog(
            "attemptLoginOnce called",
            LogType.DEBUG,
            metadata = mapOf(
                "countryCode" to entry.countryCode.toString(),
                "missionCode" to entry.missionCode.toString()
            )
        )

        val res = leasedAccountApi.leaseAccount(entry)
        val leasedAccount = when (res) {
            is SealedResult.Success -> res.data
            is SealedResult.Error -> null
        } ?: run {
            vmLog("Lease failed: no account found", LogType.ERROR, critical = true)
            return LoginAttemptOutcome.Failure
        }

        vmLog(
            "Lease success",
            LogType.SUCCESS,
            metadata = mapOf("email" to leasedAccount.email)
        )

        val cloudflareToken = TurnstileService.solveTurnstile() ?: run {
            vmLog("Cloudflare token load failed", LogType.ERROR, critical = true)
            return LoginAttemptOutcome.Failure
        }
        vmLog("Cloudflare token acquired", LogType.SUCCESS)

        val loginOutcome = authApi.login(
            username = leasedAccount.email,
            password = leasedAccount.password,
            cloudflareToken = cloudflareToken,
            countryCode = leasedAccount.countryCode,
            missionCode = leasedAccount.missionCode,
            appConfig = appConfigState.value
        )

        when (loginOutcome) {
            is LoginOutcome.Success -> {
                vmLog(
                    "Saving session data after login success",
                    LogType.SUCCESS,
                    metadata = mapOf("email" to leasedAccount.email)
                )

                sessionRepository.saveSessionData(
                    SessionData(
                        accessToken = loginOutcome.accessToken,
                        username = leasedAccount.email
                    )
                )
                return LoginAttemptOutcome.Success
            }

            is LoginOutcome.OtpRequired -> {
                vmLog(
                    "Login requires OTP verification; attempting auto verify",
                    LogType.WARNING,
                    metadata = mapOf(
                        "contactNumber" to loginOutcome.contactNumber,
                        "dialCode" to loginOutcome.dialCode
                    )
                )

                dataRepository.updateVerifyOtpJobState(JobState.IN_PROGRESS)
                return try {
                    val otpOutcome = authApi.verifyOtp(
                        username = leasedAccount.email,
                        password = leasedAccount.password,
                        cloudflareToken = cloudflareToken,
                        countryCode = leasedAccount.countryCode,
                        missionCode = leasedAccount.missionCode,
                        otp = FIXED_LOGIN_OTP,
                        appConfig = appConfigState.value
                    )
                    when (otpOutcome) {
                        is LoginOutcome.Success -> {
                            sessionRepository.saveSessionData(
                                SessionData(
                                    accessToken = otpOutcome.accessToken,
                                    username = leasedAccount.email
                                )
                            )
                            dataRepository.updateVerifyOtpJobState(JobState.COMPLETE)
                            vmLog("Auto OTP verification succeeded", LogType.SUCCESS)
                            LoginAttemptOutcome.Success
                        }

                        is LoginOutcome.OtpRequired -> {
                            dataRepository.updateVerifyOtpJobState(JobState.STOPPED)
                            vmLog(
                                "Auto OTP verification still requires OTP",
                                LogType.ERROR,
                                critical = true
                            )
                            LoginAttemptOutcome.Failure
                        }

                        is LoginOutcome.Failure -> {
                            dataRepository.updateVerifyOtpJobState(JobState.STOPPED)
                            vmLog(
                                "Auto OTP verification failed",
                                LogType.ERROR,
                                critical = true,
                                metadata = mapOf("reason" to otpOutcome.reason)
                            )
                            LoginAttemptOutcome.Failure
                        }
                    }
                } catch (e: CancellationException) {
                    dataRepository.updateVerifyOtpJobState(JobState.STOPPED)
                    throw e
                } catch (e: Exception) {
                    dataRepository.updateVerifyOtpJobState(JobState.STOPPED)
                    vmLog(
                        "Auto OTP verification crashed",
                        LogType.ERROR,
                        critical = true,
                        metadata = mapOf("error" to (e.message ?: "unknown"))
                    )
                    LoginAttemptOutcome.Failure
                }
            }

            is LoginOutcome.Failure -> {
                vmLog(
                    "Login failed. Reporting block.",
                    LogType.ERROR,
                    critical = true,
                    metadata = mapOf(
                        "email" to leasedAccount.email,
                        "reason" to loginOutcome.reason
                    )
                )
                leasedAccountApi.reportBlock(leasedAccount.email, entry = entry)
                return LoginAttemptOutcome.Failure
            }
        }
    }

    fun login(onLoginComplete: (() -> Unit)? = null) {
        if (loginJob?.isActive == true) {
            vmLog("login ignored because login job is already active", LogType.WARNING)
            return
        }
        if (verifyOtpJob?.isActive == true) {
            vmLog("login ignored because verifyOtp job is active", LogType.WARNING)
            return
        }

        dataRepository.updateOtpVerificationRequired(false)
        dataRepository.updateLoginJobState(JobState.IN_PROGRESS)

        loginJob = viewModelScope.launch(Dispatchers.IO) {
            val entry = entryState.value

            val maxAttempts = 5
            var delayMs = 1000L
            vmLog(
                "Login flow started",
                LogType.INFO,
                metadata = mapOf("maxAttempts" to maxAttempts.toString())
            )

            try {
                repeat(maxAttempts) { attemptIndex ->
                    if (!isActive) return@launch

                    vmLog(
                        "Login attempt started",
                        LogType.DEBUG,
                        metadata = mapOf("attempt" to (attemptIndex + 1).toString())
                    )
                    val outcome = try {
                        attemptLoginOnce(entry)
                    } catch (e: CancellationException) {
                        vmLog("login cancelled during attempt", LogType.DEBUG)
                        throw e
                    } catch (e: Exception) {
                        e.printStackTrace()
                        vmLog(
                            "Login attempt crashed",
                            LogType.ERROR,
                            metadata = mapOf(
                                "attempt" to (attemptIndex + 1).toString(),
                                "error" to (e.message ?: "unknown")
                            )
                        )
                        LoginAttemptOutcome.Failure
                    }

                    when (outcome) {
                        LoginAttemptOutcome.Success -> {
                            dataRepository.updateLoginJobState(JobState.COMPLETE)
                            dataRepository.updateOtpVerificationRequired(false)
                            vmLog(
                                "Login attempt succeeded",
                                LogType.SUCCESS,
                                metadata = mapOf("attempt" to (attemptIndex + 1).toString())
                            )
                            delay(1000L)
                            withContext(Dispatchers.Main) { onLoginComplete?.invoke() }
                            return@launch
                        }

                        LoginAttemptOutcome.Failure -> {
                            val isLast = attemptIndex == maxAttempts - 1
                            if (!isLast) {
                                vmLog(
                                    "Login attempt failed, backing off",
                                    LogType.WARNING,
                                    metadata = mapOf("nextDelayMs" to delayMs.toString())
                                )
                                delay(delayMs)
                                delayMs = (delayMs * 2).coerceAtMost(15_000L)
                            }
                        }

                    }
                }

                println("Login failed after $maxAttempts attempts.")
                vmLog("Login failed after max attempts", LogType.ERROR, critical = true)
                dataRepository.updateLoginJobState(JobState.STOPPED)
            } catch (e: CancellationException) {
                vmLog("Login flow cancelled", LogType.DEBUG)
                dataRepository.updateLoginJobState(JobState.STOPPED)
                throw e
            } finally {
                loginJob = null
                if (dataState.value.loginJobRunning == JobState.IN_PROGRESS) {
                    dataRepository.updateLoginJobState(JobState.STOPPED)
                }
            }
        }
    }

    fun stopLoginFlow() {
        vmLog("stopLoginFlow called", LogType.WARNING)
        loginJob?.cancel()
        loginJob = null
        verifyOtpJob?.cancel()
        verifyOtpJob = null
        dataRepository.updateLoginJobState(JobState.STOPPED)
        dataRepository.updateVerifyOtpJobState(JobState.STOPPED)
        dataRepository.updateOtpVerificationRequired(false)
        vmLog("login and verifyOtp jobs stopped", LogType.DEBUG)
    }

    fun loadApplicants() {
        if (loadApplicantsJob?.isActive == true || dataState.value.loadApplicantsJobRunning == JobState.IN_PROGRESS) {
            vmLog("loadApplicants ignored because job is already active", LogType.WARNING)
            return
        }

        val sessionData = sessionState.value ?: run {
            vmLog("loadApplicants skipped: session is null", LogType.WARNING)
            return
        }
        val entry = entryState.value
        vmLog("Loading applicants", LogType.DEBUG)

        loadApplicantsJob = viewModelScope.launch(Dispatchers.IO) {
            dataRepository.updateLoadApplicantsJobState(JobState.IN_PROGRESS)
            vmLog("Load applicants job state set to IN_PROGRESS", LogType.DEBUG)

            var completed = false
            try {
                applicantApi.loadApplicants(
                    sessionData = sessionData, entry = entry, appConfig = appConfigState.value
                )
                completed = true
                vmLog("loadApplicants completed", LogType.SUCCESS)
            } catch (e: CancellationException) {
                vmLog("loadApplicants job cancelled", LogType.DEBUG)
                throw e
            } catch (e: Exception) {
                vmLog(
                    "loadApplicants failed",
                    LogType.ERROR,
                    metadata = mapOf("error" to (e.message ?: "unknown"))
                )
            } finally {
                val finalState = if (completed) JobState.COMPLETE else JobState.STOPPED
                dataRepository.updateLoadApplicantsJobState(finalState)
                loadApplicantsJob = null
                vmLog(
                    "Load applicants job state updated",
                    LogType.DEBUG,
                    metadata = mapOf("state" to finalState.name)
                )
            }
        }
    }

    suspend fun addApplicant(triggerSlotFlowOnSuccess: Boolean = false): Boolean {
        if (dataState.value.addApplicantJobRunning == JobState.IN_PROGRESS) {
            vmLog("addApplicant ignored because job is already active", LogType.WARNING)
            return false
        }

        val sessionData = sessionState.value ?: run {
            vmLog("addApplicant skipped: session is null", LogType.WARNING)
            return false
        }
        val entry = entryState.value
        vmLog(
            "addApplicant started",
            LogType.DEBUG,
            metadata = mapOf("maxAttempts" to ADD_APPLICANT_MAX_ATTEMPTS.toString())
        )

        dataRepository.updateAddApplicantJobState(JobState.IN_PROGRESS)
        vmLog("Add applicant job state set to IN_PROGRESS", LogType.DEBUG)

        var lastFailureReason = "unknown"
        var completed = false
        try {
            repeat(ADD_APPLICANT_MAX_ATTEMPTS) { attemptIndex ->
                val attempt = attemptIndex + 1

                try {
                    val urn = applicantApi.addApplicant(
                        sessionData = sessionData, entry = entry, appConfig = appConfigState.value
                    )

                    if (urn.isBlank()) {
                        lastFailureReason = "blank urn"
                        vmLog(
                            "addApplicant attempt failed: blank URN",
                            LogType.WARNING,
                            metadata = mapOf("attempt" to attempt.toString())
                        )
                    } else {
                        dataRepository.saveUrn(urn = urn)
                        completed = true
                        vmLog(
                            "addApplicant completed",
                            LogType.SUCCESS,
                            metadata = mapOf("attempt" to attempt.toString(), "urn" to urn)
                        )
                        if (triggerSlotFlowOnSuccess) {
                            vmLog(
                                "addApplicant success: starting checkSlot and loadCalender jobs",
                                LogType.DEBUG
                            )
                            startCheckIsSlotAvailable()
                            loadCalender()
                        }
                        return true
                    }
                } catch (e: Exception) {
                    lastFailureReason = e.message ?: "unknown"
                    vmLog(
                        "addApplicant attempt failed with exception",
                        LogType.WARNING,
                        metadata = mapOf(
                            "attempt" to attempt.toString(),
                            "error" to lastFailureReason
                        )
                    )
                }

                if (attempt < ADD_APPLICANT_MAX_ATTEMPTS) {
                    vmLog(
                        "addApplicant retry scheduled",
                        LogType.DEBUG,
                        metadata = mapOf(
                            "nextAttempt" to (attempt + 1).toString(),
                            "delayMs" to ADD_APPLICANT_RETRY_DELAY_MS.toString()
                        )
                    )
                    delay(ADD_APPLICANT_RETRY_DELAY_MS)
                }
            }
        } finally {
            val finalState = if (completed) JobState.COMPLETE else JobState.STOPPED
            dataRepository.updateAddApplicantJobState(finalState)
            vmLog(
                "Add applicant job state updated",
                LogType.DEBUG,
                metadata = mapOf("state" to finalState.name)
            )
        }

        vmLog(
            "addApplicant failed after max attempts",
            LogType.ERROR,
            metadata = mapOf(
                "attempts" to ADD_APPLICANT_MAX_ATTEMPTS.toString(),
                "reason" to lastFailureReason
            )
        )
        if (triggerSlotFlowOnSuccess) {
            vmLog(
                "addApplicant failed after max retries; slot flow skipped",
                LogType.WARNING
            )
        }
        return false
    }

    fun stopLoadApplicants() {
        vmLog("stopLoadApplicants called", LogType.WARNING)
        loadApplicantsJob?.cancel()
        loadApplicantsJob = null
        dataRepository.updateLoadApplicantsJobState(JobState.STOPPED)
        vmLog("Load applicants job state set to STOPPED", LogType.DEBUG)
    }

    fun addApplicantManual() {
        if (addApplicantJob?.isActive == true || dataState.value.addApplicantJobRunning == JobState.IN_PROGRESS) {
            vmLog("addApplicantManual ignored because job is already active", LogType.WARNING)
            return
        }
        addApplicantJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                addApplicant()
            } catch (e: CancellationException) {
                vmLog("addApplicant manual job cancelled", LogType.DEBUG)
                throw e
            } finally {
                addApplicantJob = null
            }
        }
    }

    fun stopAddApplicant() {
        vmLog("stopAddApplicant called", LogType.WARNING)
        addApplicantJob?.cancel()
        addApplicantJob = null
        dataRepository.updateAddApplicantJobState(JobState.STOPPED)
        vmLog("Add applicant job state set to STOPPED", LogType.DEBUG)
    }

    fun startCheckIsSlotAvailable() {
        // Prevent double-start
        if (checkSlotJob?.isActive == true) return


        println("startCheckIsSlotAvailable")
        vmLog("startCheckIsSlotAvailable called")
        val sessionData = sessionState.value ?: run {
            vmLog("startCheckIsSlotAvailable skipped: session is null", LogType.WARNING)
            return
        }
        val entry = entryState.value


        // change the job state
        dataRepository.updateCheckSlotJobState(JobState.IN_PROGRESS)
        vmLog("Check slot job state set to IN_PROGRESS", LogType.DEBUG)

        checkSlotJob = viewModelScope.launch(Dispatchers.IO) {
            var completed = false
            try {
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
                                isCurrentCycleFinder = true
                                currentCycleEarliestDate = it
                                vmLog(
                                    "Earliest slot found",
                                    LogType.SUCCESS,
                                    metadata = mapOf("date" to it)
                                )

                                FirebaseDataService.saveEarliestSlotDate(
                                    date = result.data, entry = entry
                                )
                                if (loadCalenderJob?.isActive == true) {
                                    vmLog("Finder flow: stopping loadCalender job", LogType.DEBUG)
                                    stopLoadCalender()
                                }
                                if (loadSlotSlob?.isActive == true) {
                                    vmLog("Finder flow: stopping running loadTimeSlot job", LogType.DEBUG)
                                    stopLoadTimeSlot()
                                }
                                vmLog(
                                    "Finder flow: starting loadTimeSlot",
                                    LogType.DEBUG,
                                    metadata = mapOf("date" to it)
                                )
                                loadTimeSlot()
                                completed = true
                                vmLog(
                                    "checkSlotJob completed after earliest slot found",
                                    LogType.SUCCESS
                                )
                                return@launch
                            }
                        }

                        is SealedResult.Error -> {
                            println(result.exception.message)
                            vmLog(
                                "checkIsSlotAvailable failed",
                                LogType.ERROR,
                                metadata = mapOf("error" to (result.exception.message ?: "unknown"))
                            )
                        }
                    }

                    // ⏱ wait 3 min AFTER completion
                    val jitterMs = jitterService.nextDelayMillis()
                    val totalDelay = (3 * 60_000L) + jitterMs
                    vmLog(
                        "Next slot check scheduled",
                        LogType.DEBUG,
                        metadata = mapOf("delayMs" to totalDelay.toString())
                    )
                    delay(totalDelay)
                }
            } catch (e: CancellationException) {
                vmLog("checkSlotJob cancelled", LogType.DEBUG)
                throw e
            } finally {
                val finalState = if (completed) JobState.COMPLETE else JobState.STOPPED
                dataRepository.updateCheckSlotJobState(finalState)
                checkSlotJob = null
                vmLog(
                    "Check slot job state updated",
                    LogType.DEBUG,
                    metadata = mapOf("state" to finalState.name)
                )
            }
        }
    }

    fun stopCheckIsSlotAvailable() {
        vmLog("stopCheckIsSlotAvailable called", LogType.WARNING)
        checkSlotJob?.cancel()
        checkSlotJob = null
        // change the job state
        dataRepository.updateCheckSlotJobState(JobState.STOPPED)
        vmLog("Check slot job state set to STOPPED", LogType.DEBUG)
    }

    fun loadCalender() {
        if (loadCalenderJob?.isActive == true || dataState.value.loadCalenderJobRunning == JobState.IN_PROGRESS) {
            vmLog("loadCalender ignored because job is already active", LogType.WARNING)
            return
        }
        val sessionData = sessionState.value ?: run {
            vmLog("loadCalender skipped: session is null", LogType.WARNING)
            return
        }
        val entry = entryState.value
        vmLog("loadCalender started (finder/follower mode)", LogType.DEBUG)

        loadCalenderJob = viewModelScope.launch(Dispatchers.IO) {
            dataRepository.updateLoadCalenderJobState(JobState.IN_PROGRESS)
            var completed = false
            try {
                val earliestSlotDate = FirebaseDataService.readEarliestSlotDate(entry = entry)
                currentCycleEarliestDate = earliestSlotDate
                vmLog(
                    "loadCalender received earliest slot date from Firebase",
                    LogType.SUCCESS,
                    metadata = mapOf("date" to earliestSlotDate)
                )

                if (checkSlotJob?.isActive == true || dataState.value.checkSlotJobRunning == JobState.IN_PROGRESS) {
                    vmLog("loadCalender stopping running checkSlot job", LogType.DEBUG)
                    stopCheckIsSlotAvailable()
                }

                if (isCurrentCycleFinder) {
                    vmLog(
                        "loadCalender exiting early for finder app",
                        LogType.DEBUG,
                        metadata = mapOf("date" to earliestSlotDate)
                    )
                    followerPriorityDates = emptyList()
                    completed = true
                    return@launch
                }

                val result = calenderApi.loadCalender(
                    sessionData = sessionData, entry = entry, urn = dataState.value.urn
                )
                when (result) {
                    is SealedResult.Success -> {
                        dataRepository.saveAvailableDates(dates = result.data)
                        val priorityDates = computeFollowerPriorityDates(
                            availableDates = result.data,
                            deviceIndex = appConfigState.value.deviceIndex
                        )
                        followerPriorityDates = priorityDates
                        val selectedFollowerDate = priorityDates.firstOrNull()
                        vmLog(
                            "loadCalender follower path success",
                            LogType.SUCCESS,
                            metadata = mapOf(
                                "deviceIndex" to appConfigState.value.deviceIndex.toString(),
                                "dateCount" to result.data.size.toString(),
                                "selectedPriorityDates" to priorityDates.joinToString("|"),
                                "selectedDate" to (selectedFollowerDate ?: "")
                            )
                        )
                        if (selectedFollowerDate != null) {
                            val attemptDates = buildFollowerSlotAttemptDates(priorityDates)
                            val sessionDataForSlot = sessionState.value
                            if (sessionDataForSlot == null) {
                                vmLog("Follower flow: loadTimeSlot skipped due to missing session", LogType.WARNING)
                            } else if (loadSlotSlob?.isActive == true) {
                                vmLog("Follower flow: loadTimeSlot skipped because job is already active", LogType.WARNING)
                            } else {
                                startLoadSlotJob(
                                    sessionData = sessionDataForSlot,
                                    entry = entry,
                                    attemptDates = attemptDates,
                                    baseDateCount = minOf(priorityDates.distinct().size, FOLLOWER_MAX_DATES)
                                )
                            }
                        } else {
                            vmLog(
                                "Follower flow: no prioritized date available for loadTimeSlot",
                                LogType.WARNING
                            )
                        }
                    }

                    is SealedResult.Error -> {
                        println(result.exception.message)
                        throw result.exception
                    }
                }
                completed = true
            } catch (e: CancellationException) {
                vmLog("loadCalender job cancelled", LogType.DEBUG)
                throw e
            } catch (e: Exception) {
                vmLog(
                    "loadCalender failed (finder/follower mode)",
                    LogType.ERROR,
                    metadata = mapOf("error" to (e.message ?: "unknown"))
                )
            } finally {
                val finalState = if (completed) JobState.COMPLETE else JobState.STOPPED
                dataRepository.updateLoadCalenderJobState(finalState)
                loadCalenderJob = null
            }
        }
    }

    fun stopLoadCalender() {
        vmLog("stopLoadCalender called", LogType.WARNING)
        loadCalenderJob?.cancel()
        loadCalenderJob = null
        dataRepository.updateLoadCalenderJobState(JobState.STOPPED)
        vmLog("Load calender job state set to STOPPED", LogType.DEBUG)
    }

    private suspend fun resolveTargetSlotDate(
        entry: Entry,
        selectedDate: String?
    ): String {
        currentCycleEarliestDate
            ?.takeIf { isCurrentCycleFinder && it.isNotBlank() }
            ?.also {
                vmLog(
                    "Finder flow: using in-memory earliest slot date",
                    LogType.DEBUG,
                    metadata = mapOf("date" to it)
                )
            }
            ?.let { return it }

        selectedDate
            ?.takeIf { it.isNotBlank() }
            ?.also {
                vmLog(
                    "loadTimeSlot using follower-selected date",
                    LogType.DEBUG,
                    metadata = mapOf("date" to it)
                )
            }
            ?.let { return it }

        return FirebaseDataService.readEarliestSlotDate(entry = entry).also {
            vmLog(
                "Follower flow: earliest slot date read from Firebase",
                LogType.DEBUG,
                metadata = mapOf("date" to it)
            )
        }
    }

    fun loadTimeSlot(selectedDate: String? = null) {
        // Prevent double-start
        if (loadSlotSlob?.isActive == true) return

        println("loadTimeSlot")
        vmLog("loadTimeSlot called")

        val sessionData = sessionState.value ?: run {
            vmLog("loadTimeSlot skipped: session is null", LogType.WARNING)
            return
        }
        val entry = entryState.value

        viewModelScope.launch(Dispatchers.IO) {
            val targetDate = resolveTargetSlotDate(
                entry = entry,
                selectedDate = selectedDate
            )
            val attemptDates = if (isCurrentCycleFinder && selectedDate.isNullOrBlank()) {
                List(FINDER_SLOT_RETRY_COUNT) { targetDate }
            } else {
                listOf(targetDate)
            }
            startLoadSlotJob(
                sessionData = sessionData,
                entry = entry,
                attemptDates = attemptDates,
                baseDateCount = 1
            )
        }
    }

    fun stopLoadTimeSlot() {
        vmLog("stopLoadTimeSlot called", LogType.WARNING)
        loadSlotSlob?.cancel()
        loadSlotSlob = null
        // change the job state
        dataRepository.updateLoadSlotJobState(JobState.STOPPED)
        vmLog("Load slot job state set to STOPPED", LogType.DEBUG)
    }


    suspend fun startSchedule(allocationIds: List<String>) {
        if (allocationIds.isEmpty()) {
            vmLog("startSchedule skipped: no allocationIds", LogType.WARNING)
            return
        }

        val sessionData = sessionState.value ?: run {
            vmLog("startSchedule skipped: session is null", LogType.WARNING)
            return
        }
        val entry = entryState.value
        val urn = dataState.value.urn
        if (urn.isBlank()) {
            vmLog("startSchedule skipped: urn is blank", LogType.WARNING)
            return
        }

        vmLog(
            "startSchedule started",
            LogType.INFO,
            metadata = mapOf(
                "allocationCount" to allocationIds.size.toString(),
                "urnPresent" to "true"
            )
        )

        dataRepository.updateScheduleJobState(JobState.IN_PROGRESS)
        vmLog("Schedule job state set to IN_PROGRESS", LogType.DEBUG)

        var completed = false
        try {
            val randomizedAllocationIds = allocationIds.shuffled()
            vmLog(
                "Allocation IDs randomized before scheduling",
                LogType.DEBUG,
                metadata = mapOf("allocationCount" to randomizedAllocationIds.size.toString())
            )

            randomizedAllocationIds.forEachIndexed { index, allocationId ->
                val attemptNo = index + 1
                vmLog(
                    "Schedule attempt started",
                    LogType.INFO,
                    metadata = mapOf(
                        "attempt" to attemptNo.toString(),
                        "totalAttempts" to randomizedAllocationIds.size.toString(),
                        "allocationId" to allocationId
                    )
                )

                val scheduleResult = scheduleApi.schedule(
                    sessionData = sessionData,
                    entry = entry,
                    urn = urn,
                    allocationId = allocationId
                )
                when (scheduleResult) {
                    is SealedResult.Success -> {
                        completed = true
                        vmLog(
                            "Schedule attempt succeeded",
                            LogType.SUCCESS,
                            metadata = mapOf(
                                "attempt" to attemptNo.toString(),
                                "allocationId" to allocationId,
                                "statusCode" to scheduleResult.data.statusCode.toString()
                            )
                        )
                        return
                    }

                    is SealedResult.Error -> {
                        vmLog(
                            "Schedule attempt failed",
                            LogType.WARNING,
                            metadata = mapOf(
                                "attempt" to attemptNo.toString(),
                                "allocationId" to allocationId,
                                "error" to (scheduleResult.exception.message ?: "unknown")
                            )
                        )
                    }
                }
            }

            vmLog(
                "All schedule attempts failed",
                LogType.ERROR,
                critical = true,
                metadata = mapOf("attemptedAllocationCount" to randomizedAllocationIds.size.toString())
            )
        } catch (e: CancellationException) {
            vmLog("scheduleJob cancelled", LogType.DEBUG)
            throw e
        } finally {
            val finalState = if (completed) JobState.COMPLETE else JobState.STOPPED
            dataRepository.updateScheduleJobState(finalState)
            vmLog(
                "Schedule job state updated",
                LogType.DEBUG,
                metadata = mapOf("state" to finalState.name)
            )
        }
    }


    fun logout() {
        println("Logout")
        vmLog("logout called", LogType.WARNING)
        viewModelScope.launch(Dispatchers.IO) {
            stopAllChildJob()
            sessionRepository.clearSession()
            vmLog("session cleared", LogType.SUCCESS)
        }
    }

    fun updateAppConfig(appConfig: AppConfig) {
        vmLog(
            "updateAppConfig called",
            LogType.INFO,
            metadata = mapOf(
                "deviceIndex" to appConfig.deviceIndex.toString(),
                "entryIndex" to appConfig.entryIndex.toString()
            )
        )
        viewModelScope.launch(Dispatchers.IO) {
            appConfigRepository.updateAppConfig(appConfig = appConfig)
            vmLog("updateAppConfig persisted", LogType.SUCCESS)
        }
    }
}
