package com.fishhawk.lisu.ui.download

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.network.model.ChapterDownloadTask
import com.fishhawk.lisu.data.network.model.MangaDownloadTask
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.MangaState
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.main.navToReader
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.widget.*
import kotlinx.parcelize.Parcelize
import org.koin.androidx.compose.viewModel

private sealed interface DownloadTaskListItem {
    val key: String

    data class Manga(
        val mangaTask: MangaDownloadTask,
    ) : DownloadTaskListItem {
        override val key
            get() = "${mangaTask.providerId}/${mangaTask.mangaId}"
    }

    data class Chapter(
        val mangaTask: MangaDownloadTask,
        val chapterTask: ChapterDownloadTask,
    ) : DownloadTaskListItem {
        override val key
            get() = "${mangaTask.providerId}/${mangaTask.mangaId}/${chapterTask.collectionId}/${chapterTask.chapterId}"
    }
}

@Parcelize
private data class MangaKey(
    val providerId: String,
    val mangaId: String,
) : Parcelable {
    constructor(mangaTask: MangaDownloadTask) : this(mangaTask.providerId, mangaTask.mangaId)
}

private fun MangaKey?.match(mangaTask: MangaDownloadTask): Boolean =
    if (this == null) false
    else mangaTask.providerId == providerId && mangaTask.mangaId == mangaId

private sealed interface DownloadAction {
    object NavUp : DownloadAction

    data class NavToGallery(
        val mangaTask: MangaDownloadTask,
    ) : DownloadAction

    data class NavToReader(
        val mangaTask: MangaDownloadTask,
        val chapterTask: ChapterDownloadTask,
    ) : DownloadAction

    object StartAll : DownloadAction
    object CancelAll : DownloadAction

    data class StartMangaTask(
        val mangaTask: MangaDownloadTask,
    ) : DownloadAction

    data class CancelMangaTask(
        val mangaTask: MangaDownloadTask,
    ) : DownloadAction

    data class StartChapterTask(
        val mangaTask: MangaDownloadTask,
        val chapterTask: ChapterDownloadTask,
    ) : DownloadAction

    data class CancelChapterTask(
        val mangaTask: MangaDownloadTask,
        val chapterTask: ChapterDownloadTask,
    ) : DownloadAction
}

@Composable
fun DownloadScreen(navController: NavHostController) {
    val viewModel by viewModel<DownloadViewModel>()
    val tasksResult by viewModel.tasks.collectAsState()

    val context = LocalContext.current

    val onAction: (DownloadAction) -> Unit = { action ->
        when (action) {
            DownloadAction.NavUp ->
                navController.navigateUp()
            is DownloadAction.NavToGallery ->
                navController.navToGallery(
                    MangaDto(
                        state = MangaState.RemoteInLibrary,
                        providerId = action.mangaTask.providerId,
                        id = action.mangaTask.mangaId,
                        cover = action.mangaTask.cover,
                        title = action.mangaTask.title,
                    )
                )
            is DownloadAction.NavToReader ->
                context.navToReader(
                    providerId = action.mangaTask.providerId,
                    mangaId = action.mangaTask.mangaId,
                    collectionId = action.chapterTask.collectionId,
                    chapterId = action.chapterTask.chapterId,
                )

            DownloadAction.StartAll ->
                viewModel.startAllTasks()
            DownloadAction.CancelAll ->
                viewModel.cancelAllTasks()
            is DownloadAction.StartMangaTask ->
                viewModel.startMangaTask(action.mangaTask)
            is DownloadAction.CancelMangaTask ->
                viewModel.cancelMangaTask(action.mangaTask)
            is DownloadAction.StartChapterTask ->
                viewModel.startChapterTask(action.mangaTask, action.chapterTask)
            is DownloadAction.CancelChapterTask ->
                viewModel.cancelChapterTask(action.mangaTask, action.chapterTask)
        }
    }

    DownloadScaffold(
        tasksResult = tasksResult,
        onAction = onAction,
    )
}

