/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.notification

import com.bitmark.fbm.data.source.local.api.SharedPrefApi
import com.onesignal.NotificationExtenderService
import com.onesignal.OSNotificationReceivedResult


class AppNotificationExtenderService : NotificationExtenderService() {

    override fun onNotificationProcessing(notification: OSNotificationReceivedResult?): Boolean {
        val payload = notification?.payload
        val event = payload?.additionalData?.getString("event")
        val notificationEnabled = SharedPrefApi(applicationContext).rxSingle { sharedPrefGateway ->
            sharedPrefGateway.get(
                SharedPrefApi.NOTIFICATION_ENABLED,
                Boolean::class
            )
        }.blockingGet()
        return !notificationEnabled && event == "fb_data_analyzed"
    }
}