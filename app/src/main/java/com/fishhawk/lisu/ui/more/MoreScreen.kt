package com.fishhawk.lisu.ui.more

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.main.navToAbout
import com.fishhawk.lisu.ui.main.navToSettingAdvanced
import com.fishhawk.lisu.ui.main.navToSettingGeneral
import com.fishhawk.lisu.ui.main.navToSettingReader
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.util.nsdManager
import okhttp3.HttpUrl
import org.koin.androidx.compose.viewModel

private typealias MoreActionHandler = (MoreAction) -> Unit

private sealed interface MoreAction {
    object NavToSettingGeneral : MoreAction
    object NavToSettingReader : MoreAction
    object NavToSettingAdvanced : MoreAction
    object NavToAbout : MoreAction
    data class UpdateAddress(val address: String) : MoreAction
    data class DeleteSuggestion(val address: String) : MoreAction
}

@Composable
fun MoreScreen(navController: NavHostController) {
    val viewModel by viewModel<MoreViewModel>()
    val address by viewModel.address.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    val onAction: MoreActionHandler = { action ->
        when (action) {
            MoreAction.NavToSettingGeneral -> navController.navToSettingGeneral()
            MoreAction.NavToSettingReader -> navController.navToSettingReader()
            MoreAction.NavToSettingAdvanced -> navController.navToSettingAdvanced()
            MoreAction.NavToAbout -> navController.navToAbout()
            is MoreAction.UpdateAddress -> viewModel.updateAddress(action.address)
            is MoreAction.DeleteSuggestion -> viewModel.deleteSuggestion(action.address)
        }
    }

    Scaffold(
        topBar = { LisuToolBar(title = stringResource(R.string.label_more)) },
        content = { paddingValues ->
            LisuTransition {
                Content(
                    address, suggestions, onAction,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun Content(
    initAddress: String,
    suggestions: List<String>,
    onAction: MoreActionHandler,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ServerAddressSelector(initAddress, suggestions, onAction)

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

data class NsdService(val name: String, val address: HttpUrl)

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ServerAddressSelector(
    initAddress: String,
    suggestions: List<String>,
    onAction: MoreActionHandler
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
            DropdownMenuItem(onClick = { address = it.address.toString() }) {
                ListItem(modifier = Modifier.weight(1f),
                    secondaryText = { Text(text = it.address.toString()) }
                ) {
                    Text(text = it.name)
                }
            }
        }
        if (relativeSuggestions.isNotEmpty() && nsdServices.isNotEmpty()) {
            Divider()
        }
        relativeSuggestions.forEach {
            DropdownMenuItem(onClick = { address = it }) {
                ListItem(
                    modifier = Modifier.weight(1f),
                    trailing = {
                        IconButton(onClick = { onAction(MoreAction.DeleteSuggestion(it)) }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                ) {
                    Text(text = it)
                }
            }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextFieldWithDropdownMenu(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    hasContent: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            singleLine = true,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (expanded) IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Close, null)
                } else trailingIcon?.invoke()
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
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