@Composable
private fun DownloadScaffold(
    tasksResult: Result<List<MangaDownloadTask>>?,
    onAction: (DownloadAction) -> Unit,
) {
    Scaffold(
        topBar = {
            LisuToolBar(
                title = stringResource(R.string.label_download),
                onNavUp = { onAction(DownloadAction.NavUp) },
            ) {
                IconButton(onClick = { onAction(DownloadAction.StartAll) }) {
                    Icon(Icons.Filled.Replay, "Recover all tasks")
                }

                var isOpen by remember { mutableStateOf(false) }
                IconButton(onClick = { isOpen = true }) {
                    Icon(Icons.Filled.Delete, "Cancel all tasks")
                    if (isOpen) {
                        LisuDialog(
                            title = "Cancel all download tasks?",
                            confirmText = stringResource(R.string.action_clear),
                            dismissText = stringResource(R.string.action_cancel),
                            onConfirm = { onAction(DownloadAction.CancelAll) },
                            onDismiss = { isOpen = false },
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            LisuTransition {
                StateView(
                    result = tasksResult,
                    onRetry = { },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) { mangaTasks, modifier ->
                    if (mangaTasks.isEmpty()) {
                        EmptyView(modifier = modifier)
                    }
                    DownloadTaskList(
                        mangaTasks = mangaTasks,
                        onAction = onAction,
                        modifier = modifier,
                    )
                }
            }
        }
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun DownloadTaskList(
    mangaTasks: List<MangaDownloadTask>,
    onAction: (DownloadAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember { mutableStateListOf<DownloadTaskListItem>() }
    var selectedManga by rememberSaveable { mutableStateOf<MangaKey?>(null) }

    LaunchedEffect(mangaTasks, selectedManga) {
        items.clear()
        mangaTasks.forEach { mangaTask ->
            items.add(
                DownloadTaskListItem.Manga(
                    mangaTask = mangaTask,
                )
            )
            if (selectedManga.match(mangaTask)) {
                items.addAll(
                    mangaTask.chapterTasks.map { chapterTask ->
                        DownloadTaskListItem.Chapter(
                            mangaTask = mangaTask,
                            chapterTask = chapterTask,
                        )
                    }
                )
            }
        }
    }

    LazyColumn(modifier = modifier) {
        items(items = items, key = { it.key }) {
            when (it) {
                is DownloadTaskListItem.Manga -> {
                    MangaDownloadTask(
                        task = it.mangaTask,
                        onClick = {
                            selectedManga =
                                if (selectedManga.match(it.mangaTask)) null
                                else MangaKey(it.mangaTask)
                        },
                        onAction = onAction,
                        modifier = Modifier.animateItemPlacement(),
                    )
                }
                is DownloadTaskListItem.Chapter -> {
                    ChapterDownloadTask(
                        mangaTask = it.mangaTask,
                        chapterTask = it.chapterTask,
                        onAction = onAction,
                        modifier = Modifier.animateItemPlacement(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MangaDownloadTask(
    task: MangaDownloadTask,
    onClick: () -> Unit,
    onAction: (DownloadAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(92.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangaCover(
            cover = task.cover,
            modifier = Modifier.fillMaxHeight(),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title ?: task.mangaId,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            )

            val displayChapterTask =
                task.chapterTasks.firstOrNull { it.state is ChapterDownloadTask.State.Downloading }
                    ?: task.chapterTasks.firstOrNull { it.state is ChapterDownloadTask.State.Waiting }
                    ?: task.chapterTasks.first()

            Row {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    val state = displayChapterTask.state
                    val size = task.chapterTasks.size
                    val chapterHint = when (state) {
                        is ChapterDownloadTask.State.Downloading ->
                            listOf(
                                task.providerId,
                                displayChapterTask.collectionId,
                                displayChapterTask.name,
                            ).filter { !it.isNullOrBlank() }
                                .joinToString(" ")
                        else -> task.providerId
                    }
                    val stateHint = when (state) {
                        is ChapterDownloadTask.State.Downloading ->
                            "${state.downloadedPageNumber ?: "-"}/${state.totalPageNumber ?: "-"}(${size})"
                        ChapterDownloadTask.State.Waiting ->
                            "Waiting(${size})"
                        is ChapterDownloadTask.State.Failed ->
                            "Failed(${size})"
                    }
                    Text(
                        text = chapterHint,
                        style = MaterialTheme.typography.body2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stateHint,
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                    )
                }
            }

            (displayChapterTask.state as? ChapterDownloadTask.State.Downloading)?.let {
                val indicatorModifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                if (
                    it.downloadedPageNumber != null &&
                    it.totalPageNumber != null
                ) {
                    LinearProgressIndicator(
                        progress = it.downloadedPageNumber.toFloat() / it.totalPageNumber,
                        modifier = indicatorModifier,
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = indicatorModifier,
                    )
                }
            }
        }

        MenuButton {
            DropdownMenuItem(
                onClick = { onAction(DownloadAction.NavToGallery(task)) },
            ) {
                Text("Open gallery")
            }
            DropdownMenuItem(
                onClick = { onAction(DownloadAction.StartMangaTask(task)) },
            ) {
                Text("Recover all tasks")
            }
            DropdownMenuItem(
                onClick = { onAction(DownloadAction.CancelMangaTask(task)) },
            ) {
                Text("Cancel all tasks")
            }
        }
    }
}

@Composable
private fun ChapterDownloadTask(
    mangaTask: MangaDownloadTask,
    chapterTask: ChapterDownloadTask,
    onAction: (DownloadAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .padding(start = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val chapterHint = listOf(
                chapterTask.collectionId,
                chapterTask.name,
                chapterTask.title,
            ).filter { !it.isNullOrBlank() }
                .joinToString(" ")

            val stateHint = when (val state = chapterTask.state) {
                is ChapterDownloadTask.State.Downloading -> "Downloading"
                ChapterDownloadTask.State.Waiting -> "Waiting"
                is ChapterDownloadTask.State.Failed -> "Failed:${state.errorMessage}"
            }

            CompositionLocalProvider(
                LocalContentAlpha provides
                        if (chapterHint.isBlank()) ContentAlpha.medium
                        else ContentAlpha.high
            ) {
                Text(
                    text = chapterHint.ifBlank { "<no title>" },
                    style = MaterialTheme.typography.caption,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = stateHint,
                    style = MaterialTheme.typography.caption,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }

        MenuButton {
            DropdownMenuItem(
                onClick = { onAction(DownloadAction.NavToReader(mangaTask, chapterTask)) },
            ) {
                Text("Read")
            }
            DropdownMenuItem(
                enabled = chapterTask.state is ChapterDownloadTask.State.Failed,
                onClick = { onAction(DownloadAction.StartChapterTask(mangaTask, chapterTask)) },
            ) {
                Text("Recover task")
            }
            DropdownMenuItem(
                onClick = { onAction(DownloadAction.CancelChapterTask(mangaTask, chapterTask)) },
            ) {
                Text("Cancel task")
            }
        }
    }
}

@Composable
private fun MenuButton(
    content: @Composable ColumnScope.() -> Unit,
) {
    Box {
        var expanded by remember { mutableStateOf(false) }
        Icon(
            imageVector = LisuIcons.MoreVert,
            contentDescription = null,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false, radius = 16.dp),
                ) {
                    expanded = !expanded
                },
            tint = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            content = content,
        )
    }
}

@Preview
@Composable
private fun DownloadScaffoldPreview() {
    fun dummyChapterTaskList(): List<ChapterDownloadTask> {
        return listOf(
            ChapterDownloadTask(
                collectionId = "",
                chapterId = "",
                name = null,
                title = null,
                state = ChapterDownloadTask.State.Downloading(
                    downloadedPageNumber = 20,
                    totalPageNumber = 100,
                ),
            )
        )
    }

    fun dummyChapterTaskList(size: Int): List<ChapterDownloadTask> {
        return (1..size).map {
            val state = when (it) {
                1 -> ChapterDownloadTask.State.Downloading(
                    downloadedPageNumber = 20,
                    totalPageNumber = 100,
                )
                3 -> ChapterDownloadTask.State.Failed(
                    downloadedPageNumber = 20,
                    totalPageNumber = 100,
                    errorMessage = "Unknown error long long long long long long long."
                )
                else -> ChapterDownloadTask.State.Waiting
            }
            ChapterDownloadTask(
                collectionId = "连载",
                chapterId = it.toString(),
                name = "第${it}话",
                title = "标题",
                state = state,
            )
        }
    }

    fun dummyMangaTaskList(): List<MangaDownloadTask> {
        return (1..100).map {
            MangaDownloadTask(
                providerId = "漫画人",
                mangaId = it.toString(),
                cover = "https://api.lorem.space/image/book",
                title = "龙珠",
                chapterTasks = when (it) {
                    1 -> dummyChapterTaskList()
                    2 -> dummyChapterTaskList(100)
                    else -> dummyChapterTaskList(it.coerceAtMost(10))
                },
            )
        }
    }
    LisuTheme {
        LisuModalBottomSheetLayout {
            DownloadScaffold(
                tasksResult = Result.success(dummyMangaTaskList()),
                onAction = { println(it) }
            )
        }
    }
}