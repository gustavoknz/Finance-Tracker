package dev.gustavo.finance.di

import dev.gustavo.finance.util.CoroutineDispatchers
import dev.gustavo.finance.util.KermitMetricsCollector
import dev.gustavo.finance.util.MetricsCollector
import dev.gustavo.finance.util.RealTimeProvider
import dev.gustavo.finance.util.TimeProvider
import org.koin.dsl.module

val commonModule = module {
    single { CoroutineDispatchers() }
    single<MetricsCollector> { KermitMetricsCollector(get()) }
    single<TimeProvider> { RealTimeProvider() }
}
