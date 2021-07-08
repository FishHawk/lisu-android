package com.fishhawk.driftinglibraryandroid.ui.server

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

class ServerFragment : Fragment() {
    private val viewModel: ServerViewModel by viewModels {
        MainViewModelFactory(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    Scaffold(
                        topBar = { ToolBar() },
                        content = { Content() }
                    )
                }
            }
        }
        return view
    }

    @Composable
    private fun ToolBar() {
        TopAppBar(
            contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
            title = { Text(stringResource(R.string.label_server)) },
            actions = {
                val isOpen = remember { mutableStateOf(false) }
                IconButton(onClick = { isOpen.value = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                    ServerEditDialog(isOpen, null) { name, address ->
                        viewModel.addServer(ServerInfo(name, address))
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { findNavController().navigateUp() }) {
                    Icon(Icons.Filled.NavigateBefore, "back")
                }
            }
        )
    }

    @Composable
    private fun Content() {
        val serverList by viewModel.serverInfoList.observeAsState(listOf())
        val selectedServer by GlobalPreference.selectedServer.asFlow().collectAsState(
            GlobalPreference.selectedServer.get()
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(serverList) { ServerInfoCard(it, selectedServer == it.id) }
            if (serverList.isEmpty()) item { EmptyView() }
        }
    }

    @Composable
    private fun ServerInfoCard(info: ServerInfo, selected: Boolean) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { GlobalPreference.selectedServer.set(info.id) }
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                        .background(
                            if (selected) MaterialTheme.colors.secondary
                            else Color.LightGray
                        )
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = info.name,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = info.address,
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val isOpen = remember { mutableStateOf(false) }
                IconButton(onClick = { isOpen.value = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    ServerEditDialog(isOpen, info) { name, address ->
                        info.name = name
                        info.address = address
                        viewModel.updateServer(info)
                    }
                }
                IconButton(onClick = { viewModel.deleteServer(info) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.serverInfoList.observe(viewLifecycleOwner) { data ->
            if (data.size == 1) GlobalPreference.selectedServer.set(data.first().id)
        }
    }
}
