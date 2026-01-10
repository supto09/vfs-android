package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.vfsgm.core.JitterService
import com.example.vfsgm.data.api.ApplicantApi
import com.example.vfsgm.data.api.AuthApi
import com.example.vfsgm.data.api.CalenderApi
import com.example.vfsgm.data.api.SubjectApi
import com.example.vfsgm.data.repository.AppConfigRepository
import com.example.vfsgm.data.repository.DataRepository
import com.example.vfsgm.data.repository.SessionRepository
import com.example.vfsgm.data.repository.SubjectRepository
import kotlinx.coroutines.Job

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val appConfigRepository = AppConfigRepository(application.applicationContext)
    val appConfigState = appConfigRepository.state

    val sessionRepository = SessionRepository(application.applicationContext)
    val sessionState = sessionRepository.state

    val subjectRepository = SubjectRepository()
    val subjectState = subjectRepository.state

    val dataRepository = DataRepository()
    val dataState = dataRepository.state


    val subjectApi = SubjectApi()
    val authApi = AuthApi()
    val applicantApi = ApplicantApi()
    val calenderApi = CalenderApi()
    val jitterService = JitterService()


    // jobs
    var reLoginJob: Job? = null
    var checkSlotJob: Job? = null
    var loadSlotSlob: Job? = null
}