package com.example.vfsgm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vfsgm.network.AgentHolder
import com.example.vfsgm.service.ApplicationService
import com.example.vfsgm.service.AuthService
import com.example.vfsgm.service.ClientSourceService
import com.example.vfsgm.service.EncryptionService
import com.example.vfsgm.store.TurnstileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    fun login() {
        viewModelScope.launch(Dispatchers.IO) {

            val clientSource = ClientSourceService().getClientSource()
            println("ClientSource")
            println(clientSource)

            println("Login Start")
            val cloudflareToken = TurnstileStore.readToken()
            println("CloudflareToken Found: $cloudflareToken")

            AuthService().login(
                username = "witej38159@alexida.com",
                password = "CczEk4u6n!7Z9i$",
                cloudflareToken = cloudflareToken
            )
        }
    }

    fun loadApplicants(){
        ApplicationService().loadApplicants(
            accessToken = "",
            username = "witej38159@alexida.com"
        )
    }
}
