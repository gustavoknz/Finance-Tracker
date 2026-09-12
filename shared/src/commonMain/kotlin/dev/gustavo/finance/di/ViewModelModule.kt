package dev.gustavo.finance.di

import dev.gustavo.finance.presentation.rates.ExchangeRateDisplayMapper
import dev.gustavo.finance.presentation.rates.ExchangeRateViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelModule = module {
    singleOf(::ExchangeRateDisplayMapper)
    factoryOf(::ExchangeRateViewModel)
}
