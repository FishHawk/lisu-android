@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package com.fishhawk.lisu.widget.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.material3.tokens.SliderTokens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LisuSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    /*@IntRange(from = 0)*/
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(
        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
        inactiveTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        thumb = remember(interactionSource, colors, enabled) {
            {
                Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    enabled = enabled,
                    thumbSize = DpSize(16.dp, 16.dp),
                )
            }
        },
        track = remember(colors, enabled) {
            { sliderPositions ->
                SliderDefaults.Track(
                    colors = colors,
                    enabled = enabled,
                    sliderPositions = sliderPositions,
                )
            }
        }
    )
}

private val ThumbSize = DpSize(20.dp, 20.dp)
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp

@Composable
private fun Thumb(
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
    enabled: Boolean = true,
    thumbSize: DpSize,
) {
    val interactions = remember { mutableStateListOf<Interaction>() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }

    val elevation = if (interactions.isNotEmpty()) {
        ThumbPressedElevation
    } else {
        ThumbDefaultElevation
    }
    val shape = CircleShape

    Box(modifier = Modifier.size(ThumbSize)) {
        Spacer(
            modifier
                .size(thumbSize)
                .align(Alignment.Center)
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(
                        bounded = false,
                        radius = SliderTokens.StateLayerSize / 2
                    )
                )
                .hoverable(interactionSource = interactionSource)
                .shadow(if (enabled) elevation else 0.dp, shape, clip = false)
                .background(colors.thumbColor(enabled).value, shape)
        )
    }
}