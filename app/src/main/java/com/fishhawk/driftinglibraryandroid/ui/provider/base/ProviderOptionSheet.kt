package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProviderOptionSheet(context: Context) : BottomSheetDialog(context) {
    private val binding = ProviderOptionSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)
    }

    fun setAdapter(adapter: OptionGroupListAdapter) {
        binding.options.adapter = adapter
    }
}