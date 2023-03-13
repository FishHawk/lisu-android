package com.fishhawk.lisu.widget

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material3.SliderDefaults.Thumb
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

// see https://issuetracker.google.com/issues/254417424
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LisuSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
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
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        thumb = {
            Box(modifier = Modifier.size(20.dp, 20.dp)) {
                Thumb(
                    interactionSource = interactionSource,
                    modifier = Modifier.align(Alignment.Center),
                    colors = colors,
                    enabled = enabled,
                    thumbSize = DpSize(16.dp, 16.dp),
                )
            }
        },
    )
}