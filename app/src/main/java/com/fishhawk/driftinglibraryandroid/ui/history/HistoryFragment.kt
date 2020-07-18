package com.fishhawk.driftinglibraryandroid.ui.history

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.HistoryFragmentBinding
import com.fishhawk.driftinglibraryandroid.extension.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.extension.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.ViewModelFactory


class HistoryFragment : Fragment() {
    val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory(requireActivity().application as MainApplication)
    }
    private lateinit var binding: HistoryFragmentBinding

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
            requireActivity().navToGalleryActivity(
                it.id, it.title, it.thumb, it.source
            )
        }
        adapter.onCardClicked = {
            requireActivity().navToReaderActivity(
                it.id, it.source, it.collectionIndex, it.chapterIndex, it.pageIndex
            )
        }
        binding.list.adapter = adapter

        viewModel.filteredReadingHistoryList.observe(viewLifecycleOwner, Observer { data ->
            adapter.changeList(data.toMutableList())
            if (data.isEmpty()) binding.multipleStatusView.showEmpty()
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
