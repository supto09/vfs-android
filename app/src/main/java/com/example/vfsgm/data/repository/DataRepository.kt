package com.example.vfsgm.data.repository

import com.example.vfsgm.data.api.SubjectApi
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.dto.DataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DataRepository() {

    private val _state = MutableStateFlow(DataState())
    val state: StateFlow<DataState> = _state.asStateFlow()

    suspend fun saveUrn(urn: String) {
        _state.update { it.copy(urn = urn) }
    }

    suspend fun saveAvailableDates(dates: List<String>) {
        _state.update { it.copy(availableDates = dates) }
    }
}