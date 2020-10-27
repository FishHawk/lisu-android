package com.fishhawk.driftinglibraryandroid.ui.provider

import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels

class LatestFragment : ProviderBaseFragment() {
    override val mangaListComponent: ProviderMangaListComponent by lazy { viewModel.latestMangaList }
    override val page: Int = 1

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return optionModels.latest
    }
}
