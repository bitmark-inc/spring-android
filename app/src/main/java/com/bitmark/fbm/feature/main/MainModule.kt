/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.main

import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.remote.api.event.RemoteApiBus
import com.bitmark.fbm.di.ActivityScope
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.archiveissuing.ArchiveIssuanceProcessor
import com.bitmark.fbm.feature.auth.FbmServerAuthentication
import com.bitmark.fbm.feature.realtime.ArchiveStateBus
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides


@Module
class MainModule {

    @Provides
    @ActivityScope
    fun provideNavigator(activity: MainActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideVM(
        activity: MainActivity,
        fbmServerAuth: FbmServerAuthentication,
        appRepo: AppRepository,
        accountRepo: AccountRepository,
        rxLiveDataTransformer: RxLiveDataTransformer,
        remoteApiBus: RemoteApiBus,
        archiveIssuanceProcessor: ArchiveIssuanceProcessor,
        archiveStateBus: ArchiveStateBus
    ) =
        MainViewModel(
            activity.lifecycle,
            fbmServerAuth,
            remoteApiBus,
            appRepo,
            accountRepo,
            archiveIssuanceProcessor,
            rxLiveDataTransformer,
            archiveStateBus
        )

    @ActivityScope
    @Provides
    fun provideDialogController(activity: MainActivity) = DialogController(activity)
}