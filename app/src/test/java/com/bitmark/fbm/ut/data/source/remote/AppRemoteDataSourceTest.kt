/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.source.remote

import com.bitmark.fbm.data.model.AutomationScriptData
import com.bitmark.fbm.data.source.remote.AppRemoteDataSource
import com.bitmark.fbm.data.source.remote.api.converter.Converter
import com.bitmark.fbm.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.fbm.data.source.remote.api.service.FbmApi
import com.bitmark.fbm.ut.data.APP_INFO_DATA
import com.bitmark.fbm.ut.data.DataTest
import com.bitmark.fbm.ut.data.NETWORK_ERROR
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock


class AppRemoteDataSourceTest : DataTest() {

    @Mock
    lateinit var fbmApi: FbmApi

    @Mock
    lateinit var converter: Converter

    @Mock
    lateinit var rxErrorHandlingComposer: RxErrorHandlingComposer

    @InjectMocks
    lateinit var remoteDataSource: AppRemoteDataSource

    @Test
    fun testGetAutomationScript() {
        val observer = TestObserver<Any>()
        val automationScriptData = AutomationScriptData(listOf())

        whenever(fbmApi.getAutomationScript()).thenReturn(Single.just(automationScriptData))

        remoteDataSource.getAutomationScript().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(automationScriptData)
        observer.assertTerminated()
    }

    @Test
    fun testGetAutomationScriptError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getAutomationScript()).thenReturn(Single.error(NETWORK_ERROR))

        remoteDataSource.getAutomationScript().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetAppInfo() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getAppInfo()).thenReturn(Single.just(mapOf("information" to APP_INFO_DATA)))

        remoteDataSource.getAppInfo().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(APP_INFO_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testGetAppInfoError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getAppInfo()).thenReturn(Single.error(NETWORK_ERROR))

        remoteDataSource.getAppInfo().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

}