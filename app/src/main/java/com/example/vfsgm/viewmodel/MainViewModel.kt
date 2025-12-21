package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.data.api.SubjectApi
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.data.network.PublicIpManager
import com.example.vfsgm.data.store.TurnstileStore
import com.example.vfsgm.dto.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val subjectApi = SubjectApi()
    private val authApi = AuthApi()
    private val applicantApi = ApplicantApi()


    private val _subject = MutableStateFlow(Subject())
    val subject = _subject.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState = _sessionState.asStateFlow()


    init {
        viewModelScope.launch {
            PublicIpManager.init { ip ->
                println("IP loaded: $ip")
            }
        }

        val subjectData = subjectApi.getSubject()
        _subject.value = subjectData
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            val cloudflareToken = TurnstileStore.readToken()
            val accessToken = authApi.login(
                username = _subject.value.username,
                password = _subject.value.password,
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
                username = _subject.value.username,
            )
        }
    }

    fun addApplicant() {
        val accessToken = _sessionState.value.accessToken ?: return
        println("AccessToken: $accessToken")

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.addApplicant(
                accessToken = accessToken,
                subject = _subject.value
            )
        }
    }

    fun getGender() {
        val accessToken = _sessionState.value.accessToken ?: return
        println("AccessToken: $accessToken")

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.getGender(
                accessToken = accessToken,
            )
        }
    }
}
