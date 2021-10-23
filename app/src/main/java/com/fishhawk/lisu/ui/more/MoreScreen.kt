package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.getBlocking
import com.fishhawk.lisu.data.datastore.setBlocking
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.TextFieldWithSuggestions

private typealias MoreActionHandler = (MoreAction) -> Unit

private sealed interface MoreAction {
    object NavToSettingGeneral : MoreAction
    object NavToSettingReader : MoreAction
    object NavToSettingAdvanced : MoreAction
    object NavToAbout : MoreAction

    data class SetServerAddress(val address: String) : MoreAction
    data class DeleteSuggestion(val address: String) : MoreAction
}

@Composable
fun MoreScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<MoreViewModel>()
    val suggestedAddresses by viewModel.suggestedAddresses.collectAsState()
    val onAction: MoreActionHandler = { action ->
        when (action) {
            MoreAction.NavToSettingGeneral ->
                navController.navigate("setting-general")
            MoreAction.NavToSettingReader ->
                navController.navigate("setting-reader")
            MoreAction.NavToSettingAdvanced ->
                navController.navigate("setting-advanced")
            MoreAction.NavToAbout ->
                navController.navigate("about")

            is MoreAction.SetServerAddress -> {
                PR.serverAddress.setBlocking(action.address)
                viewModel.update(action.address)
            }
            is MoreAction.DeleteSuggestion -> {
                viewModel.delete(action.address)
            }
        }
    }
    Scaffold(
        topBar = { LisuToolBar(stringResource(R.string.label_more)) },
        content = { LisuTransition { Content(suggestedAddresses, onAction) } }
    )
}

@Composable
private fun Content(
    suggestedAddresses: List<String>,
    onAction: MoreActionHandler
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var serverAddress by remember { mutableStateOf(PR.serverAddress.getBlocking()) }
        TextFieldWithSuggestions(
            value = serverAddress,
            onValueChange = { serverAddress = it },
            suggestions = suggestedAddresses,
            placeholder = { Text("Server address") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                onAction(MoreAction.SetServerAddress(serverAddress))
            },
            onSuggestionDeleted = {
                onAction(MoreAction.DeleteSuggestion(it))
            }
        )

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