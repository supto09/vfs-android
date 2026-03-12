package com.example.vfsgm.data.repository

import com.example.vfsgm.data.api.LeasedAccountApi
import com.example.vfsgm.data.dto.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EntryRepository() {
    private val leasedAccountApi = LeasedAccountApi()

    private val _state = MutableStateFlow(Entry())
    val state: StateFlow<Entry> = _state.asStateFlow()

    suspend fun loadEntry() {
        val data = leasedAccountApi.getEntry()
        _state.update { data }
    }
}
