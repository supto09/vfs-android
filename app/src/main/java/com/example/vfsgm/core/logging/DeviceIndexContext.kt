package com.example.vfsgm.core.logging

import java.util.concurrent.atomic.AtomicInteger

object DeviceIndexContext {
    private val current = AtomicInteger(1)

    fun set(deviceIndex: Int) {
        if (deviceIndex > 0) {
            current.set(deviceIndex)
        }
    }

    fun get(): Int = current.get()
}
