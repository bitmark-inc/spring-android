/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.archiveissuing

import android.util.Log
import com.bitmark.apiservice.params.IssuanceParams
import com.bitmark.apiservice.params.RegistrationParams
import com.bitmark.cryptography.crypto.Sha3256
import com.bitmark.cryptography.crypto.encoder.Hex.HEX
import com.bitmark.cryptography.crypto.encoder.Raw.RAW
import com.bitmark.fbm.data.model.assetId
import com.bitmark.fbm.data.model.hashBytes
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.BitmarkRepository
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.logging.Level
import com.bitmark.fbm.util.ext.flatten
import com.bitmark.sdk.features.Account
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.functions.BiConsumer
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.io.IOException
import javax.inject.Inject


class ArchiveIssuanceProcessor @Inject constructor(
    private val bitmarkRepo: BitmarkRepository,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val logger: EventLogger
) {

    companion object {
        private const val TAG = "ArchiveIssuance"
    }

    private val disposeBag = CompositeDisposable()

    fun start(account: Account) {
        if (disposeBag.isDisposed) error("cannot restart the stopped one")
        disposeBag.add(startIssuingStream(account).subscribe({ bitmarkIds ->
            if (bitmarkIds.isEmpty()) return@subscribe
            logger.logEvent(
                Event.ARCHIVE_ISSUE_SUCCESS,
                Level.INFO,
                mapOf("bitmark_ids" to bitmarkIds.joinToString(","))
            )
        }, { e ->
            if (e is CompositeException) {
                e.exceptions.forEach {
                    Log.e(TAG, e.message)
                    logger.logError(Event.ARCHIVE_ISSUE_ERROR, it)
                }
            } else {
                Log.e(TAG, e.message)
                logger.logError(Event.ARCHIVE_ISSUE_ERROR, e)
            }
        }))
    }

    fun stop() {
        disposeBag.dispose()
    }

    private fun startIssuingStream(account: Account) =
        accountRepo.listProcessedArchive().flatMap { archives ->

            // filter the asset id has not been issued
            val assetIds = archives.map { a -> a.assetId }

            val streams = assetIds.map { assetId ->
                val checkIssuedBm =
                    fun(aId: String) = bitmarkRepo.listIssuedBitmark(
                        account.accountNumber,
                        aId
                    ).map { bms -> Pair(aId, bms.isNotEmpty()) }
                checkIssuedBm(assetId)
            }

            Single.merge(streams).collectInto(
                mutableListOf(),
                BiConsumer<MutableList<Pair<String, Boolean>>, Pair<String, Boolean>> { result, data ->
                    result.add(data)
                }).map { result -> result.filter { !it.second }.map { it.first } }
                .map { hasNotIssuedAssetIds ->
                    Pair(hasNotIssuedAssetIds, archives)
                }
        }.observeOn(Schedulers.io()).flatMap { p ->

            // filter the asset id has not been registered
            val hasNotIssuedAssetIds = p.first
            val archives = p.second

            if (hasNotIssuedAssetIds.isEmpty()) {
                Single.just(Triple(listOf(), listOf(), archives))
            } else {
                val streams = hasNotIssuedAssetIds.map { assetId ->
                    val checkRegisteredAsset = fun(aId: String) =
                        bitmarkRepo.listAsset(aId).map { assets ->
                            Pair(aId, assets.isNotEmpty())
                        }
                    checkRegisteredAsset(assetId)
                }

                Single.merge(streams).collectInto(
                    mutableListOf(),
                    BiConsumer<MutableList<Pair<String, Boolean>>, Pair<String, Boolean>> { collection, data ->
                        collection.add(data)
                    }).map { result -> result.filter { !it.second }.map { it.first } }
                    .map { unregisteredAssetIds ->
                        Triple(
                            hasNotIssuedAssetIds,
                            unregisteredAssetIds,
                            archives
                        )
                    }
            }

        }.observeOn(Schedulers.io()).flatMap { t ->
            val hasNotIssuedAssetIds = t.first
            val unregisteredAssetIds = t.second
            val archives = t.third

            if (hasNotIssuedAssetIds.isEmpty()) {
                Single.just(listOf())
            } else {
                val registerAssetStream = if (unregisteredAssetIds.isEmpty()) {
                    Completable.complete()
                } else {
                    appRepo.getAppInfo().map { appInfo ->
                        Pair(
                            appInfo.docs.eula,
                            appInfo.systemVersion
                        )
                    }.flatMap { p ->
                        val eula = p.first
                        val systemVer = p.second
                        getHashedEula(eula).map { hash -> Pair(hash, systemVer) }
                    }.flatMapCompletable { p ->
                        val hash = p.first
                        val systemVer = p.second

                        val streams = unregisteredAssetIds.map { assetId ->
                            val registerAsset = fun(aId: String): Completable {
                                val archive = archives.find { a -> a.assetId == aId }!!
                                val metadata = mapOf(
                                    "type" to "fbdata",
                                    "system_version" to systemVer,
                                    "eula" to hash
                                )
                                val params = RegistrationParams("", metadata)
                                params.setFingerprintFromData(archive.hashBytes)
                                params.sign(account.authKeyPair)
                                return bitmarkRepo.registerAsset(params).ignoreElement()
                                    .onErrorResumeNext { e ->
                                        Completable.error(IllegalAccessException("register asset: $aId failed with cause: ${e.message}"))
                                    }
                            }
                            registerAsset(assetId)
                        }

                        Completable.merge(streams)
                    }
                }

                val issueBitmarkStreams = hasNotIssuedAssetIds.map { assetId ->
                    val issueBm = fun(aId: String): Single<List<String>> {
                        val params = IssuanceParams(aId, account.toAddress())
                        params.sign(account.authKeyPair)
                        return bitmarkRepo.issueBitmark(params).onErrorResumeNext { e ->
                            Single.error(IllegalAccessException("issue bitmark with asset id: $aId failed with cause: ${e.message}"))
                        }
                    }
                    issueBm(assetId)
                }

                registerAssetStream.andThen(
                    Single.mergeDelayError(issueBitmarkStreams).collectInto(
                        mutableListOf(),
                        BiConsumer<MutableList<List<String>>, List<String>> { collection, data ->
                            collection.add(
                                data
                            )
                        }).map { result -> result.flatten() }
                )
            }
        }

    private fun getHashedEula(eula: String) = Single.create<String> { emt ->
        val client = OkHttpClient()
        val request = Request.Builder().url(eula).method("GET", null).build()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                emt.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body!!.string()
                // TODO change later
                val hash =
                    HEX.encode(Sha3256.hash(RAW.decode(if (res.isEmpty()) "Test EULA" else res)))
                emt.onSuccess(hash)
            }
        })

    }.subscribeOn(Schedulers.io())
}