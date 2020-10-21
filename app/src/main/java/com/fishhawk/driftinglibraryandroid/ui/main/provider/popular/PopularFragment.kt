package com.fishhawk.driftinglibraryandroid.ui.main.provider.popular

import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels
import com.fishhawk.driftinglibraryandroid.ui.main.provider.base.ProviderBaseFragment

class PopularFragment : ProviderBaseFragment() {
    override val viewModel: PopularViewModel by viewModels { getViewModelFactory() }

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return optionModels.popular
    }
}
