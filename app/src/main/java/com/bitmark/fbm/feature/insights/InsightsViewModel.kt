/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.insights

import androidx.lifecycle.Lifecycle
import com.bitmark.fbm.data.model.InsightData
import com.bitmark.fbm.data.model.isCreatedRemotely
import com.bitmark.fbm.data.model.newDefaultInstance
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.StatisticRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.fbm.util.modelview.InsightModelView
import io.reactivex.Single
import io.reactivex.functions.BiFunction


class InsightsViewModel(
    lifecycle: Lifecycle,
    private val statisticRepo: StatisticRepository,
    private val accountRepo: AccountRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) :
    BaseViewModel(lifecycle) {

    internal val listInsightLiveData = CompositeLiveData<List<InsightModelView>>()

    internal val checkAccountRegisteredLiveData = CompositeLiveData<Boolean>()

    fun listInsight(registered: Boolean) {
        val stream = Single.zip(
            accountRepo.listAdsPrefCategory(),
            if (registered) statisticRepo.getInsightData() else Single.just(InsightData.newDefaultInstance()),
            BiFunction<List<String>, InsightData, List<InsightModelView>> { categories, insightData ->
                listOf(
                    InsightModelView.newInstance(
                        insightData.fbIncome,
                        insightData.fbIncomeFrom,
                        null
                    ),
                    InsightModelView.newInstance(null, null, categories)
                )
            })

        listInsightLiveData.add(rxLiveDataTransformer.single(stream))
    }

    fun checkAccountRegistered() {
        checkAccountRegisteredLiveData.add(
            rxLiveDataTransformer.single(
                accountRepo.getAccountData().map { accountData -> accountData.isCreatedRemotely() })
        )
    }

}