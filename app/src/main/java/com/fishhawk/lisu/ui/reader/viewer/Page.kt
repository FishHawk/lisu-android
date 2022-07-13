package com.fishhawk.lisu.ui.reader.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.reader.ReaderPage
import com.fishhawk.lisu.util.interceptor.ProgressInterceptor
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@Composable
internal fun EmptyPage(
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
                text = "Chapter is empty",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun NextChapterStatePage(
    page: ReaderPage.NextChapterState,
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
                text = "${page.currentChapterName} ${page.currentChapterTitle}",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${page.nextChapterName} ${page.nextChapterTitle}",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun PrevChapterStatePage(
    page: ReaderPage.PrevChapterState,
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
                text = "${page.currentChapterName} ${page.currentChapterTitle}",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${page.prevChapterName} ${page.prevChapterTitle}",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun ImagePage(
    page: ReaderPage.Image,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
    stateModifier: Modifier = Modifier,
) {
    var retryHash by remember { mutableStateOf(0) }
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(page.url)
            .size(Size.ORIGINAL)
            .setParameter("retry_hash", retryHash, memoryCacheKey = null)
            .build()
    )
    when (val state = painter.state) {
        is AsyncImagePainter.State.Success ->
            Image(
                painter = painter,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale,
            )
        is AsyncImagePainter.State.Loading ->
            LoadingState(
                modifier = stateModifier,
                position = page.index + 1,
                url = page.url
            )
        is AsyncImagePainter.State.Error ->
            ErrorState(
                modifier = stateModifier,
                position = page.index + 1,
                throwable = state.result.throwable,
                onRetry = { retryHash++ }
            )
        AsyncImagePainter.State.Empty -> Unit
    }
}

@Composable
private fun LoadingState(modifier: Modifier, position: Int, url: String) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var progress by remember { mutableStateOf(0f) }
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.h3
            )
            if (progress > 0f) CircularProgressIndicator(progress = progress)
            else CircularProgressIndicator()

            DisposableEffect(Unit) {
                val key = url.toHttpUrlOrNull().toString()
                ProgressInterceptor.addListener(key) { progress = it }
                onDispose {
                    ProgressInterceptor.removeListener(key)
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier,
    position: Int,
    throwable: Throwable,
    onRetry: () -> Unit,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.h3
            )
            Text(
                text = throwable.message ?: "",
                style = MaterialTheme.typography.body1
            )
            TextButton(onClick = { onRetry() }) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}
