/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data

import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.model.ArchiveData
import com.bitmark.fbm.data.model.ArchiveStatus
import com.bitmark.fbm.data.source.remote.api.error.HttpException
import com.bitmark.fbm.data.source.remote.api.error.NetworkException
import java.io.IOException
import java.util.*

val ALPHABETNUMERIC = "qwertyuiopasdfghjklzxcvbnm0123456789"

val NETWORK_ERROR = NetworkException(IOException("timeout"))

val HTTP_ERROR = HttpException(404, "Not found")

val RANDOM_ERROR = IllegalStateException("random error")

val ACCOUNT_DATA = AccountData(
    "fgfGaBGJ19dLGq4MXeHMoCcyFRcpmYFXV2zhxyM6NjVFeumcDt",
    mapOf(
        "fb-identifier" to "193dae28e99ef0e0e9646b32aaa5ead303e950b043a266ecf3cbbfbcd8fe1d57",
        "last_activity_timestamp" to "123"
    ),
    "2020-01-11T12:44:51.637092Z",
    "2020-01-11T12:44:51.637092Z",
    true,
    "fgfGaBGJ19dLGq4MXeHMoCcyFRcpmYFXV2zhxyM6NjVFeumcDt.1580462486129.encryption_key"
)

val ACCOUNT_DATA_NO_ACTIVITY_TIMESTAMP = AccountData(
    "fgfGaBGJ19dLGq4MXeHMoCcyFRcpmYFXV2zhxyM6NjVFeumcDt",
    mapOf(
        "fb-identifier" to "193dae28e99ef0e0e9646b32aaa5ead303e950b043a266ecf3cbbfbcd8fe1d57"
    ),
    "2020-01-11T12:44:51.637092Z",
    "2020-01-11T12:44:51.637092Z",
    true,
    "fgfGaBGJ19dLGq4MXeHMoCcyFRcpmYFXV2zhxyM6NjVFeumcDt.1580462486129.encryption_key"
)

val REMOTE_ACCOUNT_DATA = AccountData(
    "fgfGaBGJ19dLGq4MXeHMoCcyFRcpmYFXV2zhxyM6NjVFeumcDt",
    mapOf("fb-identifier" to "193dae28e99ef0e0e9646b32aaa5ead303e950b043a266ecf3cbbfbcd8fe1d57"),
    "2020-01-11T12:44:51.637092Z",
    "2020-01-11T12:44:51.637092Z",
    false,
    ""
)

val EMPTY_ACCOUNT_DATA = AccountData.newEmptyInstance()

val INVALID_ARCHIVE_DATA = listOf(
    ArchiveData(
        1,
        "2020-01-11T12:44:51.637092Z",
        "2020-01-11T12:44:51.637092Z",
        ArchiveStatus.INVALID,
        "2020-01-11T12:44:51.637092Z",
        "2020-01-11T12:44:51.637092Z",
        "abf13fa"
    )
)

val PROCESSED_ARCHIVE_DATA = listOf(
    ArchiveData(
        1,
        "2020-01-11T12:44:51.637092Z",
        "2020-01-11T12:44:51.637092Z",
        ArchiveStatus.PROCESSED,
        "2020-01-11T12:44:51.637092Z",
        "2020-01-11T12:44:51.637092Z",
        "abf13fa"
    )
)

fun anyString(): String {
    val random = Random()
    val size = random.nextInt(50)
    val result = CharArray(size)
    val alphabetNumericLength = ALPHABETNUMERIC.length
    for (i in 0 until size) {
        result[i] = ALPHABETNUMERIC[random.nextInt(alphabetNumericLength)]
    }
    return result.joinToString("")
}