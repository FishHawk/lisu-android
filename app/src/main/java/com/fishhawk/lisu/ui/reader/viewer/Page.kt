package com.fishhawk.lisu.ui.reader.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.ui.reader.ReaderPage

@Composable
internal fun EmptyPage(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chapter is empty",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun NextChapterStatePage(
    page: ReaderPage.NextChapterState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${page.currentChapterName} ${page.currentChapterTitle}",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${page.nextChapterName} ${page.nextChapterTitle}",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun PrevChapterStatePage(
    page: ReaderPage.PrevChapterState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${page.currentChapterName} ${page.currentChapterTitle}",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${page.prevChapterName} ${page.prevChapterTitle}",
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
        }
    }
}