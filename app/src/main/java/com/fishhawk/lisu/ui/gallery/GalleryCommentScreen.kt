package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.data.LoremIpsum
import com.fishhawk.lisu.data.network.base.PagedList
import com.fishhawk.lisu.data.network.model.CommentDto
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.ui.theme.mediumEmphasisColor
import com.fishhawk.lisu.util.readableString
import com.fishhawk.lisu.util.toLocalDateTime
import com.fishhawk.lisu.widget.*
import org.koin.androidx.compose.koinViewModel

private sealed interface GalleryCommentAction {
    object NavUp : GalleryCommentAction
    object Reload : GalleryCommentAction
    object RequestNextPage : GalleryCommentAction
}

@Composable
fun GalleryCommentScreen(
    navController: NavHostController,
    viewModel: GalleryViewModel = koinViewModel(
        owner = navController.previousBackStackEntry!!
    ),
) {
    val commentList by viewModel.comments.collectAsState()

    val onAction: (GalleryCommentAction) -> Unit = { action ->
        when (action) {
            GalleryCommentAction.NavUp ->
                navController.navigateUp()
            GalleryCommentAction.Reload ->
                viewModel.reloadComments()
            GalleryCommentAction.RequestNextPage ->
                viewModel.requestCommentsNextPage()
        }
    }

    GalleryCommentScaffold(
        commentList = commentList,
        onAction = onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryCommentScaffold(
    commentList: Result<PagedList<CommentDto>>?,
    onAction: (GalleryCommentAction) -> Unit,
) {
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
            ) { commentList, modifier ->
                CommentList(
                    commentList = commentList,
                    onAction = onAction,
                    modifier = modifier,
                )
            }
        }
    )
}

@Composable
private fun CommentList(
    commentList: PagedList<CommentDto>,
    onAction: (GalleryCommentAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var maxAccessed by rememberSaveable { mutableStateOf(0) }
    LazyColumn(modifier = modifier) {
        itemsIndexed(commentList.list) { index, it ->
            if (index > maxAccessed) {
                maxAccessed = index
                if (
                    commentList.appendState?.isSuccess == true &&
                    maxAccessed < commentList.list.size + 30
                ) onAction(GalleryCommentAction.RequestNextPage)
            }
            CommentWithSubComments(comment = it)
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        }
        commentList.appendState
            ?.onFailure { item { ErrorItem(it) { onAction(GalleryCommentAction.RequestNextPage) } } }
            ?: item { LoadingItem() }
    }
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
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.08f)
                            .compositeOver(MaterialTheme.colorScheme.surface)
                    )
                    .padding(horizontal = 16.dp),
            ) {
                subComments.forEachIndexed { index, it ->
                    Comment(
                        comment = it,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    if (index < subComments.size - 1) {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    }
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
    ProvideTextStyle(value = MaterialTheme.typography.bodySmall) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MediumEmphasis {
                    Text(
                        text = comment.username,
                        fontWeight = FontWeight.Medium,
                    )
                    comment.createTime?.toLocalDateTime()?.readableString()?.let {
                        Text(
                            text = it,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    append(comment.content)
                    comment.vote?.let {
                        append("  ")
                        withStyle(
                            style = SpanStyle(
                                color = mediumEmphasisColor(),
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                fontWeight = FontWeight.Medium,
                            ),
                        ) {
                            append(if (it >= 0) "+$it" else it.toString())
                        }
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun GalleryCommentScaffoldPreview() {
    fun dummyCommentList(): List<CommentDto> {
        return List(100) {
            LoremIpsum.comment().copy(
                subComments = List(5) {
                    LoremIpsum.comment()
                }
            )
        }
    }

    LisuTheme {
        GalleryCommentScaffold(
            commentList = Result.success(PagedList(dummyCommentList(), null)),
            onAction = { println(it) },
        )
    }
}