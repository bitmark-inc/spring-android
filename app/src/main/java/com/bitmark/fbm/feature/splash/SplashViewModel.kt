/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.splash

import androidx.lifecycle.Lifecycle
import com.bitmark.cryptography.crypto.encoder.Hex
import com.bitmark.cryptography.crypto.encoder.Raw
import com.bitmark.fbm.data.ext.onNetworkErrorReturn
import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.model.AppInfoData
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.sdk.features.Account
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers


class SplashViewModel(
    lifecycle: Lifecycle,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getAccountInfoLiveData = CompositeLiveData<Pair<AccountData, Boolean>>()

    internal val getAppInfoLiveData = CompositeLiveData<AppInfoData>()

    internal val checkFirstTimeEnterNewVersionLiveData = CompositeLiveData<Boolean>()

    internal val prepareDataLiveData = CompositeLiveData<Pair<Boolean, Long>>()

    internal val checkDataReadyLiveData = CompositeLiveData<Triple<Boolean, Boolean, Boolean>>()

    internal val deleteAppDataLiveData = CompositeLiveData<Any>()

    fun getAccountInfo() {
        getAccountInfoLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(
                    accountRepo.getAccountData(),
                    accountRepo.checkFbCredentialExisting(),
                    BiFunction { account, fbCredentialExisting ->
                        Pair(account, fbCredentialExisting)
                    })
            )
        )
    }

    fun getAppInfo() {
        getAppInfoLiveData.add(rxLiveDataTransformer.single(appRepo.getAppInfo()))
    }

    fun prepareData(account: Account) {
        prepareDataLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(registerJwtStream(account)
                    .andThen(accountRepo.syncAccountData().ignoreElement())
                    .andThen(checkInvalidArchiveStream()).onNetworkErrorReturn(false),
                    accountRepo.getArchiveRequestedAt(),
                    BiFunction { invalidArchives, archiveRequestedAt ->
                        Pair(
                            invalidArchives,
                            archiveRequestedAt
                        )
                    })
            )
        )
    }

    private fun checkInvalidArchiveStream() = Single.zip(
        accountRepo.getArchiveRequestedAt(),
        accountRepo.checkInvalidArchives(),
        BiFunction<Long, Boolean, Boolean> { archiveRequestedAt, invalidArchives ->
            val stillRequested = archiveRequestedAt != -1L
            !stillRequested && invalidArchives
        })


    private fun registerJwtStream(account: Account) = Single.fromCallable {
        val requester = account.accountNumber
        val timestamp = System.currentTimeMillis().toString()
        val signature = Hex.HEX.encode(account.sign(Raw.RAW.decode(timestamp)))
        Triple(timestamp, signature, requester)
    }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io())
        .flatMapCompletable { t ->
            accountRepo.registerFbmServerJwt(t.first, t.second, t.third)
        }

    fun checkDataReady() {

        val checkDataReadyStream = appRepo.checkDataReady().flatMap { ready ->
            if (ready) {
                Single.just(ready)
            } else {
                accountRepo.checkArchiveProcessed().flatMap { processed ->
                    if (processed) {
                        appRepo.setDataReady().andThen(Single.just(true))
                    } else {
                        Single.just(false)
                    }
                }.onNetworkErrorReturn(false)
            }
        }

        val checkCategoryReadyStream = accountRepo.checkAdsPrefCategoryReady()

        val checkArchiveUploaded = appRepo.checkArchiveUploaded()

        checkDataReadyLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(
                    checkDataReadyStream,
                    checkCategoryReadyStream,
                    checkArchiveUploaded,
                    Function3<Boolean, Boolean, Boolean, Triple<Boolean, Boolean, Boolean>> { dataReady, categoryReady, archiveUploaded ->
                        Triple(dataReady, categoryReady, archiveUploaded)
                    })
            )
        )
    }

    fun checkFirstTimeEnterNewVersion(currentVerCode: Int) {
        checkFirstTimeEnterNewVersionLiveData.add(
            rxLiveDataTransformer.single(
                appRepo.getLastVersionCode().flatMap { lastVerCode ->

                    val saveLastVerCode = if (lastVerCode != currentVerCode) {
                        appRepo.saveLastVersionCode(currentVerCode)
                    } else {
                        Completable.complete()
                    }

                    val firstTimeEnter = lastVerCode != -1 && lastVerCode < currentVerCode

                    saveLastVerCode.andThen(Single.just(firstTimeEnter))
                })
        )
    }

    fun deleteAppData() {
        deleteAppDataLiveData.add(rxLiveDataTransformer.completable(appRepo.deleteAppData()))
    }

}