package dev.gustavo.finance.domain.repository

interface PreferencesRepository {
    fun getBaseCurrency(): String
    fun setBaseCurrency(code: String)
}
