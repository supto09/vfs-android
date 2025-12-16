package com.example.vfsgm.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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