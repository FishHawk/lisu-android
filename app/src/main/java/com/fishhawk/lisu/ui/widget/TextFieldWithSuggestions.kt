package com.fishhawk.lisu.ui.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextFieldWithSuggestions(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    onSuggestionDeleted: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
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

        val relativeSuggestions = suggestions
            .filter { it.contains(value) && it != value }

        ExposedDropdownMenu(
            expanded = expanded && relativeSuggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            relativeSuggestions.forEach {
                DropdownMenuItem(onClick = { onValueChange(it) }) {
                    Text(modifier = Modifier.weight(1f), text = it)
                    IconButton(onClick = { onSuggestionDeleted(it) }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }
        }
    }
}