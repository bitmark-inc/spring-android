/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.source.remote

import com.bitmark.fbm.data.source.remote.UsageRemoteDataSource
import com.bitmark.fbm.data.source.remote.api.converter.Converter
import com.bitmark.fbm.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.fbm.data.source.remote.api.service.FbmApi
import com.bitmark.fbm.ut.data.DataTest
import com.bitmark.fbm.ut.data.HTTP_ERROR
import com.bitmark.fbm.ut.data.POSTS
import com.bitmark.fbm.ut.data.REACTIONS
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import retrofit2.Response


class UsageRemoteDataSourceTest : DataTest() {

    @Mock
    lateinit var fbmApi: FbmApi

    @Mock
    lateinit var converter: Converter

    @Mock
    lateinit var rxErrorHandlingComposer: RxErrorHandlingComposer

    @InjectMocks
    lateinit var remoteDataSource: UsageRemoteDataSource

    @Test
    fun testListPost() {
        val observer = TestObserver<Any>()

        whenever(
            fbmApi.listPost(
                any(),
                any()
            )
        ).thenReturn(Single.just(mapOf("result" to POSTS)))

        remoteDataSource.listPost(0L, 1L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(POSTS)
        observer.assertTerminated()
    }

    @Test
    fun testListPostError() {
        val observer = TestObserver<Any>()

        whenever(
            fbmApi.listPost(
                any(),
                any()
            )
        ).thenReturn(Single.error(HTTP_ERROR))

        remoteDataSource.listPost(0L, 1L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testListReaction() {
        val observer = TestObserver<Any>()

        whenever(
            fbmApi.listReaction(
                any(),
                any()
            )
        ).thenReturn(Single.just(mapOf("result" to REACTIONS)))

        remoteDataSource.listReaction(0L, 1L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(REACTIONS)
        observer.assertTerminated()
    }

    @Test
    fun testListReactionError() {
        val observer = TestObserver<Any>()

        whenever(
            fbmApi.listReaction(
                any(),
                any()
            )
        ).thenReturn(Single.error(HTTP_ERROR))

        remoteDataSource.listReaction(0L, 1L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testPresignedUrl() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getPresignedUrl(any())).thenReturn(
            Single.just(
                Response.success(
                    "".toResponseBody("application/json".toMediaTypeOrNull()),
                    Headers.headersOf("Location", "https://presigned.url")
                )
            )
        )

        remoteDataSource.getPresignedUrl("uri").subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue("https://presigned.url")
        observer.assertTerminated()
    }

    @Test
    fun testPresignedUrlError() {
        val observer = TestObserver<String>()

        whenever(fbmApi.getPresignedUrl(any())).thenReturn(
            Single.just(
                Response.success(
                    "".toResponseBody("application/json".toMediaTypeOrNull()),
                    Headers.headersOf("LocationError", "https://presigned.url")
                )
            )
        )

        remoteDataSource.getPresignedUrl("uri").subscribe(observer)

        observer.assertNotComplete()
        observer.assertErrorMessage("Could not get presigned URL")
        observer.assertNoValues()
        observer.assertTerminated()
    }
}