/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.main

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.remote.api.event.RemoteApiBus
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.archiveissuing.ArchiveIssuanceProcessor
import com.bitmark.fbm.feature.auth.FbmServerAuthentication
import com.bitmark.fbm.feature.realtime.ArchiveStateBus
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.sdk.features.Account
import io.reactivex.android.schedulers.AndroidSchedulers


class MainViewModel(
    lifecycle: Lifecycle,
    private val fbmServerAuth: FbmServerAuthentication,
    private val remoteApiBus: RemoteApiBus,
    private val appRepo: AppRepository,
    private val accountRepo: AccountRepository,
    private val archiveIssuanceProcessor: ArchiveIssuanceProcessor,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val archiveStateBus: ArchiveStateBus
) :
    BaseViewModel(lifecycle) {

    internal val serviceUnsupportedLiveData = MutableLiveData<String>()

    internal val checkWaitingForArchiveLiveData = CompositeLiveData<Boolean>()

    override fun onCreate() {
        super.onCreate()
        fbmServerAuth.start()
        archiveStateBus.start()
    }

    override fun onStart() {
        super.onStart()
        remoteApiBus.serviceStatePublisher.subscribe(this) { supported ->
            if (supported) return@subscribe
            subscribe(appRepo.getUpdateAppUrl().observeOn(AndroidSchedulers.mainThread())
                .subscribe { url, e ->
                    if (e == null) {
                        serviceUnsupportedLiveData.value = url
                    } else {
                        serviceUnsupportedLiveData.value = ""
                    }
                })
        }
    }

    override fun onStop() {
        remoteApiBus.unsubscribe(this)
        super.onStop()
    }

    override fun onDestroy() {
        archiveStateBus.stop()
        fbmServerAuth.stop()
        super.onDestroy()
    }

    fun startArchiveIssuanceProcessor(account: Account) {
        archiveIssuanceProcessor.start(account)
    }

    fun stopArchiveIssuanceProcessor() {
        archiveIssuanceProcessor.stop()
    }

    fun checkWaitingForArchive() {
        checkWaitingForArchiveLiveData.add(
            rxLiveDataTransformer.single(
                accountRepo.getArchiveRequestedAt().map { archiveRequestedAt -> archiveRequestedAt != -1L }
            )
        )
    }
}