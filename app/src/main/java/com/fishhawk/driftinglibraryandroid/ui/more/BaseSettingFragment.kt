package com.fishhawk.driftinglibraryandroid.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

abstract class BaseSettingFragment : PreferenceFragmentCompat() {
    protected abstract val titleResId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contentView = super.onCreateView(inflater, container, savedInstanceState)!!
        val view = ComposeView(requireContext())
        view.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                backgroundColor = MaterialTheme.colors.surface,
                                contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
                                title = { Text(stringResource(titleResId)) },
                                navigationIcon = {
                                    IconButton(onClick = { findNavController().navigateUp() }) {
                                        Icon(Icons.Filled.NavigateBefore, "back")
                                    }
                                }
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
}