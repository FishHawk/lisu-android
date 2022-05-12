package com.fishhawk.lisu.ui.gallery

import android.graphics.drawable.Drawable
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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import com.fishhawk.lisu.util.copyToClipboard
import com.fishhawk.lisu.util.saveImage
import com.fishhawk.lisu.util.shareImage
import com.fishhawk.lisu.util.shareText
import com.fishhawk.lisu.ui.main.navToGalleryEdit
import com.fishhawk.lisu.ui.main.navToGlobalSearch
import com.fishhawk.lisu.ui.main.navToProviderSearch
import com.fishhawk.lisu.ui.main.navToReader
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.ui.widget.StateView
import com.fishhawk.lisu.ui.widget.ViewState
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

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

            is GalleryAction.SaveCover ->
                context.saveImage(action.cover, "${detail.title ?: detail.id}-cover")
            is GalleryAction.ShareCover ->
                context.shareImage(
                    "Share cover via",
                    action.cover,
                    "${detail.title ?: detail.id}-cover"
                )
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
                    Icon(LisuIcons.Favorite, stringResource(R.string.action_remove_from_library))
                }
            } else {
                IconButton(onClick = { onAction(GalleryAction.AddToLibrary) }) {
                    Icon(LisuIcons.FavoriteBorder, stringResource(R.string.action_add_to_library))
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
                modifier = Modifier.padding(bottom = 16.dp),
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

