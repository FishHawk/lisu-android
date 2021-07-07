package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.LoadState
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.*
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.fishhawk.driftinglibraryandroid.ui.theme.MaterialColors
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.hippo.refreshlayout.RefreshLayout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class GalleryFragment : Fragment() {
    private val viewModel: GalleryViewModel by navGraphViewModels(R.id.nav_graph_gallery_internal) {
        MainViewModelFactory(this)
    }

    private val coverSheet by lazy {
        GalleryCoverSheet(requireContext(), object : GalleryCoverSheet.Listener {
            override fun onSyncSource() {
                viewModel.syncSource()
            }

            override fun onDeleteSource() {
                viewModel.deleteSource()
            }

            override fun onEditMetadata() {
                findNavController().navigate(R.id.action_to_gallery_edit)
            }

            override fun onEditCover() {
                pickPictureLauncher.launch("image/*")
            }

            override fun onSaveCover() {
                val detail = viewModel.detail.value
                    ?: return makeToast(R.string.toast_manga_not_loaded)
                val url = detail.cover
                    ?: return makeToast(R.string.toast_manga_no_cover)
                saveImage(url, "${detail.id}-cover")
            }

            override fun onShareCover() {
                val detail = viewModel.detail.value
                    ?: return makeToast(R.string.toast_manga_not_loaded)
                val url = detail.cover
                    ?: return makeToast(R.string.toast_manga_no_cover)
                shareImage(url, "${detail.id}-cover")
            }
        })
    }

    private val pickPictureLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val content =
                uri?.let { requireContext().contentResolver.openInputStream(it)?.readBytes() }
            val type =
                uri?.let { requireContext().contentResolver.getType(uri)?.toMediaTypeOrNull() }
            if (content != null && type != null)
                viewModel.updateCover(content.toRequestBody(type))
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    val detail by viewModel.detail.observeAsState()
                    MangaDetail(detail)
                }
            }
        }
        return view
    }

    @Composable
    private fun MangaDetail(detail: MangaDetail?) {
        Column {
            MangaHeader(detail)
            val isRefreshing by viewModel.isRefreshing.observeAsState(true)
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.refreshManga() },
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    detail?.source?.let { MangaSource(it) }
                    detail?.metadata?.description?.let { MangaDescription(it) }
                    detail?.metadata?.tags?.let { tags ->
                        MangaTagGroups(tags,
                            onTagClick = { search(it) },
                            onTagLongClick = { copy(it, R.string.toast_manga_tag_saved) }
                        )
                    }
                    detail?.let { MangaContent(it) }
                }
            }
        }
    }

    @Composable
    private fun MangaHeader(detail: MangaDetail?) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val request = ImageRequest.Builder(LocalContext.current)
                .data(detail?.cover)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            Image(
                painter = rememberCoilPainter(request, fadeIn = true),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.2f
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    elevation = 4.dp
                ) {
                    Image(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .clickable { coverSheet.show() },
                        painter = rememberCoilPainter(request, fadeIn = true),
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop
                    )
                }
                MangaInfo(detail)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MangaInfo(detail: MangaDetail?) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (detail?.title ?: viewModel.outline.title).let {
                Text(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .combinedClickable(
                            onClick = { globalSearch(it) },
                            onLongClick = { copy(it, R.string.toast_manga_title_copied) }
                        ),
                    text = it
                )
            }
            (detail?.metadata?.authors ?: viewModel.outline.metadata.authors)
                ?.joinToString(separator = ";")?.let {
                    Text(
                        modifier = Modifier.combinedClickable(
                            onClick = { globalSearch(it) },
                            onLongClick = { copy(it, R.string.toast_manga_author_copied) }
                        ),
                        text = it,
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (detail?.metadata?.status ?: viewModel.outline.metadata.status)?.let {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.body2
                    )
                }
                viewModel.provider?.title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colors.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colors.onSurface),
                    onClick = {
                        viewModel.detail.value?.let { detail ->
                            viewModel.history.value?.let { history ->
                                navToReaderActivity(
                                    detail.id,
                                    detail.provider?.id,
                                    history.collectionIndex,
                                    history.chapterIndex,
                                    history.pageIndex
                                )
                            } ?: navToReaderActivity(detail.id, detail.provider?.id)
                        }
                    }
                ) {
                    Icon(Icons.Filled.PlayArrow, "Read")
                    Text("Read")
                }
                if (viewModel.isFromProvider) {
                    TextButton(
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colors.onSurface
                        ),
                        onClick = { viewModel.addMangaToLibrary(false) }
                    ) {
                        Icon(Icons.Filled.LibraryAdd, "Add")
                    }
                }
            }
        }
    }

    @Composable
    private fun MangaSource(source: Source) {
        Text(
            text = "From ${source.providerId} - ${source.mangaId} ${source.state}",
            color = when (source.state) {
                SourceState.DOWNLOADING -> MaterialColors.Blue400
                SourceState.WAITING -> MaterialColors.Green400
                SourceState.ERROR -> MaterialTheme.colors.error
                SourceState.UPDATED -> MaterialTheme.colors.onSurface
            }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MangaDescription(description: String) {
        Text(
            text = description,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = { copy(description, R.string.toast_manga_description_copied) }
            )
        )
    }

    @Composable
    private fun MangaContent(detail: MangaDetail) {
        val hasPreview = detail.collections.size == 1 && !detail.preview.isNullOrEmpty()
        val hasChapter = detail.collections.isNotEmpty()
        when {
            hasPreview -> MangaContentPreview(
                preview = detail.preview!!,
                onPageClick = {
                    navToReaderActivity(
                        viewModel.mangaId,
                        viewModel.provider?.id,
                        0,
                        0,
                        it
                    )
                })
            hasChapter -> {
                val history by viewModel.history.observeAsState()
                MangaContentChapter(
                    collections = detail.collections,
                    chapterMark = history?.let {
                        ChapterMark(it.collectionIndex, it.chapterIndex, it.pageIndex)
                    },
                    onChapterClick = { collectionIndex, chapterIndex, pageIndex ->
                        navToReaderActivity(
                            detail.id,
                            viewModel.providerId,
                            collectionIndex,
                            chapterIndex,
                            pageIndex
                        )
                    }
                )
            }
            else -> MangaNoChapter()
        }
    }

    private fun globalSearch(keywords: String) {
        findNavController().navigate(
            R.id.action_to_global_search,
            bundleOf("keywords" to keywords)
        )
    }

    private fun search(keywords: String) {
        if (viewModel.isFromProvider) findNavController().navigate(
            R.id.action_to_provider_search,
            bundleOf(
                "keywords" to keywords,
                "provider" to viewModel.provider!!
            )
        ) else findNavController().navigate(
            R.id.action_to_library,
            bundleOf("keywords" to keywords)
        )
    }

    private fun copy(text: String, hintResId: Int) {
        copy(text)
        makeToast(hintResId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindToFeedbackViewModel(viewModel)
//        binding.root.setProgressViewOffset(
//            false,
//            binding.root.progressViewStartOffset,
//            binding.root.progressViewEndOffset
//        )
    }
}
