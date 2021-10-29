package com.fishhawk.lisu.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.EmptyView
import com.fishhawk.lisu.ui.base.MangaCover
import com.fishhawk.lisu.ui.navToGallery
import com.fishhawk.lisu.ui.navToReader
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private typealias HistoryActionHandler = (HistoryAction) -> Unit

private sealed interface HistoryAction {
    data class NavToGallery(val history: ReadingHistory) : HistoryAction
    data class NavToReader(val history: ReadingHistory) : HistoryAction
    data class DeleteHistory(val history: ReadingHistory) : HistoryAction
    object ClearHistory : HistoryAction
}

@Composable
fun HistoryScreen(navController: NavHostController) {
    val context = LocalContext.current

    val viewModel = hiltViewModel<HistoryViewModel>()
    val histories by viewModel.histories.collectAsState()

    val onAction: HistoryActionHandler = { action ->
        when (action) {
            is HistoryAction.NavToGallery -> with(action.history) {
                navController.navToGallery(MangaDto(
                    providerId = providerId,
                    id = mangaId,
                    cover = cover,
                    title = title,
                    authors = authors?.let { listOf(it) }
                ))
            }
            is HistoryAction.NavToReader -> with(action.history) {
                context.navToReader(
                    mangaId, providerId,
                    collectionId, chapterId, page
                )
            }
            is HistoryAction.DeleteHistory -> viewModel.deleteHistory(action.history)
            HistoryAction.ClearHistory -> viewModel.clearHistory()
        }
    }

    Scaffold(
        topBar = { ToolBar(onAction) },
        content = { LisuTransition { HistoryList(histories, onAction) } }
    )
}

@Composable
private fun ToolBar(onAction: HistoryActionHandler) {
    LisuToolBar(title = stringResource(R.string.label_history)) {
        var isOpen by remember { mutableStateOf(false) }
        IconButton(onClick = { isOpen = true }) {
            Icon(LisuIcons.ClearAll, stringResource(R.string.menu_history_clear))
            if (isOpen) {
                ClearHistoryDialog(
                    onDismiss = { isOpen = false },
                    onConfirm = { onAction(HistoryAction.ClearHistory) }
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    histories: Map<LocalDate, List<ReadingHistory>>,
    onAction: HistoryActionHandler
) {
    if (histories.isEmpty()) EmptyView()
    LazyColumn {
        histories.forEach { (date, list) ->
            item { HistoryListHeader(date) }
            items(list, key = { Pair(it.providerId, it.mangaId) }) {
                HistoryListItem(it, onAction)
            }
        }
    }
}

@Composable
private fun HistoryListHeader(date: LocalDate) {
    val now = LocalDate.now()
    val days = ChronoUnit.DAYS.between(date, now)
    val dateString = when {
        days == 0L -> stringResource(R.string.history_today)
        days == 1L -> stringResource(R.string.history_yesterday)
        days <= 5L -> stringResource(R.string.history_n_days_ago).format(days)
        else -> date.format(DateTimeFormatter.ofPattern(stringResource(R.string.history_date_format)))
    }
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = dateString,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HistoryListItem(
    history: ReadingHistory,
    onAction: HistoryActionHandler
) {
    val dismissState = rememberDismissState(
        confirmStateChange = {
            (it == DismissValue.DismissedToStart).also { confirmed ->
                if (confirmed) onAction(HistoryAction.DeleteHistory(history))
            }
        }
    )
    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        dismissThresholds = { FractionalThreshold(0.4f) },
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            )
        },
        dismissContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .clickable { onAction(HistoryAction.NavToReader(history)) }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MangaCover(
                    modifier = Modifier
                        .height(92.dp)
                        .padding(vertical = 2.dp)
                        .clickable { onAction(HistoryAction.NavToGallery(history)) },
                    cover = history.cover
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = history.title ?: history.mangaId,
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        val seen = listOf(
                            history.collectionId,
                            history.chapterName,
                            stringResource(R.string.history_card_page, history.page.plus(1))
                        ).filter { it.isNotBlank() }.joinToString("-")
                        Text(text = seen, style = MaterialTheme.typography.body2)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val date = history.date.toLocalTime()
                                .format(DateTimeFormatter.ofPattern("H:mm"))
                            Text(text = date, style = MaterialTheme.typography.body2)
                            Text(text = history.providerId, style = MaterialTheme.typography.body2)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ClearHistoryDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.dialog_clear_history)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) { Text(stringResource(R.string.dialog_clear_history_positive)) }
        }
    )
}