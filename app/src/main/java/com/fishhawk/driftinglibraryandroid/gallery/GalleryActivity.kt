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
        val id: String? = arguments.getString("id")
        val title: String? = arguments.getString("title")
        val thumb: String? = arguments.getString("thumb")
        val source: String? = arguments.getString("source")

        binding.title.text = title
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

        binding.readButton.setOnClickListener {
            when (viewModel.mangaDetail.value) {
                is Result.Success -> {
                    viewModel.readingHistory.value?.let {
                        navToReaderActivity(
                            (viewModel.mangaDetail.value!! as Result.Success).data.id,
                            (viewModel.mangaDetail.value!! as Result.Success).data.source,
                            it.collectionIndex,
                            it.chapterIndex,
                            it.pageIndex
                        )
                    } ?: navToReaderActivity(
                        (viewModel.mangaDetail.value!! as Result.Success).data.id,
                        (viewModel.mangaDetail.value!! as Result.Success).data.source
                    )
                }
            }
        }

        viewModel.mangaDetail.observe(this, Observer { result ->
            println(result)
            when (result) {
                is Result.Success -> {
                    val detail = result.data

                    if (detail.author.isEmpty()) {
                        binding.author.text = "Unknown"
                    } else {
                        binding.author.text = detail.author.joinToString(" ")
                    }

                    binding.status.text = when (detail.status) {
                        0 -> "Completed"
                        1 -> "Ongoing"
                        else -> "Unknown"
                    }

                    binding.update.text = detail.update ?: "Unknown"
                    binding.source.text = source ?: "Unknown"

                    if (detail.description == null) {
                        binding.description.visibility = View.GONE
                    } else {
                        binding.description.text = detail.description

                    }

                    bindTags(result.data.tags, binding.tags)
                }
                is Result.Error -> println()
                is Result.Loading -> println()
            }
        })

        binding.content.apply {
            (layoutManager as GridLayoutManager).spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (adapter?.getItemViewType(position)) {
                            ContentAdapter.ViewType.CHAPTER.value -> 1
                            ContentAdapter.ViewType.CHAPTER_MARKED.value -> 1
                            else -> 3
                        }
                    }
                }
        }

        viewModel.readingHistory.observe(this, Observer { history ->
            if (binding.content.adapter == null) {
                val result = viewModel.mangaDetail.value as Result.Success
                bindContent(result.data.id, result.data.source, result.data.collections, history)
            } else {
                if (history != null)
                    (binding.content.adapter as ContentAdapter).markChapter(
                        history.collectionIndex,
                        history.chapterIndex,
                        history.pageIndex
                    )
                else (binding.content.adapter as ContentAdapter).unmarkChapter()
            }
        })
    }

    private fun bindContent(
        id: String,
        source: String?,
        collections: List<Collection>,
        history: ReadingHistory?
    ) {
        val items = mutableListOf<ContentItem>()
        for ((collectionIndex, collection) in collections.withIndex()) {
            if (collection.title.isNotEmpty())
                items.add(
                    ContentItem.CollectionHeader(
                        collection.title
                    )
                )
            for ((chapterIndex, chapter) in collection.chapters.withIndex()) {
                if (history != null && history.collectionIndex == collectionIndex && history.chapterIndex == chapterIndex)
                    items.add(
                        ContentItem.ChapterMarked(
                            chapter.name, collectionIndex, chapterIndex, history.pageIndex
                        )
                    )
                else
                    items.add(
                        ContentItem.Chapter(
                            chapter.name, collectionIndex, chapterIndex
                        )
                    )
            }
        }
        binding.content.adapter = ContentAdapter(this, id, source, items)
    }

    private fun bindTags(
        tags: List<TagGroup>?,
        tagsLayout: LinearLayout
    ) {
        tagsLayout.removeViews(0, tagsLayout.childCount)
        if (tags == null || tags.isEmpty()) {
            binding.tags.visibility = View.GONE
            return
        } else {
            binding.tags.visibility = View.VISIBLE
        }

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
