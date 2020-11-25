package com.fishhawk.driftinglibraryandroid.widget

import android.widget.SeekBar

open class SimpleSeekBarListener(
    private val onEach: (Int) -> Unit
) : SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
        if (fromUser) onEach(value)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}