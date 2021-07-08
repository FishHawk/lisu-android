package com.fishhawk.driftinglibraryandroid.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

class MoreFragment : PreferenceFragmentCompat() {
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
        val contentView = super.onCreateView(inflater, container, savedInstanceState)!!
        val view = ComposeView(requireContext())
        view.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                backgroundColor = MaterialTheme.colors.secondary,
                                contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
                                title = { Text(stringResource(R.string.label_more)) }
                            )
                        },
                        content = {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { contentView }
                            )
                        }
                    )
                }
            }
        }
        return view
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