package com.fishhawk.driftinglibraryandroid.ui.gallery.detail

import android.annotation.SuppressLint
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
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.Collection
import com.fishhawk.driftinglibraryandroid.data.remote.model.Source
import com.fishhawk.driftinglibraryandroid.data.remote.model.SourceState
import com.fishhawk.driftinglibraryandroid.data.remote.model.TagGroup
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
        MainViewModelFactory(requireActivity().application as MainApplication, requireArguments())
    }

    private var providerId: String? = null
    private val tagAdapter = TagGroupListAdapter(object : TagGroupListAdapter.Listener {
        override fun onTagClick(key: String, value: String) {
            val keywords = if (key.isBlank()) value else "${key}:$value"
            providerId?.let {
                findNavController().navigate(
                    R.id.action_to_provider_search,
                    bundleOf(
                        "keywords" to keywords,
                        "providerId" to it
                    )
                )
            } ?: findNavController().navigate(
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
                viewModel.updateThumb(content.toRequestBody(type))
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindToFeedbackViewModel(viewModel)

        val id: String = requireArguments().getString("id")!!
        val title: String = requireArguments().getString("title")!!
        providerId = requireArguments().getString("providerId")
        val thumb = requireArguments().getString("thumb")

        binding.title.setOnLongClickListener {
            copyToClipboard(binding.title.text as String)
            makeToast(R.string.toast_manga_title_copied)
            true
        }

        binding.author.setOnLongClickListener {
            copyToClipboard(binding.author.text as String)
            makeToast(R.string.toast_manga_author_copied)
            true
        }

        thumb?.let { setupThumb(it) }
        setupActionButton()

        binding.thumbCard.setOnLongClickListener {
            GalleryThumbSheet(requireContext(), object : GalleryThumbSheet.Listener {
                override fun onEdit() {
                    pickPictureLauncher.launch("image/*")
                }

                override fun onSave() {
                    val detail = (viewModel.detail.value as? Result.Success)?.data
                        ?: return makeToast(R.string.toast_manga_not_loaded)
                    val url = detail.thumb
                        ?: return makeToast(R.string.toast_manga_no_cover)
                    saveImage(url, "${detail.id}-thumb")
                }

                override fun onShare() {
                    val detail = (viewModel.detail.value as? Result.Success)?.data
                        ?: return makeToast(R.string.toast_manga_not_loaded)
                    val url = detail.thumb
                        ?: return makeToast(R.string.toast_manga_no_cover)
                    shareImage(url, "${detail.id}-thumb")
                }
            }).show()

            true
        }

        binding.tags.adapter = tagAdapter

        val contentAdapter = ContentAdapter(this, id, providerId)
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

        binding.title.text = title
        viewModel.detail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    binding.multiStateView.viewState = ViewState.Content

                    val detail = result.data
                    detail.thumb?.let { setupThumb(it) }

                    binding.title.text = detail.title

                    detail.metadata.authors?.let {
                        if (it.isEmpty()) null
                        else it.joinToString(separator = ";")
                    }.let {
                        binding.author.isVisible = (it != null)
                        binding.author.text = it
                    }

                    detail.metadata.status.let {
                        binding.status.isVisible = (it != null)
                        binding.status.text = it.toString()
                    }

                    detail.providerId.let {
                        val isFromProvider = (it != null)
                        binding.provider.isVisible = isFromProvider
                        binding.provider.text = it

                        if (!isFromProvider) binding.backdrop.setOnLongClickListener {
                            findNavController().navigate(R.id.action_to_gallery_edit)
                            true
                        }
                        else binding.backdrop.setOnLongClickListener(null)

                        binding.provider.isVisible = isFromProvider
                        binding.provider.text = it
                    }

                    setupSource(detail.source)
                    setupDescription(detail.metadata.description)
                    setupTags(detail.metadata.tags)
                    setupCollections(detail.collections)
                }
                is Result.Error ->
                    binding.multiStateView.viewState = ViewState.Error(result.exception)
                null -> binding.multiStateView.viewState = ViewState.Loading
            }
        }

        viewModel.history.observe(viewLifecycleOwner) { history ->
            binding.chapters.markedPosition = history?.let {
                MarkedPosition(it.collectionIndex, it.chapterIndex, it.pageIndex)
            }
        }
    }

    private fun setupThumb(thumb: String) {
        Glide.with(this)
            .load(thumb)
            .placeholder(binding.thumb.drawable)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.thumb)

        Glide.with(this)
            .load(thumb)
            .placeholder(binding.backdrop.drawable)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.backdrop)
    }

    private fun setupDescription(description: String?) {
        val hasDescription = !description.isNullOrBlank()
        binding.description.isVisible = hasDescription
        binding.descriptionEllipsizeHint.isVisible = hasDescription
        binding.descriptionEllipsizeHintScrim.isVisible = hasDescription

        binding.description.text = description

        fun updateHint() = binding.description.doOnLayout {
            fun TextView.hasEllipsize() = layout.text.toString() != text
            val visibility =
                if (binding.description.hasEllipsize()) View.VISIBLE
                else View.INVISIBLE
            binding.descriptionEllipsizeHint.visibility = visibility
            binding.descriptionEllipsizeHintScrim.visibility = visibility
        }

        if (hasDescription) updateHint()
        binding.description.setOnClickListener {
            binding.description.maxLines =
                if (binding.description.maxLines < Int.MAX_VALUE) Int.MAX_VALUE else 3
            updateHint()
        }
        binding.description.setOnLongClickListener {
            copyToClipboard(binding.description.text as String)
            makeToast(R.string.toast_manga_description_copied)
            true
        }
    }

    private fun setupSource(source: Source?) {
        binding.source.isVisible = source != null
        if (source == null) return
        binding.source.text = "From ${source.providerId} - ${source.mangaId} ${source.state}"

        when (source.state) {
            SourceState.DOWNLOADING -> binding.source.setTextColor(R.color.blue_400)
            SourceState.WAITING -> binding.source.setTextColor(R.color.green_400)
            SourceState.ERROR -> binding.source.setTextColor(R.color.red_400)
        }
        binding.source.setOnLongClickListener {
            viewModel.syncSource()
            true
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

    private fun setupCollections(collections: List<Collection>) {
        if (collections.isNotEmpty()) {
            binding.chapters.isVisible = true
            binding.noChapterHint.isVisible = false
            binding.chapters.collections = collections
        } else {
            binding.chapters.isVisible = false
            binding.noChapterHint.isVisible = true
        }
    }

    private fun setupActionButton() {
        binding.readButton.setOnClickListener {
            (viewModel.detail.value as? Result.Success)?.let { result ->
                val detail = result.data
                viewModel.history.value?.let { history ->
                    navToReaderActivity(
                        detail.id,
                        detail.providerId,
                        history.collectionIndex,
                        history.chapterIndex,
                        history.pageIndex
                    )
                } ?: navToReaderActivity(detail.id, detail.providerId)
            }
        }

        val isFromProvider = (providerId != null)
        binding.libraryAddButton.isVisible = isFromProvider
        binding.libraryAddButton.setOnClickListener {
            viewModel.addMangaToLibrary(false)
        }
        binding.libraryAddButton.setOnLongClickListener {
            viewModel.addMangaToLibrary(true)
            true
        }
    }
}
