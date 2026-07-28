package dev.gustavo.finance.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.finance.domain.model.Expense
import dev.gustavo.finance.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ExpenseListState {
    data object Loading : ExpenseListState
    data class Success(val expenses: List<Expense>) : ExpenseListState
    data class Error(val message: String) : ExpenseListState
}

class ExpenseListViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {
    val state: StateFlow<ExpenseListState> = repository.getExpenses()
        .map { expenses -> ExpenseListState.Success(expenses) as ExpenseListState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExpenseListState.Loading
        )
}
