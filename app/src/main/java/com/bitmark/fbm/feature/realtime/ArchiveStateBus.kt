/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.realtime

import android.os.Handler
import com.bitmark.fbm.AppLifecycleHandler
import com.bitmark.fbm.R
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.connectivity.ConnectivityHandler
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.util.Bus
import com.bitmark.fbm.util.ext.openIntercom
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class ArchiveStateBus(
    private val accountRepo: AccountRepository,
    private val connectivityHandler: ConnectivityHandler,
    private val appLifecycleHandler: AppLifecycleHandler,
    private val logger: EventLogger
) : Bus() {

    companion object {
        private val PERIOD_TIME = TimeUnit.MINUTES.toMillis(1)
    }

    private val handler = Handler()

    private var disposableBag: CompositeDisposable? = null

    private var dialogController: DialogController? = null

    private var running = false

    private val actionClickListeners = mutableListOf<ActionClickListener>()

    val archiveInvalidPublisher = Publisher(PublishSubject.create<Any>())

    fun addActionClickListener(listener: ActionClickListener) {
        if (actionClickListeners.contains(listener)) return
        actionClickListeners.add(listener)
    }

    fun removeActionClickListener(listener: ActionClickListener) {
        actionClickListeners.remove(listener)
    }

    fun start() {
        if (running) return
        if (disposableBag == null) disposableBag = CompositeDisposable()
        execute()
        intervalTrigger {
            execute()
        }
        running = true
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        disposableBag?.dispose()
        disposableBag = null
        running = false
    }

    private fun intervalTrigger(action: () -> Unit) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                action()
                handler.postDelayed(this, PERIOD_TIME)
            }
        }, PERIOD_TIME)
    }

    private fun execute() {
        disposableBag?.add(Single.zip(
            accountRepo.checkInvalidArchives(),
            accountRepo.getArchiveRequestedAt(),
            BiFunction<Boolean, Long, Boolean> { invalid, archiveRequestedAt -> invalid && archiveRequestedAt == -1L })
            .observeOn(AndroidSchedulers.mainThread()).subscribe { invalid, e ->
                if (e != null && connectivityHandler.isConnected()) {
                    logger.logError(Event.ARCHIVE_STATUS_CHECK_ERROR, e)
                } else if (invalid) {
                    archiveInvalidPublisher.publisher.onNext(Any())
                    stop()
                    handleArchiveInvalid()
                }
            })
    }

    private fun handleArchiveInvalid() {
        val activity = appLifecycleHandler.getRunningActivity() ?: return
        if (dialogController != null) {
            dialogController!!.dismiss()
        }
        dialogController = DialogController(activity)
        dialogController!!.confirm(
            R.string.invalid_file_format,
            R.string.the_file_you_uploaded_was_not_a_fb_archive,
            false,
            null,
            R.string.contact_us,
            {
                Navigator(activity).openIntercom()
            },
            R.string.try_again,
            {
                actionClickListeners.forEach { l -> l.onTryAgainClicked() }
            }
        )
    }

    interface ActionClickListener {

        fun onTryAgainClicked()
    }

}