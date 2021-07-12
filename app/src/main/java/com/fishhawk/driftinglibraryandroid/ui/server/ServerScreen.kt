package com.fishhawk.driftinglibraryandroid.ui.server

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition

@Composable
fun ServerScreen(navController: NavHostController) {
    Scaffold(
        topBar = { ToolBar(navController) },
        content = { ApplicationTransition { Content() } }
    )
}

@Composable
private fun ToolBar(navController: NavHostController) {
    val viewModel = hiltViewModel<ServerViewModel>()

    ApplicationToolBar(stringResource(R.string.label_server), navController) {
        val isOpen = remember { mutableStateOf(false) }
        IconButton(onClick = { isOpen.value = true }) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
            ServerEditDialog(isOpen, null) { name, address ->
                viewModel.addServer(ServerInfo(name, address))
            }
        }
    }
}

@Composable
private fun Content() {
    val viewModel = hiltViewModel<ServerViewModel>()
    val serverList by viewModel.serverInfoList.observeAsState(listOf())
    if (serverList.size == 1) GlobalPreference.selectedServer.set(serverList.first().id)

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
    val viewModel = hiltViewModel<ServerViewModel>()
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
                        if (selected) MaterialTheme.colors.primary
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
