package dev.gustavo.finance.presentation.rates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gustavo.finance.ui.Theme.Spacing
import finance_tracker.shared.generated.resources.Res
import finance_tracker.shared.generated.resources.currency_card_content_description
import finance_tracker.shared.generated.resources.pin_content_description
import finance_tracker.shared.generated.resources.unpin_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun RateItem(
    uiModel: ExchangeRateUiModel,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier,
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
            }
            .testTag(ExchangeRateTestTags.rateItem(uiModel.code)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.semantics {
                    contentDescription = pinDescription
                }.testTag(ExchangeRateTestTags.pinButton(uiModel.code))
            ) {
                Icon(
                    imageVector = if (uiModel.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = pinDescription,
                    tint = if (uiModel.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
                        modifier = Modifier.padding(start = Spacing.xSmall)
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

@Preview
@Composable
fun RateItemPreview() {
    MaterialTheme {
        RateItem(
            uiModel = ExchangeRateUiModel(
                code = "USD",
                name = "United States Dollar",
                symbol = "$",
                rate = 1.0,
                formattedRate = "1.00",
                isPinned = true,
            ),
            onClick = {},
            onTogglePin = {},
        )
    }
}
