package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaTagGroups(
    tagGroups: Map<String, List<String>>,
    onTagClick: (String) -> Unit = {},
    onTagLongClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        tagGroups.forEach { (key, tags) ->
            Row {
                if (key.isNotBlank()) {
                    Tag(key)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                FlowRow(
                    modifier = Modifier.padding(bottom = 8.dp),
                    mainAxisSpacing = 4.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    tags.forEach { tag ->
                        val fullTag = if (key.isBlank()) tag else "$key:$tag"
                        Tag(
                            tag = tag,
                            modifier = Modifier.combinedClickable(
                                onClick = { onTagClick(fullTag) },
                                onLongClick = { onTagLongClick(fullTag) },
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditMangaTagGroup(
    tags: List<String>,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        mainAxisSpacing = 4.dp,
        crossAxisSpacing = 4.dp
    ) {
        tags.forEach { tag ->
            Tag(
                tag = tag,
                modifier = Modifier.clickable { onTagClick(tag) },
                trailingIcon = {
                    Icon(
                        imageVector = LisuIcons.Close,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Tag(
    tag: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            .compositeOver(MaterialTheme.colors.surface),
        contentColor = MaterialTheme.colors.onSurface,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ChipDefaults.ContentOpacity) {
            ProvideTextStyle(value = MaterialTheme.typography.caption) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 28.dp)
                        .padding(start = 12.dp, end = if (trailingIcon == null) 12.dp else 0.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = tag)
                    if (trailingIcon != null) {
                        Spacer(Modifier.width(8.dp))
                        trailingIcon()
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}