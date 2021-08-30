package com.fishhawk.driftinglibraryandroid.ui.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo

@Composable
fun ServerEditDialog(
    isOpen: MutableState<Boolean>,
    serverInfo: ServerInfo? = null,
    onAccept: (name: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(serverInfo?.name ?: "")) }
    var address by remember { mutableStateOf(TextFieldValue(serverInfo?.address ?: "")) }

    if (isOpen.value) {
        AlertDialog(
            onDismissRequest = { isOpen.value = false },
            title = { Text(text = if (serverInfo == null) "Add server" else "Edit server") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        maxLines = 1,
                        label = { Text("Name") },
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
                    )
                    TextField(
                        value = address,
                        onValueChange = { address = it },
                        maxLines = 1,
                        label = { Text("Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAccept(name.text, address.text)
                        isOpen.value = false
                    }) {
                    Text("ok")
                }
            },
            dismissButton = {
                TextButton(onClick = { isOpen.value = false }) {
                    Text("cancel")
                }
            }
        )
    }
}