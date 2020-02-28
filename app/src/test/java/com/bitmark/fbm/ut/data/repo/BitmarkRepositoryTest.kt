/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.repo

import com.bitmark.apiservice.params.IssuanceParams
import com.bitmark.apiservice.params.RegistrationParams
import com.bitmark.apiservice.utils.Address
import com.bitmark.fbm.data.source.BitmarkRepository
import com.bitmark.fbm.data.source.remote.BitmarkRemoteDataSource
import com.bitmark.fbm.ut.data.*
import com.bitmark.sdk.features.BitmarkSDK
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock


class BitmarkRepositoryTest : DataTest() {

    @Mock
    lateinit var remoteDataSource: BitmarkRemoteDataSource

    @InjectMocks
    lateinit var repository: BitmarkRepository

    companion object {

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            // default is testnet
            BitmarkSDK.init("test_token")
        }
    }

    @Test
    fun testListAsset() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.listAsset(any())).thenReturn(Single.just(ASSET_RECORDS))

        repository.listAsset(anyString()).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(ASSET_RECORDS)
        observer.assertTerminated()
    }

    @Test
    fun testListAssetError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.listAsset(any())).thenReturn(Single.error(NETWORK_ERROR))

        repository.listAsset(anyString()).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testIssueBitmark() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.issueBitmark(any())).thenReturn(Single.just(listOf("bitmark_id")))

        repository.issueBitmark(
            IssuanceParams(
                ASSET_ID,
                Address.fromAccountNumber(ACCOUNT_NUMBER)
            )
        ).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(listOf("bitmark_id"))
        observer.assertTerminated()
    }

    @Test
    fun testIssueBitmarkError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.issueBitmark(any())).thenReturn(Single.error(HTTP_ERROR))

        repository.issueBitmark(
            IssuanceParams(
                ASSET_ID,
                Address.fromAccountNumber(ACCOUNT_NUMBER)
            )
        ).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterAsset() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.registerAsset(any())).thenReturn(Single.just(ASSET_ID))

        repository.registerAsset(
            RegistrationParams(
                anyString(),
                mapOf()
            )
        ).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(ASSET_ID)
        observer.assertTerminated()
    }

    @Test
    fun testRegisterAssetError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.registerAsset(any())).thenReturn(Single.error(HTTP_ERROR))

        repository.registerAsset(
            RegistrationParams(
                anyString(),
                mapOf()
            )
        ).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testListIssuedBitmark() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.listIssuedBitmark(any(), any())).thenReturn(
            Single.just(
                BITMARK_RECORDS
            )
        )

        repository.listIssuedBitmark(
            ACCOUNT_NUMBER,
            ASSET_ID
        ).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(BITMARK_RECORDS)
        observer.assertTerminated()
    }

    @Test
    fun testListIssuedBitmarkError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.listIssuedBitmark(any(), any())).thenReturn(
            Single.error(
                HTTP_ERROR
            )
        )

        repository.listIssuedBitmark(
            ACCOUNT_NUMBER,
            ASSET_ID
        ).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }
}