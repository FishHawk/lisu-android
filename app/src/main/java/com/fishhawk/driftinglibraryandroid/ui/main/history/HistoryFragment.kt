package com.fishhawk.driftinglibraryandroid.ui.main.history

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.HistoryFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.ui.extension.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.main.MainViewModelFactory

class HistoryFragment : Fragment() {
    private lateinit var binding: HistoryFragmentBinding
    val viewModel: HistoryViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HistoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HistoryListAdapter(requireActivity())
        adapter.onThumbClicked = {
            navToGalleryActivity(
                it.mangaId, it.title, it.thumb, it.providerId
            )
        }
        adapter.onCardClicked = {
            navToReaderActivity(
                it.mangaId, it.providerId, it.collectionIndex, it.chapterIndex, it.pageIndex
            )
        }
        binding.list.adapter = adapter

        viewModel.filteredReadingHistoryList.observe(viewLifecycleOwner, Observer {
            adapter.setList(it)
            if (it.isEmpty()) binding.multipleStatusView.showEmpty()
            else binding.multipleStatusView.showContent()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> createHistoryFilterSwitchDialog()
            R.id.action_clear_history -> createClearHistoryDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
