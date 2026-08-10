package dev.gustavo.finance.domain.repository

class FakePreferencesRepository : PreferencesRepository {
    var storedBaseCurrency: String = "USD"
    
    override fun getBaseCurrency(): String = storedBaseCurrency
    
    override fun setBaseCurrency(code: String) {
        storedBaseCurrency = code
    }
}
