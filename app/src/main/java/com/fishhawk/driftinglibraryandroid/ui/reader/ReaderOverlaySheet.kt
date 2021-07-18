package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.databinding.ReaderOverlaySheetBinding
import com.fishhawk.driftinglibraryandroid.widget.PreferenceBottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalGraphicsApi::class)
@Composable
fun ReaderColorFilterOverlay() {
    val isEnabled by P.colorFilter.let { it.asFlow().collectAsState(it.get()) }

    if (isEnabled) {
        val mode by P.colorFilterMode.let { it.asFlow().collectAsState(it.get()) }
        val blendMode = when (mode) {
            P.ColorFilterMode.DEFAULT -> BlendMode.SrcOver
            P.ColorFilterMode.MULTIPLY -> BlendMode.Multiply
            P.ColorFilterMode.SCREEN -> BlendMode.Screen
            P.ColorFilterMode.OVERLAY -> BlendMode.Overlay
            P.ColorFilterMode.LIGHTEN -> BlendMode.Lighten
            P.ColorFilterMode.DARKEN -> BlendMode.Darken
        }

        val h by P.colorFilterH.let { it.asFlow().collectAsState(it.get()) }
        val s by P.colorFilterS.let { it.asFlow().collectAsState(it.get()) }
        val l by P.colorFilterL.let { it.asFlow().collectAsState(it.get()) }
        val a by P.colorFilterA.let { it.asFlow().collectAsState(it.get()) }
        val color = Color.hsl(
            h.coerceIn(0, 360).toFloat(),
            s.coerceIn(0, 100).toFloat() / 100f,
            l.coerceIn(0, 100).toFloat() / 100f,
            a.coerceIn(0, 255).toFloat() / 255f
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = color, size = size, blendMode = blendMode)
        }
    }
}

class ReaderOverlaySheet(context: Context, scope: CoroutineScope) :
    PreferenceBottomSheetDialog(context) {

    private val binding = ReaderOverlaySheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        bindPreference(P.colorFilter, binding.colorFilterSwitch)
        bindPreference(P.colorFilterH, binding.colorFilterH)
        bindPreference(P.colorFilterS, binding.colorFilterS)
        bindPreference(P.colorFilterL, binding.colorFilterL)
        bindPreference(P.colorFilterA, binding.colorFilterA)
        bindPreference(P.colorFilterMode, binding.colorFilterMode)

        P.colorFilter.asFlow()
            .onEach {
                binding.colorFilterH.isEnabled = it
                binding.colorFilterS.isEnabled = it
                binding.colorFilterL.isEnabled = it
                binding.colorFilterA.isEnabled = it
                binding.colorFilterMode.isEnabled = it
            }
            .launchIn(scope)


        bindPreference(P.customBrightness, binding.customBrightnessSwitch)
        bindPreference(P.customBrightnessValue, binding.customBrightnessValue)

        P.customBrightness.asFlow()
            .onEach { binding.customBrightnessValue.isEnabled = it }
            .launchIn(scope)


        setContentView(binding.root)
    }
}