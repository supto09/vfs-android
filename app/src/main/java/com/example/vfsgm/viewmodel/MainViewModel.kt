package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.data.network.PublicIpManager
import com.example.vfsgm.data.repository.SessionRepository
import com.example.vfsgm.data.repository.SubjectRepository
import com.example.vfsgm.data.store.TurnstileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionRepository = SessionRepository(application.applicationContext)
    val sessionState = sessionRepository.state

    private val subjectRepository = SubjectRepository()
    val subjectState = subjectRepository.state

    private val authApi = AuthApi()
    private val applicantApi = ApplicantApi()

    init {
        viewModelScope.launch {
            // load ip
            PublicIpManager.init { ip ->
                println("IP loaded: $ip")
            }
        }

        viewModelScope.launch {
            // load subject data
            subjectRepository.loadSubject()
        }
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            val cloudflareToken = TurnstileStore.readToken()

            val subject = subjectState.value
            val accessToken = authApi.login(
                username = subject.username,
                password = subject.password,
                cloudflareToken = cloudflareToken
            )

            sessionRepository.saveAccessToken(accessToken)
        }
    }

    fun loadApplicants() {
        val accessToken = sessionState.value.accessToken ?: return
        println("AccessToken: $accessToken")

        val subject = subjectState.value
        println("Subject: $subject")

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.loadApplicants(
                accessToken = accessToken,
                username = subjectState.value.username,
            )
        }
    }

    fun addApplicant() {
        val accessToken = sessionState.value.accessToken ?: return
        println("AccessToken: $accessToken")

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.addApplicant(
                accessToken = accessToken, subject = subjectState.value
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

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.clearSession()
        }
    }
}
