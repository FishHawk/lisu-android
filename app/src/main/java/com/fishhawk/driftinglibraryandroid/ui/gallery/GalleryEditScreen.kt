package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.Publish
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaStatus
import com.fishhawk.driftinglibraryandroid.data.remote.model.TagGroup
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun GalleryEditScreen(navController: NavHostController) {
    Scaffold(
        topBar = { ToolBar(navController) },
        content = { Content(navController) }
    )
}

@Composable
private fun ToolBar(navController: NavHostController) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.secondary,
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = { Text(stringResource(R.string.label_gallery_edit)) },
        actions = {
            IconButton(onClick = { /*viewModel.updateMetadata(metadata)*/ }) {
                Icon(Icons.Filled.Publish, contentDescription = "publish")
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Filled.NavigateBefore, "back")
            }
        }
    )
}

@Composable
private fun Content(navController: NavHostController) {
    val viewModel = hiltViewModel<GalleryViewModel>(navController.getBackStackEntry("detail"))
    val detail = viewModel.detail.value!!

    Card(modifier = Modifier.padding(16.dp)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val title = remember { mutableStateOf(detail.title) }
            TextField(
                value = title.value,
                onValueChange = { title.value = it },
                label = { Text("Title") },
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
            )

            val status = remember { mutableStateOf(detail.metadata.status ?: MangaStatus.UNKNOWN) }
            Row(modifier = Modifier.fillMaxWidth()) {
                enumValues<MangaStatus>().forEach {
                    RadioButton(
                        selected = (it == status.value),
                        onClick = { status.value = it },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colors.primary
                        )
                    )
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            val authors = remember { mutableStateOf(detail.metadata.authors ?: listOf()) }
            MangaTagGroup(TagGroup("", authors.value))

            val newAuthor = remember { mutableStateOf("") }
            TextField(
                value = newAuthor.value,
                onValueChange = { newAuthor.value = it },
                label = { Text("new author") },
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                keyboardActions = KeyboardActions(onDone = {
//                authors.addTag(binding.authorInput.text.toString())
                    newAuthor.value = ""
                })
            )

            val tags = remember { mutableStateOf(detail.metadata.tags ?: listOf()) }
            MangaTagGroups(tags.value)

            val newTag = remember { mutableStateOf("") }
            TextField(
                value = newTag.value,
                onValueChange = { newTag.value = it },
                label = { Text("new tag") },
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                keyboardActions = KeyboardActions(onDone = {
//                tags.addTag(binding.tagInput.text.toString())
                    newTag.value = ""
                })
            )

            val description = remember { mutableStateOf(detail.metadata.description ?: "") }
            TextField(
                value = description.value,
                onValueChange = { description.value = it },
                label = { Text("description") },
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
            )
        }
    }
}

////    private fun buildMetadata(): MetadataDetail {
////        val title = binding.title.text.toString()
////        val authors = authorsAdapter.list
////        val status = when (binding.status.checkedRadioButtonId) {
////            binding.radioCompleted.id -> MangaStatus.COMPLETED
////            binding.radioOngoing.id -> MangaStatus.ONGOING
////            else -> MangaStatus.UNKNOWN
////        }
////        val tagsMap = mutableMapOf<String, MutableList<String>>()
////        tagsAdapter.list.forEach {
////            val key = it.substringBefore(':', "")
////            val value = it.substringAfter(':')
////            tagsMap.getOrPut(key) { mutableListOf() }.add(value)
////        }
////        val tags = tagsMap.map { TagGroup(it.key, it.value) }
////        val description = binding.description.text.toString()
////
////        return MetadataDetail(title, authors, status, description, tags)
////    }
