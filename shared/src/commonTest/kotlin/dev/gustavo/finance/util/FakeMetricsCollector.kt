package dev.gustavo.finance.util

class FakeMetricsCollector : MetricsCollector {
    var cacheHitCount = 0
    var cacheMissCount = 0
    var refreshCount = 0

    override fun trackCacheHit(source: String) {
        cacheHitCount++
    }

    override fun trackCacheMiss(source: String) {
        cacheMissCount++
    }

    override fun trackRefresh(source: String) {
        refreshCount++
    }

    override fun reportMetrics() {
        // No-op for tests
    }
}
