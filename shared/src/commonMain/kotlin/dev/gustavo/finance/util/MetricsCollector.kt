package dev.gustavo.finance.util

import co.touchlab.kermit.Logger
import kotlinx.atomicfu.atomic

interface MetricsCollector {
    fun trackCacheHit(source: String)
    fun trackCacheMiss(source: String)
    fun trackRefresh(source: String)
    fun reportMetrics()
}

class KermitMetricsCollector(
    private val platformUtils: PlatformUtils
) : MetricsCollector {
    private val logger = Logger.withTag("Metrics")
    
    private val cacheHits = atomic(0)
    private val cacheMisses = atomic(0)
    private val refreshes = atomic(0)

    override fun trackCacheHit(source: String) {
        cacheHits.incrementAndGet()
        logger.d { "Cache HIT: $source" }
    }

    override fun trackCacheMiss(source: String) {
        cacheMisses.incrementAndGet()
        logger.d { "Cache MISS: $source" }
    }

    override fun trackRefresh(source: String) {
        refreshes.incrementAndGet()
        logger.d { "Refresh (Network): $source" }
    }

    override fun reportMetrics() {
        val hits = cacheHits.value
        val misses = cacheMisses.value
        val totalRequests = hits + misses
        val hitRate = if (totalRequests > 0) {
            (hits.toDouble() / totalRequests.toDouble()) * 100.0
        } else 0.0
        
        logger.i {
            """
            --- Current Metrics ---
            Cache Hits: $hits
            Cache Misses: $misses
            Cache Hit Rate: ${platformUtils.formatDecimal(hitRate, 2)}%
            Total Refreshes: ${refreshes.value}
            -----------------------
            """.trimIndent()
        }
    }
}
