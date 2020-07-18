package com.fishhawk.driftinglibraryandroid.ui.other

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R


class MoreFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<Preference>("server")!!.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_more_to_server)
                true
            }
        }

        findPreference<Preference>("download")!!.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_more_to_download)
                true
            }
        }

        findPreference<Preference>("subscription")!!.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_more_to_subscription)
                true
            }
        }

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