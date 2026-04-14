package com.example.vfsgm.data.api

import com.example.vfsgm.data.network.VfsApiClient
import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.core.EncryptionManager
import com.example.vfsgm.core.logging.AppLogService
import com.example.vfsgm.core.logging.LogType
import com.example.vfsgm.data.dto.AppConfig
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
    private companion object {
        const val LOGIN_URL = "https://lift-api.vfsglobal.com/user/login"
    }

    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        VfsApiClient().client
    }
    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val loginResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(LoginResponse::class.java)
    }


    suspend fun login(
        username: String,
        password: String,
        cloudflareToken: String,
        countryCode: String,
        missionCode: String,
        appConfig: AppConfig
    ): LoginOutcome {
        val encryptedPassword = EncryptionManager.encryptWithRsaOaepSha256(password)

        AppLogService.log(
            appConfig.deviceIndex,
            "Login api called",
            LogType.INFO,
            tag = "AuthApi",
            metadata = mapOf(
                "username" to username,
                "missionCode" to missionCode,
                "countryCode" to countryCode
            )
        )

        AppLogService.log(
            appConfig.deviceIndex,
            "Login request body: username=$username, missioncode=$missionCode, countrycode=$countryCode, languageCode=en-US, captcha_version=cloudflare-v1, password=<redacted>, captcha_api_key=<redacted>",
            LogType.DEBUG,
            tag = "AuthApi"
        )

        return executeLoginRequest(
            username = username,
            encryptedPassword = encryptedPassword,
            cloudflareToken = cloudflareToken,
            countryCode = countryCode,
            missionCode = missionCode,
            appConfig = appConfig,
            otp = null,
            logPrefix = "Login"
        )
    }

    suspend fun verifyOtp(
        username: String,
        password: String,
        cloudflareToken: String,
        countryCode: String,
        missionCode: String,
        otp: String,
        appConfig: AppConfig
    ): LoginOutcome {
        val encryptedPassword = EncryptionManager.encryptWithRsaOaepSha256(password)
        AppLogService.log(
            appConfig.deviceIndex,
            "Verify OTP api called",
            LogType.INFO,
            tag = "AuthApi",
            metadata = mapOf(
                "username" to username,
                "missionCode" to missionCode,
                "countryCode" to countryCode
            )
        )
        AppLogService.log(
            appConfig.deviceIndex,
            "Verify OTP request body: username=$username, missioncode=$missionCode, countrycode=$countryCode, languageCode=en-US, captcha_version=cloudflare-v1, password=<redacted>, captcha_api_key=<redacted>, otp=<redacted>",
            LogType.DEBUG,
            tag = "AuthApi"
        )
        return executeLoginRequest(
            username = username,
            encryptedPassword = encryptedPassword,
            cloudflareToken = cloudflareToken,
            countryCode = countryCode,
            missionCode = missionCode,
            appConfig = appConfig,
            otp = otp,
            logPrefix = "Verify OTP"
        )
    }

    private suspend fun executeLoginRequest(
        username: String,
        encryptedPassword: String,
        cloudflareToken: String,
        countryCode: String,
        missionCode: String,
        appConfig: AppConfig,
        otp: String?,
        logPrefix: String
    ): LoginOutcome {
        val formBody = FormBody.Builder().apply {
            add("username", username)
            add("password", encryptedPassword)
            add("missioncode", missionCode)
            add("countrycode", countryCode)
            add("languageCode", "en-US")
            add("captcha_version", "cloudflare-v1")
            add("captcha_api_key", cloudflareToken)
            if (!otp.isNullOrBlank()) add("otp", otp)
        }.build()

        val request = Request.Builder().apply {
            url(LOGIN_URL)
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

        try {
            val call = client.newCall(request)
            call.await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                AppLogService.log(
                    appConfig.deviceIndex,
                    "$logPrefix response received",
                    if (res.isSuccessful) LogType.SUCCESS else LogType.WARNING,
                    tag = "AuthApi",
                    metadata = mapOf("statusCode" to res.code.toString())
                )

                if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

                val loginResponse = loginResponseAdapter.fromJson(bodyStr)
                    ?: throw IOException("Failed to parse LoginResponse. Body=$bodyStr")

                if (loginResponse.enableOTPAuthentication == true && loginResponse.accessToken.isNullOrBlank()) {
                    AppLogService.log(
                        appConfig.deviceIndex,
                        "Login requires OTP verification",
                        LogType.WARNING,
                        tag = "AuthApi",
                        metadata = mapOf(
                            "contactNumber" to (loginResponse.contactNumber ?: ""),
                            "dialCode" to (loginResponse.dialCode ?: "")
                        )
                    )
                    return LoginOutcome.OtpRequired(
                        dialCode = loginResponse.dialCode.orEmpty(),
                        contactNumber = loginResponse.contactNumber.orEmpty()
                    )
                }

                val accessToken = loginResponse.accessToken
                if (!accessToken.isNullOrBlank()) {
                    return LoginOutcome.Success(accessToken)
                }

                return LoginOutcome.Failure("Login response did not contain access token")
            }
        } catch (error: Exception) {
            error.printStackTrace()
            AppLogService.log(
                appConfig.deviceIndex,
                "$logPrefix request failed",
                LogType.ERROR,
                tag = "AuthApi",
                metadata = mapOf("error" to (error.message ?: "unknown")),
                critical = true
            )
        }
        return LoginOutcome.Failure("$logPrefix request failed")
    }
}

