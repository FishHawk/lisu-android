package com.fishhawk.driftinglibraryandroid.ui.provider.category

import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels
import com.fishhawk.driftinglibraryandroid.ui.provider.base.ProviderBaseFragment

class CategoryFragment : ProviderBaseFragment() {
    override val viewModel: CategoryViewModel by viewModels { getViewModelFactory() }
    override val page: Int = 2

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return optionModels.category
    }
}
