package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState


@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerReader(
    state: PagerState,
    pages: List<String>,
    onTap: ((Offset) -> Unit)? = null
) {
    HorizontalPager(state = state) { index ->
        Page(position = index.plus(1), url = pages[index], onTap = onTap)
    }
}

@Composable
fun Page(
    position: Int,
    url: String,
    onTap: ((Offset) -> Unit)? = null
) {
    val painter = rememberImagePainter(url)
    Box(
        modifier = Modifier
            .fillMaxSize()
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
        when (painter.state) {
            is ImagePainter.State.Loading -> {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = position.toString())
                    CircularProgressIndicator()
                }
            }
            is ImagePainter.State.Error -> {

            }
            else -> Unit
        }
    }
}
