package com.example.vfsgm.data.api

import com.example.vfsgm.data.network.NewOkHttpClient
import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.core.EncryptionManager
import com.example.vfsgm.data.network.await
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.FormBody
import okhttp3.Request
import java.io.IOException
import kotlin.getValue

class AuthApi {

    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        NewOkHttpClient().client
    }
    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val loginResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(LoginResponse::class.java)
    }


    suspend fun login(
        username: String, password: String, cloudflareToken: String
    ): String {
        val encryptedPassword = EncryptionManager.encryptWithRsaOaepSha256(password)

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

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/user/login")
            post(formBody)
            addHeader(
                "clientsource", ClientSourceManager.getClientSource(
                    mysteriousPrefix = "GA;"
                )
            )
            addHeader("accept", "application/json, text/plain, */*")
            addHeader("Origin", "https://visa.vfsglobal.com")
            addHeader("Referer", "https://visa.vfsglobal.com/")
        }.build()

        val call = client.newCall(request)
        call.await().use { res ->
            val bodyStr = res.body?.string().orEmpty()
            println("login response: $bodyStr")

            if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

            val loginResponse = loginResponseAdapter.fromJson(bodyStr)
                ?: throw IOException("Failed to parse LoginResponse. Body=$bodyStr")

            return loginResponse.accessToken
        }
    }
}






@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "accessToken")
    val accessToken: String,

    @Json(name = "isAuthenticated")
    val isAuthenticated: Boolean,

    @Json(name = "nearestVACCountryCode")
    val nearestVACCountryCode: String?,

    @Json(name = "FailedAttemptCount")
    val failedAttemptCount: Int,

    @Json(name = "isAppointmentBooked")
    val isAppointmentBooked: Boolean,

    @Json(name = "isLastTransactionPending")
    val isLastTransactionPending: Boolean,

    @Json(name = "isAppointmentExpired")
    val isAppointmentExpired: Boolean,

    @Json(name = "isLimitedDashboard")
    val isLimitedDashboard: Boolean,

    @Json(name = "isROCompleted")
    val isROCompleted: Boolean,

    @Json(name = "isSOCompleted")
    val isSOCompleted: Boolean,

    @Json(name = "roleName")
    val roleName: String,

    @Json(name = "isUkraineScheme")
    val isUkraineScheme: Boolean,

    @Json(name = "isUkraineSchemeDocumentUpload")
    val isUkraineSchemeDocumentUpload: Boolean,

    @Json(name = "loginUser")
    val loginUser: String,

    @Json(name = "dialCode")
    val dialCode: String,

    @Json(name = "contactNumber")
    val contactNumber: String,

    @Json(name = "remainingCount")
    val remainingCount: Int,

    @Json(name = "accountLockHours")
    val accountLockHours: Int,

    @Json(name = "enableOTPAuthentication")
    val enableOTPAuthentication: Boolean,

    @Json(name = "isNewUser")
    val isNewUser: Boolean,

    @Json(name = "taResetPWDToken")
    val taResetPWDToken: String?,

    @Json(name = "firstName")
    val firstName: String,

    @Json(name = "lastName")
    val lastName: String,

    @Json(name = "dateOfBirth")
    val dateOfBirth: String,

    @Json(name = "isPasswordExpiryMessage")
    val isPasswordExpiryMessage: Boolean,

    @Json(name = "PasswordExpirydays")
    val passwordExpiryDays: Int,

    @Json(name = "passportNumber")
    val passportNumber: String?,

    @Json(name = "isSpecialUser")
    val isSpecialUser: Boolean,

    @Json(name = "maximumlimit")
    val maximumLimit: Int,

    @Json(name = "isSuspendedTA")
    val isSuspendedTA: Boolean,

    @Json(name = "showPasswordExpiryMsgTA")
    val showPasswordExpiryMsgTA: Boolean,

    @Json(name = "passwordExpiryDaysLeftTA")
    val passwordExpiryDaysLeftTA: Int,

    @Json(name = "isEmbassyUser")
    val isEmbassyUser: Boolean,

    @Json(name = "error")
    val error: Any?
)