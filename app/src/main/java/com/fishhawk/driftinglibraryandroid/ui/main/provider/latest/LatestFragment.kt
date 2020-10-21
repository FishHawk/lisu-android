package com.fishhawk.driftinglibraryandroid.ui.main.provider.latest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels
import com.fishhawk.driftinglibraryandroid.ui.main.provider.base.ProviderBaseFragment

class LatestFragment : ProviderBaseFragment() {
    override val viewModel: LatestViewModel by viewModels { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            setPadding(0, 100, 0, 0)
        }

    }

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return optionModels.latest
    }
}
