package com.fishhawk.driftinglibraryandroid.ui.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Event
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result

open class OperationViewModel : ViewModel() {
    private val _feedback: MutableLiveData<Event<OperationFeedback>> = MutableLiveData()
    val feedback: LiveData<Event<OperationFeedback>> = _feedback

    protected fun feed(resId: Int) {
        _feedback.value = Event(OperationFeedback.Id(resId))
    }

    private fun feed(message: String) {
        _feedback.value = Event(OperationFeedback.Message(message))
    }

    protected fun feed(throwable: Throwable) {
        throwable.message?.let { feed(it) } ?: feed(R.string.image_unknown_error_hint)
    }

    protected fun <T> resultWarp(result: Result<T>, runIfSuccess: (T) -> Unit) {
        when (result) {
            is Result.Success -> runIfSuccess(result.data)
            is Result.Error -> feed(result.exception)
        }
    }
}

sealed class OperationFeedback {
    class Id(val id: Int) : OperationFeedback()
    class Message(val message: String) : OperationFeedback()
}

fun Fragment.makeToast(resId: Int) {
    makeToast(getString(resId))
}

fun Fragment.makeToast(throwable: Throwable) {
    val message = throwable.message
    if (message == null) makeToast(R.string.toast_unknown_error)
    else makeToast(message)
}

private fun Fragment.makeToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.setupFeedbackModule(viewModel: OperationViewModel) {
    viewModel.feedback.observe(viewLifecycleOwner,
        EventObserver {
            when (it) {
                is OperationFeedback.Id -> makeToast(
                    it.id
                )
                is OperationFeedback.Message -> makeToast(
                    it.message
                )
            }
        })
}