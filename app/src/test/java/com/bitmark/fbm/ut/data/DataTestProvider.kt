/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data

import com.bitmark.apiservice.utils.record.AssetRecord
import com.bitmark.apiservice.utils.record.BitmarkRecord
import com.bitmark.apiservice.utils.record.Head
import com.bitmark.fbm.data.model.*
import com.bitmark.fbm.data.model.entity.*
import com.bitmark.fbm.data.source.remote.api.error.HttpException
import com.bitmark.fbm.data.source.remote.api.error.NetworkException
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


val ALPHABETNUMERIC = "qwertyuiopasdfghjklzxcvbnm0123456789"

val NETWORK_ERROR = NetworkException(IOException("timeout"))

val HTTP_ERROR = HttpException(404, "Not found")

val RANDOM_ERROR = IllegalStateException("random error")

val ACCOUNT_NUMBER = "fgfGaBGJ19dLGq4MXeHMoCcyFRcpmYFXV2zhxyM6NjVFeumcDt"

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
    mapOf(
        "fb-identifier" to "193dae28e99ef0e0e9646b32aaa5ead303e950b043a266ecf3cbbfbcd8fe1d57",
        "last_activity_timestamp" to "123"
    ),
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

val AUTOMATION_SCRIPT_DATA = AutomationScriptData(
    listOf(
        Page(
            Page.Name.LOGIN,
            detection = "detection",
            actions = mapOf("action1" to "action1")
        )
    )
)

val APP_INFO_DATA = AppInfoData(
    AndroidAppInfo(5, "https://android.com"),
    IosAppInfo(5, "https://ios.com"),
    ServerInfo(
        "1.0.0",
        "fgfGaBGJ19dLGq4MXeHMoCcyFRcpmYFXV2zhxyM6NjVFeumcDt",
        "193dae28e99ef0e0e9646b32aaa5ead303e950b043a266ecf3cbbfbcd8fe1d57"
    ),
    systemVersion = "1.0.0",
    docs = Docs("https://eula.com")
)

val ASSET_ID =
    "9ef1590645df106ce428ec9cfdb48cfae29e95ee543a42bc3dcf95969ff9d8285dd07a558abf540838c151a7b34dd8c443430c766e8b1c934bdffa23cc06aa04"

val ASSET_RECORD_1 = AssetRecord().apply {
    reflectionSet(
        this,
        Pair("blockNumber", 9022),
        Pair("blockOffset", 1),
        Pair("createdAt", "2018-09-16T03:23:32.000000Z"),
        Pair(
            "fingerprint",
            "013727524647e0d9c4132af6fe6a57ca77e815280dde494d75b140050e322554ad92e27d8ebdc0e39ff4a3fa6d105419c6455c66256faecb816e7b1e1b2804beea"
        ),
        Pair(
            "id",
            "7ffe70ce4383e81b26f7a5fd8c6532c8a50525696272677120f10f44f16e377edd75e1507acd6572978697bcada43d30fb496caef86a3c77fc1808b8de2cbda3"
        ),
        Pair("metadata", object : HashMap<String, String>() {
            init {
                put(
                    "name",
                    "JavaSDK_Test_1537068149724.txt"
                )
                put(
                    "description",
                    "Temporary File create from java sdk test"
                )
            }
        }),
        Pair("name", "JavaSDK_Test_1537068149724.txt"),
        Pair("offset", 9918),
        Pair(
            "registrant",
            "ec6yMcJATX6gjNwvqp8rbc4jNEasoUgbfBBGGyV5NvoJ54NXva"
        ),
        Pair("status", AssetRecord.Status.CONFIRMED)
    )
}

val ASSET_RECORD_2 = AssetRecord().apply {
    reflectionSet(
        this,
        Pair("blockNumber", 9027),
        Pair("blockOffset", 4),
        Pair("createdAt", "2018-09-16T04:08:04.000000Z"),
        Pair(
            "fingerprint",
            "011b1860a4cf9c0142248773c84f24f8db6e2b54b6c1803700fce2818dcd001f7a6b0134fe3cb7d7bf0310c749fd0bf977b9f5060c0c34c1d7a755c6f159ab5b1b"
        ),
        Pair(
            "id",
            "9ef1590645df106ce428ec9cfdb48cfae29e95ee543a42bc3dcf95969ff9d8285dd07a558abf540838c151a7b34dd8c443430c766e8b1c934bdffa23cc06aa04"
        ),
        Pair("metadata", object : HashMap<String, String>() {
            init {
                put(
                    "name",
                    "JavaSDK_Test_1537070880489.txt"
                )
                put(
                    "description",
                    "Temporary File create from java sdk test"
                )
            }
        }),
        Pair("name", "JavaSDK_Test_1537070880489.txt"),
        Pair("offset", 9942),
        Pair(
            "registrant",
            "ec6yMcJATX6gjNwvqp8rbc4jNEasoUgbfBBGGyV5NvoJ54NXva"
        ),
        Pair("status", AssetRecord.Status.CONFIRMED)
    )
}

