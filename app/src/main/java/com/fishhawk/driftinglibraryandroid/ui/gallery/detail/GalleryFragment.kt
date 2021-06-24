package com.fishhawk.driftinglibraryandroid.ui.gallery.detail

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.*
import com.fishhawk.driftinglibraryandroid.data.remote.model.Collection
import com.fishhawk.driftinglibraryandroid.databinding.GalleryFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.util.setNext
import com.fishhawk.driftinglibraryandroid.widget.ViewState
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

    private val tagAdapter = TagGroupListAdapter(object : TagGroupListAdapter.Listener {
        override fun onTagClick(key: String, value: String) {
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
        }

        override fun onTagLongClick(key: String, value: String) {
            val keywords = if (key.isBlank()) value else "${key}:$value"
            copyToClipboard(keywords)
            makeToast(R.string.toast_manga_tag_saved)
        }
    })

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
        return binding.root
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

        setupCover(viewModel.outline.cover)
        setupTitle(viewModel.outline.title)
        setupAuthors(viewModel.outline.metadata.authors)
        setupStatus(viewModel.outline.metadata.status)
        setupProvider(viewModel.provider)

        binding.provider.isVisible = viewModel.isFromProvider
        binding.libraryAddButton.isVisible = viewModel.isFromProvider
        coverSheet.isFromProvider = viewModel.isFromProvider

        binding.title.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_global_search,
                bundleOf("keywords" to binding.title.text)
            )
        }
        binding.title.setOnLongClickListener {
            copyToClipboard(binding.title.text as String)
            makeToast(R.string.toast_manga_title_copied)
            true
        }

        binding.author.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_global_search,
                bundleOf("keywords" to binding.author.text)
            )
        }
        binding.author.setOnLongClickListener {
            copyToClipboard(binding.author.text as String)
            makeToast(R.string.toast_manga_author_copied)
            true
        }

        binding.readButton.setOnClickListener {
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

        binding.libraryAddButton.setOnClickListener {
            viewModel.addMangaToLibrary(false)
        }
        binding.libraryAddButton.setOnLongClickListener {
            viewModel.addMangaToLibrary(true)
            true
        }

        binding.coverCard.setOnClickListener { coverSheet.show() }

        binding.description.setOnClickListener {
            binding.description.maxLines =
                if (binding.description.maxLines < Int.MAX_VALUE) Int.MAX_VALUE else 3
            updateDescriptionHint()
        }
        binding.description.setOnLongClickListener {
            copyToClipboard(binding.description.text as String)
            makeToast(R.string.toast_manga_description_copied)
            true
        }

        binding.tags.adapter = tagAdapter

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

            setupCover(it.cover)
            setupTitle(it.title)
            setupAuthors(it.metadata.authors)
            setupStatus(it.metadata.status)
            setupProvider(it.provider)

            setupSource(it.source)
            setupDescription(it.metadata.description)
            setupTags(it.metadata.tags)
            setupCollections(it.collections, it.preview)
        }

        viewModel.history.observe(viewLifecycleOwner) { history ->
            binding.chapters.markedPosition = history?.let {
                MarkedPosition(it.collectionIndex, it.chapterIndex, it.pageIndex)
            }
        }
    }

    private fun setupCover(cover: String?) {
        if (cover == null) return
        Glide.with(this)
            .load(cover)
            .placeholder(binding.cover.drawable)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.cover)

        Glide.with(this)
            .load(cover)
            .placeholder(binding.backdrop.drawable)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.backdrop)
    }

    private fun setupTitle(title: String) {
        binding.title.text = title
    }

    private fun setupAuthors(authors: List<String>?) {
        authors?.let {
            if (it.isEmpty()) null
            else it.joinToString(separator = ";")
        }.let {
            binding.author.isVisible = (it != null)
            binding.author.text = it
        }
    }

    private fun setupStatus(status: MangaStatus?) {
        binding.status.isVisible = (status != null)
        binding.status.text = status.toString()
    }

    private fun setupProvider(provider: ProviderInfo?) {
        binding.provider.text = provider?.title
    }

    private fun updateDescriptionHint() = binding.description.doOnLayout {
        fun TextView.hasEllipsize() = layout.text.toString() != text
        val visibility =
            if (binding.description.hasEllipsize()) View.VISIBLE
            else View.INVISIBLE
        binding.descriptionEllipsizeHint.visibility = visibility
        binding.descriptionEllipsizeHintScrim.visibility = visibility
    }

    private fun setupDescription(description: String?) {
        val hasDescription = !description.isNullOrBlank()
        binding.description.isVisible = hasDescription
        binding.descriptionEllipsizeHint.isVisible = hasDescription
        binding.descriptionEllipsizeHintScrim.isVisible = hasDescription

        binding.description.text = description
        if (hasDescription) updateDescriptionHint()
    }

    private fun setupSource(source: Source?) {
        binding.source.isVisible = source != null
        coverSheet.hasSource = source != null
        if (source == null) return

        binding.source.text = "From ${source.providerId} - ${source.mangaId} ${source.state}"

        when (source.state) {
            SourceState.DOWNLOADING -> binding.source.setTextColor(resources.getColor(R.color.blue_400))
            SourceState.WAITING -> binding.source.setTextColor(resources.getColor(R.color.green_400))
            SourceState.ERROR -> binding.source.setTextColor(resources.getColor(R.color.red_400))
        }
    }

    private fun setupTags(tags: List<TagGroup>?) {
        if (!tags.isNullOrEmpty()) {
            binding.tags.isVisible = true
            tagAdapter.setList(tags)
        } else {
            binding.tags.isVisible = false
        }
    }

    private fun setupCollections(collections: List<Collection>, preview: List<String>?) {
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
