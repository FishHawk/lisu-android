package com.fishhawk.driftinglibraryandroid.ui.other

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.BuildConfig
import com.fishhawk.driftinglibraryandroid.R

class AboutFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<Preference>("version")!!.apply {
            summary = if (BuildConfig.DEBUG) {
                "Preview ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            } else {
                "Stable ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            }
        }

        findPreference<Preference>("check_for_updates")!!.apply {
            // TODO: check update
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about, rootKey)
    }
}