package dev.gustavo.finance.di

import com.russhwolf.settings.Settings
import dev.gustavo.finance.data.local.AppDatabase
import dev.gustavo.finance.data.local.getRoomDatabase
import dev.gustavo.finance.data.repository.SettingsPreferencesRepository
import dev.gustavo.finance.domain.repository.PreferencesRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().currencyDao() }
    single { get<AppDatabase>().exchangeRateDao() }
    single { get<AppDatabase>().metadataDao() }
    single { get<AppDatabase>().pinDao() }

    single { Settings() }
    singleOf(::SettingsPreferencesRepository) { bind<PreferencesRepository>() }
}
