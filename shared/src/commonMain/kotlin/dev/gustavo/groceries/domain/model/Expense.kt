package dev.gustavo.groceries.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val description: String,
    val date: String
)