val ASSET_RECORDS = listOf(ASSET_RECORD_1, ASSET_RECORD_2)

val BITMARK_RECORD_1 = BitmarkRecord().apply {
    reflectionSet(
        this, Pair(
            "assetId",
            "dbca8f9d3f6d7a55f5ffa6f22abbccceafe599c2b0af99ccae8dbe8620c02c4facfc5748863d4236005d2fb66b1a078c694d8038fb5aaae0e08486c2c9512710"
        ),
        Pair("blockNumber", 9028),
        Pair("confirmedAt", "2018-09-16T05:47:24.000000Z"),
        Pair("createdAt", "2018-09-16T05:47:24.000000Z"),
        Pair("head", Head.HEAD), Pair(
            "headId",
            "12d11a294088bc15aa05965098dd965afb21c87d4df317bd8acb2a7b5127ecd2"
        ),
        Pair(
            "id",
            "12d11a294088bc15aa05965098dd965afb21c87d4df317bd8acb2a7b5127ecd2"
        ),
        Pair("issuedAt", "2018-09-16T05:47:24.000000Z"),
        Pair(
            "issuer",
            "fVuED2jekRdEAoKKMw9xZvvtuLi1iyhgXkmLD1w7LLm7m2Pk4p"
        ),
        Pair("offset", 744455),
        Pair(
            "owner",
            "fVuED2jekRdEAoKKMw9xZvvtuLi1iyhgXkmLD1w7LLm7m2Pk4p"
        ),
        Pair("status", BitmarkRecord.Status.SETTLED)
    )
}

val BITMARK_RECORD_2 = BitmarkRecord().apply {
    reflectionSet(
        this, Pair(
            "assetId",
            "9ef1590645df106ce428ec9cfdb48cfae29e95ee543a42bc3dcf95969ff9d8285dd07a558abf540838c151a7b34dd8c443430c766e8b1c934bdffa23cc06aa04"
        ),
        Pair("blockNumber", 9027),
        Pair("confirmedAt", "2018-09-16T04:08:04.000000Z"),
        Pair("createdAt", "2018-09-16T04:08:04.000000Z"),
        Pair("head", Head.HEAD), Pair(
            "headId",
            "402e895c66b9d8e3920a01489b21e33c265bf191ccdd33f040d50168496435d3"
        ),
        Pair(
            "id",
            "402e895c66b9d8e3920a01489b21e33c265bf191ccdd33f040d50168496435d3"
        ),
        Pair("issuedAt", "2018-09-16T04:08:04.000000Z"),
        Pair(
            "issuer",
            "ec6yMcJATX6gjNwvqp8rbc4jNEasoUgbfBBGGyV5NvoJ54NXva"
        ),
        Pair("offset", 744439),
        Pair(
            "owner",
            "ec6yMcJATX6gjNwvqp8rbc4jNEasoUgbfBBGGyV5NvoJ54NXva"
        ),
        Pair("status", BitmarkRecord.Status.SETTLED)
    )
}

val BITMARK_RECORDS = listOf(BITMARK_RECORD_1, BITMARK_RECORD_2)

val SECTIONR_1 = SectionR(1, SectionName.POST, Period.WEEK, 0, 0f, 0, mapOf(), 0f)

val SECTIONR_2 = SectionR(2, SectionName.REACTION, Period.WEEK, 0, 0f, 0, mapOf(), 0f)

val SECTIONRS = listOf(SECTIONR_1, SECTIONR_2)

val INSIGHT_DATA = InsightData(1f, 0L)

val STATSR = StatsR(1, StatsType.POST, 0L, 1L, mapOf("123" to Stats(1f, 1f)))

val JWT_DATA = JwtData("test_token", TimeUnit.MINUTES.toSeconds(30))

val ARCHIVE_DATA = ArchiveData(
    1,
    "2018-09-16T04:08:04.000000Z",
    "2018-09-16T04:08:04.000000Z",
    ArchiveStatus.SUBMITTED,
    "2018-09-16T04:08:04.000000Z",
    "2018-09-16T04:08:04.000000Z",
    "402e895c66b9d8e3920a01489b21e33c265bf191ccdd33f040d50168496435d3"
)

val ARCHIVE_DATA_LIST = listOf(ARCHIVE_DATA)

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

fun reflectionSet(
    `object`: Any,
    vararg fieldValuePairs: Pair<String, Any>
) {
    for (fieldValuePair in fieldValuePairs) {
        val field = `object`.javaClass.getDeclaredField(fieldValuePair.first)
        field.isAccessible = true
        field.set(`object`, fieldValuePair.second)
    }
}