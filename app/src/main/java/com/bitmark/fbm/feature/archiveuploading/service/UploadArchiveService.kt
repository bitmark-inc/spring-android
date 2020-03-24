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
import com.bitmark.apiservice.utils.BackgroundJobScheduler
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.ArchiveType
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
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
import io.reactivex.Completable
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
    internal lateinit var appRepo: AppRepository

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private val disposeBag = CompositeDisposable()

    private val binder = ServiceBinder()

    private val stateListeners = mutableListOf<StateListener>()

    private var uri: Uri? = null

    private val handler = Handler()

    private val executor = BackgroundJobScheduler(3)

    private var state: State = State.STARTED

    private var throwable: Throwable? = null

    private var fileName: String = ""

    private var byteTotal = 0L

    private var byteUploaded = 0L

    fun addStateListener(stateListener: StateListener) {
        if (stateListeners.contains(stateListener)) return
        stateListeners.add(stateListener)
        notifyClient(stateListener)
    }

    fun removeStateListener(stateListener: StateListener) {
        stateListeners.remove(stateListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        state = State.STARTED

        try {
            val uriString = intent?.extras?.getString(URI) ?: error("missing uri string")
            uri = Uri.parse(uriString) ?: error("invalid uri")
            execute(uri!!)
        } catch (e: Throwable) {
            state = State.ERROR
            throwable = e
            Tracer.ERROR.log(
                TAG,
                "error when prepare uri: ${throwable!!.javaClass.canonicalName
                    ?: "UnknownError"}: ${throwable!!.message ?: "unknown"}"
            )
            logger.logError(Event.ARCHIVE_FILE_UPLOAD_ERROR, throwable)
            notifyError(throwable!!)
        }
        return START_NOT_STICKY
    }

    private fun execute(uri: Uri) {

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

        byteTotal = getFileSize(uri)
        fileName = getFileName(uri)

        notifyStarted(fileName, byteTotal)

        executor.execute {
            val fileInputStream =
                contentResolver.openInputStream(uri) ?: error("cannot open stream")

            // start executing
            disposeBag.add(
                accountRepo.uploadArchive(fileInputStream, byteTotal) { progress ->
                    state = State.UPLOADING
                    byteUploaded = progress.first
                    byteTotal = progress.second
                    val percent = (byteUploaded * 100 / byteTotal).toInt()
                    publisher.onNext(percent)
                    notifyProgressChanged(fileName, byteUploaded, byteTotal)
                }.andThen(
                    Completable.mergeArray(
                        appRepo.setArchiveUploaded(),
                        accountRepo.saveLatestArchiveType(ArchiveType.FILE).ignoreElement()
                    )
                ).subscribe({
                    state = State.FINISHED
                    logger.logEvent(Event.ARCHIVE_FILE_UPLOAD_SUCCESS)
                    notificationBundle = buildNotificationBundle(0, 0)
                    notifyFinished()
                    resetState()
                    stopSelf()
                }, { e ->
                    state = State.ERROR
                    throwable = e
                    logger.logError(Event.ARCHIVE_FILE_UPLOAD_ERROR, throwable)
                    Tracer.ERROR.log(
                        TAG,
                        "error in upload archive: ${throwable!!.javaClass.canonicalName
                            ?: "UnknownError"}: ${throwable!!.message ?: "unknown"}"
                    )
                    notifyError(throwable!!)
                })
            )
        }
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

    private fun notifyStarted(fileName: String, byteTotal: Long) {
        handler.post { stateListeners.forEach { l -> l.onStarted(fileName, byteTotal) } }
    }

    private fun notifyClient(l: StateListener) {
        when (state) {
            State.STARTED -> l.onStarted(fileName, byteTotal)
            State.UPLOADING -> l.onProgressChanged(fileName, byteUploaded, byteTotal)
            State.ERROR -> l.onError(throwable!!)
            State.FINISHED -> l.onFinished()
        }
    }

    private fun resetState() {
        byteUploaded = 0
        byteTotal = 0
        fileName = ""
        throwable = null
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
        executor.shutdown()
        super.onDestroy()
    }

    override fun onChange(connected: Boolean) {
        if (connected && state == State.ERROR && uri != null) {
            execute(uri!!)
        }
    }

    inner class ServiceBinder : Binder() {
        internal val service: UploadArchiveService
            get() = this@UploadArchiveService
    }

    interface StateListener {

        fun onStarted(fileName: String, byteTotal: Long)

        fun onProgressChanged(fileName: String, byteRead: Long, byteTotal: Long)

        fun onFinished()

        fun onError(e: Throwable)
    }

    enum class State {
        STARTED, UPLOADING, FINISHED, ERROR
    }
}