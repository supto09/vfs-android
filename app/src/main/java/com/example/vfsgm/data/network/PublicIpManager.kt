package com.example.vfsgm.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.io.use

object PublicIpManager {

    @Volatile
    var publicIp: String? = null
        private set

    /**
     * Initialize once. This will fetch and store the public IP.
     */
    suspend fun init(callback: ((String) -> Unit)?) {
        publicIp = loadPublicIp(callback)
    }

    /**
     * Fetch public IPv4/IPv6 from ipify.
     */
    private suspend fun loadPublicIp(callback: ((String) -> Unit)?): String? {
        println("loadPublicIp")

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api64.ipify.org?format=json")
                    .get()
                    .build()

                OkHttpClient().newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: return@withContext null

                    println("Ip Response: $body")
                    callback?.invoke(body)

                    return@withContext JSONObject(body).getString("ip")
                }
            } catch (e: Exception) {
                callback?.invoke("Ip not loaded")
                null
            }
        }
    }
}
