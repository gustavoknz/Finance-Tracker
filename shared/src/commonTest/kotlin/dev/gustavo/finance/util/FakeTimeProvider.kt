package dev.gustavo.finance.util

class FakeTimeProvider(var currentTime: Long = 0L) : TimeProvider {
    override fun currentTimeMillis(): Long = currentTime

    fun advanceTime(millis: Long) {
        currentTime += millis
    }
}
