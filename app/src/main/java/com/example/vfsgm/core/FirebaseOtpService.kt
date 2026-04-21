package com.example.vfsgm.core

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseOtpService {
    fun clearLoginOtp(contactNumber: String) {
        val firebaseContactKey = toFirebaseContactKey(contactNumber) ?: return

        FirebaseDatabase.getInstance()
            .getReference("otps/$firebaseContactKey/loginOtp")
            .removeValue()
    }

    suspend fun readLoginOtp(phone: String): String =
        readLoginOtpByFirebaseKey(
            toFirebaseContactKey(phone)
                ?: throw IllegalArgumentException("Contact number is blank")
        )

    suspend fun readLoginOtpByFirebaseKey(firebaseContactKey: String): String =
        withTimeout(240_000L) {
            awaitNonBlankString("otps/$firebaseContactKey/loginOtp")
        }

    private suspend fun awaitNonBlankString(path: String): String =
        suspendCancellableCoroutine { continuation ->
            val ref = FirebaseDatabase.getInstance().getReference(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val otp = snapshot.getValue(String::class.java)?.trim().orEmpty()
                    if (otp.isBlank() || !continuation.isActive) return

                    ref.removeEventListener(this)
                    continuation.resume(otp)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (continuation.isActive) {
                        ref.removeEventListener(this)
                        continuation.resumeWithException(error.toException())
                    }
                }
            }

            ref.addValueEventListener(listener)

            continuation.invokeOnCancellation {
                ref.removeEventListener(listener)
            }
        }

    private fun toFirebaseContactKey(rawContactNumber: String): String? {
        val digitsOnly = rawContactNumber.filter { it.isDigit() }
        if (digitsOnly.isBlank()) return null
        return if (digitsOnly.startsWith("0")) digitsOnly else "0$digitsOnly"
    }
}
