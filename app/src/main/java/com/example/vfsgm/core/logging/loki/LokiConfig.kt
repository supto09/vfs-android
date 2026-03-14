package com.example.vfsgm.core.logging.loki

object LokiConfig {
    const val ENABLED: Boolean = true

    const val PUSH_URL: String = "https://loki.ashulo.org/loki/api/v1/push"
    const val USERNAME: String = "android"
    const val PASSWORD: String = "ashulo10104948"

    const val APP_LABEL: String = "vfs-android"
    const val MAX_QUEUE_SIZE: Int = 2000
    const val BATCH_TRIGGER_SIZE: Int = 20
    const val BATCH_UPLOAD_SIZE: Int = 100
    const val AGE_TRIGGER_MS: Long = 10_000L
    const val IMMEDIATE_FLUSH_COOLDOWN_MS: Long = 3_000L
}
