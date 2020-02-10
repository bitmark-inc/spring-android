/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.realtime

import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.local.event.NotificationStateChangedListener
import com.bitmark.fbm.util.Bus
import io.reactivex.subjects.PublishSubject


class RealtimeBus(appRepo: AppRepository) : Bus(), NotificationStateChangedListener {

    val notificationStateChangedPublisher = Publisher(PublishSubject.create<Boolean>())

    init {
        appRepo.setNotificationStateChangedListener(this)
    }

    override fun onNotificationStateChanged(enable: Boolean) {
        notificationStateChangedPublisher.publisher.onNext(enable)
    }
}