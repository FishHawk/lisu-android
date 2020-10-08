package com.fishhawk.driftinglibraryandroid.ui.provider.latest

import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels
import com.fishhawk.driftinglibraryandroid.ui.provider.base.ProviderBaseFragment

class LatestFragment : ProviderBaseFragment() {
    override val viewModel: LatestViewModel by viewModels { getViewModelFactory() }

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return optionModels.latest
    }
}
