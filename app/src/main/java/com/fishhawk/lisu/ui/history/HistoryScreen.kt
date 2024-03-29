package com.fishhawk.lisu.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.main.navToReader
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.util.readableString
import com.fishhawk.lisu.widget.*
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private sealed interface HistoryAction {
    data class NavToGallery(val history: ReadingHistory) : HistoryAction
    data class NavToReader(val history: ReadingHistory) : HistoryAction
    data class DeleteHistory(val history: ReadingHistory) : HistoryAction
    object ClearHistory : HistoryAction
}

@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val histories by viewModel.histories.collectAsState()

    val onAction: (HistoryAction) -> Unit = { action ->
        when (action) {
            is HistoryAction.NavToGallery -> with(action.history) {
                navController.navToGallery(MangaDto(
                    state = state,
                    providerId = providerId,
                    id = mangaId,
                    cover = cover,
                    title = title,
                    authors = authors?.let { listOf(it) } ?: emptyList(),
                ))
            }

            is HistoryAction.NavToReader -> with(action.history) {
                context.navToReader(
                    providerId = providerId,
                    mangaId = mangaId,
                    collectionId = collectionId,
                    chapterId = chapterId,
                    page = page,
                )
            }

            is HistoryAction.DeleteHistory -> viewModel.deleteHistory(action.history)
            HistoryAction.ClearHistory -> viewModel.clearHistory()
        }
    }

    HistoryScaffold(
        histories = histories,
        onAction = onAction,
    )
}

@Composable
private fun HistoryScaffold(
    histories: Map<LocalDate, List<ReadingHistory>>,
    onAction: (HistoryAction) -> Unit,
) {
    LisuScaffold(
        topBar = {
            LisuToolBar(title = stringResource(R.string.label_history)) {
                var isOpen by remember { mutableStateOf(false) }
                TooltipIconButton(
                    tooltip = stringResource(R.string.action_clear_history),
                    icon = LisuIcons.ClearAll,
                    onClick = { isOpen = true },
                )
                if (isOpen) {
                    LisuDialog(
                        title = stringResource(R.string.dialog_clear_history),
                        confirmText = stringResource(R.string.action_clear),
                        dismissText = stringResource(R.string.action_cancel),
                        onConfirm = { onAction(HistoryAction.ClearHistory) },
                        onDismiss = { isOpen = false },
                    )
                }
            }
        },
        content = { paddingValues ->
            LisuTransition {
                HistoryList(
                    histories = histories,
                    onAction = onAction,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryList(
    histories: Map<LocalDate, List<ReadingHistory>>,
    onAction: (HistoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (histories.isEmpty()) {
        EmptyView(modifier = modifier)
    }
    LazyColumn(modifier = modifier) {
        histories.forEach { (date, list) ->
            item(key = date) {
                LisuListHeader(
                    text = date.readableString(),
                    modifier = Modifier.animateItemPlacement(),
                )
            }
            items(list, key = { Pair(it.providerId, it.mangaId) }) {
                HistoryListItem(
                    history = it,
                    onAction = onAction,
                    modifier = Modifier.animateItemPlacement(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryListItem(
    history: ReadingHistory,
    onAction: (HistoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            (it == DismissValue.DismissedToEnd).also { confirmed ->
                if (confirmed) onAction(HistoryAction.DeleteHistory(history))
            }
        }
    )
    SwipeToDismiss(
        state = dismissState,
        modifier = modifier,
        directions = setOf(DismissDirection.StartToEnd),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
            )
        },
        dismissContent = {
            LisuListItem(
                leadingContent = {
                    MangaCard(
                        cover = history.cover,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { onAction(HistoryAction.NavToGallery(history)) },
                    )
                },
                headlineText = {
                    Text(
                        text = history.title ?: history.mangaId,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                    )
                },
                modifier = Modifier.clickable {
                    onAction(HistoryAction.NavToReader(history))
                },
                overlineText = {
                    Text(
                        text = history.providerId,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                supportingText = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val time = history.date.toLocalTime()
                            .format(DateTimeFormatter.ofPattern(stringResource(R.string.history_time_format)))
                        Text(text = time)

                        val seen = listOf(
                            history.collectionId,
                            history.chapterName,
                        ).filter { it.isNotBlank() }.joinToString(" ")
                        if (seen.isNotBlank()) {
                            Text(
                                text = seen,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                    }
                }
            )
        }
    )
}