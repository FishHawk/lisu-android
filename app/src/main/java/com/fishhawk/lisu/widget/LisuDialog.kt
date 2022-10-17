package com.fishhawk.lisu.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LisuDialog(
    title: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    text: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = text?.let { { Text(text = it, modifier = Modifier.heightIn(max = 400.dp)) } },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
}

@Composable
fun LisuSelectDialog(
    title: String,
    options: List<String>,
    selected: Int,
    onSelectedChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { },
        title = { Text(text = title) },
        text = {
            Column {
                options.forEachIndexed { index, text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectedChanged(index) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        RadioButton(selected = index == selected, onClick = null)
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun LisuMultipleSelectDialog(
    title: String,
    options: List<String>,
    selected: Set<Int>,
    onSelectedChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { },
        title = { Text(text = title) },
        text = {
            Column {
                options.mapIndexed { index, text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectedChanged(index) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Checkbox(checked = index in selected, onCheckedChange = null)
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
    )
}
