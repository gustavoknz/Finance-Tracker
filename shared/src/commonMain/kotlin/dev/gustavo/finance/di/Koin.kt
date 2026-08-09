package dev.gustavo.finance.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    includes(
        networkModule,
        databaseModule,
        domainModule,
        viewModelModule,
        platformModule
    )
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule)
    }

// For iOS
@Suppress("unused")
fun initKoin() = initKoin {}
