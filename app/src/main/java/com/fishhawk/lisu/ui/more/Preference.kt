package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.fishhawk.lisu.data.datastore.Preference
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.get
import com.fishhawk.lisu.widget.LisuSelectDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BasePreference(
    icon: ImageVector?,
    title: String,
    summary: String?,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
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

@Composable
fun TextPreference(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    onClick: () -> Unit = {},
) = BasePreference(icon, title, summary, onClick)

@Composable
fun SwitchPreference(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    preference: Preference<Boolean>,
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

@Composable
inline fun <reified T : Enum<T>> ListPreference(
    icon: ImageVector? = null,
    title: String,
    summary: String? = "%s",
    preference: Preference<T>,
    crossinline translate: (T) -> Int,
) {
    val isOpen = remember { mutableStateOf(false) }
    val selected by preference.collectAsState()

    BasePreference(
        icon = icon,
        title = title,
        summary = summary?.format(selected.name),
        onClick = { isOpen.value = true }
    ) {
        if (isOpen.value) {
            val scope = rememberCoroutineScope()
            LisuSelectDialog(
                title = title,
                options = enumValues<T>().map { stringResource(translate(it)) },
                selected = selected.ordinal,
                onSelectedChanged = { scope.launch { preference.set(enumValues<T>()[it]) } },
                onDismiss = { isOpen.value = false },
            )
        }
    }
}