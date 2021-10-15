package com.fishhawk.lisu.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class Feedback {
    class Hint(val resId: Int) : Feedback()
    class Failure(val exception: Throwable) : Feedback()
}

open class FeedbackViewModel : ViewModel() {
    private val _feedback = Channel<Feedback>()
    val feedback = _feedback.receiveAsFlow()

    protected fun feed(resId: Int) {
        viewModelScope.launch { _feedback.send(Feedback.Hint(resId)) }
    }

    protected fun feed(throwable: Throwable) {
        viewModelScope.launch { _feedback.send(Feedback.Failure(throwable)) }
    }

    protected fun <T> resultWarp(result: Result<T>, runIfSuccess: (T) -> Unit) {
        result.onSuccess { runIfSuccess(it) }.onFailure { feed(it) }
    }
}

@Composable
fun Feedback(viewModel: FeedbackViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.feedback.collect {
            when (it) {
                is Feedback.Hint -> context.toast(it.resId)
                is Feedback.Failure -> context.toast(it.exception)
            }
        }
    }
}
