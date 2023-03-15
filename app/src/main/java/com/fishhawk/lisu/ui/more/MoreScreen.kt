package com.fishhawk.lisu.ui.more

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.main.*
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.util.nsdManager
import com.fishhawk.lisu.widget.LisuScaffold
import com.fishhawk.lisu.widget.LisuToolBar
import okhttp3.HttpUrl
import org.koin.androidx.compose.koinViewModel

private sealed interface MoreAction {
    object NavToDownload : MoreAction
    object NavToSettingGeneral : MoreAction
    object NavToSettingReader : MoreAction
    object NavToSettingAdvanced : MoreAction
    object NavToAbout : MoreAction
    data class UpdateAddress(val address: String) : MoreAction
    data class DeleteSuggestion(val address: String) : MoreAction
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    navController: NavHostController,
    viewModel: MoreViewModel = koinViewModel(),
) {
    val address by viewModel.address.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    val onAction: (MoreAction) -> Unit = { action ->
        when (action) {
            MoreAction.NavToDownload -> navController.navToDownload()
            MoreAction.NavToSettingGeneral -> navController.navToSettingGeneral()
            MoreAction.NavToSettingReader -> navController.navToSettingReader()
            MoreAction.NavToSettingAdvanced -> navController.navToSettingAdvanced()
            MoreAction.NavToAbout -> navController.navToAbout()
            is MoreAction.UpdateAddress -> viewModel.updateAddress(action.address)
            is MoreAction.DeleteSuggestion -> viewModel.deleteSuggestion(action.address)
        }
    }

    LisuScaffold(
        topBar = { LisuToolBar(title = stringResource(R.string.label_more)) },
        content = { paddingValues ->
            LisuTransition {
                Content(
                    initAddress = address,
                    suggestions = suggestions,
                    onAction = onAction,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth(),
                )
            }
        }
    )
}

@Composable
private fun Content(
    initAddress: String,
    suggestions: List<String>,
    onAction: (MoreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ServerAddressSelector(initAddress, suggestions, onAction)

        Divider()

        TextPreference(
            icon = Icons.Filled.Download,
            title = stringResource(R.string.more_download)
        ) { onAction(MoreAction.NavToDownload) }

        Divider()

        TextPreference(
            icon = Icons.Filled.Tune,
            title = stringResource(R.string.more_setting_general)
        ) { onAction(MoreAction.NavToSettingGeneral) }
        TextPreference(
            icon = Icons.Filled.Book,
            title = stringResource(R.string.more_setting_reader)
        ) { onAction(MoreAction.NavToSettingReader) }
        TextPreference(
            icon = Icons.Filled.Code,
            title = stringResource(R.string.more_setting_advanced)
        ) { onAction(MoreAction.NavToSettingAdvanced) }

        Divider()

        TextPreference(
            icon = Icons.Filled.Error,
            title = stringResource(R.string.more_about)
        ) { onAction(MoreAction.NavToAbout) }
    }
}

private data class NsdService(
    val name: String,
    val address: HttpUrl,
)

@Composable
private fun ServerAddressSelector(
    initAddress: String,
    suggestions: List<String>,
    onAction: (MoreAction) -> Unit,
) {
    val nsdServices = remember { mutableStateListOf<NsdService>() }
    var address by remember { mutableStateOf(initAddress) }
    val relativeSuggestions = suggestions
        .filter {
            it.contains(address) &&
                    it != address &&
                    nsdServices.none { s -> s.address.toString() == it }
        }
    TextFieldWithDropdownMenu(
        value = address,
        onValueChange = { address = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Server address") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            onAction(MoreAction.UpdateAddress(address))
        },
        hasContent = relativeSuggestions.isNotEmpty() || nsdServices.isNotEmpty(),
    ) {
        nsdServices.forEach {
            DropdownMenuItem(
                text = {
                    ListItem(
                        headlineContent = { Text(text = it.name) },
                        modifier = Modifier.weight(1f),
                        supportingContent = { Text(text = it.address.toString()) }
                    )
                },
                onClick = { address = it.address.toString() },
            )
        }
        if (relativeSuggestions.isNotEmpty() && nsdServices.isNotEmpty()) {
            Divider()
        }
        relativeSuggestions.forEach {
            DropdownMenuItem(
                text = {
                    ListItem(
                        headlineContent = { Text(text = it) },
                        modifier = Modifier.weight(1f),
                        trailingContent = {
                            IconButton(onClick = { onAction(MoreAction.DeleteSuggestion(it)) }) {
                                Icon(LisuIcons.Close, null)
                            }
                        }
                    )
                },
                onClick = { address = it },
            )

        }
    }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {}
            override fun onDiscoveryStopped(serviceType: String) {}

            override fun onServiceFound(service: NsdServiceInfo) {
                context.nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(service: NsdServiceInfo, errorCode: Int) {}
                    override fun onServiceResolved(service: NsdServiceInfo) {
                        service.host.hostAddress?.let {
                            nsdServices.add(
                                NsdService(
                                    name = service.serviceName,
                                    address = HttpUrl.Builder()
                                        .scheme("http")
                                        .host(it)
                                        .port(service.port)
                                        .build()
                                )
                            )
                        }
                    }
                })
            }

            override fun onServiceLost(service: NsdServiceInfo) {}

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                context.nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                context.nsdManager.stopServiceDiscovery(this)
            }
        }

        context.nsdManager.discoverServices(
            "_lisu._tcp",
            NsdManager.PROTOCOL_DNS_SD,
            discoveryListener
        )
        onDispose {
            context.nsdManager.stopServiceDiscovery(discoveryListener)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextFieldWithDropdownMenu(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    hasContent: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.menuAnchor(),
            singleLine = true,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (expanded) IconButton(onClick = { onValueChange("") }) {
                    Icon(LisuIcons.Close, null)
                } else trailingIcon?.invoke()
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        ExposedDropdownMenu(
            expanded = expanded && hasContent,
            onDismissRequest = { expanded = false },
            content = content
        )
    }
}
