package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.FilterList
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
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.datastore.HistoryFilter
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.base.MangaCover
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.more.ListPreferenceDialog
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

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
        val isOpen1 = remember { mutableStateOf(false) }
        IconButton(onClick = { isOpen1.value = true }) {
            Icon(Icons.Filled.FilterList, stringResource(R.string.menu_filter))
        }
        ListPreferenceDialog(
            isOpen = isOpen1,
            title = stringResource(R.string.dialog_filter_history),
            preference = PR.historyFilter
        ) {
            when (it) {
                HistoryFilter.All -> R.string.settings_history_filter_all
                HistoryFilter.FromLibrary -> R.string.settings_history_filter_from_library
                HistoryFilter.FromProvider -> R.string.settings_history_filter_from_provider
            }
        }

        val isOpen2 = remember { mutableStateOf(false) }
        IconButton(onClick = { isOpen2.value = true }) {
            Icon(Icons.Filled.ClearAll, stringResource(R.string.menu_history_clear))
        }
        ClearHistoryDialog(isOpen2)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HistoryList(navController: NavHostController) {
    val viewModel = hiltViewModel<HistoryViewModel>()
    val historyList by viewModel.historyList.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (historyList.isEmpty()) item { EmptyView() }
        historyList.forEach { (then, list) ->
            item { HistoryListHeader(then) }
            items(list, { Pair(it.providerId, it.mangaId) }) {
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
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@Composable
private fun HistoryListItem(navController: NavHostController, history: ReadingHistory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable { navController.navToReader(history) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MangaCover(
            modifier = Modifier.clickable { navController.navToGallery(history) },
            cover = history.cover
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = history.title,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                val seen = listOf(
                    history.collectionTitle,
                    history.chapterTitle,
                    stringResource(R.string.history_card_page, history.pageIndex.plus(1))
                ).filter { it.isNotBlank() }.joinToString("-")
                Text(text = seen, style = MaterialTheme.typography.body2)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val date =
                        Date(history.date).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                            .format(DateTimeFormatter.ofPattern("H:mm"))
                    Text(text = date, style = MaterialTheme.typography.body2)
                    history.providerId?.let {
                        Text(text = it, style = MaterialTheme.typography.body2)
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
                        viewModel.clearReadingHistory()
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
        mangaId, providerId,
        collectionIndex, chapterIndex, pageIndex
    )
}

private fun NavHostController.navToGallery(history: ReadingHistory) = with(history) {
    currentBackStackEntry?.arguments =
        bundleOf(
            "outline" to MangaOutline(
                mangaId, cover, null, null,
                MetadataOutline(title, null, null),
                null
            ),
            "provider" to providerId?.let {
                Provider(it, "", "")
            }
        )
    navigate("gallery/${mangaId}")
}