sealed interface LoginOutcome {
    data class Success(val accessToken: String) : LoginOutcome
    data class OtpRequired(
        val dialCode: String,
        val contactNumber: String
    ) : LoginOutcome
    data class Failure(val reason: String) : LoginOutcome
}


@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "accessToken")
    val accessToken: String?,

    @Json(name = "isAuthenticated")
    val isAuthenticated: Boolean?,

    @Json(name = "nearestVACCountryCode")
    val nearestVACCountryCode: String?,

    @Json(name = "FailedAttemptCount")
    val failedAttemptCount: Int?,

    @Json(name = "isAppointmentBooked")
    val isAppointmentBooked: Boolean?,

    @Json(name = "isLastTransactionPending")
    val isLastTransactionPending: Boolean?,

    @Json(name = "isAppointmentExpired")
    val isAppointmentExpired: Boolean?,

    @Json(name = "isLimitedDashboard")
    val isLimitedDashboard: Boolean?,

    @Json(name = "isROCompleted")
    val isROCompleted: Boolean?,

    @Json(name = "isSOCompleted")
    val isSOCompleted: Boolean?,

    @Json(name = "roleName")
    val roleName: String?,

    @Json(name = "isUkraineScheme")
    val isUkraineScheme: Boolean?,

    @Json(name = "isUkraineSchemeDocumentUpload")
    val isUkraineSchemeDocumentUpload: Boolean?,

    @Json(name = "loginUser")
    val loginUser: String?,

    @Json(name = "dialCode")
    val dialCode: String?,

    @Json(name = "contactNumber")
    val contactNumber: String?,

    @Json(name = "remainingCount")
    val remainingCount: Int?,

    @Json(name = "accountLockHours")
    val accountLockHours: Int?,

    @Json(name = "enableOTPAuthentication")
    val enableOTPAuthentication: Boolean?,

    @Json(name = "isNewUser")
    val isNewUser: Boolean?,

    @Json(name = "taResetPWDToken")
    val taResetPWDToken: String?,

    @Json(name = "firstName")
    val firstName: String?,

    @Json(name = "lastName")
    val lastName: String?,

    @Json(name = "dateOfBirth")
    val dateOfBirth: String?,

    @Json(name = "isPasswordExpiryMessage")
    val isPasswordExpiryMessage: Boolean?,

    @Json(name = "PasswordExpirydays")
    val passwordExpiryDays: Int?,

    @Json(name = "passportNumber")
    val passportNumber: String?,

    @Json(name = "isSpecialUser")
    val isSpecialUser: Boolean?,

    @Json(name = "maximumlimit")
    val maximumLimit: Int?,

    @Json(name = "isSuspendedTA")
    val isSuspendedTA: Boolean?,

    @Json(name = "showPasswordExpiryMsgTA")
    val showPasswordExpiryMsgTA: Boolean?,

    @Json(name = "passwordExpiryDaysLeftTA")
    val passwordExpiryDaysLeftTA: Int?,

    @Json(name = "isEmbassyUser")
    val isEmbassyUser: Boolean?,

    @Json(name = "error")
    val error: Any?
)
