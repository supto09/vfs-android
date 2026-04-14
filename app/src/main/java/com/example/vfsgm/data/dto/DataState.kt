package com.example.vfsgm.data.dto


enum class JobState { NOT_STARTED, IN_PROGRESS, COMPLETE , STOPPED}

data class DataState(
    val urn: String = "",
    val availableDates: List<String> = emptyList(),
    val allocationIds: List<String> = emptyList(),


    val reLoginJobRunning: JobState = JobState.NOT_STARTED,
    val loginJobRunning: JobState = JobState.NOT_STARTED,
    val verifyOtpJobRunning: JobState = JobState.NOT_STARTED,
    val otpVerificationRequired: Boolean = false,
    val loadApplicantsJobRunning: JobState = JobState.NOT_STARTED,
    val addApplicantJobRunning: JobState = JobState.NOT_STARTED,
    val loadCalenderJobRunning: JobState = JobState.NOT_STARTED,
    val checkSlotJobRunning: JobState = JobState.NOT_STARTED,
    val loadSlotJobRunning: JobState = JobState.NOT_STARTED,
    val scheduleJobRunning: JobState = JobState.NOT_STARTED
)
