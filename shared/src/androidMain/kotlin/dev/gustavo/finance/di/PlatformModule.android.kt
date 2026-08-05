package dev.gustavo.finance.di

import dev.gustavo.finance.data.local.getAndroidDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single { getAndroidDatabaseBuilder(androidContext()) }
}
