package com.example.vfsgm.service

import com.example.vfsgm.network.NewOkHttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthService {
    suspend fun login(
        username: String, password: String, cloudflareToken: String
    ) {
        val encryptedPassword = EncryptionService().encryptWithRsaOaepSha256(password)
        val clientSource = ClientSourceService().getClientSource()

        println("Username: $username | Password: $password")
        println(encryptedPassword)


        val formBody = FormBody.Builder().apply {
            add("username", username)
            add("password", encryptedPassword)
            add("missioncode", "ukr")
            add("countrycode", "pak")
            add("languageCode", "en-US")
            add("captcha_version", "cloudflare-v1")
            add("captcha_api_key", cloudflareToken)
        }.build()


        println(formBody.toString())

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/user/login")
            post(formBody)
            addHeader("clientsource", clientSource)
            addHeader("accept", "application/json, text/plain, */*")
            addHeader("Origin", "https://visa.vfsglobal.com")
            addHeader("Referer", "https://visa.vfsglobal.com/")
        }.build()

        NewOkHttpClient().client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    println("Status: ${it.code}")
                    println(it.body?.string())
                }
            }
        })
    }
}
