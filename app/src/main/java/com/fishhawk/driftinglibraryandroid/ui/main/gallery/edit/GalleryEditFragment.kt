package com.fishhawk.driftinglibraryandroid.ui.main.gallery.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.databinding.GalleryEditFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaStatus
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MetadataDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.TagGroup
import com.fishhawk.driftinglibraryandroid.ui.main.gallery.GalleryViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.main.gallery.detail.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.ui.main.gallery.detail.TagGroupAdapter

class GalleryEditFragment : Fragment() {
    internal val viewModel: GalleryViewModel by activityViewModels {
        val application = requireActivity().application as MainApplication
        GalleryViewModelFactory(application)
    }
    internal lateinit var binding: GalleryEditFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GalleryEditFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detail = (viewModel.detail.value as Result.Success).data

        Glide.with(this)
            .load(detail.thumb)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.thumb)

        Glide.with(this)
            .load(detail.thumb)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.backdrop)

        // edit title
        binding.title.setText(detail.title)

        // edit authors
        val authorsAdapter = TagGroupAdapter()
        detail.metadata.authors?.let { authorsAdapter.setList(it) }
        // onTagAddClicked = { createNewAuthorDialog { authorsAdapter.addTag(it) } }
        binding.authors.adapter = authorsAdapter

        // edit status
        when (detail.metadata.status) {
            MangaStatus.COMPLETED -> binding.radioCompleted.isChecked = true
            MangaStatus.ONGOING -> binding.radioOngoing.isChecked = true
            else -> binding.radioUnknown.isChecked = true
        }

        // edit description
        binding.description.setText(detail.metadata.description)

        // edit tags
        val tagsAdapter = TagGroupAdapter()
        detail.metadata.tags?.flatMap {
            if (it.key.isBlank()) it.value
            else it.value.map { value -> "${it.key}:$value" }
        }?.let { tagsAdapter.setList(it) }
        // tagsAdapter.onTagAddClicked = { createNewTagDialog { tagsAdapter.addTag(it) } }
        binding.tags.adapter = tagsAdapter

        // submit
        fun submit() {
            val title = binding.title.text.toString()
            val authors = authorsAdapter.list
            val status = when (binding.status.checkedRadioButtonId) {
                binding.radioCompleted.id -> MangaStatus.COMPLETED
                binding.radioOngoing.id -> MangaStatus.ONGOING
                else -> MangaStatus.UNKNOWN
            }
            val tagsMap = mutableMapOf<String, MutableList<String>>()
            tagsAdapter.list.forEach {
                val key = it.substringBefore(':', "")
                val value = it.substringAfter(':')
                tagsMap.getOrPut(key) { mutableListOf() }.add(value)
            }
            val tags = tagsMap.map { TagGroup(it.key, it.value) }
            val description = binding.description.text.toString()

            val metadata = MetadataDetail(title, authors, status, description, tags)
            println(metadata)
        }
    }

}
