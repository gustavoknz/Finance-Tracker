package dev.gustavo.finance.di

import dev.gustavo.finance.util.CoroutineDispatchers
import dev.gustavo.finance.util.KermitMetricsCollector
import dev.gustavo.finance.util.MetricsCollector
import org.koin.dsl.module

val commonModule = module {
    single { CoroutineDispatchers() }
    single<MetricsCollector> { KermitMetricsCollector(get()) }
}
