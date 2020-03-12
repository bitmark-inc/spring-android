/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.remote.api.request

import com.bitmark.fbm.BuildConfig
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.InputStream

class InputStreamRequestBody(
    private val inputStream: InputStream,
    private val contentType: MediaType?,
    private val contentLength: Long,
    private val listener: ProgressListener
) : RequestBody() {

    private var ignored = false

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = contentLength

    override fun writeTo(sink: BufferedSink) {
        if (BuildConfig.DEBUG && !ignored) {
            // workaround for dealing with OkHttp Logging interceptor
            // this method is call twice since logging interceptor need to load this for logging
            // so ignore the first time call
            ignored = true
            return
        }
        val countingSink = CountingSink(sink, contentLength())
        val bufferSink = countingSink.buffer()
        bufferSink.writeAll(inputStream.source())
        bufferSink.flush()
    }

    inner class CountingSink(delegate: Sink, private val contentLength: Long) :
        ForwardingSink(delegate) {

        private var bytesWritten = 0L

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            listener.update(
                bytesWritten,
                contentLength,
                bytesWritten >= contentLength
            )
        }
    }

}

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}