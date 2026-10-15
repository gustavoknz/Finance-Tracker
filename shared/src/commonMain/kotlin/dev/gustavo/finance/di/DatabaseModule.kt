package dev.gustavo.finance.di

import dev.gustavo.finance.data.local.AppDatabase
import dev.gustavo.finance.data.local.getRoomDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().currencyDao() }
    single { get<AppDatabase>().exchangeRateDao() }
    single { get<AppDatabase>().metadataDao() }
    single { get<AppDatabase>().pinDao() }
}
