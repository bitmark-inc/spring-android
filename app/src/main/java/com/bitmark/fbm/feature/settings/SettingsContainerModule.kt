/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.settings

import com.bitmark.fbm.di.FragmentScope
import com.bitmark.fbm.feature.Navigator
import dagger.Module
import dagger.Provides

@Module
class SettingsContainerModule {

    @Provides
    @FragmentScope
    fun provideNavigator(fragment: SettingsContainerFragment) = Navigator(fragment)
}