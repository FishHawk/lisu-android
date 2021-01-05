package com.fishhawk.driftinglibraryandroid.widget

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.fishhawk.driftinglibraryandroid.databinding.StatePageEmptyBinding
import com.fishhawk.driftinglibraryandroid.databinding.StatePageErrorBinding
import com.fishhawk.driftinglibraryandroid.databinding.StatePageLoadingBinding
import kotlinx.parcelize.Parcelize


sealed class ViewState : Parcelable {
    @Parcelize
    object Loading : ViewState()

    @Parcelize
    object Content : ViewState()

    @Parcelize
    object Empty : ViewState()

    @Parcelize
    data class Error(val exception: Throwable) : ViewState()
}

class MultiStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var contentView: View? = null

    val loadingBinding: StatePageLoadingBinding
    val errorBinding: StatePageErrorBinding
    val emptyBinding: StatePageEmptyBinding

    init {
        val inflater = LayoutInflater.from(getContext())
        loadingBinding = StatePageLoadingBinding.inflate(inflater, this, true)
        errorBinding = StatePageErrorBinding.inflate(inflater, this, true)
        emptyBinding = StatePageEmptyBinding.inflate(inflater, this, true)
    }

    var viewState: ViewState = ViewState.Loading
        set(value) {
            if (value != field) {
                field = value
                updateView()
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (contentView == null) throw IllegalArgumentException("Content view is not defined")
        updateView()
    }


    private fun updateView() {
        loadingBinding.root.isVisible = false
        errorBinding.root.isVisible = false
        emptyBinding.root.isVisible = false
        contentView?.isVisible = false

        val selectedView = when (viewState) {
            ViewState.Loading -> loadingBinding.root
            ViewState.Empty -> emptyBinding.root
            is ViewState.Error -> errorBinding.root
            ViewState.Content -> requireNotNull(contentView)
        }
        selectedView.isVisible = true
    }


    /**
     * Override addView for get contentView from xml
     */

    override fun addView(child: View) {
        contentView = child
        super.addView(child)
    }

    override fun addView(child: View, index: Int) {
        contentView = child
        super.addView(child, index)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        contentView = child
        super.addView(child, index, params)
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        contentView = child
        super.addView(child, params)
    }

    override fun addView(child: View, width: Int, height: Int) {
        contentView = child
        super.addView(child, width, height)
    }

    override fun addViewInLayout(child: View, index: Int, params: ViewGroup.LayoutParams): Boolean {
        contentView = child
        return super.addViewInLayout(child, index, params)
    }

    override fun addViewInLayout(
        child: View,
        index: Int,
        params: ViewGroup.LayoutParams,
        preventRequestLayout: Boolean
    ): Boolean {
        contentView = child
        return super.addViewInLayout(child, index, params, preventRequestLayout)
    }


    /**
     * Save and restore ViewState
     */

    private class SavedState(
        superState: Parcelable,
        val state: ViewState
    ) :
        BaseSavedState(superState) {

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(state, flags)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return when (val superState = super.onSaveInstanceState()) {
            null -> superState
            else -> SavedState(superState, viewState)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            viewState = state.state
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}