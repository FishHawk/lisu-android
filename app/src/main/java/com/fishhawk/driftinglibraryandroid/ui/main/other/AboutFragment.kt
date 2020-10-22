package com.fishhawk.driftinglibraryandroid.ui.main.other

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.BuildConfig
import com.fishhawk.driftinglibraryandroid.R

class AboutFragment : BaseSettingFragment() {
    override val titleResId: Int = R.string.label_about

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