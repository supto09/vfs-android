package com.example.vfsgm.core.logging

interface LogDestination {
    suspend fun log(event: LogEvent)
}
