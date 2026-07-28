package dev.gustavo.groceries.di

import dev.gustavo.groceries.data.repository.MockExchangeRateRepository
import dev.gustavo.groceries.data.repository.MockExpenseRepository
import dev.gustavo.groceries.domain.repository.ExchangeRateRepository
import dev.gustavo.groceries.domain.repository.ExpenseRepository
import dev.gustavo.groceries.presentation.expenses.ExpenseListViewModel
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
