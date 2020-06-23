package com.fishhawk.driftinglibraryandroid.more

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R

class MoreFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<Preference>("settings")!!.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_more_to_settings)
                true
            }
        }

        findPreference<Preference>("about")!!.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_more_to_about)
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.more, rootKey)
    }
}