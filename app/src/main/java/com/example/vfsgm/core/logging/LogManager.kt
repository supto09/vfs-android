package com.example.vfsgm.core.logging

import android.content.Context
import com.example.vfsgm.core.logging.loki.LokiClient
import com.example.vfsgm.core.logging.loki.LokiConfig
import com.example.vfsgm.core.logging.loki.LokiFlushWorker
import com.example.vfsgm.core.logging.loki.LokiLogBuffer
import com.example.vfsgm.core.logging.loki.LokiLogDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object LogManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var initialized = false

    @Volatile
    private lateinit var appContext: Context

    @Volatile
    private var destinations: List<LogDestination> = emptyList()

    @Volatile
    var lokiLogBuffer: LokiLogBuffer? = null
        private set

    @Volatile
    var lokiClient: LokiClient? = null
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            appContext = context.applicationContext
            val newDestinations = mutableListOf<LogDestination>()

            if (LokiConfig.ENABLED) {
                val buffer = LokiLogBuffer(appContext)
                val client = LokiClient()
                lokiLogBuffer = buffer
                lokiClient = client
                newDestinations += LokiLogDestination(
                    context = appContext,
                    logBuffer = buffer
                )
                LokiFlushWorker.schedulePeriodic(appContext)
            }

            destinations = newDestinations
            initialized = true
        }
    }

    fun log(
        deviceIndex: Int,
        message: String,
        level: LogLevel = LogLevel.INFO,
        tag: String? = null,
        metadata: Map<String, String> = emptyMap(),
        critical: Boolean = false
    ) {
        if (!initialized) return

        val event = LogEvent(
            deviceIndex = deviceIndex,
            message = message,
            level = level,
            tag = tag,
            metadata = metadata,
            critical = critical
        )
        scope.launch {
            destinations.forEach { destination ->
                runCatching {
                    destination.log(event)
                }.onFailure {
                    println("Log destination failed: ${it.message}")
                }
            }
        }
    }
}
