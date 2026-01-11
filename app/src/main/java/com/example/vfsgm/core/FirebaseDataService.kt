package com.example.vfsgm.core

import com.example.vfsgm.data.constants.CountryCode
import com.example.vfsgm.data.constants.MissionCode
import com.example.vfsgm.data.dto.Subject
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object FirebaseDataService {
    fun saveEarliestSlotDate(
        date: String,
        subject: Subject,
    ) {
        val db: DatabaseReference =
            FirebaseDatabase.getInstance()
                .getReference("z_earliest_date/${subject.countryCode}/${subject.missionCode}")

        val logEntry = mapOf(
            "date" to date
        )
        db.setValue(logEntry) { databaseError, p1 ->
            if (databaseError != null) {
                println(databaseError.message)
            }
        }
    }


    suspend fun readEarliestSlotDate(
        subject: Subject,
    ): String = suspendCancellableCoroutine { continuation ->
        val ref = FirebaseDatabase.getInstance()
            .getReference("z_earliest_date/${subject.countryCode}/${subject.missionCode}/date")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && continuation.isActive) {
                    snapshot.getValue(String::class.java)?.let {
                        ref.removeEventListener(this)
                        continuation.resume(it)
                    }
                }
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
}