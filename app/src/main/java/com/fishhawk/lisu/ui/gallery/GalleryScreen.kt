package com.fishhawk.lisu.ui.gallery

import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.LoremIpsum
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.network.model.MangaDetailDto
import com.fishhawk.lisu.data.network.model.MangaState
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.ui.main.*
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.util.*
import com.fishhawk.lisu.widget.*
import com.google.accompanist.flowlayout.FlowRow
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

internal sealed interface GalleryAction {
    object NavUp : GalleryAction
    object NavToEdit : GalleryAction
    object NavToComment : GalleryAction
    data class NavToGlobalSearch(val keywords: String) : GalleryAction
    data class NavToSearch(val keywords: String) : GalleryAction
    data class NavToReader(
        val collectionId: String,
        val chapterId: String,
        val page: Int,
    ) : GalleryAction

    data class SaveCover(val cover: Drawable) : GalleryAction
    data class ShareCover(val cover: Drawable) : GalleryAction
    object EditCover : GalleryAction

    object Continue : GalleryAction
    object Reload : GalleryAction
    object Share : GalleryAction
    object AddToLibrary : GalleryAction
    object RemoveFromLibrary : GalleryAction
    data class Copy(val text: String, val hintResId: Int? = null) : GalleryAction
}

@Composable
fun GalleryScreen(navController: NavHostController) {
    val viewModel by viewModel<GalleryViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }
    val providerId = viewModel.providerId
    val manga = viewModel.manga
    val detail by viewModel.detail.collectAsState()
    val history by viewModel.history.collectAsState()

    val state = detail?.getOrNull()?.state ?: manga.state
    val cover = detail?.getOrNull()?.cover ?: manga.cover
    val title = detail?.getOrNull()?.title ?: manga.title ?: manga.id
    val authors = detail?.getOrNull()?.authors ?: manga.authors
    val isFinished = detail?.getOrNull()?.isFinished ?: manga.isFinished

    val context = LocalContext.current

    val newCoverSelectorLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val content = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                val type = context.contentResolver.getType(uri)
                if (content == null || type == null) {
                    context.toast("Image file not found.")
                } else if (!type.startsWith("image")) {
                    context.toast("Only image file can set as cover.")
                } else {
                    viewModel.updateCover(content, type)
                }
            }
        }

    val onAction: (GalleryAction) -> Unit = { action ->
        when (action) {
            GalleryAction.NavUp ->
                navController.navigateUp()
            GalleryAction.NavToEdit ->
                navController.navToGalleryEdit()
            GalleryAction.NavToComment ->
                navController.navToGalleryComment()
            is GalleryAction.NavToGlobalSearch ->
                navController.navToGlobalSearch(action.keywords)
            is GalleryAction.NavToSearch ->
                viewModel.searchBoardId?.let {
                    navController.navToProvider(providerId, it, action.keywords)
                }
            is GalleryAction.NavToReader -> detail?.getOrNull()?.let {
                context.navToReader(it, action.collectionId, action.chapterId, action.page)
            }

            is GalleryAction.SaveCover ->
                context.saveDrawable(action.cover, "$title-cover")
            is GalleryAction.ShareCover ->
                context.shareDrawable("Share cover via", action.cover, "$title-cover")
            GalleryAction.EditCover ->
                newCoverSelectorLauncher.launch("image/*")

            GalleryAction.Continue -> {
                detail?.getOrNull()?.let { detail ->
                    history?.let { history ->
                        context.navToReader(
                            detail,
                            collectionId = history.collectionId,
                            chapterId = history.chapterId,
                            page = history.page,
                        )
                    }
                    if (history == null) {
                        detail.collections.entries
                            .firstOrNull { it.value.isNotEmpty() }
                            ?.let { (collectionId, chapters) ->
                                context.navToReader(
                                    detail,
                                    collectionId = collectionId,
                                    chapterId = chapters.first().id,
                                    page = 0,
                                )
                            }
                    }
                }
            }
            GalleryAction.Reload -> viewModel.reloadManga()
            GalleryAction.Share -> context.shareText("Share manga via", title)
            GalleryAction.AddToLibrary -> viewModel.addToLibrary()
            GalleryAction.RemoveFromLibrary -> viewModel.removeFromLibrary()
            is GalleryAction.Copy -> context.copyToClipboard(action.text, action.hintResId)
        }
    }

    OnEvent(viewModel.event) {
        when (it) {
            is GalleryEffect.AddToLibraryFailure -> context.toast("Failed to add to library.")
            is GalleryEffect.RemoveFromLibraryFailure -> context.toast("Failed to remove from library.")
            GalleryEffect.UpdateCoverSuccess -> Unit
            is GalleryEffect.UpdateCoverFailure -> context.toast(R.string.cover_update_failed)
            else -> Unit
        }
    }

    LisuTransition {
        GalleryScaffold(
            state = state,
            providerId = providerId,
            cover = cover,
            title = title,
            authors = authors,
            isFinished = isFinished,
            detail = detail,
            history = history,
            onAction = onAction,
        )
    }
}

