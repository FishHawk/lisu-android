package com.fishhawk.lisu.widget.m3

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
                SliderDefaults.Thumb(
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