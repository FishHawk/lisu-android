package com.fishhawk.driftinglibraryandroid.ui.provider

import com.fishhawk.driftinglibraryandroid.data.remote.model.OptionModels

class CategoryFragment : ProviderBaseFragment() {
    override val mangaListComponent: ProviderMangaListComponent by lazy { viewModel.categoryMangaList }
    override val page: Int = 2

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return optionModels.category
    }
}
