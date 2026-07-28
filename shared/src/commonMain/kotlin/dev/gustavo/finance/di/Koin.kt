package dev.gustavo.finance.di

import dev.gustavo.finance.data.repository.MockExchangeRateRepository
import dev.gustavo.finance.data.repository.MockExpenseRepository
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.repository.ExpenseRepository
import dev.gustavo.finance.presentation.expenses.ExpenseListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    singleOf(::MockExpenseRepository) { bind<ExpenseRepository>() }
    singleOf(::MockExchangeRateRepository) { bind<ExchangeRateRepository>() }
    factoryOf(::ExpenseListViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

// For iOS
fun initKoin() = initKoin {}
