/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.insights

import android.content.res.Resources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.bitmark.fbm.data.model.InsightData
import com.bitmark.fbm.data.model.newDefaultInstance
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.StatisticRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.realtime.RealtimeBus
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.fbm.util.modelview.InsightModelView
import io.reactivex.Single
import io.reactivex.functions.Function3


class InsightsViewModel(
    lifecycle: Lifecycle,
    private val statisticRepo: StatisticRepository,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val realtimeBus: RealtimeBus
) :
    BaseViewModel(lifecycle) {

    internal val listInsightLiveData = CompositeLiveData<List<InsightModelView>>()

    internal val setNotificationEnableLiveData = CompositeLiveData<Any>()

    internal val notificationStateChangedLiveData = MutableLiveData<Boolean>()

    fun listInsight() {
        val listInsightStream = appRepo.checkDataReady().flatMap { ready ->
            Single.zip(
                accountRepo.listAdsPrefCategory(),
                if (ready) statisticRepo.getInsightData() else Single.just(InsightData.newDefaultInstance()),
                appRepo.checkNotificationEnabled().onErrorResumeNext { e ->
                    if (e is Resources.NotFoundException) {
                        Single.just(false)
                    } else {
                        Single.error(e)
                    }
                },
                Function3<List<String>, InsightData, Boolean, List<InsightModelView>> { categories, insightData, notificationEnabled ->
                    listOf(
                        // categories
                        InsightModelView.newInstance(null, null, categories, null),

                        if (ready) {
                            // fb income
                            InsightModelView.newInstance(
                                insightData.fbIncome,
                                insightData.fbIncomeFrom,
                                null,
                                null
                            )
                        } else {
                            // data processing
                            InsightModelView.newInstance(
                                null,
                                null,
                                null,
                                notificationEnabled
                            )
                        }

                    )
                })
        }

        listInsightLiveData.add(rxLiveDataTransformer.single(listInsightStream))
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