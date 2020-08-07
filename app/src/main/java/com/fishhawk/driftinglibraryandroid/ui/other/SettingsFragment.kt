package com.fishhawk.driftinglibraryandroid.ui.other

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import com.fishhawk.driftinglibraryandroid.R


class SettingGeneralFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_general, rootKey)
    }
}

class SettingReaderFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_reader, rootKey)
    }
}

class SettingAdvancedFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<Preference>("clear_image_cache")!!.apply {
            setOnPreferenceClickListener {
                Glide.get(context).clearMemory()
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_advanced, rootKey)
    }
}
