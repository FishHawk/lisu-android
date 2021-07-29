package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderMode
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.util.interceptor.ProgressInterceptor
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.VerticalPager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull


@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerReader(
    state: PagerState,
    pages: List<String>,
    onTap: ((Offset) -> Unit)? = null
) {
    val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()
    val itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp

    val readerDirection by PR.readerMode.collectAsState()
    val layoutDirection =
        if (readerDirection == ReaderMode.Rtl) LayoutDirection.Rtl
        else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        if (readerDirection == ReaderMode.Vertical) {
            VerticalPager(state = state, itemSpacing = itemSpacing) { index ->
                Page(position = index.plus(1), url = pages[index], onTap = onTap)
            }
        } else {
            HorizontalPager(state = state, itemSpacing = itemSpacing) { index ->
                Page(position = index.plus(1), url = pages[index], onTap = onTap)
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun Page(
    position: Int,
    url: String,
    onTap: ((Offset) -> Unit)? = null
) {
    val painter = rememberImagePainter(url)
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { /* Called when the gesture starts */ },
                    onDoubleTap = { /* Called on Double Tap */ },
                    onLongPress = { /* Called on Long Press */ },
                    onTap = onTap
                )
            }
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        when (val state = painter.state) {
            is ImagePainter.State.Loading -> {
                var progress by remember { mutableStateOf<Float?>(null) }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.h3
                    )
                    progress?.let { CircularProgressIndicator(progress = it) }
                        ?: CircularProgressIndicator()
                }
                DisposableEffect(state) {
                    val key = url.toHttpUrlOrNull().toString()
                    ProgressInterceptor.addListener(key) { progress = it }
                    onDispose {
                        ProgressInterceptor.removeListener(key)
                        progress = null
                    }
                }
            }
            is ImagePainter.State.Error -> {
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
                        text = state.throwable.message ?: "",
                        style = MaterialTheme.typography.body1
                    )
                    TextButton(onClick = { }) { Text("retry") }
                }
            }
            else -> Unit
        }
    }
}
