package dev.gustavo.finance.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            networkModule,
            databaseModule,
            domainModule,
            viewModelModule,
            platformModule
        )
    }

// For iOS
fun initKoin() = initKoin {}
