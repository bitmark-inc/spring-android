/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.archiveuploading.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import javax.inject.Inject


class UploadArchiveServiceHandler @Inject constructor(private val context: Context) {

    private var shouldUnbind = false

    private var service: UploadArchiveService? = null

    private var listener: UploadArchiveService.StateListener? = null

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            shouldUnbind = false
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val serviceBinder = binder as UploadArchiveService.ServiceBinder
            service = serviceBinder.service
            if (listener != null) service?.addStateListener(listener!!)
        }

    }

    fun bind() {
        if (shouldUnbind) return
        val intent = Intent(context, UploadArchiveService::class.java)
        if (context.bindService(intent, connection, 0)) {
            shouldUnbind = true
        }
    }

    fun unbind() {
        if (!shouldUnbind) return
        if (listener != null) service?.removeStateListener(listener!!)
        context.unbindService(connection)
        shouldUnbind = false
    }

    fun setListener(listener: UploadArchiveService.StateListener) {
        this.listener = listener
    }

}