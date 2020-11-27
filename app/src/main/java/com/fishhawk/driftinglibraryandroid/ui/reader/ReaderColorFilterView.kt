package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toXfermode
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference

class ReaderColorFilterView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val colorFilterPaint: Paint = Paint()

    fun setFilterColor(color: Int, filterMode: GlobalPreference.ColorFilterMode) {
        colorFilterPaint.color = color
        colorFilterPaint.xfermode = when (filterMode) {
            GlobalPreference.ColorFilterMode.DEFAULT -> PorterDuff.Mode.SRC_OVER
            GlobalPreference.ColorFilterMode.MULTIPLY -> PorterDuff.Mode.MULTIPLY
            GlobalPreference.ColorFilterMode.SCREEN -> PorterDuff.Mode.SCREEN
            GlobalPreference.ColorFilterMode.OVERLAY -> PorterDuff.Mode.OVERLAY
            GlobalPreference.ColorFilterMode.LIGHTEN -> PorterDuff.Mode.LIGHTEN
            GlobalPreference.ColorFilterMode.DARKEN -> PorterDuff.Mode.DARKEN
        }.toXfermode()
        println(filterMode)

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPaint(colorFilterPaint)
    }
}
