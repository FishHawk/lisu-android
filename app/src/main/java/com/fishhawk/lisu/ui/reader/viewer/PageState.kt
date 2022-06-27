package com.fishhawk.lisu.ui.reader.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.reader.ReaderPage
import com.fishhawk.lisu.util.interceptor.ProgressInterceptor
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun nestedScrollConnection(
    requestMoveToPrevChapter: () -> Unit,
    requestMoveToNextChapter: () -> Unit,
    isPrepareToPrev: (offset: Offset) -> Boolean,
    isPrepareToNext: (offset: Offset) -> Boolean
) = object : NestedScrollConnection {
    var prepareToNext = false
    var prepareToPrev = false

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (source == NestedScrollSource.Drag) {
            prepareToPrev = isPrepareToPrev(available)
            prepareToNext = isPrepareToNext(available)
        }
        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (prepareToNext) {
            prepareToNext = false
            requestMoveToNextChapter()
        } else if (prepareToPrev) {
            prepareToPrev = false
            requestMoveToPrevChapter()
        }
        return Velocity.Zero
    }
}

@Composable
fun PageState(
    modifier: Modifier,
    state: AsyncImagePainter.State,
    page: ReaderPage.Image,
    onRetry: () -> Unit
) {
    when (state) {
        is AsyncImagePainter.State.Loading ->
            PageLoadingState(
                modifier = modifier,
                position = page.index + 1,
                url = page.url
            )
        is AsyncImagePainter.State.Error ->
            PageErrorState(
                modifier = modifier,
                position = page.index + 1,
                throwable = state.result.throwable,
                onRetry = onRetry
            )
        else -> Unit
    }
}

@Composable
private fun PageLoadingState(modifier: Modifier, position: Int, url: String) {
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
private fun PageErrorState(
    modifier: Modifier,
    position: Int,
    throwable: Throwable,
    onRetry: () -> Unit
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