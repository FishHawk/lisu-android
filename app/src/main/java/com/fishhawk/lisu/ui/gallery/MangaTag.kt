package com.fishhawk.lisu.ui.gallery

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun MangaTagGroups(
    tagGroups: Map<String, List<String>>,
    onTagClick: (String) -> Unit = {},
    onTagLongClick: (String) -> Unit = {},
    onTagClose: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        tagGroups.onEach {
            TagGroup(
                it.key, it.value,
                onTagClick, onTagLongClick, onTagClose
            )
        }
    }
}

@Composable
fun TagGroup(
    name: String,
    tags: List<String>,
    onTagClick: (String) -> Unit = {},
    onTagLongClick: (String) -> Unit = {},
    onTagClose: () -> Unit = {}
) {
    Row {
        if (name.isNotBlank()) Tag(name)
        FlowRow(
            modifier = Modifier.padding(bottom = 8.dp),
            mainAxisSpacing = 4.dp,
            crossAxisSpacing = 4.dp
        ) {
            tags.map { tag ->
                val fullTag =
                    if (name.isBlank()) tag
                    else "$name:$tag"
                Tag(
                    tag,
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
    tag: String,
    onTagClick: (String) -> Unit = {},
    onTagLongClick: (String) -> Unit = {},
    onTagClose: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .combinedClickable(
                    onClick = { onTagClick(tag) },
                    onLongClick = { onTagLongClick(tag) }
                ),
            text = tag,
            style = MaterialTheme.typography.body2.copy(fontSize = 12.sp)
        )
    }
}