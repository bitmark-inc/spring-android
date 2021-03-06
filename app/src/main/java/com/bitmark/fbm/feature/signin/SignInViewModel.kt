/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.signin

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.bitmark.cryptography.crypto.Sha3256
import com.bitmark.cryptography.crypto.encoder.Hex.HEX
import com.bitmark.cryptography.crypto.encoder.Raw.RAW
import com.bitmark.fbm.data.ext.isHttpError
import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.remote.api.error.HttpException
import com.bitmark.fbm.data.source.remote.api.error.errorCode
import com.bitmark.fbm.data.source.remote.api.event.RemoteApiBus
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.sdk.features.Account
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class SignInViewModel(
    lifecycle: Lifecycle,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val remoteApiBus: RemoteApiBus
) : BaseViewModel(lifecycle) {

    internal val prepareDataLiveData = CompositeLiveData<Triple<Boolean, Boolean, AccountData?>>()

    internal val serviceUnsupportedLiveData = MutableLiveData<String>()

    fun prepareData(account: Account, keyAlias: String, authRequired: Boolean) {
        prepareDataLiveData.add(
            rxLiveDataTransformer.single(
                prepareDataStream(
                    account,
                    keyAlias,
                    authRequired
                )
            )
        )
    }

    private fun prepareDataStream(account: Account, keyAlias: String, authRequired: Boolean) =
        Single.fromCallable {
            val requester = account.accountNumber
            val timestamp = System.currentTimeMillis().toString()
            val signature = HEX.encode(account.sign(RAW.decode(timestamp)))
            Triple(requester, timestamp, signature)
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io()).flatMapCompletable { t ->
            val requester = t.first
            val timestamp = t.second
            val signature = t.third
            accountRepo.registerFbmServerJwt(timestamp, signature, requester)
        }.andThen(accountRepo.syncAccountData().flatMap { accountData ->
            accountData.keyAlias = keyAlias
            accountData.authRequired = authRequired
            val intercomId =
                "Spring_android_%s".format(Sha3256.hash(RAW.decode(accountData.id)))
            Completable.mergeArray(
                accountRepo.saveAccountData(accountData),
                appRepo.registerNotificationService(accountData.id),
                appRepo.setDataReady(),
                accountRepo.registerIntercomUser(intercomId)
            ).andThen(
                Single.just(
                    Triple<Boolean, Boolean, AccountData?>(
                        first = true,
                        second = false,
                        third = accountData
                    )
                )
            )
        }).onErrorResumeNext { e ->
            if (e.isHttpError()) {
                when {
                    // did not has spring account
                    (e as HttpException).code == 401 -> Single.just(
                        Triple(
                            first = false,
                            second = false,
                            third = null
                        )
                    )
                    // deleting account
                    e.errorCode == 1008 -> Single.just(
                        Triple(
                            first = false,
                            second = true,
                            third = null
                        )
                    )
                    else -> Single.error(e)
                }
            } else {
                Single.error(e)
            }
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
}