package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fishhawk.driftinglibraryandroid.R
import com.google.accompanist.coil.rememberCoilPainter

@Composable
fun MangaContentPreview(
    preview: List<String>,
    onPageClick: (Int) -> Unit = {}
) {
    val nColumns = 3
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val rows = (preview.size + nColumns - 1) / nColumns
        items(rows) { rowIndex ->
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val request = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        Image(
            modifier = Modifier
                .aspectRatio(0.75f)
                .clickable { onPageClick(page) },
            painter = rememberCoilPainter(request, fadeIn = true),
            contentDescription = "Page",
            contentScale = ContentScale.Crop
        )
        Text(text = page.toString())
    }
}

@Composable
fun MangaNoChapter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.gallery_no_chapters_hint),
            color = MaterialTheme.colors.secondary
        )
    }
}
