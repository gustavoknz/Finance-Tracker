package dev.gustavo.finance.di

import dev.gustavo.finance.data.local.getIosDatabaseBuilder
import dev.gustavo.finance.util.AppConfig
import dev.gustavo.finance.util.IosAppConfig
import dev.gustavo.finance.util.IosPlatformUtils
import dev.gustavo.finance.util.PlatformUtils
import org.koin.dsl.module

actual val platformModule = module {
    single { getIosDatabaseBuilder() }
    single<PlatformUtils> { IosPlatformUtils() }
    single<AppConfig> { IosAppConfig() }
}
