package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fishhawk.driftinglibraryandroid.R

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
                Icon(Icons.Filled.Refresh, null)
                Text(stringResource(R.string.page_action_refresh))
            }
        }
        TextButton(onClick = { }) {
            Row {
                Icon(Icons.Filled.SaveAlt, null)
                Text(stringResource(R.string.page_action_save))
            }
        }
        TextButton(onClick = { }) {
            Row {
                Icon(Icons.Filled.Share, null)
                Text(stringResource(R.string.page_action_share))
            }
        }
    }
}