package dev.gustavo.groceries.domain.repository

import dev.gustavo.groceries.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpenses(): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense)
}
