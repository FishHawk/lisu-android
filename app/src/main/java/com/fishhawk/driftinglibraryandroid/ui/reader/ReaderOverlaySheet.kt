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
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.databinding.ReaderOverlaySheetBinding
import com.fishhawk.driftinglibraryandroid.widget.PreferenceBottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalGraphicsApi::class)
@Composable
fun ColorFilterOverlay() {
    val isEnabled by GlobalPreference.colorFilter.let { it.asFlow().collectAsState(it.get()) }

    if (isEnabled) {
        val mode by GlobalPreference.colorFilterMode.let { it.asFlow().collectAsState(it.get()) }
        val blendMode = when (mode) {
            GlobalPreference.ColorFilterMode.DEFAULT -> BlendMode.SrcOver
            GlobalPreference.ColorFilterMode.MULTIPLY -> BlendMode.Multiply
            GlobalPreference.ColorFilterMode.SCREEN -> BlendMode.Screen
            GlobalPreference.ColorFilterMode.OVERLAY -> BlendMode.Overlay
            GlobalPreference.ColorFilterMode.LIGHTEN -> BlendMode.Lighten
            GlobalPreference.ColorFilterMode.DARKEN -> BlendMode.Darken
        }

        val h by GlobalPreference.colorFilterH.let { it.asFlow().collectAsState(it.get()) }
        val s by GlobalPreference.colorFilterS.let { it.asFlow().collectAsState(it.get()) }
        val l by GlobalPreference.colorFilterL.let { it.asFlow().collectAsState(it.get()) }
        val a by GlobalPreference.colorFilterA.let { it.asFlow().collectAsState(it.get()) }
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
        bindPreference(GlobalPreference.colorFilter, binding.colorFilterSwitch)
        bindPreference(GlobalPreference.colorFilterH, binding.colorFilterH)
        bindPreference(GlobalPreference.colorFilterS, binding.colorFilterS)
        bindPreference(GlobalPreference.colorFilterL, binding.colorFilterL)
        bindPreference(GlobalPreference.colorFilterA, binding.colorFilterA)
        bindPreference(GlobalPreference.colorFilterMode, binding.colorFilterMode)

        GlobalPreference.colorFilter.asFlow()
            .onEach {
                binding.colorFilterH.isEnabled = it
                binding.colorFilterS.isEnabled = it
                binding.colorFilterL.isEnabled = it
                binding.colorFilterA.isEnabled = it
                binding.colorFilterMode.isEnabled = it
            }
            .launchIn(scope)


        bindPreference(GlobalPreference.customBrightness, binding.customBrightnessSwitch)
        bindPreference(GlobalPreference.customBrightnessValue, binding.customBrightnessValue)

        GlobalPreference.customBrightness.asFlow()
            .onEach { binding.customBrightnessValue.isEnabled = it }
            .launchIn(scope)


        setContentView(binding.root)
    }
}