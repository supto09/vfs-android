package com.example.vfsgm.data.dto


enum class JobState { NOT_STARTED, IN_PROGRESS, COMPLETE , STOPPED}

data class DataState(
    val urn: String = "",
    val earliestSlotDate: String = "",
    val availableDates: List<String> = emptyList(),


    val checkSlotJobRunning: JobState = JobState.NOT_STARTED
)
