package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.remote.model.*
import com.fishhawk.driftinglibraryandroid.ui.activity.setArgument
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.fishhawk.driftinglibraryandroid.ui.theme.MaterialColors

internal typealias GalleryActionHandler = (GalleryAction) -> Unit

internal sealed interface GalleryAction {
    object NavUp : GalleryAction
    object NavToEdit : GalleryAction
    data class NavToGlobalSearch(val keywords: String) : GalleryAction
    data class NavToSearch(val keywords: String) : GalleryAction
    data class NavToReader(
        val collectionIndex: Int,
        val chapterIndex: Int,
        val pageIndex: Int
    ) : GalleryAction

    object ShareCover : GalleryAction
    object SaveCover : GalleryAction
    object EditCover : GalleryAction

    object Reload : GalleryAction
    object Share : GalleryAction
    object AddToLibrary : GalleryAction
    object RemoveFromLibrary : GalleryAction
    data class Copy(val text: String, val hintResId: Int? = null) : GalleryAction
}

@Composable
fun GalleryScreen(navController: NavHostController) {
    navController.setArgument<MangaOutline>("outline")
    navController.setArgument<Provider>("provider")

    val context = LocalContext.current

    val viewModel = hiltViewModel<GalleryViewModel>()
    val viewState by viewModel.viewState.collectAsState()
    val detail by viewModel.detail.collectAsState()
    val history by viewModel.history.collectAsState()

    val onAction: GalleryActionHandler = { action ->
        when (action) {
            GalleryAction.NavUp -> navController.navigateUp()
            GalleryAction.NavToEdit -> navController.navigate("edit")
            is GalleryAction.NavToGlobalSearch -> {
                navController.currentBackStackEntry?.arguments =
                    bundleOf("keywords" to action.keywords)
                navController.navigate("global-search")
            }
            is GalleryAction.NavToSearch -> {
                navController.currentBackStackEntry?.arguments =
                    bundleOf(
                        "keywords" to action.keywords,
                        "provider" to detail.provider
                    )
                if (detail.provider == null) navController.navigate("library-search")
                else navController.navigate("search/${detail.provider!!.name}")
            }
            is GalleryAction.NavToReader -> {
                action.apply {
                    context.navToReaderActivity(
                        detail,
                        collectionIndex,
                        chapterIndex,
                        pageIndex
                    )
                }
            }

            GalleryAction.ShareCover -> {
                if (viewState is ViewState.Loaded) {
                    context.toast(R.string.toast_manga_not_loaded)
                } else {
                    val url = detail.cover
                    if (url == null) context.toast(R.string.toast_manga_no_cover)
                    else context.shareImage(url, "${detail.id}-cover")
                }
            }
            GalleryAction.SaveCover -> {
                if (viewState is ViewState.Loaded) {
                    context.toast(R.string.toast_manga_not_loaded)
                } else {
                    val url = detail.cover
                    if (url == null) context.toast(R.string.toast_manga_no_cover)
                    else context.saveImage(url, "${detail.id}-cover")
                }
            }
            GalleryAction.EditCover -> {
//        val context = LocalContext.current
//            val newCover = remember { mutableStateOf<Uri?>(null) }
//            val launcher = rememberLauncherForActivityResult(
//                ActivityResultContracts.GetContent()
//            ) { newCover.value = it }

//            newCover.value?.let {
//                val content = context.contentResolver.openInputStream(it)?.readBytes()
//                val type = context.contentResolver.getType(it)?.toMediaTypeOrNull()
//                if (content != null && type != null)
//                    viewModel.updateCover(content.toRequestBody(type))
//            }
//                            override fun onEditCover() {
//                                if (viewModel.isRefreshing.value)
//                                    return context.toast(R.string.toast_manga_not_loaded)
//                                launcher.launch("test")
//                            }
            }

            GalleryAction.Reload -> viewModel.reloadManga()
            GalleryAction.Share -> Unit
            GalleryAction.AddToLibrary -> Unit
            GalleryAction.RemoveFromLibrary -> Unit
            is GalleryAction.Copy -> context.copyToClipboard(action.text, action.hintResId)
        }
    }

    ApplicationTransition {
        val scrollState = rememberScrollState()
        MangaDetail(viewState, detail, history, scrollState, onAction)
        ToolBar(detail.title, detail.provider != null, scrollState, onAction)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ToolBar(
    title: String,
    isFromProvider: Boolean,
    scrollState: ScrollState,
    onAction: GalleryActionHandler
) {
    val toolBarVisibleState = remember { MutableTransitionState(false) }
    val mangaHeaderHeightPx = with(LocalDensity.current) { MangaHeaderHeight.toPx() }
    toolBarVisibleState.targetState = scrollState.value > mangaHeaderHeightPx
    AnimatedVisibility(
        visibleState = toolBarVisibleState,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ApplicationToolBar(
            title = title,
            onNavigationIconClick = { onAction(GalleryAction.NavUp) }
        ) {
            if (isFromProvider) {
                IconButton(onClick = { onAction(GalleryAction.Share) }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "share")
                }
            } else {
                IconButton(onClick = { onAction(GalleryAction.Share) }) {
                    Icon(Icons.Default.Edit, contentDescription = "share")
                }
            }
            IconButton(onClick = { onAction(GalleryAction.Share) }) {
                Icon(Icons.Default.Share, contentDescription = "share")
            }
        }
    }
}

@Composable
private fun MangaActionButton(modifier: Modifier) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Column(
            modifier = modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FavoriteBorder,
                modifier = Modifier.size(24.dp),
                contentDescription = "add to library"
            )
            Text("Add to library", style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
private fun MangaDetail(
    viewState: ViewState,
    detail: MangaDetail,
    history: ReadingHistory?,
    scrollState: ScrollState,
    onAction: GalleryActionHandler
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        MangaHeader(detail, onAction)
        StateView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            viewState = viewState,
            onRetry = { onAction(GalleryAction.Reload) }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                detail.source?.let { MangaSource(it) }
                if (!detail.metadata.description.isNullOrBlank()) {
                    MangaDescription(detail.metadata.description)
                }
                detail.metadata.tags?.let { tags ->
                    MangaTagGroups(tags,
                        onTagClick = { onAction(GalleryAction.NavToSearch(it)) },
                        onTagLongClick = {
                            onAction(
                                GalleryAction.Copy(
                                    it,
                                    R.string.toast_manga_tag_saved
                                )
                            )
                        }
                    )
                }
                MangaContent(detail, history, onAction)
            }
        }
    }
}

@Composable
private fun MangaSource(source: Source) {
    Text(
        text = "From ${source.providerId} - ${source.mangaId} ${source.state}",
        color = when (source.state) {
            SourceState.DOWNLOADING -> MaterialColors.Blue400
            SourceState.WAITING -> MaterialColors.Green400
            SourceState.ERROR -> MaterialTheme.colors.error
            SourceState.UPDATED -> MaterialTheme.colors.onSurface
        }
    )
}

@Composable
private fun MangaDescription(description: String) {
    SelectionContainer {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = description,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

