/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.settings

import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.di.FragmentScope
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.realtime.RealtimeBus
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class SettingsModule {

    @Provides
    @FragmentScope
    fun provideNavigator(fragment: SettingsFragment) = Navigator(fragment)

    @Provides
    @FragmentScope
    fun provideVM(
        fragment: SettingsFragment,
        appRepo: AppRepository,
        rxLiveDataTransformer: RxLiveDataTransformer,
        realtimeBus: RealtimeBus
    ) = SettingsViewModel(fragment.lifecycle, appRepo, rxLiveDataTransformer, realtimeBus)
}