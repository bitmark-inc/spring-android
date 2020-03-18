/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.browse

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.realtime.ArchiveStateBus
import com.bitmark.fbm.feature.realtime.RealtimeBus
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import io.reactivex.Single
import io.reactivex.functions.Function3


class BrowseViewModel(
    lifecycle: Lifecycle,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val realtimeBus: RealtimeBus,
    private val archiveStateBus: ArchiveStateBus
) :
    BaseViewModel(lifecycle) {

    internal val setNotificationEnableLiveData = CompositeLiveData<Any>()

    internal val prepareDataLiveData = CompositeLiveData<Triple<Boolean, Boolean, Long>>()

    internal val dataReadyLiveData = MutableLiveData<Any>()

    internal val archiveInvalidLiveData = MutableLiveData<Any>()

    internal val getArchiveRequestedAtLiveData = CompositeLiveData<Long>()

    fun setNotificationEnable() {
        setNotificationEnableLiveData.add(
            rxLiveDataTransformer.completable(
                appRepo.setNotificationEnabled(
                    true
                )
            )
        )
    }

    fun prepareData() {
        prepareDataLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(
                    appRepo.checkDataReady(),
                    appRepo.checkNotificationEnabled(),
                    accountRepo.getArchiveRequestedAt(),
                    Function3<Boolean, Boolean, Long, Triple<Boolean, Boolean, Long>> { dataReady, notificationEnabled, archiveRequestedAt ->
                        Triple(dataReady, notificationEnabled, archiveRequestedAt)
                    })
            )
        )
    }

    fun getArchiveRequestedAt() {
        getArchiveRequestedAtLiveData.add(rxLiveDataTransformer.single(accountRepo.getArchiveRequestedAt()))
    }

    override fun onCreate() {
        super.onCreate()
        realtimeBus.dataReadyPublisher.subscribe(this) { dataReadyLiveData.value = it }
        archiveStateBus.archiveInvalidPublisher.subscribe(this) {
            archiveInvalidLiveData.value = it
        }
    }

    override fun onDestroy() {
        archiveStateBus.unsubscribe(this)
        realtimeBus.unsubscribe(this)
        super.onDestroy()
    }

    fun startArchiveStateBus() {
        archiveStateBus.start()
    }

}