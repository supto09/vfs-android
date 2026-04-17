package com.example.vfsgm.core.logging.loki

import com.example.vfsgm.BuildConfig
import com.example.vfsgm.core.logging.LogEvent
import org.json.JSONArray
import org.json.JSONObject

object LokiPayload {
    fun build(events: List<LogEvent>): String {
        if (events.isEmpty()) return """{"streams":[]}"""

        val grouped = events.groupBy { event ->
            StreamKey(
                level = event.level.name,
                tag = event.tag ?: "app",
                deviceIndex = event.deviceIndex.toString(),
                apiSource = event.metadata["apiSource"] ?: "APP"
            )
        }

        val streams = JSONArray()
        grouped.forEach { (key, groupEvents) ->
            val streamObj = JSONObject().apply {
                put("app", LokiConfig.APP_LABEL)
                put("version", BuildConfig.VERSION_NAME)
                put("level", key.level.lowercase())
                put("tag", key.tag)
                put("device_index", key.deviceIndex)
                put("api_source", key.apiSource)
            }

            val values = JSONArray()
            groupEvents.forEach { event ->
                val lineJson = JSONObject().apply {
                    put("message", event.message)
                    if (event.metadata.isNotEmpty()) {
                        put("metadata", JSONObject(event.metadata))
                    }
                }
                val renderedLine = lineJson.toString().replace("\\/", "/")
                values.put(
                    JSONArray().apply {
                        put((event.timestampMs * 1_000_000L).toString())
                        put(renderedLine)
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

private data class StreamKey(
    val level: String,
    val tag: String,
    val deviceIndex: String,
    val apiSource: String
)
