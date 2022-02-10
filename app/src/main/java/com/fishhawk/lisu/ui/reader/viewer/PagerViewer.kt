package com.fishhawk.lisu.ui.reader.viewer

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size as CoilSize
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.ScaleType
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.ReaderViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun PagerViewer(
    state: ViewerState.Pager,
    pointer: ReaderViewModel.ReaderChapterPointer,
    isRtl: Boolean,
    onLongPress: ((drawable: Drawable, position: Int) -> Unit)
) {
    val viewModel = viewModel<ReaderViewModel>()
    val scope = rememberCoroutineScope()

    fun toNext() {
        if (state.position < state.size - 1)
            scope.launch { state.scrollToPage(state.position + 1) }
        else viewModel.moveToNextChapter()
    }

    fun toPrev() {
        if (state.position > 0)
            scope.launch { state.scrollToPage(state.position - 1) }
        else viewModel.moveToPrevChapter()
    }

    fun toLeft() = if (isRtl) toNext() else toPrev()
    fun toRight() = if (isRtl) toPrev() else toNext()

    val useVolumeKey by PR.useVolumeKey.collectAsState()
    val invertVolumeKey by PR.invertVolumeKey.collectAsState()

    val focusRequester = remember { FocusRequester() }

    val nestedScrollConnection = remember(isRtl) {
        nestedScrollConnection(
            viewModel,
            { if (isRtl) it.x < -10 else it.x > 10 },
            { if (isRtl) it.x > 10 else it.x < -10 }
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(nestedScrollConnection)
        .focusRequester(focusRequester)
        .focusable()
        .onPreviewKeyEvent {
            when (it.type) {
                KeyEventType.KeyDown -> {
                    when (it.key) {
                        Key.VolumeUp ->
                            if (useVolumeKey && !viewModel.isMenuOpened.value) {
                                if (invertVolumeKey) toNext() else toPrev()
                            } else return@onPreviewKeyEvent false
                        Key.VolumeDown ->
                            if (useVolumeKey && !viewModel.isMenuOpened.value) {
                                if (invertVolumeKey) toPrev() else toNext()
                            } else return@onPreviewKeyEvent false
                        else -> return@onPreviewKeyEvent false
                    }
                }
                KeyEventType.KeyUp -> {
                    when (it.key) {
                        Key.Menu -> viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value

                        Key.N -> viewModel.moveToNextChapter()
                        Key.P -> viewModel.moveToPrevChapter()

                        Key.DirectionUp, Key.PageUp -> toPrev()
                        Key.DirectionDown, Key.PageDown -> toNext()

                        Key.DirectionLeft -> toLeft()
                        Key.DirectionRight -> toRight()
                        else -> return@onPreviewKeyEvent false
                    }
                }
                else -> return@onPreviewKeyEvent false
            }
            true
        }
    ) {
        val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()
        val scaleType by PR.scaleType.collectAsState()

        val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            HorizontalPager(
                count = pointer.currChapter.images.size,
                modifier = Modifier.fillMaxSize(),
                state = state.state,
                itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp
            ) { index ->
                Page(
                    position = index.plus(1),
                    url = pointer.currChapter.images[index],
                    contentScale = when (scaleType) {
                        ScaleType.FitScreen -> ContentScale.Fit
                        ScaleType.FitWidth -> ContentScale.FillWidth
                        ScaleType.FitHeight -> ContentScale.FillHeight
                        ScaleType.OriginalSize -> ContentScale.None
                    },
                    onTap = { offset ->
                        if (viewModel.isMenuOpened.value) viewModel.isMenuOpened.value = false
                        else when {
                            offset.x < 0.25 -> toLeft()
                            offset.x > 0.75 -> toRight()
                            else -> viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value
                        }
                    },
                    onLongPress = onLongPress
                )
            }
        }
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

@Composable
private fun Page(
    position: Int,
    url: String,
    contentScale: ContentScale,
    onTap: ((Offset) -> Unit),
    onLongPress: ((drawable: Drawable, position: Int) -> Unit)
) {
    var retryHash by remember { mutableStateOf(0) }
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(url)
            .size(CoilSize.ORIGINAL)
            .setParameter("retry_hash", retryHash, memoryCacheKey = null)
            .build()
    )

    Box {
        ZoomableImage(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            onLongPress = {
                (painter.state as? AsyncImagePainter.State.Success)
                    ?.let { onLongPress(it.result.drawable, position) }
            },
            onTap = onTap
        )

        PageState(
            modifier = Modifier.fillMaxSize(),
            state = painter.state,
            position = position,
            url = url,
            onRetry = { retryHash++ }
        )
    }
}

private const val maxScale = 3.0f
private const val midScale = 1.5f
private const val minScale = 1.0f

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun ZoomableImage(
    painter: AsyncImagePainter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null
) {
    val srcSize = painter.intrinsicSize
    var dstSize = Size.Unspecified

    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var translation by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(minScale * 0.5f, maxScale)
        translation += panChange.times(scale)
    }

    Box(
        modifier = modifier
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
//            .pointerInput(Unit) {
//                forEachGesture {
//                    awaitPointerEventScope {
//                        val down = awaitFirstDown(requireUnconsumed = false)
//                        println("test")
//                        drag(down.id) {
//                            val rect = (size.toSize() * (scale - 1))
//                                .toRect()
//                                .run { translate(center) }
//                            val targetTranslation = (it.positionChange() + translation)
//                            if (rect.contains(targetTranslation)) {
//                                translation = targetTranslation
//                                it.consumePositionChange()
//                            }
//                        }
//                    }
//                }
//            }
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = translation.x,
                    translationY = translation.y
                ),
            contentScale = ContentScale.Fit
        )
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
                if (dstSize == Size.Unspecified) return@LaunchedEffect
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
}
