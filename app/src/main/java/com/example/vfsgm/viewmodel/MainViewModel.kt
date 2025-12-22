package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.data.api.SubjectApi
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.data.network.PublicIpManager
import com.example.vfsgm.data.repository.SessionRepository
import com.example.vfsgm.data.store.AccessTokenStore
import com.example.vfsgm.data.store.TurnstileStore
import com.example.vfsgm.dto.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionRepository = SessionRepository(application.applicationContext)
    val sessionState = sessionRepository.state

    private val subjectApi = SubjectApi()
    private val authApi = AuthApi()
    private val applicantApi = ApplicantApi()

    private val _subject = MutableStateFlow(Subject())
    val subject = _subject.asStateFlow()


    init {
        viewModelScope.launch {
            // load ip
            PublicIpManager.init { ip ->
                println("IP loaded: $ip")
            }
        }

        viewModelScope.launch {
            // load subject data
            val subjectData = subjectApi.getSubject()
            _subject.value = subjectData
        }
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            val cloudflareToken = TurnstileStore.readToken()
            val accessToken = authApi.login(
                username = _subject.value.username,
                password = _subject.value.password,
                cloudflareToken = cloudflareToken
            )

            sessionRepository.saveAccessToken(accessToken)
        }
    }

    fun loadApplicants() {
        val accessToken = sessionState.value.accessToken ?: return
        println("AccessToken: $accessToken")


        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.loadApplicants(
                accessToken = accessToken,
                username = _subject.value.username,
            )
        }
    }

    fun addApplicant() {
        val accessToken = sessionState.value.accessToken ?: return
        println("AccessToken: $accessToken")

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.addApplicant(
                accessToken = accessToken, subject = _subject.value
            )
        }
    }

    fun getGender() {
        val accessToken = sessionState.value.accessToken ?: return
        println("AccessToken: $accessToken")

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.getGender(
                accessToken = accessToken,
            )
        }
    }
}
