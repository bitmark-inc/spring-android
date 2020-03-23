/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.archiveuploading

import androidx.lifecycle.Lifecycle
import com.bitmark.cryptography.crypto.Sha3256
import com.bitmark.cryptography.crypto.encoder.Hex
import com.bitmark.cryptography.crypto.encoder.Raw
import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.model.ArchiveType
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.sdk.features.Account
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class UploadArchiveViewModel(
    lifecycle: Lifecycle,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val uploadArchiveUrlLiveData = CompositeLiveData<Any>()

    internal val registerAccountLiveData = CompositeLiveData<Any>()

    internal val getAccountDataLiveData = CompositeLiveData<AccountData>()

    fun uploadArchiveUrl(url: String) {
        uploadArchiveUrlLiveData.add(
            rxLiveDataTransformer.completable(
                accountRepo.uploadArchiveUrl(url).andThen(
                    Completable.mergeArray(
                        appRepo.setArchiveUploaded(),
                        accountRepo.saveLatestArchiveType(ArchiveType.URL).ignoreElement()
                    )
                )
            )
        )
    }

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

    private fun registerAccountStream(
        account: Account,
        alias: String
    ): Completable {

        val registerAccountStream = Single.fromCallable {
            val requester = account.accountNumber
            val timestamp = System.currentTimeMillis().toString()
            val signature = Hex.HEX.encode(account.sign(Raw.RAW.decode(timestamp)))
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
                    Hex.HEX.encode(account.encKeyPair.publicKey().toBytes())
                ).flatMap { accountData ->
                    accountData.authRequired = false
                    accountData.keyAlias = alias
                    accountRepo.saveAccountData(accountData).andThen(Single.just(accountData))
                }
            }


        return registerAccountStream
            .flatMapCompletable { accountData ->
                val intercomId =
                    "Spring_android_%s".format(Sha3256.hash(Raw.RAW.decode(accountData.id)))
                Completable.mergeArray(
                    accountRepo.registerIntercomUser(intercomId),
                    appRepo.registerNotificationService(accountData.id)
                )
            }
    }

    fun getAccountData() {
        getAccountDataLiveData.add(rxLiveDataTransformer.single(accountRepo.getAccountData()))
    }
}