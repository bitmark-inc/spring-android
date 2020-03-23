/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.source.remote

import com.bitmark.fbm.data.model.entity.Period
import com.bitmark.fbm.data.model.entity.StatsType
import com.bitmark.fbm.data.source.remote.StatisticRemoteDataSource
import com.bitmark.fbm.data.source.remote.api.converter.Converter
import com.bitmark.fbm.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.fbm.data.source.remote.api.service.FbmApi
import com.bitmark.fbm.ut.data.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock


class StatisticRemoteDataSourceTest : DataTest() {

    @Mock
    lateinit var fbmApi: FbmApi

    @Mock
    lateinit var converter: Converter

    @Mock
    lateinit var rxErrorHandlingComposer: RxErrorHandlingComposer

    @InjectMocks
    lateinit var remoteDataSource: StatisticRemoteDataSource

    @Test
    fun testListUsageStatistic() {
        val observer = TestObserver<Any>()

        whenever(
            fbmApi.listUsage(
                any(),
                any()
            )
        ).thenReturn(Single.just(mapOf("result" to SECTIONRS)))

        remoteDataSource.listUsageStatistic(Period.WEEK, 0).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(SECTIONRS)
        observer.assertTerminated()
    }

    @Test
    fun testListUsageStatisticError() {
        val observer = TestObserver<Any>()

        whenever(
            fbmApi.listUsage(
                any(),
                any()
            )
        ).thenReturn(Single.error(HTTP_ERROR))

        remoteDataSource.listUsageStatistic(Period.WEEK, 0).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetInsightData() {
        val observer = TestObserver<Any>()

        whenever(
            fbmApi.getInsight(any(), any())
        ).thenReturn(Single.just(mapOf("result" to INSIGHT_DATA)))

        remoteDataSource.getInsightData(0L, 1L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(INSIGHT_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testGetInsightDataError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getInsight(any(), any())).thenReturn(Single.error(HTTP_ERROR))

        remoteDataSource.getInsightData(0L, 1L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetStats() {
        val observer = TestObserver<Any>()

        whenever(
            fbmApi.getPostStats(
                any(),
                any()
            )
        ).thenReturn(Single.just(mapOf("result" to mapOf("123" to STATS))))

        remoteDataSource.getStats(StatsType.POST, 0L, 1L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(STATSR)
        observer.assertTerminated()
    }

    @Test
    fun testGetStatsError() {
        val observer = TestObserver<Any>()

        whenever(fbmApi.getPostStats(any(), any())).thenReturn(Single.error(HTTP_ERROR))

        remoteDataSource.getStats(StatsType.POST, 0L, 1L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

}