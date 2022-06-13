package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fishhawk.lisu.ui.widget.VerticalGrid

@Composable
internal fun MangaContentPreview(
    previews: List<String>,
    onPageClick: (Int) -> Unit = {}
) {
    VerticalGrid(
        items = previews,
        nColumns = 3,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
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
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .crossfade(500)
                    .build()
            ),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = page.toString(),
                style = MaterialTheme.typography.body2,
            )
        }
    }
}