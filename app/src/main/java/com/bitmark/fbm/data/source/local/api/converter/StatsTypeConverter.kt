/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.local.api.converter

import androidx.room.TypeConverter
import com.bitmark.fbm.data.model.entity.StatsType
import com.bitmark.fbm.data.model.entity.fromString
import com.bitmark.fbm.data.model.entity.value


class StatsTypeConverter {

    @TypeConverter
    fun fromStatsType(type: StatsType) = type.value

    @TypeConverter
    fun toStatsType(type: String) = StatsType.fromString(type)
}