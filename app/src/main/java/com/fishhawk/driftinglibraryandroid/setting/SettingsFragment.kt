package com.fishhawk.driftinglibraryandroid.setting

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val libraryAddressPreference: EditTextPreference = findPreference("library_address")!!
        libraryAddressPreference.summary = libraryAddressPreference.text
        libraryAddressPreference.setOnPreferenceChangeListener { preference, newValue ->
            val address = newValue as String
            if (Repository.setUrl(address)) {
                val textPreference = preference as EditTextPreference
                textPreference.summary = newValue
                true
            } else {
                view?.let {
                    Snackbar.make(
                        it,
                        getString(R.string.settings_library_address_error_hint),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                false
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }
}