package dev.gustavo.finance.util

import java.util.Locale

actual fun Double.format(decimals: Int): String {
    return String.format(Locale.getDefault(), "%.${decimals}f", this)
}
