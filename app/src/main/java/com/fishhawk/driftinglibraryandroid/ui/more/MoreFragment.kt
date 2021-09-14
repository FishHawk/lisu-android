package com.fishhawk.driftinglibraryandroid.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.datastore.getBlocking
import com.fishhawk.driftinglibraryandroid.data.datastore.setBlocking
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition

@Composable
fun MoreScreen(navController: NavHostController) {
    Scaffold(
        topBar = { ApplicationToolBar(stringResource(R.string.label_more)) },
        content = { ApplicationTransition { Content(navController) } }
    )
}

@Composable
private fun Content(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var serverAddress by remember { mutableStateOf(TextFieldValue(PR.serverAddress.getBlocking())) }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = serverAddress,
            onValueChange = { serverAddress = it },
            singleLine = true,
            placeholder = { Text("Server address") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                PR.serverAddress.setBlocking(serverAddress.text)
            }),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Divider()
        TextPreference(
            icon = Icons.Filled.Tune,
            title = stringResource(R.string.more_setting_general)
        ) {
            navController.navigate("setting-general")
        }
        TextPreference(
            icon = Icons.Filled.Book,
            title = stringResource(R.string.more_setting_reader)
        ) {
            navController.navigate("setting-reader")
        }
        TextPreference(
            icon = Icons.Filled.Code,
            title = stringResource(R.string.more_setting_advanced)
        ) {
            navController.navigate("setting-advanced")
        }
        Divider()
        TextPreference(
            icon = Icons.Filled.Error,
            title = stringResource(R.string.more_about)
        ) {
            navController.navigate("about")
        }
    }
}