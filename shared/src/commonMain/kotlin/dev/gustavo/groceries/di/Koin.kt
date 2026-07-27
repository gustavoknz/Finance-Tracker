package dev.gustavo.groceries.di

import dev.gustavo.groceries.data.repository.MockProductRepository
import dev.gustavo.groceries.domain.repository.ProductRepository
import dev.gustavo.groceries.presentation.products.ProductListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    singleOf(::MockProductRepository) { bind<ProductRepository>() }
    factoryOf(::ProductListViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

// For iOS
fun initKoin() = initKoin {}
