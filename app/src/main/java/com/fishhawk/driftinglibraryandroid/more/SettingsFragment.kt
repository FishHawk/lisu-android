package com.fishhawk.driftinglibraryandroid.more

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.util.makeSnackBar
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<EditTextPreference>("library_address")!!.apply {
            summary = text
            setOnPreferenceChangeListener { preference, newValue ->
                val address = newValue as String
                val application = requireContext().applicationContext as MainApplication
                if (application.setLibraryAddress(address)) {
                    val textPreference = preference as EditTextPreference
                    textPreference.summary = newValue
                    true
                } else {
                    view?.makeSnackBar(getString(R.string.settings_library_address_error_hint))
                    false
                }
            }
        }

        findPreference<Preference>("clear_image_cache")!!.apply {
            setOnPreferenceClickListener {
                Glide.get(context).clearMemory()
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}