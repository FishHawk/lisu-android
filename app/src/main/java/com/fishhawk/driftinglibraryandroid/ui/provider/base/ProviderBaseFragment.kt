package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderBaseFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.preference.ProviderBrowseHistory
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.ui.base.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.ProviderViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.changeMangaListDisplayMode

abstract class ProviderBaseFragment : Fragment() {
    private val providerViewModel: ProviderViewModel by viewModels { getViewModelFactory() }
    abstract val viewModel: ProviderBaseViewModel

    private lateinit var binding: ProviderBaseFragmentBinding
    private lateinit var providerBrowseHistory: ProviderBrowseHistory

    private lateinit var providerId: String
    abstract val page: Int

    private val optionAdapter = OptionGroupListAdapter(object : OptionGroupListAdapter.Listener {
        override fun onOptionSelect(name: String, index: Int) {
            providerBrowseHistory.setOptionHistory(providerId, page, name, index)
            viewModel.selectOption(name, index)
            viewModel.load()
        }
    })

    private val actionAdapter = object : ProviderActionSheet.Listener {
        override fun onReadClick(outline: MangaOutline, provider: String) {
            navToReaderActivity(outline.id, providerId, 0, 0, 0)
        }

        override fun onDownloadClick(outline: MangaOutline, provider: String) {
            viewModel.download(outline.id, outline.title)
        }

        override fun onSubscribeClick(outline: MangaOutline, provider: String) {
            viewModel.subscribe(outline.id, outline.title)
        }
    }

    private val mangaAdapter = MangaListAdapter(object : MangaListAdapter.Listener {
        override fun onCardClick(outline: MangaOutline) {
            findNavController().navigate(
                R.id.action_to_gallery_detail,
                bundleOf(
                    "id" to outline.id,
                    "title" to outline.title,
                    "thumb" to outline.thumb,
                    "providerId" to providerId
                )
            )
        }

        override fun onCardLongClick(outline: MangaOutline) {
            ProviderActionSheet(requireContext(), outline, providerId, actionAdapter).show()
        }
    })

    abstract fun getOptionModel(optionModels: OptionModels): Map<String, List<String>>

    protected fun getViewModelFactory(): ProviderViewModelFactory {
        providerId = requireArguments().getString("providerId")!!
        val application = requireActivity().application as MainApplication
        return ProviderViewModelFactory(providerId, application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProviderBaseFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        providerBrowseHistory = ProviderBrowseHistory(requireContext())

        binding.mangaList.list.adapter = mangaAdapter

        binding.options.adapter = optionAdapter

        GlobalPreference.displayMode.observe(viewLifecycleOwner) {
            binding.mangaList.list.changeMangaListDisplayMode(mangaAdapter)
        }

        bindToListViewModel(
            binding.mangaList.multipleStatusView,
            binding.mangaList.refreshLayout,
            viewModel,
            mangaAdapter
        )
        providerViewModel.detail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    val model = getOptionModel(result.data.optionModels)
                    optionAdapter.setList(model.map { (name, options) ->
                        val selectedIndex =
                            providerBrowseHistory.getOptionHistory(providerId, page, name)
                        viewModel.selectOption(name, selectedIndex)
                        OptionGroup(name, options, selectedIndex)
                    })

                    if (viewModel.list.value == null) viewModel.load()
                }
            }
        }
    }
}
