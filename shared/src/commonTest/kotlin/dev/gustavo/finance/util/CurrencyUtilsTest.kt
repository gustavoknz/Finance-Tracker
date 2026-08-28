package dev.gustavo.finance.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyUtilsTest {

    private val platformUtils = object : PlatformUtils {
        override fun getCurrencySymbol(code: String): String = when(code) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> code
        }
        override fun formatDecimal(value: Double, decimals: Int): String = value.toString()
    }

    @Test
    fun `getCurrencySymbol should return correct symbol for known codes`() {
        val usdSymbol = platformUtils.getCurrencySymbol("USD")
        assertEquals("$", usdSymbol)

        val eurSymbol = platformUtils.getCurrencySymbol("EUR")
        assertEquals("€", eurSymbol)

        val gbpSymbol = platformUtils.getCurrencySymbol("GBP")
        assertEquals("£", gbpSymbol)
    }

    @Test
    fun `getCurrencySymbol should return code for unknown codes`() {
        assertEquals("XYZ", platformUtils.getCurrencySymbol("XYZ"))
        assertEquals("", platformUtils.getCurrencySymbol(""))
    }
}
