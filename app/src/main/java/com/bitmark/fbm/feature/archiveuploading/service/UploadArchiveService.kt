/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.archiveuploading.service

import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import com.bitmark.fbm.R
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.di.DaggerService
import com.bitmark.fbm.feature.connectivity.ConnectivityHandler
import com.bitmark.fbm.feature.notification.buildNotification
import com.bitmark.fbm.feature.notification.buildProgressNotificationBundle
import com.bitmark.fbm.feature.notification.pushNotification
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.logging.Tracer
import com.bitmark.fbm.util.Constants
import com.bitmark.fbm.util.ext.getFileName
import com.bitmark.fbm.util.ext.getFileSize
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class UploadArchiveService : DaggerService(), ConnectivityHandler.NetworkStateChangeListener {

    companion object {

        private const val URI = "uri"

        private const val TAG = "UploadArchiveService"

        fun getBundle(uri: String): Bundle {
            val bundle = Bundle()
            bundle.putString(URI, uri)
            return bundle
        }
    }

    @Inject
    internal lateinit var accountRepo: AccountRepository

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private val disposeBag = CompositeDisposable()

    private val binder = ServiceBinder()

    private val stateListeners = mutableListOf<StateListener>()

    private var uri: Uri? = null

    private var errorDuringUploading = false

    private val handler = Handler()

    fun addStateListener(stateListener: StateListener) {
        if (stateListeners.contains(stateListener)) return
        stateListeners.add(stateListener)
    }

    fun removeStateListener(stateListener: StateListener) {
        stateListeners.remove(stateListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        try {
            val uriString = intent?.extras?.getString(URI) ?: error("missing uri string")
            uri = Uri.parse(uriString) ?: error("invalid uri")
            execute(uri!!)
        } catch (e: Throwable) {
            Tracer.ERROR.log(
                TAG,
                "error when prepare uri: ${e.javaClass.canonicalName
                    ?: "UnknownError"}: ${e.message ?: "unknown"}"
            )
            logger.logError(Event.ARCHIVE_FILE_UPLOAD_ERROR, e)
            notifyError(e)
        }
        return START_NOT_STICKY
    }

    private fun execute(uri: Uri) {
        val fileSize = getFileSize(uri)
        val fileName = getFileName(uri)
        val fileInputStream = contentResolver.openInputStream(uri) ?: error("cannot open stream")

        // push foreground notification
        var notificationBundle = buildNotificationBundle(-1, -1)
        startForeground(
            Constants.UPLOAD_ARCHIVE_NOTIFICATION_ID,
            buildNotification(applicationContext, notificationBundle)
        )

        // using delay publisher to avoid backpressure for notification updating
        val publisher = PublishSubject.create<Int>()
        disposeBag.add(publisher.debounce(100, TimeUnit.MILLISECONDS).subscribe { percent ->
            notificationBundle =
                buildNotificationBundle(100, percent)
            pushNotification(applicationContext, notificationBundle)
        })

        // start executing
        disposeBag.add(accountRepo.uploadArchive(fileInputStream, fileSize) { progress ->
            val byteRead = progress.first
            val byteTotal = progress.second
            val percent = (byteRead * 100 / byteTotal).toInt()
            publisher.onNext(percent)
            notifyProgressChanged(fileName, byteRead, byteTotal)
        }.andThen(accountRepo.setArchiveUploaded()).subscribe({
            errorDuringUploading = false
            logger.logEvent(Event.ARCHIVE_FILE_UPLOAD_SUCCESS)
            notificationBundle = buildNotificationBundle(0, 0)
            notifyFinished()
            stopSelf()
        }, { e ->
            errorDuringUploading = true
            logger.logError(Event.ARCHIVE_FILE_UPLOAD_ERROR, e)
            Tracer.ERROR.log(
                TAG,
                "error in upload archive: ${e.javaClass.canonicalName
                    ?: "UnknownError"}: ${e.message ?: "unknown"}"
            )
            notifyError(e)
        }))
    }

    private fun buildNotificationBundle(maxProgress: Int, currentProgress: Int) =
        buildProgressNotificationBundle(
            getString(R.string.archive_uploading),
            if (currentProgress > 0) "$currentProgress%" else "",
            Constants.UPLOAD_ARCHIVE_NOTIFICATION_ID,
            maxProgress = maxProgress,
            currentProgress = currentProgress
        )

    private fun notifyError(e: Throwable) {
        handler.post { stateListeners.forEach { l -> l.onError(e) } }
    }

    private fun notifyProgressChanged(fileName: String, byteRead: Long, byteTotal: Long) {
        handler.post {
            stateListeners.forEach { l ->
                l.onProgressChanged(
                    fileName,
                    byteRead,
                    byteTotal
                )
            }
        }
    }

    private fun notifyFinished() {
        handler.post { stateListeners.forEach { l -> l.onFinished() } }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        connectivityHandler.addNetworkStateChangeListener(this)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        connectivityHandler.removeNetworkStateChangeListener(this)
        disposeBag.dispose()
        super.onDestroy()
    }

    override fun onChange(connected: Boolean) {
        if (connected && errorDuringUploading && uri != null) {
            execute(uri!!)
        }
    }

    inner class ServiceBinder : Binder() {
        internal val service: UploadArchiveService
            get() = this@UploadArchiveService
    }

    interface StateListener {

        fun onProgressChanged(fileName: String, byteRead: Long, byteTotal: Long)

        fun onFinished()

        fun onError(e: Throwable)
    }
}