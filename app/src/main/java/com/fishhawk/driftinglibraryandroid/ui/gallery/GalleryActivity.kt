package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GalleryActivityBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.*
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.TagGroup
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.gallery_activity.view.*

class GalleryActivity : AppCompatActivity() {
    private val viewModel: GalleryViewModel by viewModels {
        val application = applicationContext as MainApplication
        GalleryViewModelFactory(application)
    }
    private lateinit var binding: GalleryActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeWithTranslucentStatus()
        super.onCreate(savedInstanceState)
        setupFullScreen()

        binding = GalleryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val arguments = intent.extras!!
        val id: String = arguments.getString("id")!!
        val title: String = arguments.getString("title")!!
        val providerId: String? = arguments.getString("providerId")
        val thumb: String = arguments.getString("thumb")!!

        if (providerId == null) viewModel.openMangaFromLibrary(id)
        else viewModel.openMangaFromProvider(providerId, id)

        binding.info = GalleryInfo(providerId, title)
        setupThumb(thumb)
        setupActionButton()

        val adapter = ContentAdapter(this, id, providerId)
        binding.chapters.adapter = adapter
        binding.displayModeButton.setOnClickListener {
            SettingsHelper.chapterDisplayMode.setNextValue()
        }
        binding.displayOrderButton.setOnClickListener {
            SettingsHelper.chapterDisplayOrder.setNextValue()
        }

        SettingsHelper.chapterDisplayMode.observe(this, Observer {
            binding.displayModeButton.setIconResource(getChapterDisplayModeIcon())
            binding.chapters.viewMode = when (it) {
                SettingsHelper.ChapterDisplayMode.GRID -> ContentView.ViewMode.GRID
                SettingsHelper.ChapterDisplayMode.LINEAR -> ContentView.ViewMode.LINEAR
            }
        })

        SettingsHelper.chapterDisplayOrder.observe(this, Observer {
            binding.chapters.viewOrder = when (it) {
                SettingsHelper.ChapterDisplayOrder.ASCEND -> ContentView.ViewOrder.ASCEND
                SettingsHelper.ChapterDisplayOrder.DESCEND -> ContentView.ViewOrder.DESCEND
            }
        })

        viewModel.notification.observe(this, EventObserver {
            binding.root.makeToast(getNotificationMessage(it))
        })

        viewModel.detail.observe(this, Observer { result ->
            binding.contentView.visibility = View.GONE
            binding.loadingView.visibility = View.GONE
            binding.errorView.visibility = View.GONE
            when (result) {
                is Result.Success -> {
                    binding.contentView.visibility = View.VISIBLE
                    val detail = result.data
                    binding.info = GalleryInfo(detail)
                    binding.description.setOnClickListener {
                        binding.description.maxLines =
                            if (binding.description.maxLines < Int.MAX_VALUE) Int.MAX_VALUE else 3
                    }
                    if (!detail.metadata.tags.isNullOrEmpty()) bindTags(
                        detail.metadata.tags,
                        binding.tags
                    )
                    if (detail.collections.isNotEmpty())
                        binding.chapters.collections = detail.collections
                }
                is Result.Error -> {
                    binding.errorView.visibility = View.VISIBLE
                    binding.errorView.text = result.exception.message
                }
                is Result.Loading -> binding.loadingView.visibility = View.VISIBLE
            }
        })

        viewModel.history.observe(this, Observer { history ->
            binding.contentView.chapters.markedPosition = history?.let {
                MarkedPosition(it.collectionIndex, it.chapterIndex, it.pageIndex)
            }
        })
    }

    private fun setupThumb(thumb: String) {
        Glide.with(this)
            .load(thumb)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.thumb)

        Glide.with(this)
            .load(thumb)
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
        binding.subscribeButton.setOnClickListener { viewModel.subscribe() }
        binding.downloadButton.setOnClickListener { viewModel.download() }
    }

    private fun bindTags(
        tags: List<TagGroup>,
        tagsLayout: LinearLayout
    ) {
        tagsLayout.removeViews(0, tagsLayout.childCount)
        for (tagGroup in tags) {
            val tagGroupLayout = layoutInflater.inflate(
                R.layout.gallery_tag_group, tagsLayout, false
            ) as LinearLayout
            tagsLayout.addView(tagGroupLayout)

            // bind tag group key
            val tagGroupKeyView = tagGroupLayout.findViewById<TextView>(R.id.key)
            tagGroupKeyView.text = tagGroup.key

            // bind tag group value
            val tagGroupValueLayout: FlexboxLayout = tagGroupLayout.findViewById(R.id.value)
            for (value in tagGroup.value) {
                val tagGroupValueView = layoutInflater.inflate(
                    R.layout.gallery_tag, tagGroupValueLayout, false
                ) as TextView
                tagGroupValueLayout.addView(tagGroupValueView)
                tagGroupValueView.text = value
                tagGroupValueView.setOnClickListener { navToMainActivity("${tagGroup.key}:$value") }
            }
        }
    }
}
