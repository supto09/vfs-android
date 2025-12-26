package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.data.api.CalenderApi
import com.example.vfsgm.data.network.PublicIpManager
import com.example.vfsgm.data.repository.DataRepository
import com.example.vfsgm.data.repository.SessionRepository
import com.example.vfsgm.data.repository.SubjectRepository
import com.example.vfsgm.data.store.TurnstileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionRepository = SessionRepository(application.applicationContext)
    val sessionState = sessionRepository.state

    private val subjectRepository = SubjectRepository()
    val subjectState = subjectRepository.state

    private val dataRepository = DataRepository()
    val dataState = dataRepository.state


    private val authApi = AuthApi()
    private val applicantApi = ApplicantApi()
    private val calenderApi = CalenderApi()

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
        val subject = subjectState.value

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.loadApplicants(
                accessToken = accessToken,
                username = subjectState.value.username,
            )
        }
    }

    fun addApplicant() {
        val accessToken = sessionState.value.accessToken ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val urn = applicantApi.addApplicant(
                accessToken = accessToken, subject = subjectState.value
            )

            dataRepository.saveUrn(urn = urn)
        }
    }

    fun getGender() {
        val accessToken = sessionState.value.accessToken ?: return

        viewModelScope.launch(Dispatchers.IO) {
            applicantApi.getGender(
                accessToken = accessToken,
            )
        }
    }

    fun loadCalender() {
        val accessToken = sessionState.value.accessToken ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val result = calenderApi.loadCalender(
                accessToken = accessToken,
                subject = subjectState.value,
                urn = dataState.value.urn
            )

            when (result) {
                is SealedResult.Success -> dataRepository.saveAvailableDates(dates = result.data)
                is SealedResult.Error -> {
                    println(result.exception.message)
                }
            }
        }
    }

    fun checkIsSlotAvailable() {
        val accessToken = sessionState.value.accessToken ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val result = calenderApi.checkIsSlotAvailable(
                accessToken = accessToken,
                subject = subjectState.value,
            )

            when (result) {
                is SealedResult.Success -> {
                    println("earliest Date Available at: ${result.data}")
                    result.data?.let {
                        dataRepository.saveEarliestSlotDates(it)
                    }
                }

                is SealedResult.Error -> {
                    println(result.exception.message)
                }
            }
        }
    }


    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.clearSession()
        }
    }
}
