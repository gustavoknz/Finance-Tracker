package dev.gustavo.groceries.domain.repository

import dev.gustavo.groceries.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
}
