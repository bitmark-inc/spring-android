/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.usagetimeline

import com.bitmark.fbm.di.FragmentScope
import com.bitmark.fbm.feature.Navigator
import dagger.Module
import dagger.Provides

@Module
class UsageTimelineContainerModule {

    @Provides
    @FragmentScope
    fun provideNav(fragment: UsageTimelineContainerFragment) = Navigator(fragment.parentFragment!!)

}
