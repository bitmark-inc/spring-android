/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.unlink.unlink

import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.di.FragmentScope
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class UnlinkModule {

    @Provides
    @FragmentScope
    fun provideNavigator(fragment: UnlinkFragment) = Navigator(fragment)

    @Provides
    @FragmentScope
    fun provideViewModel(
        fragment: UnlinkFragment,
        appRepo: AppRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = UnlinkViewModel(fragment.lifecycle, appRepo, rxLiveDataTransformer)

    @Provides
    @FragmentScope
    fun provideDialogController(fragment: UnlinkFragment) = DialogController(fragment.activity!!)
}