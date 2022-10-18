package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.LoremIpsum
import com.fishhawk.lisu.data.network.model.MangaMetadata
import com.fishhawk.lisu.data.network.model.toMetadata
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.LisuToolBar
import com.google.accompanist.flowlayout.FlowRow
import org.koin.androidx.compose.koinViewModel

private sealed interface GalleryEditAction {
    object NavUp : GalleryEditAction
    data class Publish(val metadata: MangaMetadata) : GalleryEditAction
}

@Composable
fun GalleryEditScreen(
    navController: NavHostController,
    viewModel: GalleryViewModel = koinViewModel(
        owner = navController.previousBackStackEntry!!
    ),
) {
    val mangaId = viewModel.mangaId
    val detailResult by viewModel.detail.collectAsState()
    val initMetadata = detailResult?.getOrNull()?.toMetadata()

    if (initMetadata == null) {
        navController.navigateUp()
        return
    }

    val onAction: (GalleryEditAction) -> Unit = { action ->
        when (action) {
            GalleryEditAction.NavUp -> navController.navigateUp()
            is GalleryEditAction.Publish -> {
                val metadataToPublish = action.metadata.copy(
                    title = action.metadata.title?.takeIf { it.isNotEmpty() && it != mangaId },
                    authors = action.metadata.authors,
                    description = action.metadata.description?.ifEmpty { null }
                )
                viewModel.updateMetadata(metadataToPublish)
            }
        }
    }

    val context = LocalContext.current
    OnEvent(viewModel.event) {
        when (it) {
            GalleryEffect.UpdateMetadataSuccess -> context.toast(R.string.metadata_updated)
            is GalleryEffect.UpdateMetadataFailure -> context.toast("Failed to update metadata.")
            else -> Unit
        }
    }

    GalleryEditScaffold(
        mangaId = mangaId,
        initMetadata = initMetadata,
        onAction = onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryEditScaffold(
    mangaId: String,
    initMetadata: MangaMetadata,
    onAction: (GalleryEditAction) -> Unit,
) {
    var metadata by remember { mutableStateOf(initMetadata) }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = stringResource(R.string.label_gallery_edit),
                onNavUp = { onAction(GalleryEditAction.NavUp) },
            ) {
                IconButton(onClick = { onAction(GalleryEditAction.Publish(metadata)) }) {
                    Icon(Icons.Filled.Publish, contentDescription = "publish")
                }
            }
        },
        content = { paddingValues ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                MediumEmphasis {
                    Text(text = "Manga ID: $mangaId")
                }

                OutlinedTextField(
                    value = metadata.title ?: mangaId,
                    onValueChange = { metadata = metadata.copy(title = it) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    label = { Text(text = "Title") },
                    modifier = Modifier.fillMaxWidth(),
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = when (metadata.isFinished) {
                            true -> "Finished"
                            false -> "Ongoing"
                            null -> "Unknown"
                        },
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        label = { Text(text = "State") },
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        listOf(
                            "Finished" to true,
                            "Outgoing" to false,
                            "Unknown" to null,
                        ).forEach { (text, isFinished) ->
                            DropdownMenuItem(
                                text = { Text(text = text) },
                                onClick = {
                                    metadata = metadata.copy(isFinished = isFinished)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                TagTextField(
                    label = "Authors",
                    tags = metadata.authors,
                    onTagClick = { metadata = metadata.copyWithoutAuthor(it) },
                    onAddTag = { metadata = metadata.copyWithNewAuthor(it) },
                )

                metadata.tags.forEach { (key, tags) ->
                    TagTextField(
                        label = if (key.isBlank()) "Tags" else "Tags:$key",
                        tags = tags,
                        onTagClick = { metadata = metadata.copyWithoutTag(key, it) },
                        onAddTag = { metadata = metadata.copyWithNewTag(key, it) },
                    )
                }

                Column {
                    OutlinedTextField(
                        value = metadata.description ?: "",
                        onValueChange = { metadata = metadata.copy(description = it) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        label = { Text(text = "Description") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagTextField(
    label: String,
    tags: List<String>,
    onTagClick: (String) -> Unit,
    onAddTag: (String) -> Unit,
) {
    Column {
        FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp) {
            tags.forEach { tag ->
                InputChip(
                    selected = false,
                    onClick = { onTagClick(tag) },
                    label = { Text(text = tag) },
                    trailingIcon = {
                        Icon(
                            imageVector = LisuIcons.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        var newTag by remember { mutableStateOf("") }
        OutlinedTextField(
            value = newTag,
            onValueChange = { newTag = it },
            textStyle = MaterialTheme.typography.bodyMedium,
            label = { Text(text = label) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (newTag.isNotBlank()) {
                    onAddTag(newTag.trim())
                    newTag = ""
                }
            }),
        )
    }
}


private fun MangaMetadata.copyWithoutAuthor(author: String) =
    copy(authors = authors.filter { it != author })

private fun MangaMetadata.copyWithNewAuthor(newAuthor: String) =
    copy(authors = authors.toMutableList().apply { add(newAuthor) })

private fun MangaMetadata.copyWithoutTagGroup(key: String) =
    copy(tags = tags.filter { it.key != key })

private fun MangaMetadata.copyWithNewTagGroup(key: String) =
    copy(tags = tags.toMutableMap().apply { putIfAbsent(key, emptyList()) })

private fun MangaMetadata.copyWithoutTag(key: String, tag: String) =
    copy(tags = tags.toMutableMap().apply {
        get(key)?.filter { it != tag }?.let { put(key, it) }
    })

private fun MangaMetadata.copyWithNewTag(key: String, newTag: String) =
    copy(tags = tags.toMutableMap().apply {
        get(key)?.toMutableList()?.let {
            it.add(newTag)
            put(key, it)
        }
    })

@Preview
@Composable
private fun GalleryEditScaffoldPreview() {
    LisuTheme {
        val detail = LoremIpsum.mangaDetail()
        GalleryEditScaffold(
            mangaId = detail.id,
            initMetadata = detail.toMetadata(),
            onAction = { println(it) },
        )
    }
}
