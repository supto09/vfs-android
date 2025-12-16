package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.data.store.TurnstileStore
import com.example.vfsgm.dto.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val authApi = AuthApi()
    private val applicantApi = ApplicantApi()

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState = _sessionState.asStateFlow()

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            val cloudflareToken = TurnstileStore.readToken()
            val accessToken = authApi.login(
                username = "witej38159@alexida.com",
                password = "CczEk4u6n!7Z9i$",
                cloudflareToken = cloudflareToken
            )

            _sessionState.value = SessionState(
                accessToken = accessToken,
                isLoggedIn = true
            )
        }
    }

    fun loadApplicants() {
        val accessToken = _sessionState.value.accessToken ?: return

        println("AccessToken: $accessToken")


        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.loadApplicants(
                accessToken = accessToken,
                username = "witej38159@alexida.com"
            )
        }
    }
}
