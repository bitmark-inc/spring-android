/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.statistic

import androidx.lifecycle.Lifecycle
import com.bitmark.fbm.data.ext.onNetworkErrorReturn
import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.model.ArchiveType
import com.bitmark.fbm.data.model.entity.*
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.StatisticRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.ext.replace
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.fbm.util.modelview.SectionModelView
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers


class StatisticViewModel(
    lifecycle: Lifecycle,
    private val statisticRepo: StatisticRepository,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) :
    BaseViewModel(lifecycle) {

    internal val getDataLiveData = CompositeLiveData<List<SectionModelView>>()

    internal val getLastActivityTimestamp = CompositeLiveData<Long>()

    internal val getAccountDataLiveData = CompositeLiveData<AccountData>()

    fun getData(period: Period, periodStartedAtSec: Long) {

        // list usage stream
        val listUsageStream = appRepo.checkDataReady().flatMap { ready ->
            if (ready) {
                statisticRepo.listUsageStatistic(period, periodStartedAtSec)
                    .onNetworkErrorReturn(listOf()).map { usageStatistics ->
                        val defaultVMs =
                            newDefaultUsages(period, periodStartedAtSec).toMutableList()
                        val vms = when {
                            usageStatistics.isEmpty() -> defaultVMs
                            usageStatistics.size == defaultVMs.size -> {
                                usageStatistics.map { s ->
                                    SectionModelView.newInstance(s)
                                }
                            }
                            else -> {
                                val vms =
                                    usageStatistics.map { s -> SectionModelView.newInstance(s) }
                                for (i in 0 until defaultVMs.size) {
                                    val vm = vms.firstOrNull { v -> v.name == defaultVMs[i].name }
                                        ?: continue
                                    defaultVMs.replace(vm, i)
                                }
                                defaultVMs
                            }
                        }.toMutableList()
                        vms.toList()
                    }
            } else {
                Single.just(listOf(SectionModelView.newEmptyInstance()))
            }.observeOn(Schedulers.computation())

        }

        // system statistic stream
        val range = period.toPeriodRangeSec(periodStartedAtSec)
        val startedAt = range.first
        val endedAt = range.last
        val systemStatisticStream = Single.zip(
            statisticRepo.getStats(StatsType.POST, period, periodStartedAtSec),
            statisticRepo.getStats(StatsType.REACTION, period, periodStartedAtSec),
            BiFunction<StatsR, StatsR, List<StatsR>> { post, reaction ->
                listOf(post, reaction)
            }).onNetworkErrorReturn(
            listOf(
                StatsR.newEmptyInstance(StatsType.POST, startedAt, endedAt),
                StatsR.newEmptyInstance(StatsType.REACTION, startedAt, endedAt)
            )
        ).map { statsList ->
            listOf(
                SectionModelView.newInstance(
                    period,
                    periodStartedAtSec,
                    statsList
                )
            )
        }

        // insight stream
        val listAdsCategoryStream = accountRepo.listAdsPrefCategory()
            .map { categories -> SectionModelView.newInstance(categories) }

/*        val insightDataStream = appRepo.checkDataReady().flatMap { ready ->
            if (ready) {
                statisticRepo.getInsightData()
            } else {
                Single.just(InsightData.newDefaultInstance())
            }
        }.map { insightData ->
            SectionModelView.newInstance(
                insightData.fbIncome,
                insightData.fbIncomeFrom
            )
        }*/

        val determineArchiveTypeStream = Single.zip(
            accountRepo.getLatestArchiveType(),
            accountRepo.getArchiveRequestedAt(),
            BiFunction<String, Long, String> { latestArchiveType, archiveRequestedAt ->
                if (archiveRequestedAt != -1L) {
                    ArchiveType.SESSION
                } else {
                    latestArchiveType
                }
            })

        getDataLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(
                    listUsageStream,
                    systemStatisticStream,
                    listAdsCategoryStream,
                    /*insightDataStream,*/
                    determineArchiveTypeStream,
                    Function4<List<SectionModelView>, List<SectionModelView>, /*SectionModelView,*/ SectionModelView, String, List<SectionModelView>>
                    { usageStatistics, systemStatistics, category/*, insights*/, archiveType ->
                        val data = mutableListOf<SectionModelView>()

                        data.addAll(systemStatistics)

                        if (archiveType == ArchiveType.SESSION) {
                            data.add(category)
                        }

                        /*data.add(insights)*/

                        data.addAll(usageStatistics)

                        data.toList()

                        data
                    })
            )
        )
    }

    private fun newDefaultUsages(period: Period, periodStartedAtSec: Long) =
        listOf(
            SectionModelView.newInstance(
                SectionName.POST,
                period,
                periodStartedAtSec
            ),
            SectionModelView.newInstance(
                SectionName.REACTION,
                period,
                periodStartedAtSec
            )
        )

    fun getLastActivityTimestamp() {
        getLastActivityTimestamp.add(
            rxLiveDataTransformer.single(accountRepo.getLastActivityTimestamp()
                .onErrorResumeNext {
                    Single.just(-1L)
                })
        )
    }

    fun getAccountData() {
        getAccountDataLiveData.add(rxLiveDataTransformer.single(accountRepo.getAccountData()))
    }
}