package com.fishhawk.driftinglibraryandroid.ui.gallery.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GalleryFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.makeToast
import com.fishhawk.driftinglibraryandroid.ui.base.setupFeedbackModule
import com.fishhawk.driftinglibraryandroid.ui.extension.*
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryViewModelFactory
import kotlinx.android.synthetic.main.gallery_fragment.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class GalleryFragment : Fragment() {
    internal val viewModel: GalleryViewModel by viewModels {
        val application = requireActivity().application as MainApplication
        GalleryViewModelFactory(application)
    }
    internal lateinit var binding: GalleryFragmentBinding

    private var providerId: String? = null
    private val tagAdapter = TagGroupListAdapter(object : TagGroupListAdapter.Listener {
        override fun onTagClick(key: String, value: String) {
            val keywords = if (key.isBlank()) value else "${key}:$value"
            providerId?.let {
                TODO("")
//                navToProviderActivity(it, keywords)
            } ?: navToMainActivity(keywords)
        }

        override fun onTagLongClick(key: String, value: String) {
            val keywords = if (key.isBlank()) value else "${key}:$value"
            copyToClipboard(keywords)
            makeToast(R.string.toast_manga_tag_saved)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFeedbackModule(viewModel)

        val arguments = requireActivity().intent.extras!!
        val id: String = arguments.getString("id")!!
        val title: String = arguments.getString("title")!!
        providerId = arguments.getString("providerId")
        val thumb = arguments.getString("thumb")

        providerId?.let {
            viewModel.openMangaFromProvider(it, id)
        } ?: viewModel.openMangaFromLibrary(id)

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
            GalleryThumbSheet(this).show()
            true
        }

        binding.tags.adapter = tagAdapter

        val contentAdapter = ContentAdapter(this, id, providerId)
        binding.chapters.adapter = contentAdapter

        binding.displayModeButton.setOnClickListener {
            SettingsHelper.chapterDisplayMode.setNextValue()
        }
        binding.displayOrderButton.setOnClickListener {
            SettingsHelper.chapterDisplayOrder.setNextValue()
        }

        SettingsHelper.chapterDisplayMode.observe(viewLifecycleOwner) {
            binding.displayModeButton.setIconResource(getChapterDisplayModeIcon())
            binding.chapters.viewMode = when (it) {
                SettingsHelper.ChapterDisplayMode.GRID -> ContentView.ViewMode.GRID
                SettingsHelper.ChapterDisplayMode.LINEAR -> ContentView.ViewMode.LINEAR
            }
        }

        SettingsHelper.chapterDisplayOrder.observe(viewLifecycleOwner) {
            binding.chapters.viewOrder = when (it) {
                SettingsHelper.ChapterDisplayOrder.ASCEND -> ContentView.ViewOrder.ASCEND
                SettingsHelper.ChapterDisplayOrder.DESCEND -> ContentView.ViewOrder.DESCEND
            }
        }

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
                is Result.Loading -> binding.multipleStatusView.showLoading()
            }
        }

        viewModel.history.observe(viewLifecycleOwner) { history ->
            binding.contentView.chapters.markedPosition = history?.let {
                MarkedPosition(it.collectionIndex, it.chapterIndex, it.pageIndex)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            val uri = data?.data
            val content =
                uri?.let { requireContext().contentResolver.openInputStream(it)?.readBytes() }
            val type =
                uri?.let { requireContext().contentResolver.getType(uri)?.toMediaTypeOrNull() }
            if (content != null && type != null)
                viewModel.updateThumb(content.toRequestBody(type))
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
        binding.editButton.setOnClickListener { GalleryEditSheet(this).show() }
        binding.subscribeButton.setOnClickListener { viewModel.subscribe() }
        binding.downloadButton.setOnClickListener { viewModel.download() }
    }
}
