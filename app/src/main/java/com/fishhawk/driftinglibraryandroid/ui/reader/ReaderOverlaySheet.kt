package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import com.fishhawk.driftinglibraryandroid.data.datastore.ColorFilterMode
import com.fishhawk.driftinglibraryandroid.data.datastore.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.Preference
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.databinding.ReaderOverlaySheetBinding
import com.fishhawk.driftinglibraryandroid.widget.SimpleSeekBarListener
import com.fishhawk.driftinglibraryandroid.widget.SimpleSpinnerListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalGraphicsApi::class)
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

class ReaderOverlaySheet(
    context: Context,
    private val scope: CoroutineScope
) : BottomSheetDialog(context) {

    private val binding = ReaderOverlaySheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        bindPreference(PR.enabledColorFilter, binding.colorFilterSwitch)
        bindPreference(PR.colorFilterH, binding.colorFilterH)
        bindPreference(PR.colorFilterS, binding.colorFilterS)
        bindPreference(PR.colorFilterL, binding.colorFilterL)
        bindPreference(PR.colorFilterA, binding.colorFilterA)
        bindPreference(PR.colorFilterMode, binding.colorFilterMode)

        bindPreference(PR.enableCustomBrightness, binding.customBrightnessSwitch)
        bindPreference(PR.customBrightness, binding.customBrightnessValue)

        PR.enabledColorFilter.flow
            .onEach {
                binding.colorFilterH.isEnabled = it
                binding.colorFilterS.isEnabled = it
                binding.colorFilterL.isEnabled = it
                binding.colorFilterA.isEnabled = it
                binding.colorFilterMode.isEnabled = it
            }.launchIn(scope)

        PR.enableCustomBrightness.flow
            .onEach { binding.customBrightnessValue.isEnabled = it }
            .launchIn(scope)

        setContentView(binding.root)
    }


    private fun bindPreference(
        preference: Preference<Int>,
        seekBar: SeekBar
    ) {
        preference.flow.onEach { seekBar.progress = it }.launchIn(scope)
        seekBar.setOnSeekBarChangeListener(SimpleSeekBarListener { scope.launch { preference.set(it) } })
    }

    private fun bindPreference(
        preference: Preference<Boolean>,
        switch: SwitchMaterial
    ) {
        preference.flow.onEach { switch.isChecked = it }.launchIn(scope)
        switch.setOnCheckedChangeListener { _, it -> scope.launch { preference.set(it) } }
    }

    private inline fun <reified T : Enum<T>> bindPreference(
        preference: Preference<T>,
        spinner: AppCompatSpinner
    ) {
        preference.flow.onEach { spinner.setSelection(it.ordinal, false) }.launchIn(scope)
        spinner.onItemSelectedListener = SimpleSpinnerListener {
            scope.launch { preference.set(enumValues<T>()[it]) }
        }
    }
}