package com.fishhawk.driftinglibraryandroid.widget

import android.content.Context
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSpinner
import com.fishhawk.driftinglibraryandroid.preference.PreferenceBooleanLiveData
import com.fishhawk.driftinglibraryandroid.preference.PreferenceEnumLiveData
import com.fishhawk.driftinglibraryandroid.preference.PreferenceIntLiveData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial

open class PreferenceBottomSheetDialog(context: Context) : BottomSheetDialog(context) {

    protected fun <T : Enum<T>> bindingEnumPreference(
        spinner: AppCompatSpinner,
        preference: PreferenceEnumLiveData<T>
    ) {
        spinner.setSelection(preference.getOrdinal(), false)
        spinner.onItemSelectedListener = IgnoreFirstSpinnerListener {
            preference.setValue(it)
        }
    }

    protected fun bindingIntPreference(
        seekBar: SeekBar,
        preference: PreferenceIntLiveData
    ) {
        seekBar.progress = preference.getValueDirectly()
        seekBar.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                if (fromUser) preference.setValue(value)
            }
        })
    }

    protected fun bindingBoolPreference(
        switch: SwitchMaterial,
        preference: PreferenceBooleanLiveData,
        callback: ((Boolean) -> Unit)? = null
    ) {
        switch.isChecked = preference.getValueDirectly()
        callback?.invoke(switch.isChecked)

        switch.setOnCheckedChangeListener { _, isChecked ->
            preference.setValue(isChecked)
            callback?.invoke(isChecked)
        }
    }
}