package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.data.remote.model.CommentDto
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.util.toDisplayString
import com.fishhawk.lisu.util.toLocalDateTime
import org.koin.androidx.compose.viewModel

@Composable
fun GalleryCommentScreen(navController: NavHostController) {
    val viewModel by viewModel<GalleryViewModel>(
        owner = navController.previousBackStackEntry!!
    )
    val commentList = viewModel.commentList.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            LisuToolBar(
                title = "Comments",
                onNavUp = { navController.navigateUp() },
            )
        },
        content = { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(commentList.itemSnapshotList.items) {
                    CommentWithSubComments(comment = it)
                    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.06f))
                }
            }
        }
    )
}

@Composable
private fun CommentWithSubComments(comment: CommentDto) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Comment(comment = comment)
        comment.subComments?.let { subComments ->
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .background(
                        MaterialTheme.colors.onSurface
                            .copy(alpha = 0.12f)
                            .compositeOver(MaterialTheme.colors.surface)
                    )
            ) {
                subComments.onEach {
                    Comment(
                        comment = it,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.06f))
                }
            }
        }
    }
}

@Composable
private fun Comment(
    comment: CommentDto,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ProvideTextStyle(value = MaterialTheme.typography.body2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = comment.username)
                    comment.createTime?.toLocalDateTime()?.toDisplayString()?.let {
                        Text(text = it)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append(comment.content)
                    comment.vote?.let {
                        append("  ")
                        appendInlineContent("vote", "vote")
                    }
                },
                inlineContent = mapOf(
                    "vote" to InlineTextContent(
                        Placeholder(
                            width = 4.em,
                            height = MaterialTheme.typography.body2.fontSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                        )
                    ) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = comment.vote?.let {
                                    if (it > 0) "+$it"
                                    else if (it < 0) "-$it"
                                    else "0"
                                } ?: "",
                                style = MaterialTheme.typography.caption,
                            )
                        }
                    },
                ),
            )
        }
    }
}
