package dev.gustavo.finance.util

import kotlin.time.Clock

interface TimeProvider {
    fun currentTimeMillis(): Long
}

class RealTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
