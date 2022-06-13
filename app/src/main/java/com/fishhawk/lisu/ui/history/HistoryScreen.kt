package com.fishhawk.lisu.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.MangaCover
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.main.navToReader
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.EmptyView
import com.fishhawk.lisu.ui.widget.LisuDialog
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.util.toDisplayString
import org.koin.androidx.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    val viewModel by viewModel<HistoryViewModel>()
    val histories by viewModel.histories.collectAsState()

    val onAction: HistoryActionHandler = { action ->
        when (action) {
            is HistoryAction.NavToGallery -> with(action.history) {
                navController.navToGallery(MangaDto(
                    providerId = providerId,
                    id = mangaId,
                    cover = cover,
                    title = title,
                    authors = authors?.let { listOf(it) } ?: emptyList()
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
        content = { paddingValues ->
            LisuTransition {
                HistoryList(
                    histories,
                    onAction,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    )
}

@Composable
private fun ToolBar(onAction: HistoryActionHandler) {
    LisuToolBar(title = stringResource(R.string.label_history)) {
        var isOpen by remember { mutableStateOf(false) }
        IconButton(onClick = { isOpen = true }) {
            Icon(LisuIcons.ClearAll, stringResource(R.string.action_clear_history))
            if (isOpen) {
                LisuDialog(
                    title = stringResource(R.string.dialog_clear_history),
                    confirmText = stringResource(R.string.action_clear),
                    onConfirm = { onAction(HistoryAction.ClearHistory) },
                    onDismiss = { isOpen = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryList(
    histories: Map<LocalDate, List<ReadingHistory>>,
    onAction: HistoryActionHandler,
    modifier: Modifier = Modifier
) {
    if (histories.isEmpty()) EmptyView()
    LazyColumn(modifier = modifier) {
        histories.forEach { (date, list) ->
            item(key = date) {
                HistoryListHeader(
                    date = date,
                    modifier = Modifier.animateItemPlacement()
                )
            }
            items(list, key = { Pair(it.providerId, it.mangaId) }) {
                HistoryListItem(
                    history = it,
                    modifier = Modifier.animateItemPlacement(),
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
private fun HistoryListHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = date.toDisplayString(),
            modifier = modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HistoryListItem(
    history: ReadingHistory,
    modifier: Modifier = Modifier,
    onAction: HistoryActionHandler = {}
) {
    val dismissState = rememberDismissState(
        confirmStateChange = {
            (it == DismissValue.DismissedToEnd).also { confirmed ->
                if (confirmed) onAction(HistoryAction.DeleteHistory(history))
            }
        }
    )
    SwipeToDismiss(
        state = dismissState,
        modifier = modifier,
        directions = setOf(DismissDirection.StartToEnd),
        dismissThresholds = { FractionalThreshold(0.4f) },
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
                .clickable { onAction(HistoryAction.NavToReader(history)) }
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MangaCover(
                cover = history.cover,
                modifier = Modifier
                    .height(92.dp)
                    .padding(vertical = 2.dp)
                    .clickable { onAction(HistoryAction.NavToGallery(history)) }
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = history.title ?: history.mangaId,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    ProvideTextStyle(value = MaterialTheme.typography.body2) {
                        val seen = listOf(
                            history.collectionId,
                            history.chapterName,
                            stringResource(R.string.history_page_n, history.page + 1)
                        ).filter { it.isNotBlank() }.joinToString("-")
                        Text(text = seen)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val time = history.date.toLocalTime()
                                .format(DateTimeFormatter.ofPattern(stringResource(R.string.history_time_format)))
                            Text(text = time)
                            Text(text = history.providerId)
                        }
                    }
                }
            }
        }
    }
}