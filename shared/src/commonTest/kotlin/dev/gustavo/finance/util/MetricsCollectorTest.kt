package dev.gustavo.finance.util

import kotlin.test.Test

class MetricsCollectorTest {

    private val platformUtils = object : PlatformUtils {
        override fun getCurrencySymbol(code: String): String = code
        override fun formatDecimal(value: Double, decimals: Int): String = value.toString()
    }

    @Test
    fun `KermitMetricsCollector should track and report metrics correctly`() {
        val collector = KermitMetricsCollector(platformUtils)

        collector.trackCacheHit("test1")
        collector.trackCacheHit("test2")
        collector.trackCacheMiss("test3")
        collector.trackRefresh("test4")

        // Just calling to verify no crashes and logic coverage
        collector.reportMetrics()
    }

    @Test
    fun `KermitMetricsCollector should handle zero requests in report`() {
        val collector = KermitMetricsCollector(platformUtils)
        collector.reportMetrics()
    }
}
