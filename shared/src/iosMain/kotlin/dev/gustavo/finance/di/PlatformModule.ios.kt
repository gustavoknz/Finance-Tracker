package dev.gustavo.finance.di

import dev.gustavo.finance.data.local.getIosDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single { getIosDatabaseBuilder() }
}
