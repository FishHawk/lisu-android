package com.fishhawk.driftinglibraryandroid.util.glide

interface OnProgressChangeListener {

    fun onProgressChange(bytesRead: Long, contentLength: Long, done: Boolean)
}