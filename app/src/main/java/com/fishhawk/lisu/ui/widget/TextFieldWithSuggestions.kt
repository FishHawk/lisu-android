package com.fishhawk.lisu.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.ui.theme.LisuToolBar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextFieldWithSuggestions(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    onSuggestionDeleted: ((String) -> Unit)? = null
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

        val relativeSuggestions = suggestions
            .filter { it.contains(value) && it != value }

        ExposedDropdownMenu(
            expanded = expanded && relativeSuggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            relativeSuggestions.forEach {
                DropdownMenuItem(onClick = { onValueChange(it) }) {
                    Text(modifier = Modifier.weight(1f), text = it)
                    if (onSuggestionDeleted != null) {
                        IconButton(onClick = { onSuggestionDeleted(it) }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LisuSearchToolBar(
    onSearch: () -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    editing: Boolean,
    onEditingChange: (Boolean) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    onNavUp: (() -> Unit) = {},
) {
    LisuToolBar {
        val focusManager = LocalFocusManager.current
        val focusRequester = remember { FocusRequester() }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onEditingChange(it.hasFocus) }
                .focusRequester(focusRequester),
            singleLine = true,
            leadingIcon = {
                IconButton(onClick = { onNavUp() }) {
                    Icon(Icons.Filled.ArrowBack, "back")
                }
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Close, "clear search")
                    }
                }
            },
            placeholder = placeholder,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions {
                onSearch()
                focusManager.clearFocus()
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        LaunchedEffect(Unit) {
            if (editing) focusRequester.requestFocus()
        }
    }
}


@Composable
fun SuggestionList(
    suggestions: List<String>,
    onSuggestionSelected: ((String) -> Unit) = {},
    onSuggestionDeleted: ((String) -> Unit)? = null
) {
    Surface {
        LazyColumn {
            items(suggestions) {
                SuggestionItem(
                    suggestion = it,
                    onSuggestionSelected = onSuggestionSelected,
                    onSuggestionDeleted = onSuggestionDeleted
                )
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: String,
    onSuggestionSelected: ((String) -> Unit) = {},
    onSuggestionDeleted: ((String) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onSuggestionSelected(suggestion) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = suggestion,
            style = MaterialTheme.typography.subtitle1
        )
        if (onSuggestionDeleted != null) {
            IconButton(onClick = { onSuggestionDeleted(suggestion) }) {
                Icon(Icons.Default.Close, null)
            }
        }
    }
}
