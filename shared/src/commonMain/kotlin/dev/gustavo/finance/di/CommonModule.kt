package dev.gustavo.finance.di

import dev.gustavo.finance.util.CoroutineDispatchers
import org.koin.dsl.module

val commonModule = module {
    single { CoroutineDispatchers() }
}
