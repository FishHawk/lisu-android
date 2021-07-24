package com.fishhawk.driftinglibraryandroid.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fishhawk.driftinglibraryandroid.data.datastore.Preference
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.data.datastore.get
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BasePreference(
    icon: ImageVector?,
    title: String,
    summary: String?,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    ListItem(
        modifier = Modifier.clickable(onClick = { onClick() }),
        icon = icon?.let {
            { Icon(it, contentDescription = "", tint = MaterialTheme.colors.primary) }
        },
        text = { Text(text = title) },
        secondaryText = summary?.let { { Text(text = it) } },
        trailing = trailing
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextPreference(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    onClick: () -> Unit = {}
) = BasePreference(icon, title, summary, onClick)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchPreference(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    preference: Preference<Boolean>
) {
    val scope = rememberCoroutineScope()
    BasePreference(
        icon = icon,
        title = title,
        summary = summary,
        onClick = { scope.launch { preference.set(!preference.get()) } }
    ) {
        val checked by preference.collectAsState()
        Switch(checked = checked, onCheckedChange = null)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
inline fun <reified T : Enum<T>> ListPreference(
    icon: ImageVector? = null,
    title: String,
    summary: String? = "%s",
    preference: Preference<T>,
    crossinline translate: (T) -> Int
) {
    val isOpen = remember { mutableStateOf(false) }
    val selected by preference.collectAsState()

    BasePreference(
        icon = icon,
        title = title,
        summary = summary?.format(selected.name),
        onClick = { isOpen.value = true }
    ) {
        ListPreferenceDialog(isOpen, title, preference, translate)
    }
}

@Composable
inline fun <reified T : Enum<T>> ListPreferenceDialog(
    isOpen: MutableState<Boolean>,
    title: String,
    preference: Preference<T>,
    crossinline translate: (T) -> Int
) {
    if (!isOpen.value) return

    val selected by preference.collectAsState()
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { isOpen.value = false },
        title = {
            Text(
                text = title,
                style = LocalTextStyle.current.copy(fontWeight = FontWeight.Medium)
            )
        },
        text = {
            Column {
                enumValues<T>().forEach {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scope.launch { preference.set(it) } }
                            .padding(top = 12.dp, bottom = 12.dp)
                    ) {
                        RadioButton(
                            selected = (it == selected),
                            onClick = null
                        )
                        Text(
                            text = stringResource(translate(it)),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { }
    )
}