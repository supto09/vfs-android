package com.example.vfsgm.core.logging.loki

import android.content.Context
import com.example.vfsgm.core.logging.LogEvent
import com.example.vfsgm.core.logging.LogLevel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.io.File

enum class EnqueueDecision {
    NONE,
    BATCH,
    FLUSH_NOW
}

class LokiLogBuffer(context: Context) {
    private val mutex = Mutex()
    private val queue = ArrayDeque<LogEvent>()
    private val queueFile: File = File(context.filesDir, "logging/loki_queue.jsonl").apply {
        parentFile?.mkdirs()
        if (!exists()) createNewFile()
    }

    init {
        loadFromDisk()
    }

    suspend fun enqueue(event: LogEvent): EnqueueDecision = mutex.withLock {
        queue.addLast(event)
        trimToMaxSize()
        persistUnsafe()

        if (event.level == LogLevel.ERROR || event.critical) {
            return EnqueueDecision.FLUSH_NOW
        }

        val oldest = queue.firstOrNull()
        if (queue.size >= LokiConfig.BATCH_TRIGGER_SIZE) {
            return EnqueueDecision.BATCH
        }
        if (oldest != null && (System.currentTimeMillis() - oldest.timestampMs) >= LokiConfig.AGE_TRIGGER_MS) {
            return EnqueueDecision.BATCH
        }

        EnqueueDecision.NONE
    }

    suspend fun dequeueBatch(maxCount: Int = LokiConfig.BATCH_UPLOAD_SIZE): List<LogEvent> = mutex.withLock {
        if (queue.isEmpty()) return emptyList()
        val out = mutableListOf<LogEvent>()
        repeat(minOf(maxCount, queue.size)) {
            out.add(queue.removeFirst())
        }
        persistUnsafe()
        out
    }

    suspend fun requeueFront(events: List<LogEvent>) = mutex.withLock {
        if (events.isEmpty()) return@withLock
        val restored = ArrayDeque<LogEvent>(events.size + queue.size)
        events.forEach { restored.addLast(it) }
        queue.forEach { restored.addLast(it) }
        queue.clear()
        queue.addAll(restored)
        trimToMaxSize()
        persistUnsafe()
    }

    private fun trimToMaxSize() {
        while (queue.size > LokiConfig.MAX_QUEUE_SIZE) {
            queue.removeFirstOrNull()
        }
    }

    private fun loadFromDisk() {
        runCatching {
            queueFile.readLines().forEach { line ->
                if (line.isBlank()) return@forEach
                parseEvent(line)?.let { queue.addLast(it) }
            }
            trimToMaxSize()
        }.onFailure {
            println("Failed to load Loki queue file: ${it.message}")
        }
    }

    private fun persistUnsafe() {
        runCatching {
            queueFile.printWriter().use { writer ->
                queue.forEach { event ->
                    writer.println(serializeEvent(event))
                }
            }
        }.onFailure {
            println("Failed to persist Loki queue file: ${it.message}")
        }
    }

    private fun serializeEvent(event: LogEvent): String {
        val json = JSONObject().apply {
            put("deviceIndex", event.deviceIndex)
            put("message", event.message)
            put("level", event.level.name)
            put("timestampMs", event.timestampMs)
            put("critical", event.critical)
            put("tag", event.tag)
            put("metadata", JSONObject(event.metadata))
        }
        return json.toString()
    }

    private fun parseEvent(line: String): LogEvent? {
        return runCatching {
            val json = JSONObject(line)
            val metadataObj = json.optJSONObject("metadata")
            val metadata = mutableMapOf<String, String>()
            metadataObj?.keys()?.forEach { key ->
                metadata[key] = metadataObj.optString(key)
            }

            LogEvent(
                deviceIndex = json.optInt("deviceIndex", 0),
                message = json.optString("message", ""),
                level = runCatching { LogLevel.valueOf(json.optString("level", LogLevel.INFO.name)) }
                    .getOrElse { LogLevel.INFO },
                tag = json.optString("tag", "").ifBlank { null },
                metadata = metadata,
                timestampMs = json.optLong("timestampMs", System.currentTimeMillis()),
                critical = json.optBoolean("critical", false)
            )
        }.getOrNull()
    }
}
