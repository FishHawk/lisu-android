package com.fishhawk.lisu.widget

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LisuSearchToolBar(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
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
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun SuggestionList(
    visible: Boolean,
    onDismiss: () -> Unit,
    keywords: String,
    suggestions: List<String>,
    onSuggestionSelected: ((String) -> Unit) = {},
    onSuggestionDeleted: ((String) -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val skController = LocalSoftwareKeyboardController.current
        val onDismissWithCloseKeyboard = {
            onDismiss()
            skController?.hide()
        }

        // Dismiss when back
        var imeIsVisiblePrev by remember { mutableStateOf(false) }
        val imeIsVisible = WindowInsets.isImeVisible
        LaunchedEffect(imeIsVisible) {
            if (visible) {
                if (!imeIsVisible && imeIsVisiblePrev) {
                    onDismiss()
                }
                imeIsVisiblePrev = imeIsVisible
            }
        }

        // Dismiss when click scrim
        val scrimColor = MaterialTheme.colorScheme.scrim
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { onDismissWithCloseKeyboard() } },
        ) { drawRect(color = scrimColor) }

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
                        onSuggestionDeleted = onSuggestionDeleted,
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
    onSuggestionDeleted: ((String) -> Unit)? = null,
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
            style = MaterialTheme.typography.bodyLarge,
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
    suggestion: String,
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


@Composable
private fun keyboardAsState(): State<Boolean> {
    val keyboardState = remember { mutableStateOf(false) }
    val view = LocalView.current
    DisposableEffect(view) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }
    return keyboardState
}