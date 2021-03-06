/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.ext

import com.bitmark.sdk.features.Account

fun Account.generateKeyAlias() =
    "%s.%d.encryption_key".format(accountNumber, System.currentTimeMillis())
