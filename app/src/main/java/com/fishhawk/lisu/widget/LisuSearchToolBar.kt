package com.fishhawk.lisu.widget

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.R
import com.google.accompanist.insets.LocalWindowInsets

@Composable
fun LisuSearchToolBar(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    placeholder: @Composable (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        // When setting a new text, set the pointer at the end
        var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
        val textFieldValue = textFieldValueState.copy(
            text = value,
            selection = TextRange(value.length)
        )

        LisuToolBar {
            val focusRequester = remember { FocusRequester() }

            TextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValueState = it
                    if (value != it.text) {
                        onValueChange(it.text)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                leadingIcon = {
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(Icons.Default.Close, stringResource(R.string.action_clear))
                        }
                    }
                },
                placeholder = placeholder,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions { onSearch(value) },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            // hack, see https://stackoverflow.com/questions/68389802/how-to-clear-textfield-focus-when-closing-the-keyboard-and-prevent-two-back-pres
            var imeIsVisiblePrev by remember { mutableStateOf(false) }
            val imeIsVisible = LocalWindowInsets.current.ime.isVisible
            LaunchedEffect(imeIsVisible) {
                if (!imeIsVisible && imeIsVisiblePrev) {
                    onDismiss()
                }
                imeIsVisiblePrev = imeIsVisible
            }
        }
    }
}

@Composable
fun SuggestionList(
    visible: Boolean,
    keywords: String,
    suggestions: List<String>,
    additionalBottom: Dp = 0.dp,
    onSuggestionSelected: ((String) -> Unit) = {},
    onSuggestionDeleted: ((String) -> Unit)? = null
) {
    // hack, avoid extra space due to BottomNavigation
    val bottomPadding = with(LocalDensity.current) {
        LocalWindowInsets.current.ime.bottom.toDp() + additionalBottom
    }.coerceAtLeast(0.dp)

    AnimatedVisibility(
        visible,
        modifier = Modifier.padding(bottom = bottomPadding),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val relativeSuggestions = suggestions.filter {
            it.contains(keywords) && it != keywords
        }
        Surface(elevation = 4.dp) {
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