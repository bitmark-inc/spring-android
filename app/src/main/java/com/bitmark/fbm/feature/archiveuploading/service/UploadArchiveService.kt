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
import android.os.IBinder
import com.bitmark.fbm.R
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.di.DaggerService
import com.bitmark.fbm.feature.notification.buildNotification
import com.bitmark.fbm.feature.notification.buildProgressNotificationBundle
import com.bitmark.fbm.feature.notification.pushNotification
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.logging.Tracer
import com.bitmark.fbm.util.Constants
import com.bitmark.fbm.util.ext.getFileSize
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class UploadArchiveService : DaggerService() {

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

    private val disposeBag = CompositeDisposable()

    private val binder = ServiceBinder()

    private val stateListeners = mutableListOf<StateListener>()

    fun addStateListener(stateListener: StateListener) {
        if (stateListeners.contains(stateListener)) return
        stateListeners.add(stateListener)
    }

    fun removeStateListener(stateListener: StateListener) {
        stateListeners.remove(stateListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val uriString = intent?.extras?.getString(URI) ?: error("missing uri")
            val uri = Uri.parse(uriString) ?: error("invalid uri")

            // push foreground notification
            var notificationBundle = buildNotificationBundle(-1, -1)
            startForeground(
                Constants.UPLOAD_ARCHIVE_NOTIFICATION_ID,
                buildNotification(applicationContext, notificationBundle)
            )

            // upload archive
            val size = getFileSize(uri)
            val inputStream = contentResolver.openInputStream(uri) ?: error("cannot open stream")

            // using delay publisher to avoid backpressure for notification updating
            val publisher = PublishSubject.create<Int>()
            disposeBag.add(publisher.debounce(100, TimeUnit.MILLISECONDS).subscribe { percent ->
                notificationBundle =
                    buildNotificationBundle(100, percent)
                pushNotification(applicationContext, notificationBundle)
            })
            disposeBag.add(accountRepo.uploadArchive(inputStream, size) { progress ->
                val byteRead = progress.first
                val byteTotal = progress.second
                val percent = (byteRead * 100 / byteTotal).toInt()
                publisher.onNext(percent)
                notifyProgressChanged(byteRead, byteTotal)
            }.subscribe({
                logger.logEvent(Event.ARCHIVE_FILE_UPLOAD_SUCCESS)
                notificationBundle = buildNotificationBundle(0, 0)
                stopSelf()
            }, { e ->
                logger.logError(Event.ARCHIVE_FILE_UPLOAD_ERROR, e)
                Tracer.ERROR.log(
                    TAG,
                    "error in upload archive: ${e.javaClass.canonicalName
                        ?: "UnknownError"}: ${e.message ?: "unknown"}"
                )
                notifyError(e)
                stopSelf()
            }))
        } catch (e: Throwable) {
            Tracer.ERROR.log(
                TAG,
                "error when prepare uri: ${e.javaClass.canonicalName
                    ?: "UnknownError"}: ${e.message ?: "unknown"}"
            )
            logger.logError(Event.ARCHIVE_FILE_UPLOAD_ERROR, e)
            notifyError(e)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun buildNotificationBundle(maxProgress: Int, currentProgress: Int) =
        buildProgressNotificationBundle(
            getString(R.string.uploading),
            if (currentProgress > 0) "$currentProgress%" else "",
            Constants.UPLOAD_ARCHIVE_NOTIFICATION_ID,
            maxProgress = maxProgress,
            currentProgress = currentProgress
        )

    private fun notifyError(e: Throwable) {
        stateListeners.forEach { l -> l.onError(e) }
    }

    private fun notifyProgressChanged(byteRead: Long, byteTotal: Long) {
        stateListeners.forEach { l -> l.onProgressChanged(byteRead, byteTotal) }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        disposeBag.dispose()
        super.onDestroy()
    }

    inner class ServiceBinder : Binder() {
        internal val service: UploadArchiveService
            get() = this@UploadArchiveService
    }

    interface StateListener {
        fun onProgressChanged(byteRead: Long, byteTotal: Long)

        fun onError(e: Throwable)
    }
}