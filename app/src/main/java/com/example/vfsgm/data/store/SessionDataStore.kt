package com.example.vfsgm.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.vfsgm.data.dto.SessionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.sessionDataStore by preferencesDataStore(name = "access_token_store")

object SessionDataStoreKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val USERNAME = stringPreferencesKey("username")
}

class SessionDataStore(private val context: Context) {
    val sessionFlow: Flow<SessionData?> = context.sessionDataStore.data
        .map { prefs ->
            val accessToken = prefs[SessionDataStoreKeys.ACCESS_TOKEN]
            val username = prefs[SessionDataStoreKeys.USERNAME]

            if (accessToken.isNullOrEmpty() || username.isNullOrEmpty()) {
                null
            } else {
                SessionData(
                    accessToken = accessToken, username = username
                )
            }

        }

    suspend fun saveSessionData(sessionData: SessionData) {
        println("Saving sessionData: $sessionData")
        context.sessionDataStore.edit { prefs ->
            prefs[SessionDataStoreKeys.ACCESS_TOKEN] = sessionData.accessToken
            prefs[SessionDataStoreKeys.USERNAME] = sessionData.username
        }
    }

    suspend fun clearToken() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(SessionDataStoreKeys.ACCESS_TOKEN)
            prefs.remove(SessionDataStoreKeys.USERNAME)
        }
    }
}
