package dev.gustavo.finance.presentation.rates

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import finance_tracker.shared.generated.resources.Res
import finance_tracker.shared.generated.resources.base_currency_label
import finance_tracker.shared.generated.resources.exchange_rates_title
import finance_tracker.shared.generated.resources.last_updated_label
import finance_tracker.shared.generated.resources.unknown_error
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

class ExchangeRateScreen : Screen {
    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<ExchangeRateViewModel>()
        val state by viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.exchange_rates_title)) }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (val currentState = state) {
                    is ExchangeRateState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ExchangeRateState.Success -> {
                        RateList(
                            base = currentState.base,
                            rates = currentState.rates,
                            currencyNames = currentState.currencyNames,
                            lastUpdated = currentState.lastUpdated,
                            onRateClick = { currency -> viewModel.fetchRates(currency) }
                        )
                        if (currentState.isRefreshing) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                            )
                        }
                    }
                    is ExchangeRateState.Error -> {
                        Text(
                            text = currentState.message.ifBlank { stringResource(Res.string.unknown_error) },
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RateList(
        base: String,
        rates: Map<String, Double>,
        currencyNames: Map<String, String>,
        lastUpdated: String,
        onRateClick: (String) -> Unit
    ) {
        var clickedCurrency by remember { mutableStateOf<String?>(null) }

        // Reset clickedCurrency when data changes (e.g. after successful fetch)
        LaunchedEffect(rates) {
            clickedCurrency = null
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.base_currency_label, base),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(Res.string.last_updated_label, lastUpdated),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rates.toList(), key = { it.first }) { (currency, rate) ->
                    val isClicked = currency == clickedCurrency
                    
                    AnimatedVisibility(
                        visible = !isClicked,
                        exit = fadeOut(tween(500)) + slideOutVertically(tween(500)) { -it },
                        modifier = Modifier.animateItem()
                    ) {
                        RateItem(
                            currency = currency,
                            fullName = currencyNames[currency] ?: "",
                            rate = rate,
                            onClick = {
                                clickedCurrency = currency
                                onRateClick(currency)
                            }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun RateItem(
        currency: String,
        fullName: String,
        rate: Double, 
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (fullName.isNotBlank()) {
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = rate.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
