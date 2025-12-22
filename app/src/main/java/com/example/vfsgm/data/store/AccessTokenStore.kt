package com.example.vfsgm.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.accessTokenDataStore by preferencesDataStore(name = "access_token_store")

object AccessTokenKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
}

class AccessTokenStore(private val context: Context) {
    val accessTokenFlow: Flow<String?> = context.accessTokenDataStore.data
        .map { prefs ->
            prefs[AccessTokenKeys.ACCESS_TOKEN]
        }

    suspend fun saveToken(accessToken: String) {
        println("Saving: $accessToken")
        context.accessTokenDataStore.edit { prefs ->
            prefs[AccessTokenKeys.ACCESS_TOKEN] = accessToken
        }
    }

    suspend fun clearToken() {
        context.accessTokenDataStore.edit { prefs ->
            prefs.remove(AccessTokenKeys.ACCESS_TOKEN)
        }
    }
}
