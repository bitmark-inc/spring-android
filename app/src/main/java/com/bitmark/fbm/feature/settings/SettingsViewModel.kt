/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.settings

import androidx.lifecycle.Lifecycle
import com.bitmark.fbm.data.model.AppInfoData
import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer


class SettingsViewModel(
    lifecycle: Lifecycle,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getAppInfoLiveData = CompositeLiveData<AppInfoData>()

    fun getAppInfo() {
        getAppInfoLiveData.add(rxLiveDataTransformer.single(appRepo.getAppInfo()))
    }
}