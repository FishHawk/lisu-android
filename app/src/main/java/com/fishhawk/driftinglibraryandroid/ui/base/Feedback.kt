package com.fishhawk.driftinglibraryandroid.ui.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fishhawk.driftinglibraryandroid.R

sealed class Feedback {
    object Silent : Feedback()
    class Hint(val resId: Int) : Feedback()
    class Failure(val exception: Throwable) : Feedback()
}

private fun Fragment.makeToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.makeToast(throwable: Throwable) {
    val message = throwable.message
    if (message == null) makeToast(R.string.toast_unknown_error)
    else makeToast(message)
}


fun Fragment.makeToast(resId: Int) {
    makeToast(getString(resId))
}

fun Fragment.processFeedback(feedback: Feedback) {
    when (feedback) {
        is Feedback.Hint -> makeToast(feedback.resId)
        is Feedback.Failure -> makeToast(feedback.exception)
        else -> return
    }
}
