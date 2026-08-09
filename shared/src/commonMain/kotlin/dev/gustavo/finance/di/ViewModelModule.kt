package dev.gustavo.finance.di

import dev.gustavo.finance.presentation.rates.ExchangeRateViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::ExchangeRateViewModel)
}
