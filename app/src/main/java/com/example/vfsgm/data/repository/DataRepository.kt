package com.example.vfsgm.data.repository

import com.example.vfsgm.data.dto.DataState
import com.example.vfsgm.data.dto.JobState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DataRepository() {

    private val _state = MutableStateFlow(DataState())
    val state: StateFlow<DataState> = _state.asStateFlow()

    fun saveUrn(urn: String) {
        _state.update { it.copy(urn = urn) }
    }

    fun saveAvailableDates(dates: List<String>) {
        _state.update { it.copy(availableDates = dates) }
    }

    fun saveEarliestSlotDates(earliestSlotDate: String) {
        _state.update { it.copy(earliestSlotDate = earliestSlotDate) }
    }

    fun updateCheckSlotJobState(jobState: JobState) {
        _state.update { it.copy(checkSlotJobRunning = jobState) }
    }

}