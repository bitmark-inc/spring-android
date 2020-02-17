/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.remote.api.error

import com.bitmark.fbm.data.ext.fromJson
import com.bitmark.fbm.data.ext.newGsonInstance

class HttpException(val code: Int, val msg: String) : Exception() {
    override val message: String?
        get() = "HTTP error: Status code: $code, detail message: \"$msg\""
}

val HttpException.errorCode: Int?
    get() = newGsonInstance().fromJson<Map<String, Map<String, String>>>(msg)?.get("error")?.get("code")?.toInt()

val HttpException.errorMessage: String?
    get() = newGsonInstance().fromJson<Map<String, Map<String, String>>>(msg)?.get("error")?.get("message")