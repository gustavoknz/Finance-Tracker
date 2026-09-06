package dev.gustavo.finance.presentation.rates

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExchangeRateUiTest : BaseExchangeRateUiTest() {

    @Test
    fun testLoadingState() = runLoadingStateTest()

    @Test
    fun testSuccessState() = runSuccessStateTest()

    @Test
    fun testErrorState() = runErrorStateTest()

    @Test
    fun testSearchQueryChanged() = runSearchQueryChangedTest()

    @Test
    fun testTogglePinAction() = runTogglePinActionTest()

    @Test
    fun testChangeBaseCurrencyAction() = runChangeBaseCurrencyActionTest()

    @Test
    fun testClearSearchAction() = runClearSearchActionTest()

    @Test
    fun testRetryAction() = runRetryActionTest()

    @Test
    fun testOfflineNotificationShown() = runOfflineNotificationShownTest()
}
