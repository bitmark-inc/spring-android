/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.archiveuploading

import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.di.ActivityScope
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class UploadArchiveModule {

    @Provides
    @ActivityScope
    fun provideVM(
        activity: UploadArchiveActivity,
        accountRepo: AccountRepository,
        appRepo: AppRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = UploadArchiveViewModel(activity.lifecycle, accountRepo, appRepo, rxLiveDataTransformer)

    @Provides
    @ActivityScope
    fun provideNav(activity: UploadArchiveActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideDialogController(activity: UploadArchiveActivity) = DialogController(activity)
}