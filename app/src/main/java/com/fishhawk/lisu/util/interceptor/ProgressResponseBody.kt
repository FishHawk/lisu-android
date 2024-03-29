package com.fishhawk.lisu.util.interceptor

import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer

class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val listener: OnProgressChangeListener
) : ResponseBody() {

    private val bufferedSource = object : ForwardingSource(responseBody.source()) {
        private var totalBytesRead = 0L
        override fun read(sink: Buffer, byteCount: Long): Long {
            return super.read(sink, byteCount).also {
                totalBytesRead += if (it != -1L) it else 0
                if (contentLength() > 0) {
                    val progress = (totalBytesRead.toFloat() / contentLength()).coerceIn(0f, 1f)
                    listener.invoke(progress)
                }
            }
        }
    }.buffer()

    override fun contentLength() = responseBody.contentLength()
    override fun contentType() = responseBody.contentType()
    override fun source(): BufferedSource = bufferedSource
}