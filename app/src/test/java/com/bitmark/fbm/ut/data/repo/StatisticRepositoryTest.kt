/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.repo

import com.bitmark.fbm.data.model.entity.Period
import com.bitmark.fbm.data.model.entity.StatsR
import com.bitmark.fbm.data.model.entity.StatsType
import com.bitmark.fbm.data.model.entity.isEmpty
import com.bitmark.fbm.data.source.StatisticRepository
import com.bitmark.fbm.data.source.local.StatisticLocalDataSource
import com.bitmark.fbm.data.source.remote.StatisticRemoteDataSource
import com.bitmark.fbm.ut.data.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock


class StatisticRepositoryTest : DataTest() {

    @Mock
    lateinit var localDataSource: StatisticLocalDataSource

    @Mock
    lateinit var remoteDataSource: StatisticRemoteDataSource

    @InjectMocks
    lateinit var repository: StatisticRepository

    @Test
    fun testListUsageStatistic() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkStoredUsageStatistic(any(), any())).thenReturn(
            Single.just(
                false
            )
        )
        whenever(
            remoteDataSource.listUsageStatistic(
                any(),
                any()
            )
        ).thenReturn(Single.just(SECTIONRS))
        whenever(localDataSource.saveUsageStatistics(any())).thenReturn(Single.just(listOf(1L, 2L)))
        whenever(localDataSource.saveUsageCriteria(any(), any())).thenReturn(Completable.complete())
        whenever(
            localDataSource.listUsageStatistic(
                any(),
                any()
            )
        ).thenReturn(Single.just(SECTIONRS))

        repository.listUsageStatistic(Period.WEEK, 0L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(SECTIONRS)
        observer.assertTerminated()
    }

    @Test
    fun testListUsageStatisticError1() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkStoredUsageStatistic(any(), any())).thenReturn(
            Single.just(
                false
            )
        )
        whenever(
            remoteDataSource.listUsageStatistic(
                any(),
                any()
            )
        ).thenReturn(Single.error(NETWORK_ERROR))
        whenever(localDataSource.listUsageStatistic(any(), any())).thenReturn(Single.just(listOf()))

        repository.listUsageStatistic(Period.WEEK, 0L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testListUsageStatisticError2() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkStoredUsageStatistic(any(), any())).thenReturn(
            Single.just(
                true
            )
        )
        whenever(localDataSource.listUsageStatistic(any(), any())).thenReturn(
            Single.error(
                RANDOM_ERROR
            )
        )

        repository.listUsageStatistic(Period.WEEK, 0L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testListUsageStatisticError3() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkStoredUsageStatistic(any(), any())).thenReturn(
            Single.just(
                false
            )
        )
        whenever(
            remoteDataSource.listUsageStatistic(
                any(),
                any()
            )
        ).thenReturn(Single.just(SECTIONRS))
        whenever(localDataSource.saveUsageStatistics(any())).thenReturn(Single.error(RANDOM_ERROR))
        whenever(localDataSource.listUsageStatistic(any(), any())).thenReturn(Single.just(listOf()))

        repository.listUsageStatistic(Period.WEEK, 0L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetInsightData() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.getInsightData(
                any(),
                any()
            )
        ).thenReturn(Single.just(INSIGHT_DATA))
        whenever(localDataSource.saveInsightData(any())).thenReturn(Completable.complete())

        repository.getInsightData(0L, 1L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(INSIGHT_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testGetInsightDataError1() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getInsightData(any(), any())).thenReturn(
            Single.error(
                NETWORK_ERROR
            )
        )
        whenever(localDataSource.getInsightData()).thenReturn(Single.just(INSIGHT_DATA))
        whenever(localDataSource.saveInsightData(any())).thenReturn(Completable.complete())

        repository.getInsightData(0L, 1L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(INSIGHT_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testGetInsightDataError2() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.getInsightData(
                any(),
                any()
            )
        ).thenReturn(Single.just(INSIGHT_DATA))
        whenever(localDataSource.saveInsightData(any())).thenReturn(Completable.error(RANDOM_ERROR))

        repository.getInsightData(0L, 1L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetStats() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getStats(any(), any(), any())).thenReturn(Single.just(STATSR))
        whenever(localDataSource.saveStats(any())).thenReturn(Completable.complete())
        whenever(
            localDataSource.saveStatsCriteria(
                any(),
                any(),
                any()
            )
        ).thenReturn(Completable.complete())

        repository.getStats(StatsType.POST, Period.WEEK, 0L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(STATSR)
        observer.assertTerminated()
    }

    @Test
    fun testGetStatsError1() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.getStats(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.error(HTTP_ERROR))
        whenever(
            localDataSource.checkStoredStats(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.just(true))
        whenever(localDataSource.getStats(any(), any(), any())).thenReturn(Single.just(STATSR))
        whenever(localDataSource.saveStats(any())).thenReturn(Completable.complete())
        whenever(
            localDataSource.saveStatsCriteria(
                any(),
                any(),
                any()
            )
        ).thenReturn(Completable.complete())

        repository.getStats(StatsType.POST, Period.WEEK, 0L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(STATSR)
        observer.assertTerminated()
    }

    @Test
    fun testGetStatsError2() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getStats(any(), any(), any())).thenReturn(Single.just(STATSR))
        whenever(localDataSource.saveStats(any())).thenReturn(Completable.error(RANDOM_ERROR))

        repository.getStats(StatsType.POST, Period.WEEK, 0L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetStatsError3() {
        val observer = TestObserver<StatsR>()

        whenever(
            remoteDataSource.getStats(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.error(HTTP_ERROR))
        whenever(
            localDataSource.checkStoredStats(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.just(false))

        repository.getStats(StatsType.POST, Period.WEEK, 0L).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue { stats -> stats.isEmpty() }
        observer.assertTerminated()
    }
}