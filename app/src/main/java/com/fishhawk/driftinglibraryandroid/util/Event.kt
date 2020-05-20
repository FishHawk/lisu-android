package com.fishhawk.driftinglibraryandroid.util

import androidx.lifecycle.Observer

open class Event<out T>(private val content: T) {
    private var handled = false

    fun handle(handleFunction: (T) -> Unit) {
        if (!handled) {
            handled = true
            handleFunction(content)
        }
    }

    fun peekContent(): T = content
}

class EventObserver<T>(private val onEventUnhandledContent: (T?) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.handle(onEventUnhandledContent)
    }
}
