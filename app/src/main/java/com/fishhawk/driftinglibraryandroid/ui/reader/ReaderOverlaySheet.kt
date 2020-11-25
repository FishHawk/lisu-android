package com.fishhawk.driftinglibraryandroid.ui.reader

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.databinding.ReaderOverlaySheetBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.widget.PreferenceBottomSheetDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class ReaderOverlaySheet(fragment: ReaderFragment) :
    PreferenceBottomSheetDialog(fragment.requireContext()) {

    private val binding = ReaderOverlaySheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        bindPreference(GlobalPreference.colorFilterIsEnabled, binding.colorFilterSwitch)
        GlobalPreference.colorFilterIsEnabled.asFlow()
            .onEach {
                binding.hueSeekBar.isEnabled = it
                binding.opacitySeekBar.isEnabled = it
                binding.colorFilterMode.isEnabled = it
            }
            .launchIn(fragment.viewLifecycleOwner.lifecycleScope)

        bindPreference(GlobalPreference.colorFilterHue, binding.hueSeekBar)
        bindPreference(GlobalPreference.colorFilterOpacity, binding.opacitySeekBar)
        bindPreference(GlobalPreference.colorFilterMode, binding.colorFilterMode)

        setContentView(binding.root)
    }
}