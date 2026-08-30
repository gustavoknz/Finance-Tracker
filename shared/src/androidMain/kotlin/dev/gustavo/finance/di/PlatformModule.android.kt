package dev.gustavo.finance.di

import dev.gustavo.finance.data.local.getAndroidDatabaseBuilder
import dev.gustavo.finance.util.AndroidAppConfig
import dev.gustavo.finance.util.AndroidPlatformUtils
import dev.gustavo.finance.util.AppConfig
import dev.gustavo.finance.util.PlatformUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single { getAndroidDatabaseBuilder(androidContext()) }
    single<PlatformUtils> { AndroidPlatformUtils() }
    single<AppConfig> { AndroidAppConfig() }
}
