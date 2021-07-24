package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fishhawk.driftinglibraryandroid.data.remote.model.TagGroup
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun MangaTagGroups(
    tagGroups: List<TagGroup>,
    onTagClick: (String) -> Unit = {},
    onTagLongClick: (String) -> Unit = {},
    onTagClose: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        tagGroups.map { MangaTagGroup(it, onTagClick, onTagLongClick, onTagClose) }
    }
}

@Composable
fun MangaTagGroup(
    group: TagGroup,
    onTagClick: (String) -> Unit = {},
    onTagLongClick: (String) -> Unit = {},
    onTagClose: () -> Unit = {}
) {
    Row {
        if (group.key.isNotBlank()) Tag(group.key)
        FlowRow(
            modifier = Modifier.padding(bottom = 8.dp),
            mainAxisSpacing = 4.dp,
            crossAxisSpacing = 4.dp
        ) {
            group.value.map { value ->
                val fullTag =
                    if (group.key.isBlank()) value
                    else "${group.key}:$value"
                Tag(
                    value,
                    onTagClick = { onTagClick(fullTag) },
                    onTagLongClick = { onTagLongClick(fullTag) },
                    onTagClose = onTagClose
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Tag(
    value: String,
    onTagClick: (String) -> Unit = {},
    onTagLongClick: (String) -> Unit = {},
    onTagClose: () -> Unit = {}
) {
    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.LightGray
    ) {
        Text(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                .combinedClickable(
                    onClick = { onTagClick(value) },
                    onLongClick = { onTagLongClick(value) }
                ),
            text = value,
            style = MaterialTheme.typography.body2.copy(fontSize = 12.sp)
        )
    }
}