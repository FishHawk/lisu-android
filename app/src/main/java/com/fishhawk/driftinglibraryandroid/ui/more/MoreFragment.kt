package com.fishhawk.driftinglibraryandroid.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.MoreFragmentBinding

class MoreFragment : PreferenceFragmentCompat() {
    private lateinit var binding: MoreFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setPreferenceNavigation("server", R.id.action_more_to_server)

        setPreferenceNavigation("setting_general", R.id.action_more_to_setting_general)
        setPreferenceNavigation("setting_reader", R.id.action_more_to_setting_reader)
        setPreferenceNavigation("setting_advanced", R.id.action_more_to_setting_advanced)

        setPreferenceNavigation("about", R.id.action_more_to_about)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding = MoreFragmentBinding.inflate(inflater, container, false)
        binding.preference.addView(view)
        return binding.root
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