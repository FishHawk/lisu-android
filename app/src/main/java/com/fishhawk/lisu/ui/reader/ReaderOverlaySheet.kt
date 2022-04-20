package com.fishhawk.lisu.ui.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.ColorFilterMode
import com.fishhawk.lisu.data.datastore.Preference
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.getBlocking
import com.fishhawk.lisu.ui.more.ListPreference
import com.fishhawk.lisu.ui.more.SwitchPreference
import com.fishhawk.lisu.ui.widget.BottomSheet
import kotlinx.coroutines.launch

@Composable
fun ReaderColorFilterOverlay() {
    val isEnabled by PR.enabledColorFilter.collectAsState()

    if (isEnabled) {
        val mode by PR.colorFilterMode.collectAsState()
        val blendMode = when (mode) {
            ColorFilterMode.Default -> BlendMode.SrcOver
            ColorFilterMode.Multiply -> BlendMode.Multiply
            ColorFilterMode.Screen -> BlendMode.Screen
            ColorFilterMode.Overlay -> BlendMode.Overlay
            ColorFilterMode.Lighten -> BlendMode.Lighten
            ColorFilterMode.Darken -> BlendMode.Darken
        }

        val h by PR.colorFilterH.collectAsState()
        val s by PR.colorFilterS.collectAsState()
        val l by PR.colorFilterL.collectAsState()
        val a by PR.colorFilterA.collectAsState()
        val color = Color.hsl(
            h.coerceIn(0f, 1f) * 360,
            s.coerceIn(0f, 1f),
            l.coerceIn(0f, 1f),
            a.coerceIn(0f, 1f)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = color, size = size, blendMode = blendMode)
        }
    }
}

object ReaderOverlaySheet : BottomSheet() {
    override val scrimColor: Color
        @Composable
        get() = Color.Transparent

    @Composable
    override fun Content() {
        ReaderOverlaySheetContent()
        BackHandler()
    }
}

@Composable
private fun ReaderOverlaySheetContent() {
    Column(modifier = Modifier.padding(8.dp)) {
        val colorFilterEnabled by PR.enabledColorFilter.collectAsState()
        SwitchPreference(title = "Custom color filter", preference = PR.enabledColorFilter)
        SliderPreference(colorFilterEnabled, label = "H", preference = PR.colorFilterH)
        SliderPreference(colorFilterEnabled, label = "S", preference = PR.colorFilterS)
        SliderPreference(colorFilterEnabled, label = "L", preference = PR.colorFilterL)
        SliderPreference(colorFilterEnabled, label = "A", preference = PR.colorFilterA)
        ListPreference(title = "Blend mode", preference = PR.colorFilterMode) {
            when (it) {
                ColorFilterMode.Default -> R.string.settings_filter_mode_default
                ColorFilterMode.Multiply -> R.string.settings_filter_mode_multiply
                ColorFilterMode.Screen -> R.string.settings_filter_mode_screen
                ColorFilterMode.Overlay -> R.string.settings_filter_mode_overlay
                ColorFilterMode.Lighten -> R.string.settings_filter_mode_lighten
                ColorFilterMode.Darken -> R.string.settings_filter_mode_darken
            }
        }

        val enableCustomBrightness by PR.enableCustomBrightness.collectAsState()
        SwitchPreference(
            title = "Custom color filter",
            preference = PR.enableCustomBrightness
        )
        SliderPreference(
            enableCustomBrightness,
            icon = Icons.Filled.BrightnessHigh,
            preference = PR.customBrightness
        )
    }
}

@Composable
private fun SliderPreference(
    enabled: Boolean,
    label: String,
    preference: Preference<Float>
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val scope = rememberCoroutineScope()
        var p by remember { mutableStateOf(preference.getBlocking()) }
        Text(text = label)
        Slider(
            modifier = Modifier.height(36.dp),
            enabled = enabled,
            value = p,
            onValueChange = {
                p = it
                scope.launch { preference.set(it) }
            })
    }
}

@Composable
private fun SliderPreference(
    enabled: Boolean,
    icon: ImageVector,
    preference: Preference<Float>
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val scope = rememberCoroutineScope()
        var p by remember { mutableStateOf(preference.getBlocking()) }
        Icon(icon, contentDescription = null)
        Slider(
            modifier = Modifier.height(36.dp),
            enabled = enabled,
            value = p,
            onValueChange = {
                p = it
                scope.launch { preference.set(it) }
            })
    }
}