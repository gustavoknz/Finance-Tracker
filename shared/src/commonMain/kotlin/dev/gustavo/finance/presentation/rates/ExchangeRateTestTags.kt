package dev.gustavo.finance.presentation.rates

object ExchangeRateTestTags {
    const val CONTENT = "exchange_rate_content"
    const val LOADING = "loading_indicator"
    const val ERROR_VIEW = "error_view"
    const val RETRY_BUTTON = "retry_button"
    const val OFFLINE_NOTIFICATION = "offline_notification"
    const val SEARCH_FIELD = "search_field"
    const val CLEAR_SEARCH_BUTTON = "clear_search_button"
    const val HEADER_PINNED = "header_pinned"
    const val HEADER_ALL = "header_all"

    fun rateItem(code: String) = "rate_item_$code"
    fun pinButton(code: String) = "pin_button_$code"
}
