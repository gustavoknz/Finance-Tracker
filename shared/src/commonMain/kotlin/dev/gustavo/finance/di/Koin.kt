package dev.gustavo.finance.di

import dev.gustavo.finance.data.local.AppDatabase
import dev.gustavo.finance.data.local.getRoomDatabase
import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.data.remote.KtorCurrencyService
import dev.gustavo.finance.data.repository.RealExchangeRateRepository
import dev.gustavo.finance.data.repository.SettingsPreferencesRepository
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.repository.PreferencesRepository
import dev.gustavo.finance.domain.usecase.*
import dev.gustavo.finance.presentation.rates.ExchangeRateViewModel
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().currencyDao() }
    single { get<AppDatabase>().exchangeRateDao() }
    single { get<AppDatabase>().metadataDao() }

    single { Settings() }
    singleOf(::SettingsPreferencesRepository) { bind<PreferencesRepository>() }

    singleOf(::KtorCurrencyService) { bind<CurrencyService>() }
    singleOf(::RealExchangeRateRepository) { bind<ExchangeRateRepository>() }
    factoryOf(::GetCurrenciesUseCase)
    factoryOf(::GetLatestRatesUseCase)
    factoryOf(::GetBaseCurrencyUseCase)
    factoryOf(::SetBaseCurrencyUseCase)
    factoryOf(::ExchangeRateViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule)
    }

// For iOS
fun initKoin() = initKoin {}
