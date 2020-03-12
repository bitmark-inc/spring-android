/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.splash.SplashActivity
import com.bitmark.fbm.util.ext.getResIdentifier

fun buildSimpleNotificationBundle(
    context: Context, @StringRes title: Int, @StringRes message: Int,
    notificationId: Int = 0,
    receiver: Class<*> = SplashActivity::class.java
): Bundle {
    return buildSimpleNotificationBundle(
        context.getString(title),
        context.getString(message),
        notificationId,
        receiver
    )
}

fun buildSimpleNotificationBundle(
    title: String,
    message: String,
    notificationId: Int = 0,
    receiver: Class<*> = SplashActivity::class.java
): Bundle {
    val bundle = Bundle()
    bundle.putString("title", title)
    bundle.putString("message", message)
    bundle.putString("receiver", receiver.name)
    bundle.putInt("notification_id", notificationId)
    return bundle
}

fun buildProgressNotificationBundle(
    title: String,
    message: String,
    notificationId: Int,
    receiver: Class<*> = SplashActivity::class.java,
    maxProgress: Int = -1,
    currentProgress: Int = -1
): Bundle {
    val bundle = buildSimpleNotificationBundle(title, message, notificationId, receiver)
    bundle.putBoolean("progress", true)
    bundle.putInt("max_progress", maxProgress)
    bundle.putInt("current_progress", currentProgress)
    return bundle
}

fun buildNotification(context: Context, bundle: Bundle): Notification {
    val receiver = try {
        val receiverName = bundle.getString("receiver") ?: SplashActivity::class.java.name
        Class.forName(receiverName)
    } catch (e: Throwable) {
        SplashActivity::class.java
    }
    val intent = Intent(context, receiver)
    intent.putExtra("notification", bundle)
    intent.putExtra("direct_from_notification", true)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_ONE_SHOT
    )

    val channelName =
        bundle.getString("channel") ?: context.getString(R.string.notification_channel_name)
    val notificationBuilder = NotificationCompat.Builder(context, channelName)
        .setContentTitle(bundle.getString("title", ""))
        .setContentText(bundle.getString("message"))
        .setAutoCancel(true)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .setContentIntent(pendingIntent)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(bundle.getString("message"))
        )

    val progress = bundle.getBoolean("progress")
    if (progress) {
        val maxProgress = bundle.getInt("max_progress")
        val currentProgress = bundle.getInt("current_progress")
        notificationBuilder.setProgress(maxProgress, currentProgress, maxProgress == -1)
    }

    val icon =
        context.getResIdentifier(bundle.getString("icon", ""), "drawable")
    notificationBuilder.setSmallIcon(if (icon != null && icon > 0) icon else R.drawable.ic_stat_onesignal_default)
    notificationBuilder.setLargeIcon(
        BitmapFactory.decodeResource(
            context.resources,
            if (icon != null && icon > 0) icon else R.drawable.ic_onesignal_large_icon_default
        )
    )

    val color = try {
        Color.parseColor(bundle.getString("color", ""))
    } catch (e: Throwable) {
        null
    }
    if (color != null) notificationBuilder.color = color

    return notificationBuilder.build()
}

fun pushNotification(context: Context, bundle: Bundle) {

    val notification = buildNotification(context, bundle)

    val channelName =
        bundle.getString("channel") ?: context.getString(R.string.notification_channel_name)

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelName,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    notificationManager.notify(bundle.getInt("notification_id", 0), notification)
}

fun cancelNotification(context: Context, id: Int) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(id)
}

fun pushDailyRepeatingNotification(
    context: Context,
    bundle: Bundle,
    triggerAtMillis: Long = System.currentTimeMillis() + AlarmManager.INTERVAL_DAY,
    requestCode: Int = 0x00
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ScheduledNotificationReceiver::class.java)
    intent.putExtras(bundle)
    val pendingIntent =
        PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )

}

fun cancelDailyRepeatingNotification(context: Context, requestCode: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ScheduledNotificationReceiver::class.java)
    val pendingIntent =
        PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.cancel(pendingIntent)
}