package com.fishhawk.lisu.ui.provider

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.network.model.*
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.*
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.delay
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

private sealed interface ProviderAction {
    object NavUp : ProviderAction
    data class NavToGallery(val manga: MangaDto) : ProviderAction

    data class Search(val keywords: String) : ProviderAction
    data class DeleteSuggestion(val keywords: String) : ProviderAction
    data class SetFilterValue(val name: String, val value: Any) : ProviderAction
    data class SetFilterValues(val values: Map<String, Any>) : ProviderAction

    data class AddToLibrary(val manga: MangaDto) : ProviderAction
    data class RemoveFromLibrary(val manga: MangaDto) : ProviderAction

    object Reload : ProviderAction
    object Refresh : ProviderAction
    object RequestNextPage : ProviderAction
}

@Composable
fun ProviderScreen(navController: NavHostController) {
    val viewModel by viewModel<ProviderViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }
    val providerId = viewModel.providerId
    val boardId = viewModel.boardId
    val hasAdvanceFilters = viewModel.hasAdvanceFilters
    val hasSearchBar = viewModel.hasSearchBar

    val keywords by viewModel.keywords.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val board = viewModel.board.collectAsState().value ?: return
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val onAction: (ProviderAction) -> Unit = { action ->
        when (action) {
            ProviderAction.NavUp ->
                navController.navigateUp()
            is ProviderAction.NavToGallery ->
                navController.navToGallery(action.manga)

            is ProviderAction.DeleteSuggestion ->
                viewModel.deleteSuggestion(action.keywords)
            is ProviderAction.Search ->
                viewModel.search(action.keywords)
            is ProviderAction.SetFilterValue ->
                viewModel.updateFilterHistory(action.name, action.value)
            is ProviderAction.SetFilterValues ->
                viewModel.updateFilterHistory(action.values)

            is ProviderAction.AddToLibrary ->
                viewModel.addToLibrary(action.manga)
            is ProviderAction.RemoveFromLibrary ->
                viewModel.removeFromLibrary(action.manga)

            ProviderAction.Reload ->
                viewModel.reload()
            ProviderAction.Refresh ->
                viewModel.refresh()
            ProviderAction.RequestNextPage ->
                viewModel.requestNextPage()
        }
    }

    val context = LocalContext.current
    OnEvent(viewModel.event) {
        when (it) {
            is ProviderEvent.AddToLibraryFailure ->
                context.toast(it.exception.localizedMessage ?: "")
            is ProviderEvent.RemoveFromLibraryFailure ->
                context.toast(it.exception.localizedMessage ?: "")
            is ProviderEvent.RefreshFailure ->
                context.toast(it.exception.localizedMessage ?: "")
        }
    }

    ProviderScaffold(
        providerId = providerId,
        boardId = boardId,
        keywords = keywords,
        suggestions = suggestions,
        hasSearchBar = hasSearchBar,
        hasAdvanceFilters = hasAdvanceFilters,
        isRefreshing = isRefreshing,
        board = board,
        onAction = onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderScaffold(
    providerId: String,
    boardId: BoardId,
    keywords: String,
    suggestions: List<String>,
    hasSearchBar: Boolean,
    hasAdvanceFilters: Boolean,
    isRefreshing: Boolean,
    board: Board,
    onAction: (ProviderAction) -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(100)
        PR.lastUsedProvider.set(providerId)
    }

    val searchAndWaitInput = boardId == BoardId.Search && keywords.isBlank()
    var editingKeywords by remember { mutableStateOf(keywords) }
    var editing by remember { mutableStateOf(searchAndWaitInput) }

    var showAdvanceFilterList by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = keywords.ifBlank { providerId },
                onNavUp = { onAction(ProviderAction.NavUp) },
            ) {
                if (hasSearchBar || boardId == BoardId.Search) {
                    IconButton(onClick = { editing = true }) {
                        Icon(Icons.Default.Search, stringResource(R.string.action_search))
                    }
                }
                if (hasAdvanceFilters && !showAdvanceFilterList) {
                    IconButton(onClick = { showAdvanceFilterList = true }) {
                        Icon(LisuIcons.Add, "toggle")
                    }
                }
            }
            LisuSearchToolBar(
                visible = editing,
                value = editingKeywords,
                onValueChange = { editingKeywords = it },
                onSearch = {
                    if (boardId != BoardId.Search || it.isNotBlank()) {
                        onAction(ProviderAction.Search(it))
                        editing = false
                    }
                },
                onDismiss = {
                    if (searchAndWaitInput) onAction(ProviderAction.NavUp)
                    else editing = false
                },
                placeholder = { Text("${providerId}-${boardId}") }
            )
        },
        content = { paddingValues ->
            LisuTransition {
                Box(modifier = Modifier.padding(paddingValues)) {
                    var addDialogManga by remember { mutableStateOf<MangaDto?>(null) }
                    var removeDialogManga by remember { mutableStateOf<MangaDto?>(null) }

                    if (!searchAndWaitInput) {
                        ProviderMangaList(
                            board = board,
                            isRefreshing = isRefreshing,
                            onCardLongClick = {
                                when (it.state) {
                                    MangaState.Remote -> addDialogManga = it
                                    MangaState.RemoteInLibrary -> removeDialogManga = it
                                    else -> Unit
                                }
                            },
                            onAction = onAction,
                        )
                    }

                    if (board.filterValues.advance.isNotEmpty()) {
                        AnimatedVisibility(
                            visible = showAdvanceFilterList,
                            enter = fadeIn(initialAlpha = 0.3f),
                            exit = fadeOut()
                        ) {
                            ProviderFilterList(
                                filters = board.filterValues,
                                onBack = { showAdvanceFilterList = false },
                                onAction = onAction,
                            )
                        }
                    }

                    SuggestionList(
                        visible = editing,
                        onDismiss = {
                            editing = false
                            if (searchAndWaitInput) onAction(ProviderAction.NavUp)
                        },
                        keywords = editingKeywords,
                        suggestions = suggestions,
                        onSuggestionSelected = { editingKeywords = it },
                        onSuggestionDeleted = { onAction(ProviderAction.DeleteSuggestion(it)) }
                    )

                    addDialogManga?.let {
                        LisuDialog(
                            title = it.titleOrId,
                            confirmText = "Add to library",
                            dismissText = stringResource(R.string.action_cancel),
                            onConfirm = { onAction(ProviderAction.AddToLibrary(it)) },
                            onDismiss = { addDialogManga = null },
                        )
                    }

                    removeDialogManga?.let {
                        LisuDialog(
                            title = it.titleOrId,
                            confirmText = "Remove from library",
                            dismissText = stringResource(R.string.action_cancel),
                            onConfirm = { onAction(ProviderAction.RemoveFromLibrary(it)) },
                            onDismiss = { removeDialogManga = null },
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ProviderMangaList(
    board: Board,
    isRefreshing: Boolean,
    onCardLongClick: (manga: MangaDto) -> Unit,
    onAction: (ProviderAction) -> Unit,
) {
    Column {
        board.filterValues.base.forEach { (name, filterValue) ->
            when (val model = filterValue.model) {
                is FilterModel.Text -> Unit
                is FilterModel.Switch -> Unit
                is FilterModel.Select ->
                    FilterSelectBase(name, model, filterValue.value as Int, onAction)
                is FilterModel.MultipleSelect -> Unit
            }
        }

        StateView(
            result = board.mangaResult,
            onRetry = { onAction(ProviderAction.Reload) },
            modifier = Modifier.fillMaxSize(),
        ) { mangaList ->
            RefreshableMangaList(
                mangaList = mangaList,
                isRefreshing = isRefreshing,
                onRefresh = { onAction(ProviderAction.Refresh) },
                onRequestNextPage = { onAction(ProviderAction.RequestNextPage) },
                aboveCover = {
                    if (it.state == MangaState.RemoteInLibrary) {
                        MangaBadge(text = "in library")
                    }
                },
                onCardClick = { onAction(ProviderAction.NavToGallery(it)) },
                onCardLongClick = onCardLongClick
            )
        }
    }
}

@Composable
private fun FilterSelectBase(
    name: String,
    model: FilterModel.Select,
    value: Int,
    onAction: (ProviderAction) -> Unit,
) {
    FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 2.dp) {
        model.options.mapIndexed { index, text ->
            Text(
                modifier = Modifier.clickable {
                    onAction(ProviderAction.SetFilterValue(name, index))
                },
                text = text,
                style = TextStyle(fontSize = 12.sp).merge(),
                color = MaterialTheme.colorScheme.run {
                    if (index != value) onSurface else primary
                }
            )
        }
    }
}

@Composable
private fun ProviderFilterList(
    filters: BoardFilterValue,
    onBack: () -> Unit,
    onAction: (ProviderAction) -> Unit,
) {
    val tempFilterValues = remember(filters) {
        mutableStateMapOf<String, Any>().apply {
            (filters.base + filters.advance).forEach { (name, value) ->
                set(name, value.value)
            }
        }
    }

    val state = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state)
            .background(MaterialTheme.colorScheme.background)
    ) {
        (filters.base + filters.advance).forEach { (name, filterValue) ->
            FilterAdvance(
                name = name,
                model = filterValue.model,
                value = tempFilterValues[name]!!,
            ) {
                tempFilterValues[name] = it
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = {
                val filterValues = (filters.base + filters.advance).mapValues { (_, filterValue) ->
                    when (val model = filterValue.model) {
                        is FilterModel.Text -> ""
                        is FilterModel.Switch -> model.default
                        is FilterModel.Select -> 0
                        is FilterModel.MultipleSelect -> emptySet<Int>()
                    }
                }
                onAction(ProviderAction.SetFilterValues(filterValues))
            }) { Text("Reset") }
            Button(onClick = {
                onAction(ProviderAction.SetFilterValues(tempFilterValues))
                onBack()
            }) { Text("Apply") }
        }
    }
    BackHandler(true, onBack)
}

@Composable
private fun FilterAdvance(
    name: String,
    model: FilterModel,
    value: Any,
    onValueChange: (Any) -> Unit,
) {
    when (model) {
        is FilterModel.Text ->
            FilterText(name, value as String, onValueChange)
        is FilterModel.Switch ->
            FilterSwitch(name, value as Boolean, onValueChange)
        is FilterModel.Select ->
            FilterSelectAdvance(name, model, value as Int, onValueChange)
        is FilterModel.MultipleSelect ->
            FilterMultipleSelectAdvance(name, model, value as Set<Int>, onValueChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterText(
    name: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    var text by remember { mutableStateOf(value) }
    OutlinedTextField(
        value = value,
        onValueChange = { text = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        label = { Text(name) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions { onValueChange(text) },
    )
}

@Composable
private fun FilterSwitch(
    name: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onValueChange(!value) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = value,
            onCheckedChange = null,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun FilterSelectAdvance(
    name: String,
    model: FilterModel.Select,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    var isOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isOpen = true }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
        )
        MediumEmphasis {
            Text(
                text = model.options[value],
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (isOpen) {
            LisuSelectDialog(
                title = name,
                options = model.options,
                selected = value,
                onSelectedChanged = { onValueChange(it) },
                onDismiss = { isOpen = false },
            )
        }
    }
}

@Composable
private fun FilterMultipleSelectAdvance(
    name: String,
    model: FilterModel.MultipleSelect,
    value: Set<Int>,
    onValueChange: (Set<Int>) -> Unit,
) {
    var isOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isOpen = true }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = name,
            style =
            MaterialTheme.typography.bodyMedium,
        )
        Icon(
            imageVector = LisuIcons.Add,
            contentDescription = "edit",
        )
        if (isOpen) {
            LisuMultipleSelectDialog(
                title = name,
                options = model.options,
                selected = value,
                onSelectedChanged = { onValueChange(if (it in value) value - it else value + it) },
                onDismiss = { isOpen = false },
            )
        }
    }
}
