package com.fishhawk.driftinglibraryandroid.history

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private val viewModel: HistoryViewModel by viewModels {
        val readingHistoryRepository =
            (requireContext().applicationContext as MainApplication).readingHistoryRepository
        HistoryViewModelFactory(readingHistoryRepository)
    }
    private lateinit var binding: FragmentHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        viewModel.readingHistoryList.observe(viewLifecycleOwner, Observer { data ->
            binding.list.apply {
//            addItemDecoration(GridSpacingItemDecoration(mColumnCount, 16, true))

                // set adapter
                adapter = HistoryListAdapter(context, data) { item, imageView ->
//                val extras = FragmentNavigatorExtras(imageView to item.id)
//                val bundle = bundleOf(
//                    "id" to item.id,
//                    "title" to item.title,
//                    "thumb" to item.thumb
//                )
//                findNavController().navigate(R.id.action_library_to_gallery, bundle, null, extras)
                }

                // set transition
                viewTreeObserver.addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
            }
            if (data.isEmpty()) binding.multipleStatusView.showEmpty()
            else binding.multipleStatusView.showContent()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_history, menu)
    }
}
