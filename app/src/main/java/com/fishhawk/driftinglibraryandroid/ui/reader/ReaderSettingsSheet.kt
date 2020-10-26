package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatSpinner
import com.fishhawk.driftinglibraryandroid.databinding.ReaderSettingsSheetBinding
import com.fishhawk.driftinglibraryandroid.preference.PreferenceBooleanLiveData
import com.fishhawk.driftinglibraryandroid.preference.PreferenceEnumLiveData
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial

class ReaderSettingsSheet(
    context: Context
) : BottomSheetDialog(context) {
    private val binding =
        ReaderSettingsSheetBinding.inflate(
            LayoutInflater.from(context), null, false
        )

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGeneralPreferences()
    }

    private fun initGeneralPreferences() {
        bindingEnumPreference(binding.direction, GlobalPreference.readingDirection)
        bindingEnumPreference(binding.orientation, GlobalPreference.screenOrientation)

        bindingBoolPreference(binding.keepScreenOn, GlobalPreference.keepScreenOn)
        bindingBoolPreference(binding.useVolumeKey, GlobalPreference.useVolumeKey)
        bindingBoolPreference(binding.longTapDialog, GlobalPreference.longTapDialog)
    }

    private fun <T : Enum<T>> bindingEnumPreference(
        spinner: AppCompatSpinner,
        preference: PreferenceEnumLiveData<T>
    ) {
        spinner.onItemSelectedListener =
            IgnoreFirstSpinnerListener {
                preference.setValue(it)
            }
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