/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm

import android.app.Application
import com.bitmark.fbm.data.source.RepositoryModule
import com.bitmark.fbm.data.source.remote.api.NetworkModule
import com.bitmark.fbm.di.ActivityBuilderModule
import com.bitmark.fbm.di.FragmentBuilderModule
import com.bitmark.fbm.di.ServiceBuilderModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Component(
    modules = [AndroidSupportInjectionModule::class, AppModule::class,
        ActivityBuilderModule::class, FragmentBuilderModule::class,
        NetworkModule::class, RepositoryModule::class, ServiceBuilderModule::class]
)
@Singleton
interface AppComponent : AndroidInjector<FbmApplication> {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): AppComponent

    }
}