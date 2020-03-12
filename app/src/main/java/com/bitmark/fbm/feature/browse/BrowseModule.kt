/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.browse

import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.StatisticRepository
import com.bitmark.fbm.di.FragmentScope
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.realtime.RealtimeBus
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class BrowseModule {

    @Provides
    @FragmentScope
    fun provideNavigator(fragment: BrowseFragment) = Navigator(fragment.parentFragment!!)

    @Provides
    @FragmentScope
    fun provideViewModel(
        fragment: BrowseFragment,
        statisticRepo: StatisticRepository,
        accountRepo: AccountRepository,
        appRepo: AppRepository,
        rxLiveDataTransformer: RxLiveDataTransformer,
        realtimeBus: RealtimeBus
    ) = BrowseViewModel(
        fragment.lifecycle,
        statisticRepo,
        accountRepo,
        appRepo,
        rxLiveDataTransformer,
        realtimeBus
    )

    @Provides
    @FragmentScope
    fun provideDialogController(fragment: BrowseFragment) = DialogController(fragment.activity!!)
}