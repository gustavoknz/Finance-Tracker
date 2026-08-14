package dev.gustavo.finance.di

import dev.gustavo.finance.data.repository.RealExchangeRateRepository
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.usecase.GetBaseCurrencyUseCase
import dev.gustavo.finance.domain.usecase.GetCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.GetLatestRatesUseCase
import dev.gustavo.finance.domain.usecase.GetPinnedCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.SetBaseCurrencyUseCase
import dev.gustavo.finance.domain.usecase.TogglePinUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::RealExchangeRateRepository) { bind<ExchangeRateRepository>() }
    
    factoryOf(::GetCurrenciesUseCase)
    factoryOf(::GetLatestRatesUseCase)
    factoryOf(::GetBaseCurrencyUseCase)
    factoryOf(::SetBaseCurrencyUseCase)
    factoryOf(::GetPinnedCurrenciesUseCase)
    factoryOf(::TogglePinUseCase)
}
