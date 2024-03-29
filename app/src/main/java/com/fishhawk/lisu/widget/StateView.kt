package com.fishhawk.lisu.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.R

@Composable
fun <T> StateView(
    result: Result<T>?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (value: T, modifier: Modifier) -> Unit,
) {
    result
        ?.onSuccess { content(it, modifier) }
        ?.onFailure { ErrorView(it, onRetry, modifier) }
        ?: LoadingView(modifier)
}

@Composable
fun <T> StateView(
    result: Result<T>?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (value: T) -> Unit,
) {
    result
        ?.onSuccess { content(it) }
        ?.onFailure { ErrorView(it, onRetry, modifier) }
        ?: LoadingView(modifier)
}

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(48.dp)) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ErrorView(
    throwable: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = throwable.localizedMessage
                    ?: stringResource(R.string.unknown_error),
                style = typography.titleMedium,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
fun EmptyView(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = "List is empty.")
    }
}

@Composable
fun LoadingItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ErrorItem(
    throwable: Throwable,
    onRetry: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = throwable.localizedMessage
                ?: stringResource(R.string.unknown_error),
            modifier = Modifier.weight(1f),
            style = typography.titleMedium,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.action_retry))
        }
    }
}
