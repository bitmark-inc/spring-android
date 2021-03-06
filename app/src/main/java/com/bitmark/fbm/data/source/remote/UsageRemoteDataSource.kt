/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.remote

import com.bitmark.fbm.data.source.remote.api.converter.Converter
import com.bitmark.fbm.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.fbm.data.source.remote.api.service.FbmApi
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class UsageRemoteDataSource @Inject constructor(
    fbmApi: FbmApi,
    converter: Converter,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(fbmApi, converter, rxErrorHandlingComposer) {

    fun listPost(startedAtSec: Long, endedAtSec: Long) =
        fbmApi.listPost(startedAtSec, endedAtSec).map { res ->
            res["result"] ?: error("invalid response")
        }.subscribeOn(
            Schedulers.io()
        )

    fun listReaction(startedAtSec: Long, endedAtSec: Long) =
        fbmApi.listReaction(startedAtSec, endedAtSec).map { res ->
            res["result"] ?: error("invalid response")
        }.subscribeOn(
            Schedulers.io()
        )

    fun getPresignedUrl(uri: String) =
        fbmApi.getPresignedUrl(uri).map { res ->
            res.headers()["Location"] ?: error("Could not get presigned URL")
        }.subscribeOn(Schedulers.io())

    fun listMedia(startedAtSec: Long?, endedAtSec: Long?, limit: Int = 100) =
        fbmApi.listMedia(startedAtSec, endedAtSec, limit).map { res ->
            res["result"] ?: error("invalid response")
        }.subscribeOn(Schedulers.io())
}