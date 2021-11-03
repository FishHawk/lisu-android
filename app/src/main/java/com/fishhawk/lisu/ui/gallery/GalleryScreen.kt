package com.fishhawk.lisu.ui.gallery

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.ProviderDto
import com.fishhawk.lisu.ui.*
import com.fishhawk.lisu.ui.base.*
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition

internal typealias GalleryActionHandler = (GalleryAction) -> Unit

internal sealed interface GalleryAction {
    object NavUp : GalleryAction
    object NavToEdit : GalleryAction
    data class NavToGlobalSearch(val keywords: String) : GalleryAction
    data class NavToSearch(val keywords: String) : GalleryAction
    data class NavToReader(
        val collectionId: String,
        val chapterId: String,
        val page: Int
    ) : GalleryAction

    data class ShareCover(val cover: Drawable) : GalleryAction
    data class SaveCover(val cover: Drawable) : GalleryAction
    object EditCover : GalleryAction

    object Reload : GalleryAction
    object Share : GalleryAction
    object AddToLibrary : GalleryAction
    object RemoveFromLibrary : GalleryAction
    data class Copy(val text: String, val hintResId: Int? = null) : GalleryAction
}

@Composable
fun GalleryScreen(navController: NavHostController) {
    navController.setArgument<MangaDto>("manga")
    navController.setArgument<ProviderDto>("provider")

    val context = LocalContext.current

    val viewModel = hiltViewModel<GalleryViewModel>()
    val viewState by viewModel.viewState.collectAsState()
    val detail by viewModel.detail.collectAsState()
    val history by viewModel.history.collectAsState()

    val onAction: GalleryActionHandler = { action ->
        when (action) {
            GalleryAction.NavUp ->
                navController.navigateUp()
            GalleryAction.NavToEdit ->
                navController.navToGalleryEdit()
            is GalleryAction.NavToGlobalSearch ->
                navController.navToGlobalSearch(action.keywords)
            is GalleryAction.NavToSearch ->
                navController.navToProviderSearch(detail.providerId, action.keywords)
            is GalleryAction.NavToReader -> with(action) {
                context.navToReader(detail, collectionId, chapterId, page)
            }

            is GalleryAction.ShareCover ->
                context.shareImage("Share cover via",action.cover, "${detail.title ?: detail.id}-cover")
            is GalleryAction.SaveCover ->
                context.saveImage(action.cover, "${detail.title ?: detail.id}-cover")
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
            GalleryAction.Share ->
                context.shareText("Share manga via", detail.title ?: detail.id)
            GalleryAction.AddToLibrary -> viewModel.addToLibrary()
            GalleryAction.RemoveFromLibrary -> viewModel.removeFromLibrary()
            is GalleryAction.Copy -> context.copyToClipboard(action.text, action.hintResId)
        }
    }

    LisuTransition {
        val scrollState = rememberScrollState()
        MangaDetail(viewState, detail, history, scrollState, onAction)
        ToolBar(detail.title ?: detail.id, detail.inLibrary, scrollState, onAction)
    }
}

@Composable
private fun ToolBar(
    title: String,
    inLibrary: Boolean,
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
        LisuToolBar(
            title = title,
            onNavUp = { onAction(GalleryAction.NavUp) }
        ) {
            if (inLibrary) {
                IconButton(onClick = { onAction(GalleryAction.RemoveFromLibrary) }) {
                    Icon(LisuIcons.Favorite, contentDescription = "remove from library")
                }
            } else {
                IconButton(onClick = { onAction(GalleryAction.AddToLibrary) }) {
                    Icon(LisuIcons.FavoriteBorder, contentDescription = "add to library")
                }
            }
            IconButton(onClick = { onAction(GalleryAction.Share) }) {
                Icon(LisuIcons.Share, contentDescription = "share")
            }
        }
    }
}

@Composable
private fun MangaDetail(
    viewState: ViewState,
    detail: MangaDetailDto,
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
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row {
                    if (detail.inLibrary)
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.primary) {
                            MangaActionButton(
                                icon = LisuIcons.Favorite,
                                text = "In library"
                            ) { onAction(GalleryAction.RemoveFromLibrary) }
                        }
                    else CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        MangaActionButton(
                            icon = LisuIcons.FavoriteBorder,
                            text = "Add to library"
                        ) { onAction(GalleryAction.AddToLibrary) }
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        MangaActionButton(
                            icon = LisuIcons.AutoStories,
                            text = if (history == null) "Read" else "Continue"
                        ) {
                            onAction(
                                GalleryAction.NavToReader(
                                    collectionId = history?.collectionId
                                        ?: detail.collections?.keys?.first() ?: " ",
                                    chapterId = history?.chapterId
                                        ?: detail.collections?.values?.first()?.first()?.id
                                        ?: detail.chapters?.first()?.id
                                        ?: " ",
                                    page = history?.page ?: 0
                                )
                            )
                        }
                    }
                }

                if (!detail.description.isNullOrBlank()) {
                    MangaDescription(detail.description)
                }
                detail.tags?.let { tags ->
                    MangaTagGroups(tags,
                        onTagClick = { onAction(GalleryAction.NavToSearch(it)) },
                        onTagLongClick = {
                            onAction(GalleryAction.Copy(it, R.string.toast_manga_tag_saved))
                        }
                    )
                }
                MangaContent(detail, history, onAction)
            }
        }
    }
}

@Composable
private fun RowScope.MangaActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            modifier = Modifier.size(24.dp),
            contentDescription = text
        )
        Text(text, style = MaterialTheme.typography.caption)
    }
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

