package com.fishhawk.driftinglibraryandroid.setting

import android.os.Bundle
import android.webkit.URLUtil
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val libraryAddressPreference: EditTextPreference = findPreference("library_address")!!

        libraryAddressPreference.summary = libraryAddressPreference.text
        libraryAddressPreference.setOnPreferenceChangeListener { preference, newValue ->
            val textPreference = preference as EditTextPreference
            textPreference.summary = newValue as String
            true
        }

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }
}