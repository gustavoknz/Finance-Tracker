package dev.gustavo.finance.data.repository

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class CacheConfig(
    val currenciesTtlMillis: Long = 24.hours.inWholeMilliseconds,
    val ratesTtlMillis: Long = 30.minutes.inWholeMilliseconds,
    val cleanupThresholdMillis: Long = 7.days.inWholeMilliseconds,
)
