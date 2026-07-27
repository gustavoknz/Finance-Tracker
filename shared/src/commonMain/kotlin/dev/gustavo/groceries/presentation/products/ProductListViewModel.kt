package dev.gustavo.groceries.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.groceries.domain.model.Product
import dev.gustavo.groceries.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductListState {
    data object Loading : ProductListState
    data class Success(val products: List<Product>) : ProductListState
    data class Error(val message: String) : ProductListState
}

class ProductListViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ProductListState>(ProductListState.Loading)
    val state: StateFlow<ProductListState> = _state.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.value = ProductListState.Loading
            try {
                val products = repository.getProducts()
                _state.value = ProductListState.Success(products)
            } catch (e: Exception) {
                _state.value = ProductListState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
