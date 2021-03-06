/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.ext

import com.bitmark.fbm.R
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.util.view.ActionAppCompatDialog
import com.bitmark.fbm.util.view.AuthRequiredDialog

fun DialogController.unexpectedAlert(action: () -> Unit = {}) =
    alert(
        R.string.error,
        R.string.unexpected_error,
        R.string.contact_us,
        false,
        clickEvent = action
    )

fun DialogController.showUpdateRequired(action: () -> Unit) = show(
    ActionAppCompatDialog(
        activity,
        R.string.update_available,
        R.string.a_new_version_is_available,
        R.string.update_now_arrow,
        "update_required",
        action
    )
)

fun DialogController.showAuthRequired(action: () -> Unit) {
    val authRequiredDialog = AuthRequiredDialog(activity, "auth_required") {
        dismissAuthRequired()
        action()
    }
    show(authRequiredDialog)
}

fun DialogController.dismissAuthRequired() = dismiss("auth_required")

fun DialogController.isAuthRequiredShowing() = isShowing("auth_required")

fun DialogController.showNoInternetConnection(action: () -> Unit = {}) =
    alert(R.string.no_internet_connection, R.string.pls_check_your_connection, clickEvent = action)