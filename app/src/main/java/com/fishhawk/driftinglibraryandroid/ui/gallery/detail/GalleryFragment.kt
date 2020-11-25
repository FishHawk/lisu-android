package com.fishhawk.driftinglibraryandroid.ui.gallery.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GalleryFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.makeToast
import com.fishhawk.driftinglibraryandroid.ui.base.bindToFeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.util.next
import com.fishhawk.driftinglibraryandroid.util.setNext
import kotlinx.android.synthetic.main.gallery_fragment.view.*
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
    ): View? {
        binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

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

        binding.info = GalleryInfo(providerId, title)
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

        viewModel.detail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    binding.multipleStatusView.showContent()

                    val detail = result.data
                    detail.thumb?.let { setupThumb(it) }

                    binding.info = GalleryInfo(detail)
                    binding.description.setOnClickListener {
                        binding.description.maxLines =
                            if (binding.description.maxLines < Int.MAX_VALUE) Int.MAX_VALUE else 3
                    }
                    binding.description.setOnLongClickListener {
                        copyToClipboard(binding.description.text as String)
                        makeToast(R.string.toast_manga_description_copied)
                        true
                    }

                    if (!detail.metadata.tags.isNullOrEmpty())
                        tagAdapter.setList(detail.metadata.tags)
                    if (detail.collections.isNotEmpty())
                        binding.chapters.collections = detail.collections
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                null -> binding.multipleStatusView.showLoading()
            }
        }

        viewModel.history.observe(viewLifecycleOwner) { history ->
            binding.contentView.chapters.markedPosition = history?.let {
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
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_gallery_edit)
        }
        binding.subscribeButton.setOnClickListener { viewModel.subscribe() }
        binding.downloadButton.setOnClickListener { viewModel.download() }
    }
}
