package com.example.vfsgm.core.logging

enum class LogType {
    DEBUG, INFO, WARNING, ERROR, SUCCESS
}

object AppLogService {
    fun log(
        deviceIndex: Int,
        message: String,
        logType: LogType = LogType.INFO
    ) {
        log(
            deviceIndex = deviceIndex,
            message = message,
            logType = logType,
            tag = null,
            metadata = emptyMap(),
            critical = logType == LogType.ERROR
        )
    }

    fun log(
        deviceIndex: Int,
        message: String,
        logType: LogType = LogType.INFO,
        tag: String? = null,
        metadata: Map<String, String> = emptyMap(),
        critical: Boolean = false
    ) {
        LogManager.log(
            deviceIndex = deviceIndex,
            message = message,
            level = logType.toLogLevel(),
            tag = tag,
            metadata = metadata,
            critical = critical
        )
    }
}

private fun LogType.toLogLevel(): LogLevel {
    return when (this) {
        LogType.DEBUG -> LogLevel.DEBUG
        LogType.INFO -> LogLevel.INFO
        LogType.WARNING -> LogLevel.WARNING
        LogType.ERROR -> LogLevel.ERROR
        LogType.SUCCESS -> LogLevel.SUCCESS
    }
}
