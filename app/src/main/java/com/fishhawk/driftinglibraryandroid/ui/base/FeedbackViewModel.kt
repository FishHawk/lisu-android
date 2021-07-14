package com.fishhawk.driftinglibraryandroid.ui.base

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.data.remote.ResultX

sealed class Feedback {
    class Hint(val resId: Int) : Feedback()
    class Failure(val exception: Throwable) : Feedback()
}

fun Context.feedback(feedback: Feedback) {
    when (feedback) {
        is Feedback.Hint -> toast(feedback.resId)
        is Feedback.Failure -> toast(feedback.exception)
    }
}

open class FeedbackViewModel : ViewModel() {
    private val _feedback: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val feedback: LiveData<Event<Feedback>> = _feedback

    protected fun feed(resId: Int) {
        _feedback.value = Event(Feedback.Hint(resId))
    }

    protected fun feed(throwable: Throwable) {
        _feedback.value = Event(Feedback.Failure(throwable))
    }

    protected fun <T> resultWarp(result: ResultX<T>, runIfSuccess: (T) -> Unit) {
        result.onSuccess { runIfSuccess(it) }.onFailure { feed(it) }
    }
}