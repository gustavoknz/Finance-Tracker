package dev.gustavo.finance.util

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidUtilTest {

    @Test
    fun testAndroidPlatformUtils() {
        val utils = AndroidPlatformUtils()
        
        val usdSymbol = utils.getCurrencySymbol("USD")
        assertTrue(usdSymbol == "$" || usdSymbol == "US$")
        
        assertEquals("XYZ", utils.getCurrencySymbol("XYZ"))
        
        val formatted = utils.formatDecimal(1.2345, 2)
        // Depends on locale, but in US/Default typically 1.23 or 1,23
        assertTrue(formatted.contains("1") && formatted.contains("23"))
    }

    @Test
    fun testAndroidAppConfig() {
        val config = AndroidAppConfig()
        assertNotNull(config.baseUrl)
        assertTrue(config.isDebug)
    }
}
