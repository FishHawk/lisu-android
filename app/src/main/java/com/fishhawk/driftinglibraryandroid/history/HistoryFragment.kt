package com.fishhawk.driftinglibraryandroid.history

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.HistoryFragmentBinding
import com.fishhawk.driftinglibraryandroid.util.SpacingItemDecoration

class HistoryFragment : Fragment() {
    private val viewModel: HistoryViewModel by viewModels {
        val readingHistoryRepository =
            (requireContext().applicationContext as MainApplication).readingHistoryRepository
        HistoryViewModelFactory(readingHistoryRepository)
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
        binding.list.apply {
            addItemDecoration(SpacingItemDecoration(1, 16, true))

            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        postponeEnterTransition()
        viewModel.readingHistoryList.observe(viewLifecycleOwner, Observer { data ->
            binding.list.adapter = HistoryListAdapter(requireActivity(), data)
            if (data.isEmpty()) binding.multipleStatusView.showEmpty()
            else binding.multipleStatusView.showContent()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_history -> {
                AlertDialog.Builder(requireActivity())
                    .setMessage(R.string.dialog_clear_history)
                    .setPositiveButton(R.string.dialog_clear_history_positive) { _, _ ->
                        viewModel.clearReadingHistory()
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
