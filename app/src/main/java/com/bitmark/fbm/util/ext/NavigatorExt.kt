/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.ext

import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.annotation.StringRes
import com.bitmark.fbm.BuildConfig
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.NONE
import com.bitmark.fbm.util.Constants
import io.intercom.android.sdk.Intercom
import java.net.URL


fun Navigator.gotoSecuritySetting() {
    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
    anim(Navigator.BOTTOM_UP).startActivity(intent)
}

fun Navigator.openBrowser(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (ignore: Throwable) {

    }
}

fun Navigator.Companion.openBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (ignore: Throwable) {
    }
}

fun Navigator.openAppSetting(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        startActivity(intent)
    } catch (ignore: Throwable) {
    }
}

fun Navigator.openMail(
    context: Context,
    email: String
) {
    try {
        val intent =
            Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        startActivity(
            Intent.createChooser(
                intent,
                "send to %s".format(email)
            )
        )
    } catch (ignore: Throwable) {
    }
}

fun Navigator.browseMedia(
    mime: String,
    requestCode: Int
) {
    val intent = Intent(Intent.ACTION_PICK)
    when (mime) {
        "image/*" -> {
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mime
            )
        }

        "video/*" -> {
            intent.setDataAndType(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mime
            )
        }
    }
    anim(NONE).startActivityForResult(intent, requestCode)
}

fun Navigator.browseDocument(requestCode: Int) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "*/*"
    anim(NONE).startActivityForResult(intent, requestCode)
}

fun Navigator.goToPlayStore() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
            )
        )
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            )
        )
    }

}

fun Navigator.openVideoPlayer(url: String, error: (Throwable) -> Unit) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse(url)
        intent.setDataAndType(uri, "video/*")
        startActivity(intent)
    } catch (e: Throwable) {
        error(e)
    }
}

fun Navigator.goToUpdateApp(updateUrl: String) {
    val url = URL(updateUrl)
    if (url.host == Constants.GOOGLE_PLAY_HOST) {
        goToPlayStore()
    } else {
        openBrowser(updateUrl)
    }
}

fun Navigator.openIntercom(exitApp: Boolean = false) {
    Intercom.client().displayMessenger()
    if (exitApp) exitApp()
}

fun Navigator.share(url: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, url)
    intent.putExtra(Intent.EXTRA_TEXT, url)
    startActivity(Intent.createChooser(intent, "Share URL"))
}

fun Navigator.openKeyGuardConfirmation(
    context: Context,
    @StringRes title: Int,
    @StringRes description: Int,
    requestCode: Int
) {
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val intent = keyguardManager.createConfirmDeviceCredentialIntent(
        context.getString(title),
        context.getString(description)
    )
    startActivityForResult(intent, requestCode)
}

