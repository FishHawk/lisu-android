package com.fishhawk.lisu.ui.provider

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.data.datastore.BoardFilter
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.MangaState
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.widget.*
import com.google.accompanist.flowlayout.FlowRow
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

internal typealias ProviderActionHandler = (ProviderAction) -> Unit

internal sealed interface ProviderAction {
    object NavUp : ProviderAction
    data class NavToGallery(val manga: MangaDto) : ProviderAction

    data class SelectFilter(val name: String, val selected: Int) : ProviderAction
    data class AddToLibrary(val manga: MangaDto) : ProviderAction
    data class RemoveFromLibrary(val manga: MangaDto) : ProviderAction

    object ReloadProvider : ProviderAction
    object Reload : ProviderAction
    object RequestNextPage : ProviderAction
}

@Composable
fun ProviderScreen(navController: NavHostController) {
    val viewModel by viewModel<ProviderViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }
    val providerId = viewModel.providerId
    val boardId = viewModel.providerId
    val boardResult by viewModel.board.collectAsState()

    val onAction: ProviderActionHandler = { action ->
        when (action) {
            ProviderAction.NavUp ->
                navController.navigateUp()
            is ProviderAction.NavToGallery ->
                navController.navToGallery(action.manga)

            is ProviderAction.SelectFilter ->
                viewModel.updateFilterHistory(action.name, action.selected)
            is ProviderAction.AddToLibrary ->
                viewModel.addToLibrary(action.manga)
            is ProviderAction.RemoveFromLibrary ->
                viewModel.removeFromLibrary(action.manga)

            ProviderAction.ReloadProvider ->
                viewModel.reloadProvider()
            ProviderAction.Reload ->
                viewModel.reload()
            ProviderAction.RequestNextPage ->
                viewModel.requestNextPage()
        }
    }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = "${providerId}-${boardId}",
                onNavUp = { onAction(ProviderAction.NavUp) },
            )
        },
        content = { paddingValues ->
            LisuTransition {
                var addDialogManga by remember { mutableStateOf<MangaDto?>(null) }
                var removeDialogManga by remember { mutableStateOf<MangaDto?>(null) }

                ProviderBoard(
                    boardResult = boardResult,
                    onCardLongClick = {
                        when (it.state) {
                            MangaState.Local -> Log.w(null, "Manga state should be local here")
                            MangaState.Remote -> addDialogManga = it
                            MangaState.RemoteInLibrary -> removeDialogManga = it
                        }
                    },
                    modifier = Modifier.padding(paddingValues),
                    onAction = onAction,
                )

                addDialogManga?.let {
                    LisuDialog(
                        title = it.titleOrId,
                        confirmText = "Add to library",
                        onConfirm = { onAction(ProviderAction.AddToLibrary(it)) },
                        onDismiss = { addDialogManga = null },
                    )
                }

                removeDialogManga?.let {
                    LisuDialog(
                        title = it.titleOrId,
                        confirmText = "Remove from library",
                        onConfirm = { onAction(ProviderAction.RemoveFromLibrary(it)) },
                        onDismiss = { removeDialogManga = null },
                    )
                }
            }
        }
    )

}

@Composable
private fun ProviderBoard(
    boardResult: Result<Board>?,
    onCardLongClick: (manga: MangaDto) -> Unit,
    modifier: Modifier = Modifier,
    onAction: ProviderActionHandler,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StateView(
            result = boardResult,
            onRetry = { onAction(ProviderAction.ReloadProvider) },
        ) { board ->
            board.filters.forEach { BoardFilter(it, onAction) }

            StateView(
                result = board.mangaResult,
                onRetry = { onAction(ProviderAction.Reload) },
                modifier = Modifier.fillMaxSize(),
            ) { mangaList ->
                RefreshableMangaList(
                    result = mangaList,
                    onRefresh = { onAction(ProviderAction.Reload) },
                    onRequestNextPage = { onAction(ProviderAction.RequestNextPage) },
                    decorator = {
                        if (it != null && it.state == MangaState.RemoteInLibrary) {
                            MangaBadge(text = "in library")
                        }
                    },
                    onCardClick = { onAction(ProviderAction.NavToGallery(it)) },
                    onCardLongClick = onCardLongClick
                )
            }
        }
    }
}

@Composable
private fun BoardFilter(
    filter: BoardFilter,
    onAction: ProviderActionHandler,
) {
    FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 2.dp) {
        filter.options.mapIndexed { index, text ->
            Text(
                modifier = Modifier.clickable {
                    onAction(ProviderAction.SelectFilter(filter.name, index))
                },
                text = text,
                style = TextStyle(fontSize = 12.sp).merge(),
                color = MaterialTheme.colors.run {
                    if (index != filter.selected) onSurface else primary
                }
            )
        }
    }
}