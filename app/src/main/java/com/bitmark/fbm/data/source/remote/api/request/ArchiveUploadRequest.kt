/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.remote.api.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class ArchiveUploadRequest(
    @Expose
    @SerializedName("file_url")
    val fileUrl: String,

    @Expose
    @SerializedName("archive_type")
    val archiveType: String
) : Request