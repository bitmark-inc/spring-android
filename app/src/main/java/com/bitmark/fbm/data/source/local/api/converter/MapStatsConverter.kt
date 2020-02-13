/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.local.api.converter

import androidx.room.TypeConverter
import com.bitmark.fbm.data.ext.newGsonInstance
import com.bitmark.fbm.data.model.entity.Stats
import com.google.gson.reflect.TypeToken


class MapStatsConverter {

    @TypeConverter
    fun toString(map: Map<String, Stats>?): String? {
        return if (map == null || map.isEmpty()) {
            null
        } else {
            newGsonInstance().toJsonTree(map).asJsonObject.toString()
        }
    }

    @TypeConverter
    fun fromString(json: String?): Map<String, Stats>? {
        return if (json.isNullOrEmpty()) {
            null
        } else {
            newGsonInstance().fromJson<Map<String, Stats>>(
                json,
                object : TypeToken<Map<String, Stats>>() {}.type
            )
        }
    }
}