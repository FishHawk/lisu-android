package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel as hiltViewModel1

@Composable
fun HistoryScreen(navController: NavHostController) {
    Scaffold(
        topBar = { ToolBar() },
        content = { Content(navController) }
    )
}

@Composable
private fun ToolBar() {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.surface,
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = { Text(stringResource(R.string.label_history)) },
        actions = {
            val isOpen1 = remember { mutableStateOf(false) }
            IconButton(onClick = { isOpen1.value = true }) {
                Icon(Icons.Filled.FilterList, stringResource(R.string.menu_filter))
            }
            FilterSwitchDialog(isOpen1)

            val isOpen2 = remember { mutableStateOf(false) }
            IconButton(onClick = { isOpen2.value = true }) {
                Icon(Icons.Filled.ClearAll, stringResource(R.string.menu_history_clear))
            }
            ClearHistoryDialog(isOpen2)
        }
    )
}

@Composable
private fun Content(navController: NavHostController) {
    val viewModel = hiltViewModel1<HistoryViewModel>()
    val historyList by viewModel.filteredHistoryList.observeAsState(listOf())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(historyList) { HistoryCard(navController, it) }
        if (historyList.isEmpty()) item { EmptyView() }
    }
}

@Composable
private fun HistoryCard(navController: NavHostController, history: ReadingHistory) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                with(history) {
                    context.navToReaderActivity(
                        mangaId, providerId,
                        collectionIndex, chapterIndex, pageIndex
                    )
                }
            }
    ) {
        Row {
            Box(
                Modifier
                    .aspectRatio(0.75f)
                    .clickable {
                        with(history) {
                            navController.currentBackStackEntry?.arguments =
                                bundleOf(
                                    "outline" to MangaOutline(
                                        mangaId, cover, null, null,
                                        MetadataOutline(title, null, null),
                                        null
                                    ),
                                    "provider" to providerId?.let {
                                        ProviderInfo(it, it, "", "")
                                    }
                                )
                            navController.navigate("gallery/${mangaId}")
                        }
                    }) {
                Image(
                    painter = rememberCoilPainter(history.cover, fadeIn = true),
                    contentDescription = history.mangaId,
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = history.title,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    val seen = listOf(
                        history.collectionTitle,
                        history.chapterTitle,
                        stringResource(R.string.history_card_page, history.pageIndex.plus(1))
                    ).filter { it.isNotBlank() }.joinToString(" ")
                    Text(text = seen, style = MaterialTheme.typography.body2)

                    val dateFormat =
                        SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault())
                    val date = dateFormat.format(Date(history.date))
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
    val viewModel = hiltViewModel1<HistoryViewModel>()
    if (isOpen.value) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(0.8f),
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

@Composable
private fun FilterSwitchDialog(isOpen: MutableState<Boolean>) {
    if (isOpen.value) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(0.8f),
            onDismissRequest = { isOpen.value = false },
            title = { Text(text = stringResource(R.string.dialog_filter_history)) },
            text = {
                val optionEntries = stringArrayResource(R.array.settings_history_filter_entries)
                val selectedOption by GlobalPreference.historyFilter.asFlow().collectAsState(
                    GlobalPreference.historyFilter.get()
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    enumValues<GlobalPreference.HistoryFilter>().forEachIndexed { index, it ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { GlobalPreference.historyFilter.set(it) }
                        ) {
                            RadioButton(
                                selected = (it == selectedOption),
                                onClick = { GlobalPreference.historyFilter.set(it) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colors.primary
                                )
                            )
                            Text(
                                text = optionEntries[index],
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }
}