@Composable
private fun GalleryScaffold(
    state: MangaState,
    providerId: String,
    cover: String?,
    title: String,
    authors: List<String>,
    isFinished: Boolean?,
    detail: Result<MangaDetailDto>?,
    history: ReadingHistory?,
    onAction: (GalleryAction) -> Unit
) {
    val scrollState = rememberLazyListState()
    MangaDetail(
        state = state,
        providerId = providerId,
        cover = cover,
        title = title,
        authors = authors,
        isFinished = isFinished,
        detailResult = detail,
        history = history,
        scrollState = scrollState,
        onAction = onAction,
    )
    ToolBar(
        state = state,
        title = title,
        scrollState = scrollState,
        onAction = onAction,
    )
}

@Composable
private fun ToolBar(
    state: MangaState,
    title: String,
    scrollState: LazyListState,
    onAction: (GalleryAction) -> Unit,
) {
    val toolBarVisible by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
    }
    AnimatedVisibility(
        visible = toolBarVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        LisuToolBar(
            title = title,
            onNavUp = { onAction(GalleryAction.NavUp) },
        ) {
            when (state) {
                MangaState.Local -> {
                    IconButton(onClick = { onAction(GalleryAction.NavToEdit) }) {
                        Icon(
                            imageVector = LisuIcons.Edit,
                            contentDescription = stringResource(R.string.action_edit_manga)
                        )
                    }
                }
                MangaState.Remote -> {
                    IconButton(onClick = { onAction(GalleryAction.AddToLibrary) }) {
                        Icon(
                            imageVector = LisuIcons.FavoriteBorder,
                            contentDescription = stringResource(R.string.action_add_to_library)
                        )
                    }
                }
                MangaState.RemoteInLibrary -> {
                    IconButton(onClick = { onAction(GalleryAction.RemoveFromLibrary) }) {
                        Icon(
                            imageVector = LisuIcons.Favorite,
                            contentDescription = stringResource(R.string.action_remove_from_library)
                        )
                    }
                }
            }
            if (state != MangaState.Local) {
                IconButton(onClick = { onAction(GalleryAction.NavToComment) }) {
                    Icon(
                        imageVector = LisuIcons.Comment,
                        contentDescription = "Comment"
                    )
                }
            }
            IconButton(onClick = { onAction(GalleryAction.Share) }) {
                Icon(
                    imageVector = LisuIcons.Share,
                    contentDescription = stringResource(R.string.action_share_manga),
                )
            }
        }
    }
}

@Composable
private fun MangaDetail(
    state: MangaState,
    providerId: String,
    cover: String?,
    title: String,
    authors: List<String>,
    isFinished: Boolean?,
    detailResult: Result<MangaDetailDto>?,
    history: ReadingHistory?,
    scrollState: LazyListState,
    onAction: (GalleryAction) -> Unit,
) {
    val mode by PR.chapterDisplayMode.collectAsState()
    val order by PR.chapterDisplayOrder.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState,
    ) {
        item {
            MangaHeader(
                state = state,
                providerId = providerId,
                cover = cover,
                title = title,
                authors = authors,
                isFinished = isFinished,
                history = history,
                onAction = onAction,
            )
        }

        detailResult?.onSuccess { detail ->
            item {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!detail.description.isNullOrBlank()) {
                        MangaDescription(detail.description)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    MangaTagGroups(
                        detail.tags,
                        onTagClick = { onAction(GalleryAction.NavToSearch(it)) },
                        onTagLongClick = { onAction(GalleryAction.Copy(it, R.string.tag_copied)) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            mangaContent(
                mode = mode,
                order = order,
                detail = detail,
                history = history,
                onAction = onAction,
            )
        }?.onFailure {
            item {
                ErrorView(
                    throwable = it,
                    onRetry = { onAction(GalleryAction.Reload) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } ?: item {
            LoadingView(modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MangaTagGroups(
    tagGroups: Map<String, List<String>>,
    onTagClick: (String) -> Unit = {},
    onTagLongClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        tagGroups.forEach { (key, tags) ->
            Row {
                if (key.isNotBlank()) {
                    Tag(key)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                FlowRow(
                    modifier = Modifier.padding(bottom = 8.dp),
                    mainAxisSpacing = 4.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    tags.forEach { tag ->
                        val fullTag = if (key.isBlank()) tag else "$key:$tag"
                        Tag(
                            tag = tag,
                            modifier = Modifier.combinedClickable(
                                onClick = { onTagClick(fullTag) },
                                onLongClick = { onTagLongClick(fullTag) },
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Tag(
    tag: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            .compositeOver(MaterialTheme.colorScheme.surface),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 28.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun MangaDescription(description: String) {
    SelectionContainer {
        MediumEmphasis {
            Text(
                text = description,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun GalleryScaffoldPreview() {
    val detail = LoremIpsum.mangaDetail()
    LisuTheme {
        GalleryScaffold(
            state = detail.state,
            providerId = detail.providerId,
            cover = detail.cover,
            title = detail.title ?: detail.id,
            authors = detail.authors,
            isFinished = detail.isFinished,
            detail = Result.success(detail),
            history = null,
            onAction = { println(it) },
        )
    }
}