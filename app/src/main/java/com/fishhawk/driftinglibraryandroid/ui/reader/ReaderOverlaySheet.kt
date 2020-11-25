package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import android.widget.SeekBar
import com.fishhawk.driftinglibraryandroid.databinding.ReaderColorFilterSheetBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.widget.IgnoreFirstSpinnerListener
import com.fishhawk.driftinglibraryandroid.widget.PreferenceBottomSheetDialog
import com.fishhawk.driftinglibraryandroid.widget.SimpleSeekBarListener
import com.google.android.material.bottomsheet.BottomSheetDialog


class ReaderOverlaySheet(context: Context) : PreferenceBottomSheetDialog(context) {

    private val binding = ReaderColorFilterSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        bindingBoolPreference(binding.colorFilterSwitch, GlobalPreference.colorFilterEnabled) {
            toggleColorFilter(it)
        }
        bindingIntPreference(binding.hueSeekBar, GlobalPreference.colorFilterHue)
        bindingIntPreference(binding.opacitySeekBar, GlobalPreference.colorFilterOpacity)
        bindingEnumPreference(binding.colorFilterMode, GlobalPreference.colorFilterMode)

        setContentView(binding.root)
    }

    private fun toggleColorFilter(isEnabled: Boolean) {
        binding.hueSeekBar.isEnabled = isEnabled
        binding.opacitySeekBar.isEnabled = isEnabled
        binding.colorFilterMode.isEnabled = isEnabled
    }
}