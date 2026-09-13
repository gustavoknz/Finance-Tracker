package dev.gustavo.finance.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getBaseCurrencyFlow(): Flow<String>
    fun setBaseCurrency(code: String)
}
