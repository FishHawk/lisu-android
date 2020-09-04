package com.fishhawk.driftinglibraryandroid.ui.reader

import android.os.Bundle
import androidx.appcompat.widget.AppCompatSpinner
import com.fishhawk.driftinglibraryandroid.databinding.ReaderSettingsSheetBinding
import com.fishhawk.driftinglibraryandroid.setting.PreferenceBooleanLiveData
import com.fishhawk.driftinglibraryandroid.setting.PreferenceEnumLiveData
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
        bindingEnumPreference(binding.direction, SettingsHelper.readingDirection)
        bindingEnumPreference(binding.orientation, SettingsHelper.screenOrientation)

        bindingBoolPreference(binding.keepScreenOn, SettingsHelper.keepScreenOn)
        bindingBoolPreference(binding.useVolumeKey, SettingsHelper.useVolumeKey)
        bindingBoolPreference(binding.longTapDialog, SettingsHelper.longTapDialog)
    }

    private fun <T : Enum<T>> bindingEnumPreference(
        spinner: AppCompatSpinner,
        preference: PreferenceEnumLiveData<T>
    ) {
        spinner.onItemSelectedListener = IgnoreFirstSpinnerListener { preference.setValue(it) }
        spinner.setSelection(preference.getOrdinal(), false)
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