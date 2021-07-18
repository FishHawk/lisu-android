package com.fishhawk.driftinglibraryandroid.widget.comicimageview

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class ComicImageView : AppCompatImageView {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)

    constructor(context: Context, attr: AttributeSet?, defStyle: Int)
            : super(context, attr, defStyle)

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        attacher.resetDrawable()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        attacher.resetDrawable()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        attacher.resetDrawable()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (changed) attacher.resetDrawable()
        return changed
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (attacher == null) {
            pendingScaleType = scaleType
        } else {
            attacher.scaleType = scaleType
        }
    }

    override fun getScaleType(): ScaleType {
        return attacher.scaleType
    }


    private var attacher: ComicImageViewAttacher = ComicImageViewAttacher(this)
    private var pendingScaleType: ScaleType? = null

    init {
        super.setScaleType(ScaleType.MATRIX)
        pendingScaleType?.let {
            attacher.scaleType = scaleType
        }
        pendingScaleType = null
    }

    fun setImageDrawableMy(drawable: Drawable?) {
        super.setImageDrawable(drawable)
    }

    var zoomable = true
    var isAreaInterpolationEnabled = true

    companion object {
        private const val DEFAULT_MAX_SCALE = 2.5f
        private const val DEFAULT_MID_SCALE = 1.5f
        private const val DEFAULT_MIN_SCALE = 1.0f
    }

    var minScale = DEFAULT_MIN_SCALE
    var midScale = DEFAULT_MID_SCALE
    var maxScale = DEFAULT_MAX_SCALE

    var allowParentInterceptOnHorizontalEdge = true
    var allowParentInterceptOnVerticalEdge = false

    var onScaleListener: OnScaleListener? = null
    var onFlingListener: OnFlingListener? = null
    var onDragListener: com.fishhawk.driftinglibraryandroid.widget.comicimageview.OnDragListener? = null
    var onTapListener: OnTapListener? = null

    override fun setOnClickListener(l: OnClickListener?) {
        attacher.onClickListener = l
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        attacher.onLongClickListener = l
    }
}