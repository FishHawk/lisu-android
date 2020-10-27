package com.fishhawk.driftinglibraryandroid.ui.provider

import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels

class PopularFragment : ProviderBaseFragment() {
    override val mangaListComponent: ProviderMangaListComponent by lazy { viewModel.popularMangaList }
    override val page: Int = 0

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return optionModels.popular
    }
}
