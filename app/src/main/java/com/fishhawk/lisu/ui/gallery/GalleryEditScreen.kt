package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Publish
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.network.model.MangaMetadataDto
import com.fishhawk.lisu.data.network.model.toMetadataDetail
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.LisuToolBar
import org.koin.androidx.compose.viewModel

internal typealias GalleryEditActionHandler = (GalleryEditAction) -> Unit

internal sealed interface GalleryEditAction {
    object NavUp : GalleryEditAction
    object Publish : GalleryEditAction
}

@Composable
fun GalleryEditScreen(navController: NavHostController) {
    val viewModel by viewModel<GalleryViewModel>(
        owner = navController.previousBackStackEntry!!
    )
    val id = viewModel.mangaId
    val detailResult by viewModel.detail.collectAsState()
    val initDetail = detailResult?.getOrNull()

    if (initDetail == null) {
        navController.navigateUp()
        return
    }
    var detail by remember { mutableStateOf(initDetail.toMetadataDetail()) }

    val onAction: GalleryEditActionHandler = { action ->
        when (action) {
            GalleryEditAction.NavUp -> navController.navigateUp()
            GalleryEditAction.Publish -> {
                val detailToPublish = detail.copy(
                    title = detail.title?.takeIf { it.isNotEmpty() && it != id },
                    authors = detail.authors?.ifEmpty { null },
                    description = detail.description?.ifEmpty { null }
                )
                viewModel.updateMetadata(detailToPublish)
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

    Scaffold(
        topBar = {
            LisuToolBar(
                title = "${stringResource(R.string.label_gallery_edit)} - $id",
                onNavUp = { onAction(GalleryEditAction.NavUp) },
            ) {
                IconButton(onClick = { onAction(GalleryEditAction.Publish) }) {
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
                Column {
                    Label(text = "Title:")
                    CustomTextField(
                        value = detail.title ?: initDetail.id,
                        onValueChange = { detail = detail.copy(title = it) },
                        placeholder = "input title",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column {
                    Label(text = "State:")
                    listOf(true, false, null).forEach {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = it == detail.isFinished,
                                onClick = { detail = detail.copy(isFinished = it) }
                            )
                            Text(
                                text = when (it) {
                                    true -> "Finished"
                                    false -> "Ongoing"
                                    null -> "Unknown"
                                },
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Label(text = "Authors:")
                    EditMangaTagGroup(
                        tags = detail.authors ?: emptyList(),
                        onTagClick = { author -> detail = detail.copyWithoutAuthor(author) }
                    )
                    TagTextField(
                        placeholder = "new author",
                        onDone = { author -> detail = detail.copyWithNewAuthor(author) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                detail.tags.forEach { (key, tags) ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Label(text = "Tags-$key:")
                        EditMangaTagGroup(
                            tags = tags,
                            onTagClick = { tag -> detail = detail.copyWithoutTag(key, tag) },
                        )
                        TagTextField(
                            placeholder = "new tag",
                            onDone = { tag -> detail = detail.copyWithNewTag(key, tag) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Column {
                    Label(text = "Description:")
                    CustomTextField(
                        value = detail.description ?: "",
                        onValueChange = { detail = detail.copy(description = it) },
                        placeholder = "input description",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    )
}

@Composable
private fun Label(text: String) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle2,
        )
    }
}

@Composable
private fun TagTextField(
    placeholder: String,
    onDone: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var newTag by remember { mutableStateOf("") }
    CustomTextField(
        value = newTag,
        onValueChange = { newTag = it },
        placeholder = placeholder,
        modifier = modifier,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            if (newTag.isNotBlank()) {
                onDone(newTag.trim())
                newTag = ""
            }
        })
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.TextFieldShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(colors.backgroundColor(true).value, shape)
            .indicatorLine(
                enabled = true,
                isError = false,
                interactionSource,
                colors
            ),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            val isFocused = interactionSource.collectIsFocusedAsState().value
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (value.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
                    )
                }
                innerTextField()
            }
        }
    )
}

private fun MangaMetadataDto.copyWithoutAuthor(author: String) =
    copy(authors = authors?.toMutableList()?.apply { remove(author) })

private fun MangaMetadataDto.copyWithNewAuthor(newAuthor: String) =
    copy(authors = (authors ?: emptyList()).toMutableList().apply { add(newAuthor) })

private fun MangaMetadataDto.copyWithoutTag(key: String, tag: String) =
    copy(tags = tags.toMutableMap().also { tags ->
        tags[key]?.toMutableList()?.let {
            it.remove(tag)
            tags[key] = it
        }
    })

private fun MangaMetadataDto.copyWithNewTag(key: String, newTag: String) =
    copy(tags = tags.toMutableMap().also { tags ->
        (tags[key] ?: emptyList()).toMutableList().let {
            it.add(newTag)
            tags[key] = it
        }
    })
