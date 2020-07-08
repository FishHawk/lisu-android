package com.fishhawk.driftinglibraryandroid.gallery

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GalleryActivityBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Collection
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.repository.data.TagGroup
import com.fishhawk.driftinglibraryandroid.util.*
import com.google.android.flexbox.FlexboxLayout

class GalleryActivity : AppCompatActivity() {
    private val viewModel: GalleryViewModel by viewModels {
        val arguments = intent.extras!!
        val id = arguments.getString("id")!!
        val source = arguments.getString("source")

        val application = applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        val readingHistoryRepository = application.readingHistoryRepository

        GalleryViewModelFactory(id, source, remoteLibraryRepository, readingHistoryRepository)
    }
    private lateinit var binding: GalleryActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupThemeWithTranslucentStatus()
        setupFullScreen()

        binding = GalleryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val arguments = intent.extras!!
        val id: String = arguments.getString("id")!!
        val title: String = arguments.getString("title")!!
        val thumb: String = arguments.getString("thumb")!!
        val source: String? = arguments.getString("source")

        binding.detail = MangaDetail(
            source, id, title, thumb,
            null,
            null,
            null,
            null,
            null,
            mutableListOf()
        )
        setupThumb()
        setupActionButton()

        val adapter = ContentAdapter(this, id, source)
        binding.chapters.adapter = adapter
        binding.chapters.apply {
            (layoutManager as GridLayoutManager).spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (adapter.getItemViewType(position)) {
                            ContentAdapter.ViewType.CHAPTER.value -> 1
                            ContentAdapter.ViewType.CHAPTER_MARKED.value -> 1
                            else -> 3
                        }
                    }
                }
        }

        viewModel.operationError.observe(this, EventObserver { exception ->
            when (exception) {
                null -> binding.root.makeSnackBar("Success")
                else -> binding.root.makeSnackBar("Fail: ${exception.message}")
            }
        })

        viewModel.detail.observe(this, Observer { result ->
            binding.contentView.visibility = View.GONE
            binding.loadingView.visibility = View.GONE
            binding.errorView.visibility = View.GONE
            when (result) {
                is Result.Success -> {
                    binding.contentView.visibility = View.VISIBLE

                    val detail = result.data
                    binding.detail = detail

                    binding.status.text = when (detail.status) {
                        0 -> "Completed"
                        1 -> "Ongoing"
                        else -> "Unknown"
                    }

                    if (detail.description == null || detail.description.isBlank())
                        binding.description.visibility = View.GONE

                    if (detail.tags == null || detail.tags.isEmpty())
                        binding.tags.visibility = View.GONE
                    else bindTags(detail.tags, binding.tags)

                    if (detail.collections.isEmpty()) {
                        binding.chapters
                    }
                    bindContent(adapter, detail.collections)
                }
                is Result.Error -> {
                    binding.errorView.visibility = View.VISIBLE
                    binding.errorView.text = result.exception.message
                }
                is Result.Loading -> binding.loadingView.visibility = View.VISIBLE
            }
        })

        viewModel.readingHistory.observe(this, Observer { history ->
            if (history != null)
                history.let {
                    adapter.markChapter(it.collectionIndex, it.chapterIndex, it.pageIndex)
                }
            else adapter.unmarkChapter()
        })
    }

    private fun setupThumb() {
        val arguments = intent.extras!!
        val thumb: String = arguments.getString("thumb")!!
        binding.thumb.apply {
            Glide.with(this).load(thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(this)
        }
        binding.backdrop.apply {
            Glide.with(this).load(thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(this)
        }
    }

    private fun setupActionButton() {
        binding.readButton.setOnClickListener {
            (viewModel.detail.value as? Result.Success)?.let { result ->
                val detail = result.data
                viewModel.readingHistory.value?.let { history ->
                    navToReaderActivity(
                        detail.id,
                        detail.source,
                        history.collectionIndex,
                        history.chapterIndex,
                        history.pageIndex
                    )
                } ?: navToReaderActivity(detail.id, detail.source)
            }
        }
        binding.subscribeButton.setOnClickListener {
            (viewModel.detail.value as? Result.Success)?.let { viewModel.subscribe() }
        }
        binding.downloadButton.setOnClickListener {
            (viewModel.detail.value as? Result.Success)?.let { viewModel.download() }
        }
    }

    private fun bindContent(adapter: ContentAdapter, collections: List<Collection>) {
        val items = mutableListOf<ContentItem>()
        for ((collectionIndex, collection) in collections.withIndex()) {
            if (collection.title.isNotEmpty())
                items.add(ContentItem.CollectionHeader(collection.title))
            for ((chapterIndex, chapter) in collection.chapters.withIndex()) {
                items.add(ContentItem.Chapter(chapter.name, collectionIndex, chapterIndex))
            }
        }
        adapter.changeList(items)
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
