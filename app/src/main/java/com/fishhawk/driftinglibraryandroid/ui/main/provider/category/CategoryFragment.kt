package com.fishhawk.driftinglibraryandroid.ui.main.provider.category

import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels
import com.fishhawk.driftinglibraryandroid.ui.main.provider.base.ProviderBaseFragment

class CategoryFragment : ProviderBaseFragment() {
    override val viewModel: CategoryViewModel by viewModels { getViewModelFactory() }

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return optionModels.category
    }
}
