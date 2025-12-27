package com.example.vfsgm.core

import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class JitterService {

    private val minDelayMs: Long = 5_000L
    private val maxDelayMs: Long = 10_000L

    fun nextDelayMillis(): Long {
        return Random.nextLong(minDelayMs, maxDelayMs + 1)
    }

    fun nextDelay(): Duration {
        return nextDelayMillis().milliseconds
    }
}