/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source

import com.bitmark.fbm.data.model.entity.*
import com.bitmark.fbm.data.source.local.StatisticLocalDataSource
import com.bitmark.fbm.data.source.remote.StatisticRemoteDataSource
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class StatisticRepository(
    private val remoteDataSource: StatisticRemoteDataSource,
    private val localDataSource: StatisticLocalDataSource
) : Repository {

    fun listUsageStatistic(period: Period, periodStartedAtSec: Long): Single<List<SectionR>> {
        val range = period.toPeriodRangeSec(periodStartedAtSec)
        return localDataSource.checkStoredUsageStatistic(range.first, range.last)
            .flatMap { stored ->
                if (stored) {
                    localDataSource.listUsageStatistic(period, periodStartedAtSec)
                } else {
                    listRemoteUsageStatistic(
                        period,
                        periodStartedAtSec
                    ).andThen(localDataSource.listUsageStatistic(period, periodStartedAtSec))
                }
            }
    }

    private fun listRemoteUsageStatistic(
        period: Period,
        periodStartedAtSec: Long
    ) = remoteDataSource.listUsageStatistic(
        period,
        periodStartedAtSec
    ).flatMapCompletable { statistics ->
        val range = period.toPeriodRangeSec(periodStartedAtSec)
        localDataSource.saveUsageStatistics(statistics)
            .flatMapCompletable { localDataSource.saveUsageCriteria(range.first, range.last) }
    }

    fun getInsightData() = remoteDataSource.getInsightData()
        .observeOn(Schedulers.io())
        .onErrorResumeNext { localDataSource.getInsightData() }
        .flatMap { insightData ->
            localDataSource.saveInsightData(insightData).andThen(Single.just(insightData))
        }

    fun getStats(type: StatsType, period: Period, periodStartedAtSec: Long): Single<StatsR> {
        val range = period.toPeriodRangeSec(periodStartedAtSec)
        val startedAt = range.first
        val endedAt = range.last
        return remoteDataSource.getStats(type, startedAt, endedAt).onErrorResumeNext {
            localDataSource.checkStoredStats(type, startedAt, endedAt).flatMap { stored ->
                if (stored) {
                    localDataSource.getStats(type, startedAt, endedAt)
                } else {
                    Single.just(StatsR.newEmptyInstance(type, startedAt, endedAt))
                }
            }
        }.flatMap { stats ->
            if (stats.isEmpty()) {
                Completable.complete()
            } else {
                Completable.mergeArray(
                    localDataSource.saveStats(stats),
                    localDataSource.saveStatsCriteria(type, startedAt, endedAt)
                )
            }.andThen(Single.just(stats))
        }
    }

}