package com.example.vfsgm.data.repository

import android.content.Context
import com.example.vfsgm.data.store.SessionDataStore
import com.example.vfsgm.data.dto.SessionData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionRepository(
    context: Context
) {
    private val sessionDataStore = SessionDataStore(context)

    private val _state = MutableStateFlow<SessionData?>(null)
    val state: StateFlow<SessionData?> = _state.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            sessionDataStore.sessionFlow
                .distinctUntilChanged()
                .collect { session ->
                    _state.update { session }
                }
        }
    }


    suspend fun clearSession() {
        sessionDataStore.clearToken()
        _state.update { null }
    }

    suspend fun saveSessionData(sessionData: SessionData) {
        sessionDataStore.saveSessionData(sessionData)
    }
}