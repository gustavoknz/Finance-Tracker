package dev.gustavo.finance.presentation.rates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import dev.gustavo.finance.ui.Theme.Spacing
import finance_tracker.shared.generated.resources.Res
import finance_tracker.shared.generated.resources.exchange_rates_title
import finance_tracker.shared.generated.resources.offline_notification
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

class ExchangeRateScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinViewModel<ExchangeRateViewModel>()
        val uiState by viewModel.state.collectAsStateWithLifecycle()
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
            uiState = uiState,
            onAction = viewModel::onAction,
            snackbarHostState = snackbarHostState,
        )
    }

    @Composable
    fun ExchangeRateScreenContent(
        uiState: ExchangeRateUiState,
        onAction: (ExchangeRateAction) -> Unit,
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(Res.string.exchange_rates_title))
                            Spacer(Modifier.width(Spacing.small))
                            PlatformIcon()
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).testTag(ExchangeRateTestTags.CONTENT)) {
                when (val content = uiState.content) {
                    is ExchangeRateState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag(ExchangeRateTestTags.LOADING)
                        )
                    }

                    is ExchangeRateState.Success -> {
                        PullToRefreshBox(
                            isRefreshing = content.isRefreshing,
                            onRefresh = { onAction(ExchangeRateAction.Refresh) },
                        ) {
                            RateList(
                                base = uiState.base,
                                pinnedRates = content.pinnedRates,
                                otherRates = content.otherRates,
                                lastUpdated = content.lastUpdated,
                                searchQuery = uiState.searchQuery,
                                isOffline = content.syncError != null,
                                onSearchQueryChange = { onAction(ExchangeRateAction.SearchQueryChanged(it)) },
                                onRateClick = { onAction(ExchangeRateAction.ChangeBaseCurrency(it)) },
                                onTogglePin = { onAction(ExchangeRateAction.TogglePin(it)) },
                            )
                        }
                    }

                    is ExchangeRateState.Error -> {
                        ErrorView(
                            error = content.error,
                            onRetry = { onAction(ExchangeRateAction.ChangeBaseCurrency(uiState.base)) },
                        )
                    }
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
internal fun ExchangeRateScreenPreview() {
    MaterialTheme {
        ExchangeRateScreen().ExchangeRateScreenContent(
            uiState = ExchangeRateUiState(
                base = "EUR",
                searchQuery = "",
                content = ExchangeRateState.Success(
                    pinnedRates = persistentListOf(
                        ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08", isPinned = true),
                    ),
                    otherRates = persistentListOf(
                        ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85"),
                    ),
                    lastUpdated = "2024-05-20",
                ),
            ),
            onAction = {},
        )
    }
}
