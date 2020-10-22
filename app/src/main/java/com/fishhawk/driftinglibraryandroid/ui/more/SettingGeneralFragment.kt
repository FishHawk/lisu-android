package com.fishhawk.driftinglibraryandroid.ui.more

import android.os.Bundle
import androidx.preference.ListPreference
import com.fishhawk.driftinglibraryandroid.R

class SettingGeneralFragment : BaseSettingFragment() {
    override val titleResId: Int = R.string.label_settings_general
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<ListPreference>("theme")!!.apply {
            setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_general, rootKey)
    }
}