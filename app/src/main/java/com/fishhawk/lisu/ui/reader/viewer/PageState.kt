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
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import com.fishhawk.lisu.ui.reader.ReaderViewModel
import com.fishhawk.lisu.util.interceptor.ProgressInterceptor
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun nestedScrollConnection(
    viewModel: ReaderViewModel,
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
            viewModel.moveToNextChapter()
        } else if (prepareToPrev) {
            prepareToPrev = false
            viewModel.moveToPrevChapter()
        }
        return Velocity.Zero
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PageState(
    modifier: Modifier,
    state: ImagePainter.State,
    position: Int,
    url: String
) {
    when (state) {
        is ImagePainter.State.Loading ->
            PageLoadingState(
                modifier = modifier,
                position = position,
                url = url
            )
        is ImagePainter.State.Error ->
            PageErrorState(
                modifier = modifier,
                position = position,
                throwable = state.throwable
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
private fun PageErrorState(modifier: Modifier, position: Int, throwable: Throwable) {
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
            TextButton(onClick = { }) { Text("retry") }
        }
    }
}
