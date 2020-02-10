/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.deleteaccount

import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.di.ActivityScope
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class DeleteAccountModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: DeleteAccountActivity,
        appRepo: AppRepository,
        accountRepo: AccountRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = DeleteAccountViewModel(activity.lifecycle, appRepo, accountRepo, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNav(activity: DeleteAccountActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: DeleteAccountActivity) = DialogController(activity)
}