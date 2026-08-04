package dev.gustavo.finance.util

fun getCurrencySymbol(code: String): String {
    return when (code) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "BRL" -> "R$"
        "AUD" -> "A$"
        "CAD" -> "C$"
        "CHF" -> "CHF"
        "CNY" -> "¥"
        "HKD" -> "HK$"
        "NZD" -> "NZ$"
        "BGN" -> "лв"
        "CZK" -> "Kč"
        "DKK" -> "kr"
        "HUF" -> "Ft"
        "IDR" -> "Rp"
        "ILS" -> "₪"
        "INR" -> "₹"
        "ISK" -> "kr"
        "KRW" -> "₩"
        "MXN" -> "$"
        "MYR" -> "RM"
        "NOK" -> "kr"
        "PHP" -> "₱"
        "PLN" -> "zł"
        "RON" -> "lei"
        "SEK" -> "kr"
        "SGD" -> "S$"
        "THB" -> "฿"
        "TRY" -> "₺"
        "ZAR" -> "R"
        else -> code
    }
}
