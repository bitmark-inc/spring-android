/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.source.remote

import com.bitmark.fbm.data.source.local.Jwt
import com.bitmark.fbm.data.source.remote.AccountRemoteDataSource
import com.bitmark.fbm.data.source.remote.api.converter.Converter
import com.bitmark.fbm.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.fbm.data.source.remote.api.service.FbmApi
import com.bitmark.fbm.ut.data.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock

class AccountRemoteDataSourceTest : DataTest() {

    @Mock
    lateinit var fbmApi: FbmApi

    @Mock
    lateinit var converter: Converter

    @Mock
    lateinit var rxErrorHandlingComposer: RxErrorHandlingComposer

    @InjectMocks
    lateinit var remoteDataSource: AccountRemoteDataSource

    override fun after() {
        super.after()
        Jwt.getInstance().clear()
    }

    @Test
    fun testRegisterFbmServerJwt() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.registerJwt(any())).thenReturn(Single.just(JWT_DATA))

        remoteDataSource.registerFbmServerJwt(anyString(), anyString(), anyString())
            .subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
        assertEquals(JWT_DATA.token, Jwt.getInstance().token)
    }

    @Test
    fun testRegisterFbmServerJwtError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.registerJwt(any())).thenReturn(Single.error(HTTP_ERROR))

        remoteDataSource.registerFbmServerJwt(anyString(), anyString(), anyString())
            .subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
        assertEquals("", Jwt.getInstance().token)
    }

    @Test
    fun testRegisterFbmServerAccount() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.registerAccount(any())).thenReturn(Single.just(mapOf("result" to ACCOUNT_DATA)))

        remoteDataSource.registerFbmServerAccount(anyString())
            .subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(ACCOUNT_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testRegisterFbmServerAccountError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.registerAccount(any())).thenReturn(Single.error(HTTP_ERROR))

        remoteDataSource.registerFbmServerAccount(anyString())
            .subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSendArchiveDownloadRequest() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.sendArchiveDownloadRequest(any())).thenReturn(Completable.complete())

        remoteDataSource.sendArchiveDownloadRequest(anyString(), anyString(), 0L, 0L)
            .subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSendArchiveDownloadRequestError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.sendArchiveDownloadRequest(any())).thenReturn(
            Completable.error(
                NETWORK_ERROR
            )
        )

        remoteDataSource.sendArchiveDownloadRequest(anyString(), anyString(), 0L, 0L)
            .subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetArchives() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getArchives()).thenReturn(Single.just(mapOf("result" to ARCHIVE_DATA_LIST)))

        remoteDataSource.getArchives().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(ARCHIVE_DATA_LIST)
        observer.assertTerminated()
    }

    @Test
    fun testGetArchivesError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getArchives()).thenReturn(Single.error(NETWORK_ERROR))

        remoteDataSource.getArchives().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetAccountInfo() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getAccountInfo()).thenReturn(Single.just(mapOf("result" to ACCOUNT_DATA)))

        remoteDataSource.getAccountInfo().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(ACCOUNT_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testGetAccountInfoError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getAccountInfo()).thenReturn(Single.error(NETWORK_ERROR))

        remoteDataSource.getAccountInfo().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testUpdateMetadata() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.updateMetadata(any())).thenReturn(Single.just(mapOf("result" to ACCOUNT_DATA)))

        remoteDataSource.updateMetadata(mapOf()).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(ACCOUNT_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testUpdateMetadataError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.updateMetadata(any())).thenReturn(Single.error(NETWORK_ERROR))

        remoteDataSource.updateMetadata(mapOf()).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testDeleteAccount() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.deleteAccount()).thenReturn(Completable.complete())

        remoteDataSource.deleteAccount().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testDeleteAccountError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.deleteAccount()).thenReturn(Completable.error(HTTP_ERROR))

        remoteDataSource.deleteAccount().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

}