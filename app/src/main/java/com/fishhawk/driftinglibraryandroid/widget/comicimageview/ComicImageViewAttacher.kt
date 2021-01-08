package com.fishhawk.driftinglibraryandroid.widget.comicimageview

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.max
import kotlin.math.min

private fun Matrix.getScale(): Float {
    val values = FloatArray(9)
    getValues(values)
    val scaleX = values[Matrix.MSCALE_X]
    val scaleY = values[Matrix.MSCALE_Y]
    return (scaleX + scaleY) / 2
}

private fun Matrix.getTranslate(): Pair<Float, Float> {
    val values = FloatArray(9)
    getValues(values)
    val scaleX = values[Matrix.MTRANS_X]
    val scaleY = values[Matrix.MTRANS_Y]
    return Pair(scaleX, scaleY)
}

@SuppressLint("ClickableViewAccessibility")
class ComicImageViewAttacher(private val imageView: ComicImageView) : View.OnTouchListener,
    View.OnLayoutChangeListener {

    companion object {
        private const val HORIZONTAL_EDGE_NONE = -1
        private const val HORIZONTAL_EDGE_LEFT = 0
        private const val HORIZONTAL_EDGE_RIGHT = 1
        private const val HORIZONTAL_EDGE_BOTH = 2

        private const val VERTICAL_EDGE_NONE = -1
        private const val VERTICAL_EDGE_TOP = 0
        private const val VERTICAL_EDGE_BOTTOM = 1
        private const val VERTICAL_EDGE_BOTH = 2

        private fun isSupportedScaleType(scaleType: ScaleType?): Boolean {
            return when (scaleType) {
                null -> false
                ScaleType.MATRIX -> throw IllegalStateException("Matrix scale type is not supported")
                else -> true
            }
        }
    }

    // Listeners
    val onScaleListener: OnScaleListener?
        get() = imageView.onScaleListener
    val onFlingListener: OnFlingListener?
        get() = imageView.onFlingListener
    val onDragListener: OnDragListener?
        get() = imageView.onDragListener
    val onTapListener: OnTapListener?
        get() = imageView.onTapListener

    var onClickListener: View.OnClickListener? = null
    var onLongClickListener: View.OnLongClickListener? = null

    // Properties
    private val minScale
        get() = imageView.minScale
    private val midScale
        get() = imageView.midScale
    private val maxScale
        get() = imageView.maxScale

    private val zoomable
        get() = imageView.zoomable
    private val isOpenCVEnabled
        get() = imageView.isOpenCVEnabled

    private val allowParentInterceptOnHorizontalEdge
        get() = imageView.allowParentInterceptOnHorizontalEdge
    private val allowParentInterceptOnVerticalEdge
        get() = imageView.allowParentInterceptOnVerticalEdge


    private var mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH
    private var mVerticalScrollEdge = VERTICAL_EDGE_BOTH

    private var drawable = imageView.drawable
    private var initScale = 1.0f
    private var initTranslateX = 0.0f
    private var initTranslateY = 0.0f
    var scaleType = ScaleType.FIT_CENTER
        set(value) {
            if (isSupportedScaleType(value) && value != field) {
                field = value
                resetLayout()
            }
        }

    private var fixScale = 1.0f
    private val matrix: Matrix = Matrix()

    private var customGestureDetector = CustomGestureDetector(
        imageView.context,
        object : CustomGestureDetector.OnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent?,
                distanceX: Float, distanceY: Float
            ): Boolean {
                translateImage(-distanceX, -distanceY)
                interceptTouchEventIfNeed(-distanceX, -distanceY)
                onDragListener?.onDrag(distanceX, distanceY)
                return true
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent,
                velocityX: Float, velocityY: Float
            ): Boolean {
                startFlingRunnable(-velocityX, -velocityY)
                onFlingListener?.onFling(e1, e2, velocityX, velocityY)
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleImage(detector.scaleFactor, detector.focusX, detector.focusY)
                onScaleListener?.onScale(detector.scaleFactor, detector.focusX, detector.focusY)
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                val scale = getScale()
                if (scale in minScale..maxScale)
                    resizeBitmap()
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val originScale = getScale()
                val targetScale = when {
                    originScale < midScale -> midScale
                    originScale >= midScale && originScale < maxScale -> maxScale
                    else -> minScale
                }
                startScaleRunnable(targetScale)
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onClickListener?.onClick(imageView)
                onTapListener?.onTap(imageView, e)
                return (onClickListener != null || onTapListener != null)
            }

            override fun onLongPress(e: MotionEvent?) {
                onLongClickListener?.onLongClick(imageView)
            }
        })

    private fun interceptTouchEventIfNeed(dx: Float, dy: Float) {
        if (!customGestureDetector.isScaling) {
            val reachHorizontalEdge = mHorizontalScrollEdge == HORIZONTAL_EDGE_BOTH
                    || mHorizontalScrollEdge == HORIZONTAL_EDGE_LEFT && dx >= 1f
                    || mHorizontalScrollEdge == HORIZONTAL_EDGE_RIGHT && dx <= -1f

            val reachVerticalEdge = mVerticalScrollEdge == VERTICAL_EDGE_BOTH
                    || mVerticalScrollEdge == VERTICAL_EDGE_TOP && dy >= 1f
                    || mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM && dy <= -1f

            if ((allowParentInterceptOnHorizontalEdge && reachHorizontalEdge)
                || (allowParentInterceptOnVerticalEdge && reachVerticalEdge)
            ) {
                imageView.parent.requestDisallowInterceptTouchEvent(false)
            }
        } else {
            imageView.parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    override fun onTouch(v: View, ev: MotionEvent): Boolean {
        if (!zoomable) return false

        var handled = false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                v.parent.requestDisallowInterceptTouchEvent(true)
                cancelFlingRunnable()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                val scale = getScale()
                val targetScale = when {
                    scale < minScale -> minScale
                    scale > maxScale -> maxScale
                    else -> null
                }
                targetScale?.let {
                    startScaleRunnable(it)
                    handled = true
                }
            }
        }
        handled = customGestureDetector.onTouchEvent(ev) || handled
        return handled
    }

    override fun onLayoutChange(
        v: View?,
        left: Int, top: Int, right: Int, bottom: Int,
        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
    ) {
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            resetLayout()
        }
    }

    init {
        imageView.setOnTouchListener(this)
        imageView.addOnLayoutChangeListener(this)
    }

    private var currentFlingRunnable: FlingRunnable? = null

    private fun startFlingRunnable(velocityX: Float, velocityY: Float) {
        correctBound()
        currentFlingRunnable = FlingRunnable(
            imageView,
            getDisplayRect(matrix),
            getImageViewWidth(imageView), getImageViewHeight(imageView),
            velocityX.toInt(), velocityY.toInt()
        ) { dx, dy ->
            translateImage(dx, dy)
        }
        imageView.post(currentFlingRunnable)
    }

    private fun cancelFlingRunnable() {
        currentFlingRunnable?.cancelFling()
        currentFlingRunnable = null
    }

    private fun startScaleRunnable(targetScale: Float) {
        correctBound()
        val rect = getDisplayRect(matrix)
        val runnable = ScaleRunnable(
            imageView,
            getScale(), targetScale,
            rect.centerX(), rect.centerY(),
            { newScale, focalX, focalY ->
                val deltaScale = newScale / getScale()
                scaleImage(deltaScale, focalX, focalY)
            },
            { resizeBitmap() }
        )
        imageView.post(runnable)
    }


    private fun translateImage(dx: Float, dy: Float) {
        matrix.postTranslate(dx, dy)
        correctBound()
        applyMatrix()
    }

    private fun scaleImage(scaleFactor: Float, focusX: Float, focusY: Float) {
        if (!scaleFactor.isFinite() || scaleFactor < 0) return
        if (getScale() < maxScale || scaleFactor < 1f) {
            matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            correctBound()
            applyMatrix()
        }
    }

    private fun getScale(): Float {
        return fixScale * matrix.getScale()
    }

    fun resetDrawable() {
        drawable = imageView.drawable
        resetLayout()
    }

    private fun resetLayout() {
        if (drawable == null) return

        val viewWidth = getImageViewWidth(imageView)
        val viewHeight = getImageViewHeight(imageView)
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        if (viewWidth <= 0 || viewHeight <= 0) return
        if (drawableWidth <= 0 || drawableHeight <= 0) return

        val widthScale = viewWidth.toFloat() / drawableWidth
        val heightScale = viewHeight.toFloat() / drawableHeight

        when (scaleType) {
            ScaleType.CENTER -> {
                initScale = 1.0F
                initTranslateX = (viewWidth - drawableWidth) / 2f
                initTranslateY = (viewHeight - drawableHeight) / 2f
            }
            ScaleType.CENTER_CROP -> {
                initScale = max(widthScale, heightScale)
                initTranslateX = (viewWidth - drawableWidth * initScale) / 2f
                initTranslateY = (viewHeight - drawableHeight * initScale) / 2f
            }
            ScaleType.CENTER_INSIDE -> {
                initScale = min(1.0f, min(widthScale, heightScale))
                initTranslateX = (viewWidth - drawableWidth * initScale) / 2f
                initTranslateY = (viewHeight - drawableHeight * initScale) / 2f
            }
            else -> {
                val tempSrc = RectF(0F, 0F, drawableWidth.toFloat(), drawableHeight.toFloat())
                val tempDst = RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
                val tempMatrix = Matrix()
                tempMatrix.reset()
                when (scaleType) {
                    ScaleType.FIT_CENTER -> tempMatrix.setRectToRect(
                        tempSrc, tempDst, ScaleToFit.CENTER
                    )
                    ScaleType.FIT_START -> tempMatrix.setRectToRect(
                        tempSrc, tempDst, ScaleToFit.START
                    )
                    ScaleType.FIT_END -> tempMatrix.setRectToRect(
                        tempSrc, tempDst, ScaleToFit.END
                    )
                    ScaleType.FIT_XY -> tempMatrix.setRectToRect(
                        tempSrc, tempDst, ScaleToFit.FILL
                    )
                    else -> {
                    }
                }
                initScale = tempMatrix.getScale()
                val (tx, ty) = tempMatrix.getTranslate()
                initTranslateX = tx
                initTranslateY = ty
            }
        }

        matrix.setTranslate(initTranslateX, initTranslateY)
        matrix.preScale(initScale, initScale)
        fixScale = 1 / initScale
        applyMatrix()

        resizeBitmap()
    }

    private fun resizeBitmap() {
        if (!isOpenCVEnabled) return

        val scale = matrix.getScale()
        val bitmap = ScaleAlgorithm.scale(drawable.toBitmap(), initScale * fixScale * scale)
        val newD = BitmapDrawable(imageView.context.resources, bitmap)
        (imageView as ComicImageView).setImageDrawableMy(newD)

        val (tx, ty) = matrix.getTranslate()
        fixScale *= scale
        matrix.setTranslate(tx, ty)
        applyMatrix()
    }

    private fun applyMatrix() {
        imageView.imageMatrix = matrix
    }

    private fun getDisplayRect(matrix: Matrix): RectF {
        val d: Drawable = imageView.drawable
        val rect = RectF(0F, 0F, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        matrix.mapRect(rect)
        return rect
    }

    private fun correctBound() {
        val rect: RectF = getDisplayRect(matrix)
        val height = rect.height()
        val width = rect.width()
        var deltaX = 0f
        var deltaY = 0f
        val viewHeight = getImageViewHeight(imageView)
        when {
            height <= viewHeight -> {
                deltaY = when (scaleType) {
                    ScaleType.FIT_START -> -rect.top
                    ScaleType.FIT_END -> viewHeight - height - rect.top
                    else -> (viewHeight - height) / 2 - rect.top
                }
                mVerticalScrollEdge = VERTICAL_EDGE_BOTH
            }
            rect.top > 0 -> {
                mVerticalScrollEdge = VERTICAL_EDGE_TOP
                deltaY = -rect.top
            }
            rect.bottom < viewHeight -> {
                mVerticalScrollEdge = VERTICAL_EDGE_BOTTOM
                deltaY = viewHeight - rect.bottom
            }
            else -> {
                mVerticalScrollEdge = VERTICAL_EDGE_NONE
            }
        }
        val viewWidth = getImageViewWidth(imageView)
        when {
            width <= viewWidth -> {
                mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH
                deltaX = when (scaleType) {
                    ScaleType.FIT_START -> -rect.left
                    ScaleType.FIT_END -> viewWidth - width - rect.left
                    else -> (viewWidth - width) / 2 - rect.left
                }
            }
            rect.left > 0 -> {
                mHorizontalScrollEdge = HORIZONTAL_EDGE_LEFT
                deltaX = -rect.left
            }
            rect.right < viewWidth -> {
                deltaX = viewWidth - rect.right
                mHorizontalScrollEdge = HORIZONTAL_EDGE_RIGHT
            }
            else -> {
                mHorizontalScrollEdge = HORIZONTAL_EDGE_NONE
            }
        }
        matrix.postTranslate(deltaX, deltaY)
    }


    private fun getImageViewWidth(imageView: ImageView): Int {
        return imageView.width - imageView.paddingLeft - imageView.paddingRight
    }

    private fun getImageViewHeight(imageView: ImageView): Int {
        return imageView.height - imageView.paddingTop - imageView.paddingBottom
    }
}