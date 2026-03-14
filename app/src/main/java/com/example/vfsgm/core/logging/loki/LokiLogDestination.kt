package com.example.vfsgm.core.logging.loki

import android.content.Context
import com.example.vfsgm.core.logging.LogDestination
import com.example.vfsgm.core.logging.LogEvent
import java.util.concurrent.atomic.AtomicLong

class LokiLogDestination(
    private val context: Context,
    private val logBuffer: LokiLogBuffer
) : LogDestination {
    private val lastUrgentFlushAt = AtomicLong(0L)

    override suspend fun log(event: LogEvent) {
        val decision = logBuffer.enqueue(event)
        when (decision) {
            EnqueueDecision.FLUSH_NOW -> maybeScheduleUrgentFlush()
            EnqueueDecision.BATCH -> LokiFlushWorker.scheduleImmediate(context, urgent = false)
            EnqueueDecision.NONE -> LokiFlushWorker.scheduleDelayed(context)
        }
    }

    private fun maybeScheduleUrgentFlush() {
        val now = System.currentTimeMillis()
        val last = lastUrgentFlushAt.get()

        if (now - last < LokiConfig.IMMEDIATE_FLUSH_COOLDOWN_MS) {
            LokiFlushWorker.scheduleImmediate(context, urgent = false)
            return
        }

        if (lastUrgentFlushAt.compareAndSet(last, now)) {
            LokiFlushWorker.scheduleImmediate(context, urgent = true)
        } else {
            LokiFlushWorker.scheduleImmediate(context, urgent = false)
        }
    }
}
