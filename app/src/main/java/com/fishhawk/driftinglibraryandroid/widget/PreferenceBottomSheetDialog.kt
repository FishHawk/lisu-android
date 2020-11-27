package com.fishhawk.driftinglibraryandroid.widget

import android.content.Context
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSpinner
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.tfcporciuncula.flow.Preference

open class PreferenceBottomSheetDialog(context: Context) : BottomSheetDialog(context) {

    protected fun bindPreference(
        preference: Preference<Int>,
        seekBar: SeekBar
    ) {
        seekBar.progress = preference.get()
        seekBar.setOnSeekBarChangeListener(SimpleSeekBarListener { preference.set(it) })
    }

    protected fun bindPreference(
        preference: Preference<Boolean>,
        switch: SwitchMaterial
    ) {
        switch.isChecked = preference.get()
        switch.setOnCheckedChangeListener { _, isChecked -> preference.set(isChecked) }
    }

    protected inline fun <reified T : Enum<T>> bindPreference(
        preference: Preference<T>,
        spinner: AppCompatSpinner
    ) {
        spinner.setSelection(preference.get().ordinal, false)
        spinner.onItemSelectedListener =
            SimpleSpinnerListener { preference.set(enumValues<T>()[it]) }
    }
}