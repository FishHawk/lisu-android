package com.fishhawk.driftinglibraryandroid.gallery

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionInflater
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
import com.fishhawk.driftinglibraryandroid.databinding.GalleryFragmentBinding
import com.fishhawk.driftinglibraryandroid.reader.ReaderActivity
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Collection
import com.fishhawk.driftinglibraryandroid.repository.data.TagGroup
import com.google.android.flexbox.FlexboxLayout

class GalleryFragment : Fragment() {
    private val viewModel: GalleryViewModel by viewModels {
        val id = arguments?.getString("id")!!
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        val readingHistoryRepository = application.readingHistoryRepository
        GalleryViewModelFactory(id, remoteLibraryRepository, readingHistoryRepository)
    }
    private lateinit var binding: GalleryFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        activity?.supportPostponeEnterTransition()
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
        super.onViewCreated(view, savedInstanceState)

        val id: String? = arguments?.getString("id")
        val title: String? = arguments?.getString("title")
        val thumb: String? = arguments?.getString("thumb")

        (requireActivity() as AppCompatActivity).supportActionBar?.title = title

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
                        activity?.supportStartPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        activity?.supportStartPostponedEnterTransition()
                        return false
                    }
                })
                .into(this)
        }

        binding.readButton.setOnClickListener {
            when (viewModel.mangaDetail.value) {
                is Result.Success -> {
                    viewModel.readingHistory.value?.let {
                        openChapter(it.collectionIndex, it.chapterIndex, it.pageIndex)
                    } ?: openChapter()
                }
            }
        }

        viewModel.mangaDetail.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    bindTags(result.data.tags, binding.tags)
                    bindContent(result.data.collections)
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

        viewModel.readingHistory.observe(viewLifecycleOwner, Observer { history ->
            if (history != null)
                (binding.content.adapter as ContentAdapter).markChapter(
                    history.collectionIndex,
                    history.chapterIndex,
                    history.pageIndex
                )
            else
                (binding.content.adapter as ContentAdapter).unmarkChapter()
        })
    }

    private fun openChapter(collectionIndex: Int = 0, chapterIndex: Int = 0, pageIndex: Int = 0) {
        val detail = (viewModel.mangaDetail.value!! as Result.Success).data
        val bundle = bundleOf(
            "detail" to detail,
            "collectionIndex" to collectionIndex,
            "chapterIndex" to chapterIndex,
            "pageIndex" to pageIndex
        )

        val intent = Intent(activity, ReaderActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun bindContent(collections: List<Collection>) {
        val items = mutableListOf<ContentItem>()
        for ((collectionIndex, collection) in collections.withIndex()) {
            if (collection.title.isNotEmpty())
                items.add(
                    ContentItem.CollectionHeader(
                        collection.title
                    )
                )
            for ((chapterIndex, chapter) in collection.chapters.withIndex()) {
                items.add(
                    ContentItem.Chapter(
                        chapter, collectionIndex, chapterIndex
                    )
                )
            }
        }
        binding.content.adapter = ContentAdapter(requireContext(), items, ::openChapter)
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
                tagGroupValueView.setOnClickListener {
                    val filter = "${tagGroup.key}:$value"
                    val bundle = bundleOf("filter" to filter)
                    findNavController().navigate(R.id.action_gallery_to_library, bundle)
                }
            }
        }
    }
}
