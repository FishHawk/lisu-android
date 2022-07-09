package com.fishhawk.lisu.ui.reader.viewer

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.ReaderPage
import kotlinx.coroutines.launch

@Composable
internal fun WebtoonViewer(
    modifier: Modifier = Modifier,
    isMenuOpened: MutableState<Boolean>,
    state: ViewerState.Webtoon,
    onLongPress: ((drawable: Drawable, position: Int) -> Unit),
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val nestedScrollConnection = remember {
        nestedScrollConnection(
            requestMoveToPrevChapter = state.requestMoveToPrevChapter,
            requestMoveToNextChapter = state.requestMoveToNextChapter,
            isPrepareToPrev = { it.y > 10 },
            isPrepareToNext = { it.y < -10 },
        )
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .nestedScroll(nestedScrollConnection)
            .zoomable(
                onLongPress = {
//                        (painter.state as? AsyncImagePainter.State.Success)
//                            ?.let { onLongPress(it.result.drawable, page.index + 1) }
                },
                onTap = {
                    isMenuOpened.value = !isMenuOpened.value
                },
            )
    ) {
        val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()
        val itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state.state,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            items(state.pages) { page ->
                when (page) {
                    is ReaderPage.Image -> ImagePage(page = page)
                    ReaderPage.Empty ->
                        EmptyPage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                    is ReaderPage.NextChapterState ->
                        NextChapterStatePage(
                            page = page,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                        )
                    is ReaderPage.PrevChapterState ->
                        PrevChapterStatePage(
                            page = page,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                        )
                }
            }
        }
    }

}

@Composable
private fun ImagePage(page: ReaderPage.Image) {
    var retryHash by remember { mutableStateOf(0) }
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(page.url)
            .size(Size.ORIGINAL)
            .setParameter("retry_hash", retryHash, memoryCacheKey = null)
            .build()
    )

    Box(modifier = Modifier.wrapContentHeight()) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.FillWidth
        )
        PageState(
            modifier = Modifier
                .height(240.dp)
                .align(Alignment.Center),
            state = painter.state,
            page = page,
            onRetry = { retryHash++ }
        )
    }
}


private const val maxScale = 3.0f
private const val midScale = 1.5f
private const val minScale = 1.0f


private fun Modifier.zoomable(
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
): Modifier = composed {
    var dstSize = androidx.compose.ui.geometry.Size.Unspecified

    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var translation by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(minScale * 0.5f, maxScale)
        translation += panChange.times(scale)
    }


    LaunchedEffect(transformableState.isTransformInProgress) {
        if (!transformableState.isTransformInProgress) {
            if (scale < 1f) {
                val originScale = scale
                val originTranslation = translation
                AnimationState(initialValue = 0f).animateTo(
                    1f,
                    SpringSpec(stiffness = Spring.StiffnessLow)
                ) {
                    scale = originScale + (1 - originScale) * this.value
                    translation = originTranslation * (1 - this.value)
                }
            } else {
                if (dstSize == androidx.compose.ui.geometry.Size.Unspecified) return@LaunchedEffect
                val maxX = dstSize.width * (scale - 1) / 2f
                val maxY = dstSize.height * (scale - 1) / 2f
                val target = Offset(
                    translation.x.coerceIn(-maxX, maxX),
                    translation.y.coerceIn(-maxY, maxY)
                )
                AnimationState(
                    typeConverter = Offset.VectorConverter,
                    initialValue = translation
                ).animateTo(target, SpringSpec(stiffness = Spring.StiffnessLow)) {
                    translation = this.value
                }
            }
        }
    }

    this
        .clipToBounds()
        .onPlaced { dstSize = it.size.toSize() }
        .transformable(state = transformableState)
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = onLongPress,
                onDoubleTap = {
                    val targetScale = when {
                        scale >= maxScale - 1e-4f -> minScale
                        scale >= midScale - 1e-4f -> maxScale
                        scale >= minScale - 1e-4f -> midScale
                        else -> minScale
                    }
                    scope.launch {
                        transformableState.animateZoomBy(
                            targetScale / scale
                        )
                    }
                },
                onTap = {
                    if (onTap != null) {
                        onTap(Offset(it.x / size.width, it.y / size.height))
                    }
                }
            )
        }
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = translation.x,
            translationY = translation.y
        )
}