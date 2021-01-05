package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.data.Result

open class FeedbackViewModel : ViewModel() {
    private val _feedback: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val feedback: LiveData<Event<Feedback>> = _feedback

    protected fun feed(resId: Int) {
        _feedback.value = Event(Feedback.Hint(resId))
    }

    protected fun feed(throwable: Throwable) {
        _feedback.value = Event(Feedback.Failure(throwable))
    }

    protected fun <T> resultWarp(result: Result<T>, runIfSuccess: (T) -> Unit) {
        when (result) {
            is Result.Success -> runIfSuccess(result.data)
            is Result.Error -> feed(result.exception)
        }
    }
}

fun Fragment.bindToFeedbackViewModel(viewModel: FeedbackViewModel) {
    viewModel.feedback.observe(viewLifecycleOwner, EventObserver {
        processFeedback(it)
    })
}