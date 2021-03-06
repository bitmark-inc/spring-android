/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.remote.api.error

class UnknownException(cause: Throwable) : Exception(cause) {

    constructor(message: String? = null) : this(Throwable(message))

    override val message: String?
        get() = cause?.message ?: "unknown"
}