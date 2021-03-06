/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.ext

import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.pow

val KB = 1024

val MB = KB * 1024

val GB = MB * 1024

fun Double.abbreviate(): String {
    val formatter = DecimalFormat("#.#")
    if (this < 1000) return formatter.format(this)
    val exp = (ln(this) / ln(1000.0)).toInt()
    val n = formatter.format(this / 1000.0.pow(exp.toDouble()))
    return String.format(
        "%s%c",
        n,
        "KMGTPE"[exp - 1]
    )
}

fun Int.decimalFormat(format: String = "###,###.###"): String {
    val formatter = DecimalFormat(format)
    return formatter.format(this)
}

fun Long.formatByteString() = when {
    this < KB -> "$%.1fB".format(this)
    this < MB -> "%.1fKB".format(this / KB.toFloat())
    this < GB -> "%.1fMB".format(this / MB.toFloat())
    else -> "%.1fGB".format(this / GB.toFloat())
}
