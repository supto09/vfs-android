package com.example.vfsgm.core.logging

data class LogEvent(
    val deviceIndex: Int,
    val message: String,
    val level: LogLevel,
    val tag: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val timestampMs: Long = System.currentTimeMillis(),
    val critical: Boolean = false
)
