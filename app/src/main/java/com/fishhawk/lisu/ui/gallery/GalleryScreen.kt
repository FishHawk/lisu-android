package com.fishhawk.lisu.ui.gallery

import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.data.remote.model.MangaState
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.ui.main.*
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.ui.widget.StateView
import com.fishhawk.lisu.ui.widget.ViewState
import com.fishhawk.lisu.util.*
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

internal typealias GalleryActionHandler = (GalleryAction) -> Unit

internal sealed interface GalleryAction {
    object NavUp : GalleryAction
    object NavToEdit : GalleryAction
    object NavToComment : GalleryAction
    data class NavToGlobalSearch(val keywords: String) : GalleryAction
    data class NavToSearch(val keywords: String) : GalleryAction
    data class NavToReader(
        val collectionId: String,
        val chapterId: String,
        val page: Int
    ) : GalleryAction

    data class SaveCover(val cover: Drawable) : GalleryAction
    data class ShareCover(val cover: Drawable) : GalleryAction
    object EditCover : GalleryAction

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
    val viewState by viewModel.viewState.collectAsState()
    val detail by viewModel.detail.collectAsState()
    val history by viewModel.history.collectAsState()

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

    val onAction: GalleryActionHandler = { action ->
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
                navController.navToProviderSearch(detail.providerId, action.keywords)
            is GalleryAction.NavToReader -> with(action) {
                context.navToReader(detail, collectionId, chapterId, page)
            }

            is GalleryAction.SaveCover ->
                context.saveImage(action.cover, "${detail.title ?: detail.id}-cover")
            is GalleryAction.ShareCover ->
                context.shareImage(
                    "Share cover via",
                    action.cover,
                    "${detail.title ?: detail.id}-cover"
                )
            GalleryAction.EditCover ->
                newCoverSelectorLauncher.launch("image/*")

            GalleryAction.Reload -> viewModel.reloadManga()
            GalleryAction.Share -> context.shareText("Share manga via", detail.title ?: detail.id)
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
        val scrollState = rememberScrollState()
        MangaDetail(viewState, detail, history, scrollState, onAction)
        ToolBar(detail.title ?: detail.id, detail.state, scrollState, onAction)
    }
}

@Composable
private fun ToolBar(
    title: String,
    state: MangaState,
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
            when (state) {
                MangaState.Local -> {
                    IconButton(onClick = { onAction(GalleryAction.NavToEdit) }) {
                        Icon(LisuIcons.Edit, stringResource(R.string.action_edit_manga))
                    }
                }
                MangaState.Remote -> {
                    IconButton(onClick = { onAction(GalleryAction.AddToLibrary) }) {
                        Icon(
                            LisuIcons.FavoriteBorder,
                            stringResource(R.string.action_add_to_library)
                        )
                    }
                }
                MangaState.RemoteInLibrary -> {
                    IconButton(onClick = { onAction(GalleryAction.RemoveFromLibrary) }) {
                        Icon(
                            LisuIcons.Favorite,
                            stringResource(R.string.action_remove_from_library)
                        )
                    }
                }
            }
            if (state != MangaState.Local) {
                IconButton(onClick = { onAction(GalleryAction.NavToComment) }) {
                    Icon(
                        LisuIcons.Comment,
                        "Comment"
                    )
                }
            }
            IconButton(onClick = { onAction(GalleryAction.Share) }) {
                Icon(LisuIcons.Share, stringResource(R.string.action_share_manga))
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
        MangaHeader(detail, history, onAction)
        StateView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            viewState = viewState,
            onRetry = { onAction(GalleryAction.Reload) }
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!detail.description.isNullOrBlank()) {
                    MangaDescription(detail.description)
                }
                detail.tags?.let { tags ->
                    MangaTagGroups(tags,
                        onTagClick = { onAction(GalleryAction.NavToSearch(it)) },
                        onTagLongClick = {
                            onAction(GalleryAction.Copy(it, R.string.tag_copied))
                        }
                    )
                }
                MangaContent(detail, history, onAction)
            }
        }
    }
}

@Composable
private fun MangaDescription(description: String) {
    SelectionContainer {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = description,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.body2
            )
        }
    }
}