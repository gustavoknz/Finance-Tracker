package dev.gustavo.groceries.data.repository

import dev.gustavo.groceries.domain.model.Product
import dev.gustavo.groceries.domain.repository.ProductRepository
import kotlinx.coroutines.delay

class MockProductRepository : ProductRepository {
    override suspend fun getProducts(): List<Product> {
        delay(1000) // Simulate network delay
        return listOf(
            Product(1, "Apples", 2.99, "Fresh red apples", "https://example.com/apples.jpg"),
            Product(2, "Milk", 1.49, "1L Whole milk", "https://example.com/milk.jpg"),
            Product(3, "Bread", 2.00, "Whole grain bread", "https://example.com/bread.jpg"),
            Product(4, "Eggs", 3.50, "Organic eggs (12 pack)", "https://example.com/eggs.jpg"),
            Product(5, "Bananas", 1.20, "Bunch of ripe bananas", "https://example.com/bananas.jpg")
        )
    }
}
