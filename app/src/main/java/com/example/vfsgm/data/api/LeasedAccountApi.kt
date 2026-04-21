package com.example.vfsgm.data.api

import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.core.logging.AppLogService
import com.example.vfsgm.core.logging.DeviceIndexContext
import com.example.vfsgm.core.logging.LogType
import com.example.vfsgm.data.dto.Entry
import com.example.vfsgm.data.dto.LeasedAccount
import com.example.vfsgm.data.network.MyApiClient
import com.example.vfsgm.data.network.await
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LeasedAccountApi {
    private companion object {
        const val CLIENT_API_TOKEN = "vfs-client-token-2026-temp-9Kq2Lm7P"
    }

    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        MyApiClient().client
    }
    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val leaseAccountResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(LeaseAccountResponse::class.java)
    }

    suspend fun leaseAccount(entry: Entry): SealedResult<LeasedAccount> {
        val leaseOwner = "worker-${DeviceIndexContext.get()}"
        val requestBodyJson = """
            {
              "leaseOwner": "$leaseOwner",
              "countryCode": "${entry.countryCode}",
              "missionCode": "${entry.missionCode}",
              "clientToken": "$CLIENT_API_TOKEN"
            }
            """.trimIndent()

        AppLogService.log(
            deviceIndex = DeviceIndexContext.get(),
            message = "Lease account request started",
            logType = LogType.INFO,
            tag = "LeasedAccountApi",
            metadata = mapOf(
                "countryCode" to entry.countryCode,
                "missionCode" to entry.missionCode,
                "leaseOwner" to leaseOwner
            )
        )

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        AppLogService.log(
            deviceIndex = DeviceIndexContext.get(),
            message = "Lease account requestBody: $requestBodyJson",
            tag = "LeasedAccountApi"
        )

        val request = Request.Builder().apply {
            url("https://vfsapi.ashulo.org/accounts/lease")
            post(requestBody)
        }.build()

        return try {
            val call = client.newCall(request)
            call.await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                AppLogService.log(
                    deviceIndex = DeviceIndexContext.get(),
                    message = "Lease account response received",
                    logType = if (res.isSuccessful) LogType.SUCCESS else LogType.WARNING,
                    tag = "LeasedAccountApi",
                    metadata = mapOf("statusCode" to res.code.toString())
                )

                if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

                val leaseAccountResponse = leaseAccountResponseAdapter.fromJson(bodyStr)
                    ?: throw IOException("Failed to parse LoginResponse. Body=$bodyStr")

                SealedResult.Success(
                    LeasedAccount(
                        id = leaseAccountResponse.id,
                        email = leaseAccountResponse.email,
                        password = leaseAccountResponse.password,
                        countryCode = leaseAccountResponse.countryCode,
                        missionCode = leaseAccountResponse.missionCode,
                        dialCode = leaseAccountResponse.dialCode,
                        phoneNumber = leaseAccountResponse.phoneNumber
                    )
                )
            }
        } catch (error: Exception) {
            error.printStackTrace()
            AppLogService.log(
                deviceIndex = DeviceIndexContext.get(),
                message = "Lease account request failed",
                logType = LogType.ERROR,
                tag = "LeasedAccountApi",
                metadata = mapOf("error" to (error.message ?: "unknown")),
                critical = true
            )

            SealedResult.Error(error)
        }
    }

    suspend fun getFollowerAppCount(entry: Entry): SealedResult<Int> {
        val requestBodyJson = """
            {
              "countryCode": "${entry.countryCode}",
              "missionCode": "${entry.missionCode}",
              "clientToken": "$CLIENT_API_TOKEN"
            }
            """.trimIndent()

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder().apply {
            url("https://vfsapi.ashulo.org/accounts/follower-app-number")
            post(requestBody)
        }.build()

        return try {
            val call = client.newCall(request)
            call.await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

                val followerAppCount = parseFollowerAppCount(bodyStr)
                    ?: throw IOException("Follower app count missing in response: $bodyStr")

                if (followerAppCount <= 0) {
                    throw IOException("Follower app count must be > 0: $followerAppCount")
                }

                SealedResult.Success(followerAppCount)
            }
        } catch (error: Exception) {
            SealedResult.Error(error)
        }
    }

    private fun parseFollowerAppCount(rawBody: String): Int? {
        val json = runCatching { JSONObject(rawBody.trim()) }.getOrNull() ?: return null
        return json.optString("followerAppNumber")
            .trim()
            .toIntOrNull()
    }

    suspend fun reportBlock(email: String, entry: Entry): SealedResult<Unit> {
        val requestBodyJson = """
            {
              "email": "$email",
              "countryCode": "${entry.countryCode}",
              "missionCode": "${entry.missionCode}",
              "clientToken": "$CLIENT_API_TOKEN"
            }
            """.trimIndent()

        AppLogService.log(
            deviceIndex = DeviceIndexContext.get(),
            message = "Report blocked request started",
            logType = LogType.INFO,
            tag = "LeasedAccountApi",
            metadata = mapOf(
                "email" to email,
                "countryCode" to entry.countryCode,
                "missionCode" to entry.missionCode
            )
        )

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        AppLogService.log(
            deviceIndex = DeviceIndexContext.get(),
            message = "Report blocked requestBody: $requestBodyJson",
            tag = "LeasedAccountApi"
        )

        val request = Request.Builder().apply {
            url("https://vfsapi.ashulo.org/accounts/reportBlocked")
            post(requestBody)
        }.build()

        return try {
            val call = client.newCall(request)
            call.await().use { res ->
                res.body?.string().orEmpty()
                AppLogService.log(
                    deviceIndex = DeviceIndexContext.get(),
                    message = "Report blocked response received",
                    logType = if (res.isSuccessful) LogType.SUCCESS else LogType.WARNING,
                    tag = "LeasedAccountApi",
                    metadata = mapOf("statusCode" to res.code.toString())
                )

                SealedResult.Success(Unit)
            }
        } catch (error: Exception) {
            error.printStackTrace()
            AppLogService.log(
                deviceIndex = DeviceIndexContext.get(),
                message = "Report blocked request failed",
                logType = LogType.ERROR,
                tag = "LeasedAccountApi",
                metadata = mapOf("error" to (error.message ?: "unknown"))
            )

            SealedResult.Error(error)
        }
    }
}


@JsonClass(generateAdapter = true)
data class LeaseAccountResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "countryCode")
    val countryCode: String,

    @Json(name = "missionCode")
    val missionCode: String,

    @Json(name = "dialCode")
    val dialCode: String,

    @Json(name = "phoneNumber")
    val phoneNumber: String,
)
