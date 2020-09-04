package com.fishhawk.driftinglibraryandroid.ui.other

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R

class MoreFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setPreferenceNavigation("server", R.id.action_more_to_server)
        setPreferenceNavigation("download", R.id.action_more_to_download)
        setPreferenceNavigation("subscription", R.id.action_more_to_subscription)

        setPreferenceNavigation("setting_general", R.id.action_more_to_setting_general)
        setPreferenceNavigation("setting_reader", R.id.action_more_to_setting_reader)
        setPreferenceNavigation("setting_advanced", R.id.action_more_to_setting_advanced)

        setPreferenceNavigation("about", R.id.action_more_to_about)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.more, rootKey)
    }

    private fun setPreferenceNavigation(key: String, action: Int) {
        findPreference<Preference>(key)!!.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(action)
                true
            }
        }
    }
}