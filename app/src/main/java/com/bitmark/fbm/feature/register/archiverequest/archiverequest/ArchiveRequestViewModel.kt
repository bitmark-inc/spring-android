/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.register.archiverequest.archiverequest

import androidx.lifecycle.Lifecycle
import com.bitmark.cryptography.crypto.Sha3256
import com.bitmark.cryptography.crypto.encoder.Hex.HEX
import com.bitmark.cryptography.crypto.encoder.Raw.RAW
import com.bitmark.fbm.data.model.ArchiveType
import com.bitmark.fbm.data.model.AutomationScriptData
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.sdk.features.Account
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class ArchiveRequestViewModel(
    lifecycle: Lifecycle,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val registerAccountLiveData = CompositeLiveData<Any>()

    internal val prepareDataLiveData =
        CompositeLiveData<Pair<AutomationScriptData, Boolean>>()

    internal val saveArchiveRequestedAtLiveData = CompositeLiveData<Any>()

    internal val saveFbAdsPrefCategoriesLiveData = CompositeLiveData<Any>()

    internal val sendArchiveDownloadRequestLiveData = CompositeLiveData<Any>()

    fun registerAccount(
        account: Account,
        alias: String
    ) {
        registerAccountLiveData.add(
            rxLiveDataTransformer.completable(
                registerAccountStream(
                    account,
                    alias
                )
            )
        )
    }

    fun registerJwt(account: Account) {
        registerAccountLiveData.add(rxLiveDataTransformer.completable(registerJwtStream(account)))
    }

    private fun registerJwtStream(account: Account) = Single.fromCallable {
        val requester = account.accountNumber
        val timestamp = System.currentTimeMillis().toString()
        val signature = HEX.encode(account.sign(RAW.decode(timestamp)))
        Triple(requester, timestamp, signature)
    }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io())
        .flatMapCompletable { t ->
            val requester = t.first
            val timestamp = t.second
            val signature = t.third
            accountRepo.registerFbmServerJwt(timestamp, signature, requester)
                .andThen(accountRepo.syncAccountData()).ignoreElement()
        }

    private fun registerAccountStream(
        account: Account,
        alias: String
    ): Completable {

        val registerAccountStream = Single.fromCallable {
            val requester = account.accountNumber
            val timestamp = System.currentTimeMillis().toString()
            val signature = HEX.encode(account.sign(RAW.decode(timestamp)))
            Triple(requester, timestamp, signature)
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io())
            .flatMap { t ->
                val requester = t.first
                val timestamp = t.second
                val signature = t.third
                accountRepo.registerFbmServerAccount(
                    timestamp,
                    signature,
                    requester,
                    HEX.encode(account.encKeyPair.publicKey().toBytes())
                ).flatMap { accountData ->
                    accountData.authRequired = false
                    accountData.keyAlias = alias
                    accountRepo.saveAccountData(accountData).andThen(Single.just(accountData))
                }
            }


        return registerAccountStream
            .flatMapCompletable { accountData ->
                val intercomId =
                    "Spring_android_%s".format(Sha3256.hash(RAW.decode(accountData.id)))
                Completable.mergeArray(
                    accountRepo.registerIntercomUser(intercomId),
                    appRepo.registerNotificationService(accountData.id)
                )
            }
    }

    fun sendArchiveDownloadRequest(archiveUrl: String, cookie: String) {
        sendArchiveDownloadRequestLiveData.add(
            rxLiveDataTransformer.completable(
                accountRepo.getArchiveRequestedAt().flatMapCompletable { archiveRequestedAt ->
                    accountRepo.sendArchiveDownloadRequest(
                        archiveUrl,
                        cookie,
                        0L,
                        archiveRequestedAt / 1000
                    )
                }.andThen(
                    Completable.mergeArray(
                        accountRepo.clearArchiveRequestedAt(),
                        accountRepo.saveLatestArchiveType(ArchiveType.SESSION).ignoreElement()
                    )
                )
            )
        )
    }

    fun prepareData() {
        prepareDataLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(
                    appRepo.getAutomationScript(),
                    accountRepo.checkAdsPrefCategoryReady(),
                    BiFunction<AutomationScriptData, Boolean, Pair<AutomationScriptData, Boolean>> { script, categoriesFetched ->
                        Pair(
                            script,
                            categoriesFetched
                        )
                    })
            )
        )
    }

    fun saveArchiveRequestedAt(timestamp: Long) {
        saveArchiveRequestedAtLiveData.add(
            rxLiveDataTransformer.completable(
                accountRepo.setArchiveRequestedAt(timestamp)
            )
        )
    }

    fun saveFbAdsPrefCategories(categories: List<String>) {
        saveFbAdsPrefCategoriesLiveData.add(
            rxLiveDataTransformer.completable(
                accountRepo.saveAdsPrefCategories(
                    categories
                )
            )
        )
    }

}