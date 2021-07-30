package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderMode
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.data.datastore.setNext
import com.fishhawk.driftinglibraryandroid.ui.base.findActivity
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch

@Composable
fun BoxScope.ReaderInfoBar(name: String, title: String, position: Int, size: Int) {
    val viewModel = viewModel<ReaderViewModel>()
    val isMenuOpened by viewModel.isMenuOpened.collectAsState()

    val showInfoBar by PR.showInfoBar.collectAsState()
    if (!showInfoBar || isMenuOpened) return

    val infoBarText =
        if (size != 0) "$name $title ${position + 1}/$size"
        else "$name $title"

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .background(Color(0xAA000000))
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Text(text = infoBarText, color = Color.White)
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun BoxScope.ReaderMenu(
    name: String,
    title: String,
    size: Int,
    pagerState: PagerState
) {
    val viewModel = viewModel<ReaderViewModel>()
    val isMenuOpened by viewModel.isMenuOpened.collectAsState()

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.TopCenter),
        visible = isMenuOpened,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        ReaderMenuTop(name, title)
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = isMenuOpened,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        ReaderMenuBottom(size, pagerState)
    }
}

@Composable
private fun ReaderMenuTop(name: String, title: String) {
    Row(
        modifier = Modifier
            .background(Color(0xAA000000))
            .padding(4.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        IconButton(onClick = { context.findActivity().finish() }) {
            Icon(Icons.Filled.ArrowBack, "back", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            val viewModel = viewModel<ReaderViewModel>()
            val mangaTitle by viewModel.mangaTitle.collectAsState()
            Text(
                text = mangaTitle,
                style = MaterialTheme.typography.subtitle1.copy(fontSize = 18.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = "$name $title",
                    style = MaterialTheme.typography.body2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ReaderMenuBottom(size: Int, pagerState: PagerState) {
    val viewModel = viewModel<ReaderViewModel>()

    Column {
        val readingDirection by PR.readerMode.collectAsState()
        val layoutDirection =
            if (readingDirection == ReaderMode.Rtl) LayoutDirection.Rtl
            else LayoutDirection.Ltr

        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isOnlyOneChapter by viewModel.isOnlyOneChapter.collectAsState()
                if (!isOnlyOneChapter)
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color(0xAA000000)
                    ) {
                        IconButton(onClick = { viewModel.openPrevChapter() }) {
                            Icon(
                                if (LocalLayoutDirection.current == LayoutDirection.Ltr)
                                    Icons.Filled.SkipPrevious else Icons.Filled.SkipNext,
                                "prev", tint = Color.White
                            )
                        }
                    }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xAA000000)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            modifier = Modifier.width(32.dp),
                            text = pagerState.currentPage.plus(1).toString(),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        val scope = rememberCoroutineScope()
                        Slider(
                            modifier = Modifier.weight(1f),
                            value = pagerState.currentPage.toFloat() /
                                    size.minus(1).coerceAtLeast(1),
                            onValueChange = {
                                val target = (it * (size - 1)).toInt().coerceIn(0, size - 1)
                                scope.launch { pagerState.scrollToPage(target) }
                            },
                            enabled = size > 1
                        )

                        Text(
                            modifier = Modifier.width(32.dp),
                            text = size.toString(),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (!isOnlyOneChapter)
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color(0xAA000000)
                    ) {
                        IconButton(onClick = { viewModel.openNextChapter() }) {
                            Icon(
                                if (LocalLayoutDirection.current == LayoutDirection.Ltr)
                                    Icons.Filled.SkipNext else Icons.Filled.SkipPrevious,
                                "next", tint = Color.White
                            )
                        }
                    }
            }
        }

        Row(
            modifier = Modifier
                .background(Color(0xAA000000))
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current

            IconButton(modifier = Modifier.weight(1f), onClick = {
                viewModel.viewModelScope.launch { PR.readerMode.setNext() }
            }) {
                val icon = when (readingDirection) {
                    ReaderMode.Ltr -> Icons.Filled.ArrowForward
                    ReaderMode.Rtl -> Icons.Filled.ArrowBack
                    ReaderMode.Vertical -> Icons.Filled.ArrowDownward
                    ReaderMode.Continuous -> Icons.Filled.Expand
                }
                Icon(icon, "setting", tint = Color.White)
            }

            IconButton(modifier = Modifier.weight(1f), onClick = {
                viewModel.viewModelScope.launch { PR.readerOrientation.setNext() }
            }) { Icon(Icons.Filled.ScreenRotation, null, tint = Color.White) }

            IconButton(modifier = Modifier.weight(1f), onClick = {
                openSheet(BottomSheet.ColorFilterSheet)
            }) { Icon(Icons.Filled.BrightnessMedium, "color-filter", tint = Color.White) }

            IconButton(modifier = Modifier.weight(1f), onClick = {
                openSheet(BottomSheet.SettingSheet)
            }) { Icon(Icons.Filled.Settings, "setting", tint = Color.White) }
        }
    }
}
