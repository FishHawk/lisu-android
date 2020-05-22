package com.fishhawk.driftinglibraryandroid.gallery

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.fishhawk.driftinglibraryandroid.MainActivity
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GalleryActivityBinding
import com.fishhawk.driftinglibraryandroid.reader.ReaderActivity
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Collection
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.repository.data.TagGroup
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.navToMainActivity
import com.fishhawk.driftinglibraryandroid.util.navToReaderActivity
import com.google.android.flexbox.FlexboxLayout

class GalleryActivity : AppCompatActivity() {
    private val viewModel: GalleryViewModel by viewModels {
        val arguments = intent.extras!!
        val id = arguments.getString("id")!!

        val application = applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        val readingHistoryRepository = application.readingHistoryRepository

        GalleryViewModelFactory(id, remoteLibraryRepository, readingHistoryRepository)
    }
    private lateinit var binding: GalleryActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (SettingsHelper.theme.getValueDirectly()) {
            SettingsHelper.THEME_LIGHT -> setTheme(R.style.AppTheme_NoActionBar)
            SettingsHelper.THEME_DARK -> setTheme(R.style.AppTheme_Dark_NoActionBar)
        }

        supportPostponeEnterTransition()

        binding = GalleryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val arguments = intent.extras!!
        val id: String? = arguments.getString("id")
        val title: String? = arguments.getString("title")
        val thumb: String? = arguments.getString("thumb")

//        (requireActivity() as AppCompatActivity).supportActionBar?.title = title

        binding.thumb.apply {
            transitionName = id
            Glide.with(this).load(thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        supportStartPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        supportStartPostponedEnterTransition()
                        return false
                    }
                })
                .into(this)
        }

        binding.readButton.setOnClickListener {
            when (viewModel.mangaDetail.value) {
                is Result.Success -> {
                    val id = (viewModel.mangaDetail.value!! as Result.Success).data.id
                    viewModel.readingHistory.value?.let {
                        navToReaderActivity(
                            id,
                            it.collectionIndex,
                            it.chapterIndex,
                            it.pageIndex
                        )
                    } ?: navToReaderActivity(id)
                }
            }
        }

        viewModel.mangaDetail.observe(this, Observer { result ->
            when (result) {
                is Result.Success -> bindTags(result.data.tags, binding.tags)
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
                bindContent(result.data.collections, history)
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

    private fun bindContent(collections: List<Collection>, history: ReadingHistory?) {
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
                            chapter, collectionIndex, chapterIndex, history.pageIndex
                        )
                    )
                else
                    items.add(
                        ContentItem.Chapter(
                            chapter, collectionIndex, chapterIndex
                        )
                    )
            }
        }
        binding.content.adapter = ContentAdapter(this, items)
        { collectionIndex, chapterIndex, pageIndex ->
            val id = (viewModel.mangaDetail.value!! as Result.Success).data.id
            navToReaderActivity(id, collectionIndex, chapterIndex, pageIndex)
        }
    }

    private fun bindTags(
        tags: List<TagGroup>?,
        tagsLayout: LinearLayout
    ) {
        val noTagsView = tagsLayout.findViewById<TextView>(R.id.no_tags)

        tagsLayout.removeViews(1, tagsLayout.childCount - 1)
        if (tags == null || tags.isEmpty()) {
            noTagsView.visibility = View.VISIBLE
            return
        } else {
            noTagsView.visibility = View.GONE
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
