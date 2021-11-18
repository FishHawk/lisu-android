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
import com.fishhawk.lisu.ui.widget.VerticalGrid

@Composable
internal fun MangaContentPreview(
    previews: List<String>,
    onPageClick: (Int) -> Unit = {}
) {
    VerticalGrid(
        items = previews,
        nColumns = 3,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) { index, it ->
        Box(
            modifier = Modifier.weight(1f),
            propagateMinConstraints = true
        ) {
            PreviewPage(it, index + 1, onPageClick)
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