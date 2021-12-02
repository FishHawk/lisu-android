package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.*
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.TextFieldWithSuggestions
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
        content = { LisuTransition { Content(address, suggestions, onAction) } }
    )
}

@Composable
private fun Content(
    initAddress: String,
    suggestions: List<String>,
    onAction: MoreActionHandler
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var address by remember { mutableStateOf(initAddress) }
        TextFieldWithSuggestions(
            value = address,
            onValueChange = { address = it },
            suggestions = suggestions,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Server address") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                onAction(MoreAction.UpdateAddress(address))
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