package dev.gustavo.finance.util

class FakePlatformUtils : PlatformUtils {
    override fun getCurrencySymbol(code: String): String = when(code) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> code
    }

    override fun formatDecimal(value: Double, decimals: Int): String {
        // Simple mock formatting
        return value.toString()
    }
}
