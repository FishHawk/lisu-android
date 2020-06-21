package com.fishhawk.driftinglibraryandroid.explore

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ExplorePopularFragmentBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.EventObserver

class PopularFragment : Fragment() {
    private val viewModel: PopularViewModel by viewModels {
        val source = arguments?.getString("source")!!
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        ExploreViewModelFactory(source, remoteLibraryRepository)
    }
    private lateinit var binding: ExplorePopularFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ExplorePopularFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mangaList.setup(viewModel, requireActivity())

        viewModel.load()

        SettingsHelper.displayMode.observe(viewLifecycleOwner, Observer {
            binding.mangaList.updateMangaListDisplayMode()
        })

        viewModel.mangaList.observe(viewLifecycleOwner, Observer { result ->
            binding.mangaList.onMangaListChanged(result)
        })

        viewModel.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver { exception ->
            binding.mangaList.onFetchMoreFinishEvent(exception)
        })

        viewModel.refreshFinish.observe(viewLifecycleOwner, EventObserver { exception ->
            binding.mangaList.onRefreshFinishEvent(exception)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_popular, menu)

        val item = menu.findItem(R.id.action_display_mode)
        when (SettingsHelper.displayMode.getValueDirectly()) {
            SettingsHelper.DISPLAY_MODE_GRID -> {
                item.setIcon(R.drawable.ic_baseline_view_list_24)
            }
            SettingsHelper.DISPLAY_MODE_LINEAR -> {
                item.setIcon(R.drawable.ic_baseline_view_module_24)
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                when (SettingsHelper.displayMode.getValueDirectly()) {
                    SettingsHelper.DISPLAY_MODE_GRID -> {
                        item.setIcon(R.drawable.ic_baseline_view_module_24)
                        SettingsHelper.displayMode.setValue(SettingsHelper.DISPLAY_MODE_LINEAR)
                    }
                    SettingsHelper.DISPLAY_MODE_LINEAR -> {
                        item.setIcon(R.drawable.ic_baseline_view_list_24)
                        SettingsHelper.displayMode.setValue(SettingsHelper.DISPLAY_MODE_GRID)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}