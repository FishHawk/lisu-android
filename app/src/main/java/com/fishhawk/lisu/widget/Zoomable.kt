package com.fishhawk.lisu.widget

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

private const val maxScale = 3.0f
private const val midScale = 1.5f
private const val minScale = 1.0f

fun Modifier.zoomable(
    onLongPress: (PointerInputScope.(Offset) -> Unit) = {},
    onTap: (PointerInputScope.(Offset) -> Unit) = {},
): Modifier = composed {
    var dstSize = Size.Unspecified

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

    this
        .clipToBounds()
        .onPlaced { dstSize = it.size.toSize() }
        .transformable(state = transformableState)
        .pointerInput(Unit) {
            detectTapGestures(
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
                onLongPress = { onLongPress(it) },
                onTap = { onTap(it) },
            )
        }
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = translation.x,
            translationY = translation.y,
        )
}
