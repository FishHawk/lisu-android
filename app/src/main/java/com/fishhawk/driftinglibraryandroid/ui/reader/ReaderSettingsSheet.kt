package com.fishhawk.driftinglibraryandroid.ui.reader

import android.os.Bundle
import com.fishhawk.driftinglibraryandroid.databinding.ReaderSettingsSheetBinding
import com.fishhawk.driftinglibraryandroid.setting.PreferenceBooleanLiveData
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial


class ReaderSettingsSheet(activity: ReaderActivity) : BottomSheetDialog(activity) {
    private val binding = ReaderSettingsSheetBinding.inflate(activity.layoutInflater, null, false)

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGeneralPreferences()
    }

    private fun initGeneralPreferences() {
        binding.direction.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            SettingsHelper.readingDirection.setValue(position)
        }
        binding.direction.setSelection(
            SettingsHelper.readingDirection.getOrdinal(), false
        )

        bindingBoolPreference(binding.keepScreenOn, SettingsHelper.keepScreenOn)
        bindingBoolPreference(binding.useVolumeKey, SettingsHelper.useVolumeKey)
        bindingBoolPreference(binding.longTapDialog, SettingsHelper.longTapDialog)
    }

    private fun bindingBoolPreference(
        switch: SwitchMaterial,
        preference: PreferenceBooleanLiveData
    ) {
        switch.isChecked = preference.getValueDirectly()
        switch.setOnCheckedChangeListener { _, isChecked ->
            preference.setValue(isChecked)
        }
    }
}