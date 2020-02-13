/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.local.api.converter

import androidx.room.TypeConverter
import com.bitmark.fbm.data.ext.fromJson
import com.bitmark.fbm.data.ext.newGsonInstance
import com.bitmark.fbm.data.model.entity.Stats


class StatsConverter {

    @TypeConverter
    fun fromStats(stats: Stats) = newGsonInstance().toJson(stats)

    @TypeConverter
    fun toStats(stats: String) = newGsonInstance().fromJson<Stats>(stats)
}