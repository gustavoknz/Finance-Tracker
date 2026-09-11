package dev.gustavo.finance.presentation.rates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import dev.gustavo.finance.ui.Theme.Durations
import dev.gustavo.finance.ui.Theme.Spacing
import finance_tracker.shared.generated.resources.Res
import finance_tracker.shared.generated.resources.all_currencies_header
import finance_tracker.shared.generated.resources.base_currency_label
import finance_tracker.shared.generated.resources.clear_search_description
import finance_tracker.shared.generated.resources.last_updated_label
import finance_tracker.shared.generated.resources.offline_notification
import finance_tracker.shared.generated.resources.pinned_header
import finance_tracker.shared.generated.resources.search_placeholder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RateList(
    base: String,
    pinnedRates: ImmutableList<ExchangeRateUiModel>,
    otherRates: ImmutableList<ExchangeRateUiModel>,
    lastUpdated: String,
    searchQuery: String,
    isOffline: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onRateClick: (String) -> Unit,
    onTogglePin: (String) -> Unit,
) {
    var clickedCurrency by remember { mutableStateOf<String?>(null) }
    var lastPinnedCode by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(pinnedRates, otherRates) {
        clickedCurrency = null
    }

    LaunchedEffect(pinnedRates) {
        val newlyPinned = pinnedRates.find { it.code == lastPinnedCode }
        if (newlyPinned != null) {
            // Find index: "Pinned" header at 0, items follow
            val index = pinnedRates.indexOf(newlyPinned) + 1
            listState.animateScrollToItem(index)
            lastPinnedCode = null
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.base_currency_label, base),
                    style = MaterialTheme.typography.headlineSmall
                )
                if (isOffline) {
                    Text(
                        text = stringResource(Res.string.offline_notification),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(ExchangeRateTestTags.OFFLINE_NOTIFICATION)
                    )
                }
            }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium)
                .testTag(ExchangeRateTestTags.SEARCH_FIELD),
            placeholder = { Text(stringResource(Res.string.search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.outline
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.semantics { contentDescription = clearSearchDesc }
                            .testTag(ExchangeRateTestTags.CLEAR_SEARCH_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = clearSearchDesc
                        )
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            if (pinnedRates.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.pinned_header),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(vertical = Spacing.small)
                            .animateItem()
                            .semantics { heading() }
                            .testTag(ExchangeRateTestTags.HEADER_PINNED)
                    )
                }
                items(pinnedRates, key = { "pinned_${it.code}" }) { uiModel ->
                    val isClicked = uiModel.code == clickedCurrency
                    AnimatedVisibility(
                        visible = !isClicked,
                        exit = fadeOut(tween(Durations.SHORT)) + slideOutVertically(tween(Durations.SHORT)) { -it },
                        modifier = Modifier.animateItem()
                    ) {
                        RateItem(
                            uiModel = uiModel,
                            onClick = {
                                clickedCurrency = uiModel.code
                                coroutineScope.launch {
                                    delay(Durations.SHORT.toLong().milliseconds)
                                    onRateClick(uiModel.code)
                                }
                            },
                            onTogglePin = { onTogglePin(uiModel.code) },
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = stringResource(Res.string.all_currencies_header),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(vertical = Spacing.small)
                            .animateItem()
                            .semantics { heading() }
                            .testTag(ExchangeRateTestTags.HEADER_ALL)
                    )
                }
            }

            items(otherRates, key = { it.code }) { uiModel ->
                val isClicked = uiModel.code == clickedCurrency
                AnimatedVisibility(
                    visible = !isClicked,
                    exit = fadeOut(tween(Durations.SHORT)) + slideOutVertically(tween(Durations.SHORT)) { -it },
                    modifier = Modifier.animateItem()
                ) {
                    RateItem(
                        uiModel = uiModel,
                        onClick = {
                            clickedCurrency = uiModel.code
                            coroutineScope.launch {
                                delay(Durations.SHORT.toLong().milliseconds)
                                onRateClick(uiModel.code)
                            }
                        },
                        onTogglePin = {
                            if (!uiModel.isPinned) {
                                lastPinnedCode = uiModel.code
                            }
                            onTogglePin(uiModel.code)
                        },
                    )
                }
            }
        }
    }
}
