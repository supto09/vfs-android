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
                    vmLog("Loading entry for changed entryIndex", metadata = mapOf("entryIndex" to entryIndex.toString()))
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
        checkSlotJob?.cancel()
        checkSlotJob = null
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
                    vmLog("ReLogin cycle: logout start", LogType.DEBUG)
                    logout()

                    vmLog("ReLogin cycle: waiting for CF cookie", LogType.DEBUG)
                    CfCookieCheckManager.waitUntilCfCookieKeyExists()

                    vmLog("ReLogin cycle: login start", LogType.DEBUG)
                    login {
                        vmLog("ReLogin callback: login complete, triggering applicant+slot jobs", LogType.SUCCESS)
                        viewModelScope.launch(Dispatchers.IO) {
                            val addOk = addApplicant()
                            if (addOk) {
                                loadTimeSlot()
                                startCheckIsSlotAvailable()
                            } else {
                                vmLog(
                                    "ReLogin cycle: addApplicant failed after max retries; slot flow skipped",
                                    LogType.WARNING
                                )
                            }
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

    private suspend fun attemptLoginOnce(entry: Entry): Boolean {
        vmLog(
            "attemptLoginOnce called",
            LogType.DEBUG,
            metadata = mapOf(
                "countryCode" to entry.countryCode.toString(),
                "missionCode" to entry.missionCode.toString()
            )
        )

        val leasedAccount = when (val res = leasedAccountApi.leaseAccount(entry)) {
            is SealedResult.Success -> res.data
            is SealedResult.Error -> null
        } ?: run {
            vmLog("Lease failed: no account found", LogType.ERROR, critical = true)
            return false
        }

        vmLog(
            "Lease success",
            LogType.SUCCESS,
            metadata = mapOf("email" to leasedAccount.email)
        )

        val cloudflareToken = TurnstileService.solveTurnstile() ?: run {
            vmLog("Cloudflare token load failed", LogType.ERROR, critical = true)
            return false
        }
        vmLog("Cloudflare token acquired", LogType.SUCCESS)

        val accessToken = authApi.login(
            username = leasedAccount.email,
            password = leasedAccount.password,
            cloudflareToken = cloudflareToken,
            appConfig = appConfigState.value
        )

        if (accessToken.isNullOrEmpty()) {
            vmLog(
                "Login failed. Reporting block.",
                LogType.ERROR,
                critical = true,
                metadata = mapOf("email" to leasedAccount.email)
            )
            leasedAccountApi.reportBlock(leasedAccount.email, entry = entry)

            return false
        }


        vmLog(
            "Saving session data after login success",
            LogType.SUCCESS,
            metadata = mapOf("email" to leasedAccount.email)
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
            vmLog("Login flow started", LogType.INFO, metadata = mapOf("maxAttempts" to maxAttempts.toString()))

            repeat(maxAttempts) { attemptIndex ->
                if (!isActive) return@launch

                vmLog("Login attempt started", LogType.DEBUG, metadata = mapOf("attempt" to (attemptIndex + 1).toString()))
                val ok = try {
                    attemptLoginOnce(entry)
                } catch (e: Exception) {
                    e.printStackTrace()
                    vmLog(
                        "Login attempt crashed",
                        LogType.ERROR,
                        metadata = mapOf("attempt" to (attemptIndex + 1).toString(), "error" to (e.message ?: "unknown"))
                    )
                    false
                }

                if (ok) {
                    vmLog("Login attempt succeeded", LogType.SUCCESS, metadata = mapOf("attempt" to (attemptIndex + 1).toString()))
                    delay(1000L)
                    withContext(Dispatchers.Main) { onLoginComplete?.invoke() }
                    return@launch
                }

                val isLast = attemptIndex == maxAttempts - 1
                if (!isLast) {
                    // simple backoff
                    vmLog("Login attempt failed, backing off", LogType.WARNING, metadata = mapOf("nextDelayMs" to delayMs.toString()))
                    delay(delayMs)
                    delayMs = (delayMs * 2).coerceAtMost(15_000L)
                }
            }

            println("Login failed after $maxAttempts attempts.")
            // TODO sent a critical alert that a device failed to login
            vmLog("Login failed after max attempts", LogType.ERROR, critical = true)
        }
    }

    fun loadApplicants() {
        if (dataState.value.loadApplicantsJobRunning == JobState.IN_PROGRESS) {
            vmLog("loadApplicants ignored because job is already active", LogType.WARNING)
            return
        }

        val sessionData = sessionState.value ?: run {
            vmLog("loadApplicants skipped: session is null", LogType.WARNING)
            return
        }
        val entry = entryState.value
        vmLog("Loading applicants", LogType.DEBUG)

        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.updateLoadApplicantsJobState(JobState.IN_PROGRESS)
            vmLog("Load applicants job state set to IN_PROGRESS", LogType.DEBUG)

            var completed = false
            try {
                applicantApi.loadApplicants(
                    sessionData = sessionData, entry = entry, appConfig = appConfigState.value
                )
                completed = true
                vmLog("loadApplicants completed", LogType.SUCCESS)
            } catch (e: Exception) {
                vmLog(
                    "loadApplicants failed",
                    LogType.ERROR,
                    metadata = mapOf("error" to (e.message ?: "unknown"))
                )
            } finally {
                val finalState = if (completed) JobState.COMPLETE else JobState.STOPPED
                dataRepository.updateLoadApplicantsJobState(finalState)
                vmLog(
                    "Load applicants job state updated",
                    LogType.DEBUG,
                    metadata = mapOf("state" to finalState.name)
                )
            }
        }
    }

    suspend fun addApplicant(): Boolean {
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
        return false
    }

    fun addApplicantManual() {
        viewModelScope.launch(Dispatchers.IO) {
            addApplicant()
        }
    }

    fun loadCalender() {
        val sessionData = sessionState.value ?: run {
            vmLog("loadCalender skipped: session is null", LogType.WARNING)
            return
        }
        val entry = entryState.value
        vmLog("loadCalender started", LogType.DEBUG, metadata = mapOf("urn" to dataState.value.urn))

        viewModelScope.launch(Dispatchers.IO) {
            val result = calenderApi.loadCalender(
                sessionData = sessionData, entry = entry, urn = dataState.value.urn
            )

            when (result) {
                is SealedResult.Success -> {
                    dataRepository.saveAvailableDates(dates = result.data)
                    vmLog(
                        "loadCalender success",
                        LogType.SUCCESS,
                        metadata = mapOf("dateCount" to result.data.size.toString())
                    )
                }
                is SealedResult.Error -> {
                    println(result.exception.message)
                    vmLog(
                        "loadCalender failed",
                        LogType.ERROR,
                        metadata = mapOf("error" to (result.exception.message ?: "unknown"))
                    )
                }
            }
        }
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
                                vmLog("Earliest slot found", LogType.SUCCESS, metadata = mapOf("date" to it))
                                dataRepository.saveEarliestSlotDates(it)

                                FirebaseDataService.saveEarliestSlotDate(
                                    date = result.data, entry = entry
                                )
                                completed = true
                                vmLog("checkSlotJob completed after earliest slot found", LogType.SUCCESS)
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
                    vmLog("Next slot check scheduled", LogType.DEBUG, metadata = mapOf("delayMs" to totalDelay.toString()))
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


    fun loadTimeSlot() {
        // Prevent double-start
        if (loadSlotSlob?.isActive == true) return

        println("loadTimeSlot")
        vmLog("loadTimeSlot called")

        val sessionData = sessionState.value ?: run {
            vmLog("loadTimeSlot skipped: session is null", LogType.WARNING)
            return
        }
        val entry = entryState.value

        dataRepository.updateLoadSlotJobState(JobState.IN_PROGRESS)
        vmLog("Load slot job state set to IN_PROGRESS", LogType.DEBUG)

        loadSlotSlob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val earliestSlotDate = FirebaseDataService.readEarliestSlotDate(
                    entry = entry,
                )
                vmLog("Earliest slot date read from Firebase", LogType.DEBUG, metadata = mapOf("date" to earliestSlotDate))

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


                        vmLog(
                            "loadTimeSlot success",
                            LogType.SUCCESS,
                            metadata = mapOf(
                                "earliestSlotDate" to earliestSlotDate,
                                "allocationCount" to loadSlotResult.data.size.toString()
                            )
                        )
                    }

                    is SealedResult.Error -> {
                        println(loadSlotResult.exception.message)
                        vmLog(
                            "loadTimeSlot failed",
                            LogType.ERROR,
                            metadata = mapOf("error" to (loadSlotResult.exception.message ?: "unknown"))
                        )
                    }
                }


                println("Earliest Slot Date received via firebase: $earliestSlotDate")
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

            when (val scheduleResult = scheduleApi.schedule(
                sessionData = sessionData,
                entry = entry,
                urn = urn,
                allocationId = allocationId
            )) {
                is SealedResult.Success -> {
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
