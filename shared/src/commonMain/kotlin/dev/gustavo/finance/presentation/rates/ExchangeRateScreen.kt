package dev.gustavo.finance.presentation.rates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import dev.gustavo.finance.domain.util.DataError
import finance_tracker.shared.generated.resources.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
                                pinnedRates = currentState.pinnedRates,
                                otherRates = currentState.otherRates,
                                lastUpdated = currentState.lastUpdated,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                onRateClick = { currency -> viewModel.fetchRates(currency) },
                                onTogglePin = { viewModel.togglePin(it) }
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
    internal fun ErrorView(
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
    internal fun RateList(
        base: String,
        pinnedRates: ImmutableList<ExchangeRateUiModel>,
        otherRates: ImmutableList<ExchangeRateUiModel>,
        lastUpdated: String,
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
        onRateClick: (String) -> Unit,
        onTogglePin: (String) -> Unit
    ) {
        var clickedCurrency by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(pinnedRates, otherRates) {
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

            val clearSearchDesc = stringResource(Res.string.clear_search_description)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(Res.string.search_placeholder)) },
                leadingIcon = { Text("🔍", modifier = Modifier.semantics { contentDescription = "Search" }) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.semantics { contentDescription = clearSearchDesc }
                        ) {
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
                if (pinnedRates.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.pinned_header),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(pinnedRates, key = { "pinned_${it.code}" }) { uiModel ->
                        val isClicked = uiModel.code == clickedCurrency
                        AnimatedVisibility(
                            visible = !isClicked,
                            exit = fadeOut(tween(500)) + slideOutVertically(tween(500)) { -it }
                        ) {
                            RateItem(
                                uiModel = uiModel,
                                onClick = {
                                    clickedCurrency = uiModel.code
                                    onRateClick(uiModel.code)
                                },
                                onTogglePin = { onTogglePin(uiModel.code) }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.all_currencies_header),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(otherRates, key = { it.code }) { uiModel ->
                    val isClicked = uiModel.code == clickedCurrency
                    AnimatedVisibility(
                        visible = !isClicked,
                        exit = fadeOut(tween(500)) + slideOutVertically(tween(500)) { -it }
                    ) {
                        RateItem(
                            uiModel = uiModel,
                            onClick = {
                                clickedCurrency = uiModel.code
                                onRateClick(uiModel.code)
                            },
                            onTogglePin = { onTogglePin(uiModel.code) }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    internal fun RateItem(
        uiModel: ExchangeRateUiModel,
        onClick: () -> Unit,
        onTogglePin: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val description = stringResource(
            Res.string.currency_card_content_description,
            uiModel.name.ifBlank { uiModel.code },
            uiModel.formattedRate
        )
        val pinDescription = stringResource(
            if (uiModel.isPinned) Res.string.unpin_content_description else Res.string.pin_content_description,
            uiModel.code
        )

        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = description
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.semantics {
                        contentDescription = pinDescription
                    }
                ) {
                    Text(
                        text = if (uiModel.isPinned) "★" else "☆",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (uiModel.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
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

@Preview
@Composable
internal fun RateItemPreview() {
    MaterialTheme {
        ExchangeRateScreen().RateItem(
            uiModel = ExchangeRateUiModel(
                code = "USD",
                name = "United States Dollar",
                symbol = "$",
                rate = 1.0,
                formattedRate = "1.00",
                isPinned = true
            ),
            onClick = {},
            onTogglePin = {}
        )
    }
}

@Preview
@Composable
internal fun ErrorViewPreview() {
    MaterialTheme {
        ExchangeRateScreen().ErrorView(
            error = DataError.Network.NO_INTERNET,
            onRetry = {}
        )
    }
}

@Preview
@Composable
internal fun RateListPreview() {
    MaterialTheme {
        ExchangeRateScreen().RateList(
            base = "EUR",
            pinnedRates = persistentListOf(
                ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08", isPinned = true)
            ),
            otherRates = persistentListOf(
                ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85")
            ),
            lastUpdated = "2024-05-20",
            searchQuery = "",
            onSearchQueryChange = {},
            onRateClick = {},
            onTogglePin = {}
        )
    }
}
