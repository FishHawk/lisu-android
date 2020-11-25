package com.fishhawk.driftinglibraryandroid.ui.reader

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.graphics.ColorUtils
import com.fishhawk.driftinglibraryandroid.databinding.ReaderColorFilterSheetBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.preference.PreferenceBooleanLiveData
import com.fishhawk.driftinglibraryandroid.preference.PreferenceEnumLiveData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial


class ReaderOverlaySheet(context: Context) : BottomSheetDialog(context) {

    private val binding = ReaderColorFilterSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        toggleColorFilter(GlobalPreference.colorFilterEnabled.getValueDirectly())
        binding.colorFilterSwitch.isChecked = GlobalPreference.colorFilterEnabled.getValueDirectly()
        binding.colorFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
            GlobalPreference.colorFilterEnabled.setValue(isChecked)
            toggleColorFilter(isChecked)
        }

        binding.hueSeekBar.progress = GlobalPreference.colorFilterHue.getValueDirectly()
        binding.hueSeekBar.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                if (fromUser) GlobalPreference.colorFilterHue.setValue(value)
            }
        })

        binding.opacitySeekBar.progress = GlobalPreference.colorFilterOpacity.getValueDirectly()
        binding.opacitySeekBar.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                if (fromUser) GlobalPreference.colorFilterOpacity.setValue(value)
            }
        })

        binding.colorFilterMode.onItemSelectedListener =
            IgnoreFirstSpinnerListener {
                GlobalPreference.colorFilterMode.setValue(it)
            }
        binding.colorFilterMode.setSelection(GlobalPreference.colorFilterMode.getOrdinal(), false)

        setContentView(binding.root)
    }

    private fun toggleColorFilter(isEnabled: Boolean) {
        binding.hueSeekBar.isEnabled = isEnabled
        binding.opacitySeekBar.isEnabled = isEnabled
        binding.colorFilterMode.isEnabled = isEnabled
    }
}