package com.fishhawk.driftinglibraryandroid.setting

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
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
                view?.let { Snackbar.make(it, "格式错误", Snackbar.LENGTH_LONG).show() }
                false
            }
        }

        val readingDirectionPreference: ListPreference = findPreference("reading_direction")!!
        readingDirectionPreference.setOnPreferenceChangeListener { _, newValue ->
            println(newValue)
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }
}