package com.fishhawk.driftinglibraryandroid.widget

import android.view.View
import android.widget.AdapterView

class SimpleSpinnerListener(
    private val onEach: (Int) -> Unit
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onEach(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
