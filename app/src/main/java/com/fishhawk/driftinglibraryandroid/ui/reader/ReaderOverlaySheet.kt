package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.ReaderOverlaySheetBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.widget.PreferenceBottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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