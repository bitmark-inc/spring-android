/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.deleteaccount

import androidx.lifecycle.Lifecycle
import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer


class DeleteAccountViewModel(
    lifecycle: Lifecycle,
    private val appRepo: AppRepository,
    private val accountRepo: AccountRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val deleteAccountLiveData = CompositeLiveData<Any>()

    internal val getAccountInfoLiveData = CompositeLiveData<AccountData>()

    fun deleteAccount() {
        deleteAccountLiveData.add(
            rxLiveDataTransformer.completable(
                accountRepo.deleteAccount().andThen(appRepo.deleteAppData())
            )
        )
    }

    fun getAccountInfo() {
        getAccountInfoLiveData.add(rxLiveDataTransformer.single(accountRepo.getAccountData()))
    }
}