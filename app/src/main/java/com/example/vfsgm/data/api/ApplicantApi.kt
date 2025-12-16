package com.example.vfsgm.data.api

import com.example.vfsgm.data.network.NewOkHttpClient
import com.example.vfsgm.core.ClientSourceManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class ApplicantApi {
    fun loadApplicants(accessToken: String, username: String){
        val requestBodyJson = """
            {
              "countryCode": "pak",
              "missionCode": "ukr",
              "languageCode": "en-US",
              "visaToken": null,
              "loginUser": "$username"
            }
            """.trimIndent()

        val mediaType = "application/json;charset=UTF-8".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/appointment/application")
            post(requestBody)
            addHeader("clientsource", ClientSourceManager.getClientSource("CC;"))
            addHeader("Authorize", accessToken)
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