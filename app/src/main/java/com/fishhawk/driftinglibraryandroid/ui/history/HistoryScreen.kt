package com.fishhawk.driftinglibraryandroid.ui.history

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataOutline
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.base.MangaCover
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun HistoryScreen(navController: NavHostController) {
    Scaffold(
        topBar = { ToolBar() },
        content = { ApplicationTransition { HistoryList(navController) } }
    )
}

@Composable
private fun ToolBar() {
    ApplicationToolBar(stringResource(R.string.label_history)) {
        val isOpen = remember { mutableStateOf(false) }
        IconButton(onClick = { isOpen.value = true }) {
            Icon(Icons.Filled.ClearAll, stringResource(R.string.menu_history_clear))
        }
        ClearHistoryDialog(isOpen)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HistoryList(navController: NavHostController) {
    val viewModel = hiltViewModel<HistoryViewModel>()
    val historyList by viewModel.historyList.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (historyList.isEmpty()) item { EmptyView() }
        historyList.forEach { (then, list) ->
            item { HistoryListHeader(then) }
            items(list, { Pair(it.provider?.name, it.mangaId) }) {
                HistoryListItem(navController, it)
            }
        }
    }
}

@Composable
private fun HistoryListHeader(then: LocalDate) {
    val now = LocalDate.now()
    val days = ChronoUnit.DAYS.between(then, now)
    val dateString = when {
        days == 0L -> stringResource(R.string.history_today)
        days == 1L -> stringResource(R.string.history_yesterday)
        days <= 5L -> stringResource(R.string.history_n_days_ago).format(days)
        else -> then.format(DateTimeFormatter.ofPattern(stringResource(R.string.history_date_format)))
    }
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = dateString,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@Composable
private fun HistoryListItem(navController: NavHostController, history: ReadingHistory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navToReader(history) }
            .padding(horizontal = 8.dp)
            .height(88.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MangaCover(
            modifier = Modifier.clickable { navController.navToGallery(history) },
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
                    history.collection,
                    history.chapterName,
                    stringResource(R.string.history_card_page, history.page.plus(1))
                ).filter { it.isNotBlank() }.joinToString("-")
                Text(text = seen, style = MaterialTheme.typography.body2)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val date = history.date.toLocalTime()
                        .format(DateTimeFormatter.ofPattern("H:mm"))
                    Text(text = date, style = MaterialTheme.typography.body2)
                    history.provider?.let {
                        Text(text = it.name, style = MaterialTheme.typography.body2)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearHistoryDialog(isOpen: MutableState<Boolean>) {
    val viewModel = hiltViewModel<HistoryViewModel>()
    if (isOpen.value) {
        AlertDialog(
            onDismissRequest = { isOpen.value = false },
            title = { Text(text = stringResource(R.string.dialog_clear_history)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clear()
                        isOpen.value = false
                    }) {
                    Text(stringResource(R.string.dialog_clear_history_positive))
                }
            }
        )
    }
}

private fun NavHostController.navToReader(history: ReadingHistory) = with(history) {
    context.navToReaderActivity(
        mangaId, provider?.name,
        collection, chapterId, page
    )
}

private fun NavHostController.navToGallery(history: ReadingHistory) = with(history) {
    currentBackStackEntry?.arguments =
        bundleOf(
            "outline" to MangaOutline(
                mangaId, cover, null, null,
                MetadataOutline(title, authors = authors?.let { listOf(it) }, null),
                null
            ),
            "provider" to provider
        )
    navigate("gallery/${mangaId}")
}