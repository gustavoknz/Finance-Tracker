package dev.gustavo.finance.presentation.rates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import dev.gustavo.finance.domain.util.DataError
import finance_tracker.shared.generated.resources.Res
import finance_tracker.shared.generated.resources.base_currency_label
import finance_tracker.shared.generated.resources.error_client
import finance_tracker.shared.generated.resources.error_network
import finance_tracker.shared.generated.resources.error_server
import finance_tracker.shared.generated.resources.error_service_unavailable
import finance_tracker.shared.generated.resources.error_unknown
import finance_tracker.shared.generated.resources.exchange_rates_title
import finance_tracker.shared.generated.resources.last_updated_label
import finance_tracker.shared.generated.resources.retry_button
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

class ExchangeRateScreen : Screen {
    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<ExchangeRateViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

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
                        PullToRefreshBox(
                            isRefreshing = currentState.isRefreshing,
                            onRefresh = { viewModel.refresh() }
                        ) {
                            RateList(
                                base = currentState.base,
                                rates = currentState.rates,
                                lastUpdated = currentState.lastUpdated,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                onRateClick = { currency -> viewModel.fetchRates(currency) }
                            )
                        }
                    }
                    is ExchangeRateState.Error -> {
                        ErrorView(
                            error = currentState.error,
                            onRetry = { viewModel.fetchRates(currentState.base) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ErrorView(
        error: DataError.Network,
        onRetry: () -> Unit
    ) {
        val message = when (error) {
            DataError.Network.NO_INTERNET -> stringResource(Res.string.error_network)
            DataError.Network.SERVICE_UNAVAILABLE -> stringResource(Res.string.error_service_unavailable)
            DataError.Network.CLIENT_ERROR -> stringResource(Res.string.error_client)
            DataError.Network.SERVER_ERROR -> stringResource(Res.string.error_server)
            else -> stringResource(Res.string.error_unknown)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(Res.string.retry_button))
            }
        }
    }

    @Composable
    private fun RateList(
        base: String,
        rates: ImmutableList<ExchangeRateUiModel>,
        lastUpdated: String,
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
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

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search currencies...") },
                leadingIcon = { Text("🔍") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Text("✕")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rates, key = { it.code }) { uiModel ->
                    val isClicked = uiModel.code == clickedCurrency
                    
                    AnimatedVisibility(
                        visible = !isClicked,
                        exit = fadeOut(tween(500)) + slideOutVertically(tween(500)) { -it },
                        modifier = Modifier.animateItem()
                    ) {
                        RateItem(
                            uiModel = uiModel,
                            onClick = {
                                clickedCurrency = uiModel.code
                                onRateClick(uiModel.code)
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
        uiModel: ExchangeRateUiModel,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiModel.code,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = " (${uiModel.symbol})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    if (uiModel.name.isNotBlank()) {
                        Text(
                            text = uiModel.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = uiModel.formattedRate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
