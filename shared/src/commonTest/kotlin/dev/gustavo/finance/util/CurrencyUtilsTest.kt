package dev.gustavo.finance.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyUtilsTest {

    @Test
    fun `getCurrencySymbol should return correct symbol for known codes`() {
        val usdSymbol = getCurrencySymbol("USD")
        assertTrue(usdSymbol == "$" || usdSymbol == "US$")

        val eurSymbol = getCurrencySymbol("EUR")
        assertEquals("€", eurSymbol)

        val gbpSymbol = getCurrencySymbol("GBP")
        assertEquals("£", gbpSymbol)
    }

    @Test
    fun `getCurrencySymbol should return code for unknown codes`() {
        assertEquals("XYZ", getCurrencySymbol("XYZ"))
        assertEquals("", getCurrencySymbol(""))
    }
}
