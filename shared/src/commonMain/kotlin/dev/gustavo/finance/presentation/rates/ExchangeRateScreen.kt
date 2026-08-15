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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import dev.gustavo.finance.domain.util.DataError
import finance_tracker.shared.generated.resources.Res
import finance_tracker.shared.generated.resources.all_currencies_header
import finance_tracker.shared.generated.resources.base_currency_label
import finance_tracker.shared.generated.resources.clear_search_description
import finance_tracker.shared.generated.resources.currency_card_content_description
import finance_tracker.shared.generated.resources.error_client
import finance_tracker.shared.generated.resources.error_network
import finance_tracker.shared.generated.resources.error_server
import finance_tracker.shared.generated.resources.error_service_unavailable
import finance_tracker.shared.generated.resources.error_unknown
import finance_tracker.shared.generated.resources.exchange_rates_title
import finance_tracker.shared.generated.resources.last_updated_label
import finance_tracker.shared.generated.resources.offline_notification
import finance_tracker.shared.generated.resources.pin_content_description
import finance_tracker.shared.generated.resources.pinned_header
import finance_tracker.shared.generated.resources.retry_button
import finance_tracker.shared.generated.resources.search_placeholder
import finance_tracker.shared.generated.resources.unpin_content_description
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

class ExchangeRateScreen : Screen {

    companion object {
        private const val ANIMATION_DURATION = 500
    }

    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<ExchangeRateViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val offlineMessage = stringResource(Res.string.offline_notification)

        LaunchedEffect(Unit) {
            viewModel.uiEvents.collectLatest { event ->
                when (event) {
                    ExchangeRateUiEvent.ShowOfflineNotification -> {
                        snackbarHostState.showSnackbar(
                            message = offlineMessage,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }

        ExchangeRateScreenContent(
            state = state,
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onRefresh = viewModel::refresh,
            onRateClick = viewModel::fetchRates,
            onTogglePin = viewModel::togglePin,
            snackbarHostState = snackbarHostState
        )
    }

    @Suppress("LongParameterList")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ExchangeRateScreenContent(
        state: ExchangeRateState,
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
        onRefresh: () -> Unit,
        onRateClick: (String) -> Unit,
        onTogglePin: (String) -> Unit,
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(Res.string.exchange_rates_title))
                            Spacer(Modifier.width(8.dp))
                            PlatformIcon()
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (state) {
                    is ExchangeRateState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is ExchangeRateState.Success -> {
                        PullToRefreshBox(
                            isRefreshing = state.isRefreshing,
                            onRefresh = onRefresh
                        ) {
                            RateList(
                                base = state.base,
                                pinnedRates = state.pinnedRates,
                                otherRates = state.otherRates,
                                lastUpdated = state.lastUpdated,
                                searchQuery = searchQuery,
                                onSearchQueryChange = onSearchQueryChange,
                                onRateClick = onRateClick,
                                onTogglePin = onTogglePin
                            )
                        }
                    }

                    is ExchangeRateState.Error -> {
                        ErrorView(
                            error = state.error,
                            onRetry = { onRateClick(state.base) }
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

    @Suppress("LongParameterList", "LongMethod")
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
                            exit = fadeOut(tween(ANIMATION_DURATION)) + slideOutVertically(tween(ANIMATION_DURATION)) { -it }
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
                        exit = fadeOut(tween(ANIMATION_DURATION)) + slideOutVertically(tween(ANIMATION_DURATION)) { -it }
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

    @Suppress("LongMethod")
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

@Suppress("MagicNumber")
@Preview
@Composable
internal fun ExchangeRateScreenPreview() {
    MaterialTheme {
        ExchangeRateScreen().ExchangeRateScreenContent(
            state = ExchangeRateState.Success(
                base = "EUR",
                pinnedRates = persistentListOf(
                    ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08", isPinned = true)
                ),
                otherRates = persistentListOf(
                    ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85")
                ),
                lastUpdated = "2024-05-20"
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            onRefresh = {},
            onRateClick = {},
            onTogglePin = {}
        )
    }
}
