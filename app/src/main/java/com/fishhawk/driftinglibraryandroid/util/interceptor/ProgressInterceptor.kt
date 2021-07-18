package com.fishhawk.driftinglibraryandroid.util.interceptor

import okhttp3.Interceptor
import okhttp3.Response

typealias OnProgressChangeListener = (Float) -> Unit

class ProgressInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val responseBody = response.body ?: return response

        val url = request.url.toString()
        val listener = getListener(url) ?: return response

        val progressResponseBody = ProgressResponseBody(responseBody, listener)
        return response.newBuilder().body(progressResponseBody).build()
    }

    companion object {
        private val LISTENERS = hashMapOf<String, OnProgressChangeListener>()

        fun addListener(url: String, listener: OnProgressChangeListener) {
            LISTENERS[url] = listener
        }

        fun removeListener(url: String) {
            LISTENERS.remove(url)
        }

        fun getListener(url: String) = LISTENERS[url]
    }
}