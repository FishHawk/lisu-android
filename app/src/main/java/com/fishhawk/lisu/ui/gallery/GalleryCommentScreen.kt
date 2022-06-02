package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.CommentDto
import com.fishhawk.lisu.ui.widget.LisuToolBar
import org.koin.androidx.compose.viewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                items(commentList.itemCount) { index ->
                    val comment = commentList[index]
                    Comment(comment = comment)
                    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.06f))
                }
            }
        }
    )
}

@Composable
private fun SComment(
    comment: CommentDto?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val dateString = comment?.createTime?.let {
            val now = LocalDateTime.now()
            val date = Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            val days = ChronoUnit.DAYS.between(date, now)
            when {
                days == 0L -> {
                    val hours = ChronoUnit.HOURS.between(date, now)
                    if (hours == 0L) {
                        val minutes = ChronoUnit.MINUTES.between(date, now)
                        if (minutes == 0L) "just now"
                        else "$minutes minutes age"
                    } else "$hours hours age"
                }
                days == 1L -> stringResource(R.string.history_yesterday)
                days <= 5L -> stringResource(R.string.history_n_days_ago).format(days)
                else -> date.format(DateTimeFormatter.ofPattern(stringResource(R.string.history_date_format)))
            }
        } ?: ""
        ProvideTextStyle(value = MaterialTheme.typography.body2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = comment?.username ?: "")
                    Text(text = dateString)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append(comment?.content ?: "")
                    append("  ")
                    appendInlineContent("vote", "vote")
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
                                text = comment?.vote?.let {
                                    if (it > 0) "+$it"
                                    else if (it < 0) "-$it"
                                    else null
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

@Composable
private fun Comment(comment: CommentDto?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SComment(comment = comment)
        comment?.subComments?.let { subComments ->
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
                    SComment(
                        comment = it,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.06f))
                }
            }
        }
    }
}