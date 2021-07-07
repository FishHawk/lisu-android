package com.fishhawk.driftinglibraryandroid.ui.gallery.detail

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.*
import com.fishhawk.driftinglibraryandroid.databinding.GalleryFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.fishhawk.driftinglibraryandroid.ui.theme.MaterialColors
import com.fishhawk.driftinglibraryandroid.util.setNext
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class GalleryFragment : Fragment() {
    private lateinit var binding: GalleryFragmentBinding
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
        binding = GalleryFragmentBinding.inflate(inflater, container, false)
        binding.compose.apply {
            setContent {
                ApplicationTheme {
                    ProvideWindowInsets {
                        val detail by viewModel.detail.observeAsState()
                        Column {
                            Header(detail)
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                detail?.source?.let { MangaSource(it) }
                                detail?.metadata?.description?.let { MangaDescription(it) }
                                detail?.metadata?.tags?.let { MangaTags(it) }
                            }
                        }
                    }
                }
            }
        }
        return binding.root
    }

    @Composable
    private fun Header(detail: MangaDetail?) {
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
                            onClick = {
                                findNavController().navigate(
                                    R.id.action_to_global_search,
                                    bundleOf("keywords" to it)
                                )
                            },
                            onLongClick = {
                                copyToClipboard(it)
                                makeToast(R.string.toast_manga_title_copied)
                            }
                        ),
                    text = it
                )
            }
            (detail?.metadata?.authors ?: viewModel.outline.metadata.authors)
                ?.joinToString(separator = ";")?.let {
                    Text(
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                findNavController().navigate(
                                    R.id.action_to_global_search,
                                    bundleOf("keywords" to it)
                                )
                            },
                            onLongClick = {
                                copyToClipboard(it)
                                makeToast(R.string.toast_manga_author_copied)
                            }
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
                    Row {
                        Icon(Icons.Filled.PlayArrow, "Read")
                        Text("Read")
                    }
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
                onLongClick = {
                    copyToClipboard(description)
                    makeToast(R.string.toast_manga_description_copied)
                }
            )
        )
    }

    @Composable
    private fun MangaTags(tags: List<TagGroup>) {
        Column(modifier = Modifier.fillMaxWidth()) {
            tags.map { TagGroup(it) }
        }
    }

    @Composable
    private fun TagGroup(group: TagGroup) {
        Row {
//            if (group.key.isNotBlank()) Tag(group.key)
            FlowRow(
                modifier = Modifier.padding(bottom = 8.dp),
                mainAxisSpacing = 4.dp,
                crossAxisSpacing = 4.dp
            ) {
                group.value.map { Tag(group.key, it) }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Tag(key: String, value: String) {
        Surface(
            modifier = Modifier,
            shape = RoundedCornerShape(16.dp),
            color = androidx.compose.ui.graphics.Color.LightGray
        ) {
            Text(
                modifier = Modifier
                    .padding(2.dp)
                    .combinedClickable(
                        onClick = {
                            val keywords = if (key.isBlank()) value else "${key}:$value"
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
                        },
                        onLongClick = {
                            val keywords = if (key.isBlank()) value else "${key}:$value"
                            copyToClipboard(keywords)
                            makeToast(R.string.toast_manga_tag_saved)
                        }
                    ),
                text = value,
                style = MaterialTheme.typography.body2.copy(fontSize = 12.sp)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindToFeedbackViewModel(viewModel)

        binding.root.setColorSchemeColors(requireContext().resolveAttrColor(R.attr.colorAccent))
        binding.root.setProgressViewOffset(
            false,
            binding.root.progressViewStartOffset,
            binding.root.progressViewEndOffset
        )
        binding.root.isRefreshing = true
        binding.root.setOnRefreshListener { viewModel.refreshManga() }
        viewModel.refreshFinish.observe(viewLifecycleOwner, EventObserver {
            binding.root.isRefreshing = false
        })

        coverSheet.isFromProvider = viewModel.isFromProvider

        val contentAdapter = ContentAdapter(this, viewModel.mangaId, viewModel.providerId)
        binding.chapters.adapter = contentAdapter

        binding.displayModeButton.setOnClickListener {
            GlobalPreference.chapterDisplayMode.setNext()
        }
        binding.displayOrderButton.setOnClickListener {
            GlobalPreference.chapterDisplayOrder.setNext()
        }

        GlobalPreference.chapterDisplayMode.asFlow()
            .onEach {
                binding.displayModeButton.setIconResource(getChapterDisplayModeIcon())
                binding.chapters.viewMode = when (it) {
                    GlobalPreference.ChapterDisplayMode.GRID -> ContentView.ViewMode.GRID
                    GlobalPreference.ChapterDisplayMode.LINEAR -> ContentView.ViewMode.LINEAR
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        GlobalPreference.chapterDisplayOrder.asFlow()
            .onEach {
                binding.chapters.viewOrder = when (it) {
                    GlobalPreference.ChapterDisplayOrder.ASCEND -> ContentView.ViewOrder.ASCEND
                    GlobalPreference.ChapterDisplayOrder.DESCEND -> ContentView.ViewOrder.DESCEND
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.detail.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.contentView.isVisible = true
            setupCollections(it.collections, it.preview)
        }

        viewModel.history.observe(viewLifecycleOwner) { history ->
            binding.chapters.markedPosition = history?.let {
                MarkedPosition(it.collectionIndex, it.chapterIndex, it.pageIndex)
            }
        }
    }

    private fun setupCollections(collections: List<ChapterCollection>, preview: List<String>?) {
        val hasPreview = collections.size == 1 && !preview.isNullOrEmpty()
        val hasChapter = collections.isNotEmpty()

        binding.previewPages.isVisible = hasPreview
        binding.chapterHeader.isVisible = !hasPreview && hasChapter
        binding.chapters.isVisible = !hasPreview && hasChapter
        binding.noChapterHint.isVisible = !hasPreview && !hasChapter
        if (hasChapter) binding.chapters.collections = collections
        if (hasPreview) binding.previewPages.adapter =
            PreviewAdapter(object : PreviewAdapter.Listener {
                override fun onPageClick(page: Int) {
                    navToReaderActivity(
                        viewModel.mangaId,
                        viewModel.provider?.id,
                        0,
                        0,
                        page
                    )
                }
            }).also { it.setList(preview!!) }
    }
}
