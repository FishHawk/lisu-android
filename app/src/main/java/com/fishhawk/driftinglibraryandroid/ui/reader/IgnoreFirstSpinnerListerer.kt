package com.fishhawk.driftinglibraryandroid.ui.reader

import android.view.View
import android.widget.AdapterView

class IgnoreFirstSpinnerListener(
    private val onSelected: (Int) -> Unit
) : AdapterView.OnItemSelectedListener {
    private var hasSelected = false

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (hasSelected) onSelected(position)
        else hasSelected = true
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
