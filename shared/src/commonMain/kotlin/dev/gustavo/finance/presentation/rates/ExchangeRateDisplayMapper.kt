package dev.gustavo.finance.presentation.rates

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.util.format
import dev.gustavo.finance.util.getCurrencySymbol
import kotlinx.collections.immutable.toImmutableList

class ExchangeRateDisplayMapper {

    companion object {
        private const val LOW_RATE_THRESHOLD = 0.1
        private const val HIGH_PRECISION_DECIMALS = 4
        private const val STANDARD_PRECISION_DECIMALS = 2
    }

    fun mapToSuccessState(
        currencyNames: Map<String, String>,
        ratesResponse: ExchangeRatesResponse,
        pinnedCodes: Set<String>
    ): ExchangeRateState.Success {
        val allUiRates = ratesResponse.rates.map { (code, rate) ->
            val decimals = if (rate < LOW_RATE_THRESHOLD) HIGH_PRECISION_DECIMALS else STANDARD_PRECISION_DECIMALS
            ExchangeRateUiModel(
                code = code,
                name = currencyNames[code] ?: "",
                symbol = getCurrencySymbol(code),
                rate = rate,
                formattedRate = rate.format(decimals),
                isPinned = pinnedCodes.contains(code)
            )
        }

        val (pinned, others) = allUiRates.partition { it.isPinned }

        return ExchangeRateState.Success(
            base = ratesResponse.base,
            pinnedRates = pinned.toImmutableList(),
            otherRates = others.toImmutableList(),
            lastUpdated = ratesResponse.date
        )
    }
}
