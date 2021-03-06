/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm

import android.app.Application
import android.content.Context
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.feature.connectivity.ConnectivityHandler
import com.bitmark.fbm.feature.realtime.ArchiveStateBus
import com.bitmark.fbm.feature.realtime.RealtimeBus
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.logging.SentryEventLogger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideAppLifecycleHandler() = AppLifecycleHandler()

    @Provides
    @Singleton
    fun provideConnectivityHandler(context: Context) = ConnectivityHandler(context)

    @Provides
    @Singleton
    fun provideEventLogger(accountRepo: AccountRepository): EventLogger =
        SentryEventLogger(accountRepo)

    @Provides
    @Singleton
    fun provideRealtimeBus(appRepo: AppRepository) = RealtimeBus(appRepo)

    @Provides
    @Singleton
    fun provideArchiveStateBus(
        accountRepo: AccountRepository,
        connectivityHandler: ConnectivityHandler,
        appLifecycleHandler: AppLifecycleHandler,
        logger: EventLogger
    ) = ArchiveStateBus(accountRepo, connectivityHandler, appLifecycleHandler, logger)

}