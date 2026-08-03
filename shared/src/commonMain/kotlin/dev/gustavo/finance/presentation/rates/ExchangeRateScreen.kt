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
                    title = { Text("Exchange Rates") }
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
                            text = currentState.message,
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
        onRateClick: (String) -> Unit
    ) {
        var clickedCurrency by remember { mutableStateOf<String?>(null) }

        // Reset clickedCurrency when data changes (e.g. after successful fetch)
        LaunchedEffect(rates) {
            clickedCurrency = null
        }

        Column {
            Text(
                text = "Base: $base",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
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
                Text(
                    text = currency,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = rate.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
