package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun ReaderPageSheet() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextButton(onClick = { }) {
            Row {
                Icon(Icons.Filled.Edit, null)
                Text("Edit metadata")
            }
        }
        TextButton(onClick = { }) {
            Row {
                Icon(Icons.Filled.Edit, null)
                Text("Edit cover")
            }
        }
        TextButton(onClick = { }) {
            Row {
                Icon(Icons.Filled.SaveAlt, null)
                Text("Save cover")
            }
        }
        TextButton(onClick = { }) {
            Row {
                Icon(Icons.Filled.Share, null)
                Text("Share cover")
            }
        }
    }
}
