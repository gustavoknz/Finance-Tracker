package dev.gustavo.groceries.data.repository

import dev.gustavo.groceries.domain.model.Expense
import dev.gustavo.groceries.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockExpenseRepository : ExpenseRepository {
    private val _expenses = MutableStateFlow(
        listOf(
            Expense("1", 45.50, "USD", "Food", "Dinner at Italian place", "2024-05-20"),
            Expense("2", 12.00, "EUR", "Transport", "Bus ticket", "2024-05-20"),
            Expense("3", 1200.00, "USD", "Rent", "Monthly rent", "2024-05-01"),
            Expense("4", 30.00, "GBP", "Entertainment", "Movie night", "2024-05-18")
        )
    )

    override fun getExpenses(): Flow<List<Expense>> = _expenses.asStateFlow()

    override suspend fun addExpense(expense: Expense) {
        _expenses.value = _expenses.value + expense
    }
}
