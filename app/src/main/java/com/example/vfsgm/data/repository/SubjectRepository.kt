package com.example.vfsgm.data.repository

import com.example.vfsgm.data.api.SubjectApi
import com.example.vfsgm.data.dto.Subject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubjectRepository() {
    private val subjectApi = SubjectApi()

    private val _state = MutableStateFlow(Subject())
    val state: StateFlow<Subject> = _state.asStateFlow()

    suspend fun loadSubject() {
        val data = subjectApi.getSubject()
        _state.value = data
    }
}