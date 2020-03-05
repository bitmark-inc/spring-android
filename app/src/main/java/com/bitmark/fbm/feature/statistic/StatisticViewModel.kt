/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.statistic

import android.content.res.Resources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.bitmark.fbm.data.ext.onNetworkErrorReturn
import com.bitmark.fbm.data.model.entity.*
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.StatisticRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.realtime.RealtimeBus
import com.bitmark.fbm.util.ext.append
import com.bitmark.fbm.util.ext.replace
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.fbm.util.modelview.SectionModelView
import com.bitmark.fbm.util.modelview.order
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.*


class StatisticViewModel(
    lifecycle: Lifecycle,
    private val statisticRepo: StatisticRepository,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val realtimeBus: RealtimeBus
) :
    BaseViewModel(lifecycle) {

    internal val getDataLiveData =
        CompositeLiveData<Pair<List<SectionModelView>, Boolean>>()

    internal val getLastActivityTimestamp = CompositeLiveData<Long>()

    internal val setNotificationEnableLiveData = CompositeLiveData<Any>()

    internal val notificationStateChangedLiveData = MutableLiveData<Boolean>()

    fun getData(period: Period, periodStartedAtSec: Long) {
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
                                    SectionModelView.newInstance(
                                        s,
                                        Random().nextInt(100)
                                    )
                                }
                            }
                            else -> {
                                val vms =
                                    usageStatistics.map { s ->
                                        SectionModelView.newInstance(
                                            s,
                                            Random().nextInt(100)
                                        )
                                    }
                                for (i in 0 until defaultVMs.size) {
                                    val vm =
                                        vms.firstOrNull { v -> v.name == defaultVMs[i].name }
                                            ?: continue
                                    defaultVMs.replace(vm, i)
                                }
                                defaultVMs
                            }
                        }.toMutableList()
                        vms.sortWith(Comparator { o1, o2 -> o2.order().compareTo(o1.order()) })
                        vms.toList()
                    }
            } else {
                Single.just(listOf(SectionModelView.newEmptyInstance()))
            }.observeOn(Schedulers.computation())

        }

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

        getDataLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(
                    listUsageStream,
                    systemStatisticStream,
                    appRepo.checkNotificationEnabled().onErrorResumeNext { e ->
                        if (e is Resources.NotFoundException) {
                            Single.just(false)
                        } else {
                            Single.error(e)
                        }
                    },
                    Function3<List<SectionModelView>, List<SectionModelView>, Boolean, Pair<List<SectionModelView>, Boolean>> { usageStatistics, systemStatistics, notificationEnabled ->
                        val data = systemStatistics.toMutableList().append(usageStatistics)
                        Pair(
                            data,
                            notificationEnabled
                        )
                    })
            )
        )
    }

    private fun newDefaultUsages(period: Period, periodStartedAtSec: Long) =
        listOf(
            SectionModelView.newDefaultInstance(
                SectionName.POST,
                period,
                periodStartedAtSec
            ),
            SectionModelView.newDefaultInstance(
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

    fun setNotificationEnable() {
        setNotificationEnableLiveData.add(
            rxLiveDataTransformer.completable(
                appRepo.setNotificationEnabled(
                    true
                )
            )
        )
    }

    override fun onStart() {
        super.onStart()
        realtimeBus.notificationStateChangedPublisher.subscribe(this) { enable ->
            notificationStateChangedLiveData.value = enable
        }
    }

    override fun onDestroy() {
        realtimeBus.unsubscribe(this)
        super.onDestroy()
    }
}