package com.fishhawk.lisu.ui.reader.viewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.*
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.ReaderPage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun PagerViewer(
    modifier: Modifier = Modifier,
    isMenuOpened: MutableState<Boolean>,
    state: ViewerState.Pager,
    onLongPress: (page: ReaderPage.Image) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val nestedScrollConnection = remember(state.isRtl) {
        nestedScrollConnection(
            requestMoveToPrevChapter = state.requestMoveToPrevChapter,
            requestMoveToNextChapter = state.requestMoveToNextChapter,
            isPrepareToPrev = { if (state.isRtl) it.x < -10 else it.x > 10 },
            isPrepareToNext = { if (state.isRtl) it.x > 10 else it.x < -10 },
        )
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .nestedScroll(nestedScrollConnection),
    ) {
        fun onTap(offset: Offset, size: IntSize) {
            if (isMenuOpened.value) isMenuOpened.value = false
            else {
                val x = offset.x / size.width
                when {
                    x < 0.25 -> scope.launch { state.toLeft() }
                    x > 0.75 -> scope.launch { state.toRight() }
                    else -> isMenuOpened.value = true
                }
            }
        }

        val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()

        val layoutDirection = if (state.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            HorizontalPager(
                count = state.pages.size,
                modifier = Modifier.fillMaxSize(),
                state = state.state,
                key = {
                    when (val page = state.pages[it]) {
                        ReaderPage.Empty -> 0
                        is ReaderPage.Image -> page.index
                        is ReaderPage.NextChapterState -> -1
                        is ReaderPage.PrevChapterState -> state.pages.size
                    }
                },
                itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp
            ) { index ->
                // hacky, see https://github.com/google/accompanist/issues/1249
                Text("placeholder placeholder placeholder", color = Color.Transparent)

                val pageModifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onTap(it, size) })
                    }
                when (val page = state.pages[index]) {
                    is ReaderPage.Image -> {
                        ImagePage(
                            page = page,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .zoomable(
                                    onTap = { onTap(it, size) },
                                    onLongPress = { onLongPress(page) },
                                ),
                            stateModifier = pageModifier,
                        )
                    }
                    ReaderPage.Empty ->
                        EmptyPage(modifier = pageModifier)
                    is ReaderPage.NextChapterState ->
                        NextChapterStatePage(page = page, modifier = pageModifier)
                    is ReaderPage.PrevChapterState ->
                        PrevChapterStatePage(page = page, modifier = pageModifier)
                }
            }
        }
    }
}

/* Zoom logic */
private const val maxScale = 3.0f
private const val midScale = 1.5f
private const val minScale = 1.0f

private fun Modifier.zoomable(
    onLongPress: (PointerInputScope.(Offset) -> Unit) = {},
    onTap: (PointerInputScope.(Offset) -> Unit) = {},
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val translation = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    this
        .clipToBounds()
        .pointerInput(Unit) {
            val decay = splineBasedDecay<Offset>(this)
            customDetectTransformGestures(
                onGesture = { centroid, pan, zoom ->
                    val targetScale = (scale.value * zoom).coerceIn(minScale, maxScale)

                    val realZoom = targetScale / scale.value
                    val center = size.toSize().center
                    val targetTranslation =
                        translation.value * realZoom - (centroid - center) * (realZoom - 1) + pan

                    val bound = center * (targetScale - 1f)
                    translation.updateBounds(-bound, bound)

                    runBlocking {
                        scale.snapTo(targetScale)
                        translation.snapTo(targetTranslation)
                    }

                    targetTranslation.x > -bound.x && targetTranslation.x < bound.x
                },
                onFling = { velocity ->
                    scope.launch {
                        translation.animateDecay(Offset(velocity.x, velocity.y), decay)
                    }
                },
            )
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { centroid ->
                    val targetScale = when {
                        scale.value >= maxScale - 1e-4f -> minScale
                        scale.value >= midScale - 1e-4f -> maxScale
                        scale.value >= minScale - 1e-4f -> midScale
                        else -> minScale
                    }

                    val realZoom = targetScale / scale.value
                    val center = size.toSize().center
                    val targetTranslation =
                        translation.value * realZoom - (centroid - center) * (realZoom - 1)

                    val bound = center * (targetScale - 1f)
                    translation.updateBounds(-bound, bound)

                    scope.launch {
                        scale.animateTo(targetScale)
                    }
                    scope.launch {
                        translation.animateTo(targetTranslation)
                    }
                },
                onLongPress = { onLongPress(it) },
                onTap = { onTap(it) },
            )
        }
        .graphicsLayer(
            scaleX = scale.value,
            scaleY = scale.value,
            translationX = translation.value.x,
            translationY = translation.value.y,
        )
}

private suspend fun PointerInputScope.customDetectTransformGestures(
    onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Boolean,
    onFling: (velocity: Velocity) -> Unit = {},
) {

    forEachGesture {
        awaitPointerEventScope {
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            var isFirstOnGesture = true

            val velocityTracker = VelocityTracker()
            var shouldStartFling = true

            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.any { it.isConsumed }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val panChange = event.calculatePan()

                    if (!pastTouchSlop) {
                        zoom *= zoomChange
                        pan += panChange

                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        val zoomMotion = abs(1 - zoom) * centroidSize
                        val panMotion = pan.getDistance()

                        if (zoomMotion > touchSlop ||
                            panMotion > touchSlop
                        ) {
                            pastTouchSlop = true
                        }
                    }

                    if (pastTouchSlop) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        if (event.changes.size >= 2) {
                            velocityTracker.resetTracking()
                        } else if (centroid.isSpecified) {
                            val change = event.changes.firstOrNull()
                            if (change?.pressed == true) {
                                velocityTracker.addPosition(
                                    change.uptimeMillis,
                                    centroid,
                                )
                            }
                        }
                        if (
                            zoomChange != 1f ||
                            panChange != Offset.Zero
                        ) {
                            val inBound = onGesture(centroid, panChange, zoomChange)
                            if (isFirstOnGesture && !inBound && zoomChange == 1f) {
                                shouldStartFling = false
                                break
                            }
                            isFirstOnGesture = false
                        }
                        event.changes.forEach {
                            if (it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }
            } while (!canceled && event.changes.any { it.pressed })

            if (shouldStartFling) {
                val velocity = velocityTracker.calculateVelocity()
                onFling(velocity)
            }
        }
    }
}
