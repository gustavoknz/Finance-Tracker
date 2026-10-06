package dev.gustavo.finance.di

import dev.gustavo.finance.data.repository.CacheConfig
import dev.gustavo.finance.data.repository.RealExchangeRateRepository
import dev.gustavo.finance.data.repository.SettingsPreferencesRepository
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.repository.PreferencesRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single { CacheConfig() }
    singleOf(::RealExchangeRateRepository) { bind<ExchangeRateRepository>() }
    singleOf(::SettingsPreferencesRepository) { bind<PreferencesRepository>() }
}
