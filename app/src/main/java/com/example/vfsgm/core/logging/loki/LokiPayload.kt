package com.example.vfsgm.core.logging.loki

import com.example.vfsgm.BuildConfig
import com.example.vfsgm.core.logging.LogEvent
import org.json.JSONArray
import org.json.JSONObject

object LokiPayload {
    fun build(events: List<LogEvent>): String {
        if (events.isEmpty()) return """{"streams":[]}"""

        val grouped = events.groupBy { event ->
            Triple(event.level.name, event.tag ?: "app", event.deviceIndex.toString())
        }

        val streams = JSONArray()
        grouped.forEach { (key, groupEvents) ->
            val streamObj = JSONObject().apply {
                put("app", LokiConfig.APP_LABEL)
                put("version", BuildConfig.VERSION_NAME)
                put("level", key.first.lowercase())
                put("tag", key.second)
                put("device_index", key.third)
            }

            val values = JSONArray()
            groupEvents.forEach { event ->
                val lineJson = JSONObject().apply {
                    put("message", event.message)
                    if (event.metadata.isNotEmpty()) {
                        put("metadata", JSONObject(event.metadata))
                    }
                }
                values.put(
                    JSONArray().apply {
                        put((event.timestampMs * 1_000_000L).toString())
                        put(lineJson.toString())
                    }
                )
            }

            streams.put(
                JSONObject().apply {
                    put("stream", streamObj)
                    put("values", values)
                }
            )
        }

        return JSONObject().put("streams", streams).toString()
    }
}
