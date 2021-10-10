package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDto
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.base.MangaCover
import com.fishhawk.driftinglibraryandroid.ui.reader.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private typealias HistoryActionHandler = (HistoryAction) -> Unit

private sealed interface HistoryAction {
    data class NavToGallery(val history: ReadingHistory) : HistoryAction
    data class NavToReader(val history: ReadingHistory) : HistoryAction

    data class Delete(val history: ReadingHistory) : HistoryAction
    object Clear : HistoryAction
}

@Composable
fun HistoryScreen(navController: NavHostController) {
    val context = LocalContext.current

    val viewModel = hiltViewModel<HistoryViewModel>()
    val histories by viewModel.histories.collectAsState()
    val onAction: HistoryActionHandler = { action ->
        when (action) {
            is HistoryAction.NavToGallery -> with(action.history) {
                navController.currentBackStackEntry?.arguments =
                    bundleOf(
                        "manga" to MangaDto(
                            providerId = providerId,
                            id = mangaId,
                            cover = cover,
                            title = title,
                            authors = authors?.let { listOf(it) }
                        )
                    )
                navController.navigate("gallery/${mangaId}")
            }
            is HistoryAction.NavToReader -> with(action.history) {
                context.navToReaderActivity(
                    mangaId, providerId,
                    collectionId, chapterId, page
                )
            }
            is HistoryAction.Delete -> viewModel.delete(action.history)
            HistoryAction.Clear -> viewModel.clear()
        }
    }

    Scaffold(
        topBar = { ToolBar(onAction) },
        content = { ApplicationTransition { HistoryList(histories, onAction) } }
    )
}

@Composable
private fun ToolBar(onAction: HistoryActionHandler) {
    ApplicationToolBar(stringResource(R.string.label_history)) {
        val isOpen = remember { mutableStateOf(false) }
        IconButton(onClick = { isOpen.value = true }) {
            Icon(Icons.Filled.ClearAll, stringResource(R.string.menu_history_clear))
        }
        ClearHistoryDialog(isOpen, onAction)
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
                if (confirmed) onAction(HistoryAction.Delete(history))
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
    isOpen: MutableState<Boolean>,
    onAction: HistoryActionHandler
) {
    if (!isOpen.value) return
    AlertDialog(
        onDismissRequest = { isOpen.value = false },
        title = { Text(text = stringResource(R.string.dialog_clear_history)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onAction(HistoryAction.Clear)
                    isOpen.value = false
                }
            ) { Text(stringResource(R.string.dialog_clear_history_positive)) }
        }
    )
}