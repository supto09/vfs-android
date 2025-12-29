package com.example.vfsgm.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.data.store.SessionDataStore
import com.example.vfsgm.data.dto.SessionData
import com.example.vfsgm.data.store.AppConfigKeys
import com.example.vfsgm.data.store.AppConfigStore
import com.example.vfsgm.data.store.appConfigDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppConfigRepository(
    context: Context
) {
    private val appConfigStore = AppConfigStore(context)

    private val _state = MutableStateFlow(AppConfig())
    val state: StateFlow<AppConfig> = _state.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            appConfigStore.appConfigFlow
                .distinctUntilChanged()
                .collect { appConfig ->
                    _state.update { appConfig }
                }
        }
    }

    suspend fun updateAppConfig(appConfig: AppConfig) {
        appConfigStore.updateAppConfig(appConfig = appConfig)
    }
}