package dev.gustavo.finance.presentation.rates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.ui.Theme.Spacing
import finance_tracker.shared.generated.resources.Res
import finance_tracker.shared.generated.resources.error_client
import finance_tracker.shared.generated.resources.error_network
import finance_tracker.shared.generated.resources.error_server
import finance_tracker.shared.generated.resources.error_service_unavailable
import finance_tracker.shared.generated.resources.error_unknown
import finance_tracker.shared.generated.resources.retry_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorView(
    error: DataError.Network,
    onRetry: () -> Unit,
) {
    val message = when (error) {
        DataError.Network.NO_INTERNET -> stringResource(Res.string.error_network)
        DataError.Network.SERVICE_UNAVAILABLE -> stringResource(Res.string.error_service_unavailable)
        DataError.Network.CLIENT_ERROR -> stringResource(Res.string.error_client)
        DataError.Network.SERVER_ERROR -> stringResource(Res.string.error_server)
        else -> stringResource(Res.string.error_unknown)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.medium)
            .testTag(ExchangeRateTestTags.ERROR_VIEW),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.medium))
        Button(
            onClick = onRetry,
            modifier = Modifier.testTag(ExchangeRateTestTags.RETRY_BUTTON)
        ) {
            Text(stringResource(Res.string.retry_button))
        }
    }
}

@Preview
@Composable
fun ErrorViewPreview() {
    MaterialTheme {
        ErrorView(
            error = DataError.Network.NO_INTERNET,
            onRetry = {},
        )
    }
}
