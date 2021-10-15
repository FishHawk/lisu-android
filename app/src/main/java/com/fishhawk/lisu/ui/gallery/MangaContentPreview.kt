package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
internal fun MangaContentPreview(
    preview: List<String>,
    onPageClick: (Int) -> Unit = {}
) {
    val nColumns = 3
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val rows = (preview.size + nColumns - 1) / nColumns
        (0..rows).map { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (columnIndex in 0 until nColumns) {
                    val itemIndex = rowIndex * nColumns + columnIndex
                    if (itemIndex < preview.size) {
                        Box(
                            modifier = Modifier.weight(1f, fill = true),
                            propagateMinConstraints = true
                        ) { PreviewPage(preview[itemIndex], itemIndex.plus(1), onPageClick) }
                    } else {
                        Spacer(Modifier.weight(1f, fill = true))
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewPage(
    url: String,
    page: Int,
    onPageClick: (Int) -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            modifier = Modifier
                .aspectRatio(0.75f)
                .clickable { onPageClick(page) },
            painter = rememberImagePainter(url) {
                crossfade(true)
                crossfade(500)
            },
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Text(text = page.toString())
    }
}