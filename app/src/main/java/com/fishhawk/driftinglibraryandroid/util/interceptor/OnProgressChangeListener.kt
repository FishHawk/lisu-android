package com.fishhawk.driftinglibraryandroid.util.interceptor

interface OnProgressChangeListener {

    fun onProgressChange(bytesRead: Long, contentLength: Long, done: Boolean)
}