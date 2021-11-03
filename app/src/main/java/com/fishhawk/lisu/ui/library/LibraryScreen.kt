package com.fishhawk.lisu.ui.library

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.getBlocking
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.base.findActivity
import com.fishhawk.lisu.ui.base.toast
import com.fishhawk.lisu.ui.navToGallery
import com.fishhawk.lisu.ui.navToLibrarySearch
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import kotlinx.coroutines.flow.collect

private typealias LibraryActionHandler = (LibraryAction) -> Unit

private sealed interface LibraryAction {
    object NavToSearch : LibraryAction
    data class NavToGallery(val manga: MangaDto) : LibraryAction
    data class Delete(val manga: MangaDto) : LibraryAction
    object Random : LibraryAction
}

@Composable
fun LibraryScreen(navController: NavHostController) {
    val context = LocalContext.current

    val viewModel = hiltViewModel<LibraryViewModel>()
    val mangaList = viewModel.mangaList.collectAsLazyPagingItems()

    val onAction: LibraryActionHandler = { action ->
        when (action) {
            LibraryAction.NavToSearch -> navController.navToLibrarySearch()
            is LibraryAction.NavToGallery -> navController.navToGallery(action.manga)
            is LibraryAction.Delete -> viewModel.deleteManga(action.manga)
            LibraryAction.Random -> viewModel.getRandomManga()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.NavToGallery -> navController.navToGallery(effect.manga)
                is LibraryEffect.Toast -> context.toast(effect.message)
            }
        }
    }

    Scaffold(
        topBar = { ToolBar(onAction) },
        content = { LisuTransition { Content(mangaList, onAction) } }
    )

    var exitPressedOnce by remember { mutableStateOf(false) }
    val isConfirmExitEnabled = PR.isConfirmExitEnabled.getBlocking()
    BackHandler(isConfirmExitEnabled) {
        if (exitPressedOnce) {
            context.findActivity().finish()
        } else {
            exitPressedOnce = true
            context.toast(R.string.confirm_exit)
            Handler(Looper.getMainLooper()).postDelayed({
                exitPressedOnce = false
            }, 2000)
        }
    }
}

@Composable
private fun ToolBar(onAction: LibraryActionHandler) {
    LisuToolBar(title = stringResource(R.string.label_library)) {
        val isRandomButtonEnabled by PR.isRandomButtonEnabled.collectAsState()
        if (isRandomButtonEnabled) {
            IconButton(onClick = { onAction(LibraryAction.Random) }) {
                Icon(LisuIcons.Casino, contentDescription = "random-pick")
            }
        }
        IconButton(onClick = { onAction(LibraryAction.NavToSearch) }) {
            Icon(LisuIcons.Search, contentDescription = "search")
        }
    }
}

@Composable
private fun Content(
    mangaList: LazyPagingItems<MangaDto>,
    onAction: LibraryActionHandler
) {
    var isOpen by remember { mutableStateOf(false) }
    var mangaWaitToDelete by remember { mutableStateOf<MangaDto?>(null) }
    RefreshableMangaList(
        mangaList = mangaList,
        onCardClick = { onAction(LibraryAction.NavToGallery(it)) },
        onCardLongClick = {
            mangaWaitToDelete = it
            isOpen = true
        }
    )
    mangaWaitToDelete?.let {
        if (isOpen) {
            DeleteMangaDialog(
                onDismiss = { isOpen = false },
                onConfirm = { onAction(LibraryAction.Delete(it)) }
            )
        }
    }
}

@Composable
private fun DeleteMangaDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Confirm to delete manga?") },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("ok")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("cancel")
            }
        }
    )
}