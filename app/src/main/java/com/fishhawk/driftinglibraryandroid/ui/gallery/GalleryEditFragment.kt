package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GalleryEditFragmentBinding
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaStatus
import com.fishhawk.driftinglibraryandroid.data.remote.model.TagGroup
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.bindToFeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.ProvideWindowInsets

class GalleryEditFragment : Fragment() {
    private lateinit var binding: GalleryEditFragmentBinding
    private val viewModel: GalleryViewModel by navGraphViewModels(R.id.nav_graph_gallery_internal) {
        MainViewModelFactory(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GalleryEditFragmentBinding.inflate(inflater, container, false)
        binding.authors.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    val detail by viewModel.detail.observeAsState()
                    detail?.metadata?.authors?.let { TagGroup("", it) }
                }
            }
        }
        binding.tags.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    val detail by viewModel.detail.observeAsState()
                    detail?.metadata?.tags?.flatMap {
                        if (it.key.isBlank()) it.value
                        else it.value.map { value -> "${it.key}:$value" }
                    }?.let { TagGroup("", it) }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)

        bindToFeedbackViewModel(viewModel)

        val detail = viewModel.detail.value!!

        // edit title
        binding.title.setText(detail.title)

        // edit status
        when (detail.metadata.status) {
            MangaStatus.COMPLETED -> binding.radioCompleted.isChecked = true
            MangaStatus.ONGOING -> binding.radioOngoing.isChecked = true
            else -> binding.radioUnknown.isChecked = true
        }

        // edit authors
        binding.authorInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                authorsAdapter.addTag(binding.authorInput.text.toString())
                binding.authorInput.text = null
                true
            } else false
        }

        // edit tags
        binding.tagInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                tagsAdapter.addTag(binding.tagInput.text.toString())
                binding.tagInput.text = null
                true
            } else false
        }

        // edit description
        binding.description.setText(detail.metadata.description)
    }

//    private fun buildMetadata(): MetadataDetail {
//        val title = binding.title.text.toString()
//        val authors = authorsAdapter.list
//        val status = when (binding.status.checkedRadioButtonId) {
//            binding.radioCompleted.id -> MangaStatus.COMPLETED
//            binding.radioOngoing.id -> MangaStatus.ONGOING
//            else -> MangaStatus.UNKNOWN
//        }
//        val tagsMap = mutableMapOf<String, MutableList<String>>()
//        tagsAdapter.list.forEach {
//            val key = it.substringBefore(':', "")
//            val value = it.substringAfter(':')
//            tagsMap.getOrPut(key) { mutableListOf() }.add(value)
//        }
//        val tags = tagsMap.map { TagGroup(it.key, it.value) }
//        val description = binding.description.text.toString()
//
//        return MetadataDetail(title, authors, status, description, tags)
//    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_publish -> {
//                viewModel.updateMetadata(buildMetadata())
                true
            }
            else -> false
        }
    }
}
