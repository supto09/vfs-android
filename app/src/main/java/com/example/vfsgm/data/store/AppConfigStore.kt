package com.example.vfsgm.data.store

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.vfsgm.data.dto.AppConfig
import kotlinx.coroutines.flow.*


val Context.appConfigDataStore by preferencesDataStore(name = "app_config")

object AppConfigKeys {
    val DEVICE_INDEX = intPreferencesKey("device_index")
}

class AppConfigStore(private val context: Context) {
    val appConfigFlow: Flow<AppConfig> = context.appConfigDataStore.data
        .map { prefs ->
            AppConfig(
                deviceIndex = prefs[AppConfigKeys.DEVICE_INDEX] ?: 1,
            )
        }

    suspend fun updateAppConfig(appConfig: AppConfig) {
        context.appConfigDataStore.edit { prefs ->
            prefs[AppConfigKeys.DEVICE_INDEX] = appConfig.deviceIndex
        }
    }
}