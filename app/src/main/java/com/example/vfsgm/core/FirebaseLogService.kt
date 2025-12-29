package com.example.vfsgm.core

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.snapshot.Index
import kotlin.to


enum class LogType {
    INFO, WARNING, ERROR, SUCCESS
}

object FirebaseLogService {
    fun log(
        deviceIndex: Int,
        message: String,
        logType: LogType = LogType.INFO
    ) {
        val db: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("z_logs/$deviceIndex")

        val logEntry = mapOf(
            "message" to message,
            "type" to logType,
            "timestamp" to ServerValue.TIMESTAMP
        )
        db.push().setValue(logEntry) { databaseError, p1 ->
            if (databaseError != null) {
                println(databaseError.message)
            }
        }
    }
}