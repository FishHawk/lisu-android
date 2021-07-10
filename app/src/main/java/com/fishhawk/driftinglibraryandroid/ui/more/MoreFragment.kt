package com.fishhawk.driftinglibraryandroid.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.fishhawk.driftinglibraryandroid.util.setNext
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

class MoreFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setPreferenceNavigation("server", R.id.action_more_to_server)
//
//        setPreferenceNavigation("setting_general", R.id.action_more_to_setting_general)
//        setPreferenceNavigation("setting_reader", R.id.action_more_to_setting_reader)
//        setPreferenceNavigation("setting_advanced", R.id.action_more_to_setting_advanced)
//
//        setPreferenceNavigation("about", R.id.action_more_to_about)
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
                    MoreScreenX(contentView)
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

@Composable
private fun MoreScreenX(contentView: View) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.surface,
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

@Composable
fun MoreScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.surface,
                contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
                title = { Text(stringResource(R.string.label_more)) }
            )
        },
        content = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Button(onClick = { navController.navigate("server") }) {
                        Text(text = "Server")
                    }
                    Button(onClick = { GlobalPreference.theme.setNext() }) {
                        Text(text = "Theme")
                    }
                }
            }
        }
    )
}
