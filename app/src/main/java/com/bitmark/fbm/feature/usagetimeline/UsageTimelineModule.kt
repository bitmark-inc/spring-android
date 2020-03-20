/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.usagetimeline

import com.bitmark.fbm.data.source.UsageRepository
import com.bitmark.fbm.di.FragmentScope
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides


@Module
class UsageTimelineModule {

    @Provides
    @FragmentScope
    fun provideVM(
        fragment: UsageTimelineFragment,
        usageRepo: UsageRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = UsageTimelineViewModel(fragment.lifecycle, usageRepo, rxLiveDataTransformer)

    @Provides
    @FragmentScope
    fun provideNav(fragment: UsageTimelineFragment) = Navigator(fragment)

    @Provides
    @FragmentScope
    fun provideDialogController(fragment: UsageTimelineFragment) =
        DialogController(fragment.activity!!)
}