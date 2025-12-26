package com.example.vfsgm.data.repository

import android.content.Context
import com.example.vfsgm.data.store.AccessTokenStore
import com.example.vfsgm.data.dto.SessionState
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
    private val accessTokenStore = AccessTokenStore(context)

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            accessTokenStore.accessTokenFlow
                .distinctUntilChanged()
                .collect { token ->
                    _state.update { it.copy(accessToken = token) }
                }
        }
    }


    suspend fun clearSession() {
        accessTokenStore.clearToken()
        _state.update { SessionState() }
    }

    suspend fun saveAccessToken(accessToken: String) {
        accessTokenStore.saveToken(accessToken)
    }
}