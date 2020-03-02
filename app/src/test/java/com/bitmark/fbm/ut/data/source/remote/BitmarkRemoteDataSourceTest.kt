/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.source.remote

import com.bitmark.apiservice.params.IssuanceParams
import com.bitmark.apiservice.params.RegistrationParams
import com.bitmark.apiservice.response.GetBitmarksResponse
import com.bitmark.apiservice.response.RegistrationResponse
import com.bitmark.apiservice.utils.Address
import com.bitmark.apiservice.utils.callback.Callback1
import com.bitmark.apiservice.utils.record.AssetRecord
import com.bitmark.fbm.data.ext.isHttpError
import com.bitmark.fbm.data.source.remote.BitmarkRemoteDataSource
import com.bitmark.fbm.data.source.remote.api.converter.Converter
import com.bitmark.fbm.data.source.remote.api.error.HttpException
import com.bitmark.fbm.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.fbm.data.source.remote.api.service.FbmApi
import com.bitmark.fbm.ut.data.*
import com.bitmark.sdk.features.Asset
import com.bitmark.sdk.features.Bitmark
import com.bitmark.sdk.features.BitmarkSDK
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.SingleOnSubscribe
import io.reactivex.observers.TestObserver
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(Bitmark::class, Asset::class)
class BitmarkRemoteDataSourceTest : DataTest() {

    @Mock
    lateinit var fbmApi: FbmApi

    @Mock
    lateinit var converter: Converter

    @Mock
    lateinit var rxErrorHandlingComposer: RxErrorHandlingComposer

    @InjectMocks
    lateinit var remoteDataSource: BitmarkRemoteDataSource

    companion object {

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            BitmarkSDK.init("test_token")
        }

    }

    @Test
    fun testIssueBitmark() {
        val observer = TestObserver<Any>()

        PowerMockito.mockStatic(Bitmark::class.java)
        whenever(Bitmark.issue(any(), any<Callback1<List<String>>>())).thenAnswer { invocation ->
            (invocation.arguments[1] as Callback1<List<String>>).onSuccess(listOf("bitmark_id"))
            null
        }
        whenever(rxErrorHandlingComposer.single(any<SingleOnSubscribe<List<String>>>())).thenCallRealMethod()

        remoteDataSource.issueBitmark(
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

        PowerMockito.mockStatic(Bitmark::class.java)
        whenever(Bitmark.issue(any(), any<Callback1<List<String>>>())).thenAnswer { invocation ->
            (invocation.arguments[1] as Callback1<List<String>>).onError(
                com.bitmark.apiservice.utils.error.HttpException(
                    404,
                    "{\"code\" : \"1000\", \"message\" : \"test message\", \"reason\" : \"test reason\" }"
                )
            )
            null
        }
        whenever(rxErrorHandlingComposer.single(any<SingleOnSubscribe<List<String>>>())).thenCallRealMethod()

        remoteDataSource.issueBitmark(
            IssuanceParams(
                ASSET_ID,
                Address.fromAccountNumber(ACCOUNT_NUMBER)
            )
        ).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError { e -> e.isHttpError() && (e as HttpException).code == 404 }
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterAsset() {
        val observer = TestObserver<Any>()

        PowerMockito.mockStatic(Asset::class.java)
        whenever(
            Asset.register(
                any(),
                any<Callback1<RegistrationResponse>>()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as Callback1<RegistrationResponse>).onSuccess(
                RegistrationResponse(listOf(RegistrationResponse.Asset(ASSET_ID, false)))
            )
            null
        }
        whenever(rxErrorHandlingComposer.single(any<SingleOnSubscribe<List<String>>>())).thenCallRealMethod()

        remoteDataSource.registerAsset(
            RegistrationParams(
                "asset_name",
                mapOf("size" to "1024KB")
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

        PowerMockito.mockStatic(Asset::class.java)
        whenever(
            Asset.register(
                any(),
                any<Callback1<RegistrationResponse>>()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as Callback1<RegistrationResponse>).onError(
                com.bitmark.apiservice.utils.error.HttpException(
                    400,
                    "{\"code\" : \"1000\", \"message\" : \"test message\", \"reason\" : \"test reason\" }"
                )
            )
            null
        }
        whenever(rxErrorHandlingComposer.single(any<SingleOnSubscribe<List<String>>>())).thenCallRealMethod()

        remoteDataSource.registerAsset(
            RegistrationParams(
                "asset_name",
                mapOf("size" to "1024KB")
            )
        ).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError { e -> e.isHttpError() && (e as HttpException).code == 400 }
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testListAsset() {
        val observer = TestObserver<Any>()

        PowerMockito.mockStatic(Asset::class.java)
        whenever(Asset.list(any(), any<Callback1<List<AssetRecord>>>())).thenAnswer { invocation ->
            (invocation.arguments[1] as Callback1<List<AssetRecord>>).onSuccess(ASSET_RECORDS)
            null
        }
        whenever(rxErrorHandlingComposer.single(any<SingleOnSubscribe<List<String>>>())).thenCallRealMethod()

        remoteDataSource.listAsset(ASSET_ID).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(ASSET_RECORDS)
        observer.assertTerminated()
    }

    @Test
    fun testListAssetError() {
        val observer = TestObserver<Any>()

        PowerMockito.mockStatic(Asset::class.java)
        whenever(Asset.list(any(), any<Callback1<List<AssetRecord>>>())).thenAnswer { invocation ->
            (invocation.arguments[1] as Callback1<List<AssetRecord>>).onError(
                com.bitmark.apiservice.utils.error.HttpException(
                    400,
                    "{\"code\" : \"1000\", \"message\" : \"test message\", \"reason\" : \"test reason\" }"
                )
            )
            null
        }
        whenever(rxErrorHandlingComposer.single(any<SingleOnSubscribe<List<String>>>())).thenCallRealMethod()

        remoteDataSource.listAsset(ASSET_ID).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError { e -> e.isHttpError() && (e as HttpException).code == 400 }
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testListIssuedBitmark() {
        val observer = TestObserver<Any>()

        PowerMockito.mockStatic(Bitmark::class.java)
        whenever(
            Bitmark.list(
                any(),
                any<Callback1<GetBitmarksResponse>>()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as Callback1<GetBitmarksResponse>).onSuccess(
                GetBitmarksResponse(
                    BITMARK_RECORDS, listOf()
                )
            )
            null
        }
        whenever(rxErrorHandlingComposer.single(any<SingleOnSubscribe<List<String>>>())).thenCallRealMethod()

        remoteDataSource.listIssuedBitmark(ACCOUNT_NUMBER, ASSET_ID).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(BITMARK_RECORDS)
        observer.assertTerminated()
    }

    @Test
    fun testListIssuedBitmarkError() {
        val observer = TestObserver<Any>()

        PowerMockito.mockStatic(Bitmark::class.java)
        whenever(
            Bitmark.list(
                any(),
                any<Callback1<GetBitmarksResponse>>()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as Callback1<GetBitmarksResponse>).onError(
                com.bitmark.apiservice.utils.error.HttpException(
                    400,
                    "{\"code\" : \"1000\", \"message\" : \"test message\", \"reason\" : \"test reason\" }"
                )
            )
            null
        }
        whenever(rxErrorHandlingComposer.single(any<SingleOnSubscribe<List<String>>>())).thenCallRealMethod()

        remoteDataSource.listIssuedBitmark(ACCOUNT_NUMBER, ASSET_ID).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError { e -> e.isHttpError() && (e as HttpException).code == 400 }
        observer.assertNoValues()
        observer.assertTerminated()
    }
}