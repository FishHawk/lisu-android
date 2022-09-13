package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavHostController
import com.fishhawk.lisu.data.network.model.CommentDto
import com.fishhawk.lisu.util.toDisplayString
import com.fishhawk.lisu.util.toLocalDateTime
import com.fishhawk.lisu.widget.ErrorItem
import com.fishhawk.lisu.widget.LisuToolBar
import com.fishhawk.lisu.widget.LoadingItem
import com.fishhawk.lisu.widget.StateView
import org.koin.androidx.compose.viewModel

internal typealias GalleryCommentActionHandler = (GalleryCommentAction) -> Unit

internal sealed interface GalleryCommentAction {
    object NavUp : GalleryCommentAction
    object Reload : GalleryCommentAction
    object RequestNextPage : GalleryCommentAction
}

@Composable
fun GalleryCommentScreen(navController: NavHostController) {
    val viewModel by viewModel<GalleryViewModel>(
        owner = navController.previousBackStackEntry!!
    )
    val commentList by viewModel.comments.collectAsState()

    val onAction: GalleryCommentActionHandler = { action ->
        when (action) {
            GalleryCommentAction.NavUp ->
                navController.navigateUp()
            GalleryCommentAction.Reload ->
                viewModel.reloadComments()
            GalleryCommentAction.RequestNextPage ->
                viewModel.requestCommentsNextPage()
        }
    }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = "Comments",
                onNavUp = { onAction(GalleryCommentAction.NavUp) },
            )
        },
        content = { paddingValues ->
            StateView(
                result = commentList,
                onRetry = { onAction(GalleryCommentAction.Reload) },
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            ) { commentList ->
                var maxAccessed by rememberSaveable { mutableStateOf(0) }
                LazyColumn {
                    itemsIndexed(commentList.list) { index, it ->
                        if (index > maxAccessed) {
                            maxAccessed = index
                            if (
                                commentList.appendState?.isSuccess == true &&
                                maxAccessed < commentList.list.size + 30
                            ) onAction(GalleryCommentAction.RequestNextPage)
                        }
                        CommentWithSubComments(comment = it)
                        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.06f))
                    }
                    commentList.appendState
                        ?.onFailure { item { ErrorItem(it) { onAction(GalleryCommentAction.RequestNextPage) } } }
                        ?: item { LoadingItem() }
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
    modifier: Modifier = Modifier,
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
