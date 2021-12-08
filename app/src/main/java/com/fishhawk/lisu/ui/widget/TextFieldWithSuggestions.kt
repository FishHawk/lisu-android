package com.fishhawk.lisu.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.imePadding

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
                    IconButton(onClick = {
                        onValueChange("")
                        focusRequester.requestFocus()
                    }) {
                        Icon(Icons.Default.Close, "clear")
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

        // hack, see https://stackoverflow.com/questions/68389802/how-to-clear-textfield-focus-when-closing-the-keyboard-and-prevent-two-back-pres
        var imeIsVisiblePrev by remember { mutableStateOf(false) }
        val imeIsVisible = LocalWindowInsets.current.ime.isVisible
        LaunchedEffect(imeIsVisible) {
            if (!imeIsVisible && imeIsVisiblePrev) {
                focusManager.clearFocus()
            }
            imeIsVisiblePrev = imeIsVisible
        }
    }
}

@Composable
fun SuggestionList(
    editing: Boolean,
    keywords: String,
    suggestions: List<String>,
    onSuggestionSelected: ((String) -> Unit) = {},
    onSuggestionDeleted: ((String) -> Unit)? = null
) {
    AnimatedVisibility(
        editing,
        modifier = Modifier.imePadding(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val relativeSuggestions = suggestions.filter {
            it.contains(keywords) && it != keywords
        }
        Surface {
            LazyColumn {
                items(relativeSuggestions) {
                    SuggestionItem(
                        keywords = keywords,
                        suggestion = it,
                        onSuggestionSelected = onSuggestionSelected,
                        onSuggestionDeleted = onSuggestionDeleted
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    keywords: String,
    suggestion: String,
    onSuggestionSelected: ((String) -> Unit) = {},
    onSuggestionDeleted: ((String) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSuggestionSelected(suggestion) }
            .padding(start = 64.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildHighlightedSuggestion(
                keywords = keywords,
                suggestion = suggestion
            ),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            style = MaterialTheme.typography.subtitle1
        )
        if (onSuggestionDeleted != null) {
            IconButton(onClick = { onSuggestionDeleted(suggestion) }) {
                Icon(Icons.Default.Close, null)
            }
        }
    }
}

private fun buildHighlightedSuggestion(
    keywords: String,
    suggestion: String
): AnnotatedString {
    return buildAnnotatedString {
        if (keywords.isEmpty()) {
            append(suggestion)
            return@buildAnnotatedString
        }
        Regex("((?<=%1\$s)|(?=%1\$s))".format(keywords))
            .split(suggestion)
            .onEach {
                if (it == keywords) withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold)
                ) { append(it) }
                else append(it)
            }
    }
}