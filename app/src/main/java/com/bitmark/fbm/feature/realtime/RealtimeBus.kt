/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.realtime

import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.local.event.DataReadyListener
import com.bitmark.fbm.util.Bus
import io.reactivex.subjects.PublishSubject


class RealtimeBus(appRepo: AppRepository) : Bus(), DataReadyListener {

    val dataReadyPublisher = Publisher(PublishSubject.create<Any>())

    init {
        appRepo.setDataReadyListener(this)
    }

    override fun onDataReady() {
        dataReadyPublisher.publisher.onNext(Any())
    }

}