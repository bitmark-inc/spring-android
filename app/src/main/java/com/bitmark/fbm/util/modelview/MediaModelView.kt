/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.modelview

import android.webkit.MimeTypeMap
import com.bitmark.fbm.data.model.entity.MediaR


data class MediaModelView(
    val id: String,

    val source: String,

    val thumbnail: String,

    val uri: String,

    val timestampSec: Long,

    val isVideo: Boolean
) : ModelView {

    companion object {

        fun newInstance(media: MediaR) =
            MediaModelView(
                media.id,
                media.source,
                media.thumbnail,
                media.uri,
                media.timestampSec,
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(media.extension.removePrefix("."))?.contains(
                    "video"
                ) ?: false
            )
    }

}

val MediaModelView.timestamp: Long
    get() = timestampSec * 1000