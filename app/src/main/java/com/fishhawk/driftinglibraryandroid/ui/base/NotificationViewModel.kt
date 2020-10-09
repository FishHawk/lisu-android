package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.repository.Event
import com.fishhawk.driftinglibraryandroid.repository.Result

open class NotificationViewModel : ViewModel() {
    private val _notification: MutableLiveData<Event<Notification>> = MutableLiveData()
    val notification: LiveData<Event<Notification>> = _notification

    protected fun notify(notification: Notification) {
        _notification.value = Event(notification)
    }

    protected fun notifyNetworkError(throwable: Throwable) {
        _notification.value = Event(NetworkErrorNotification(throwable))
    }

    protected fun <T> resultWarp(result: Result<T>, runIfSuccess: (T) -> Unit) {
        when (result) {
            is Result.Success -> runIfSuccess(result.data)
            is Result.Error -> notifyNetworkError(result.exception)
        }
    }
}