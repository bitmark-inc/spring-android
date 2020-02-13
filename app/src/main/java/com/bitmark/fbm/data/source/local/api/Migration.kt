/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.local.api

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `Stats` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `type` TEXT NOT NULL, `started_at` INTEGER NOT NULL, `ended_at` INTEGER NOT NULL, `data` TEXT NOT NULL)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Stats_started_at_ended_at` ON `Stats` (`started_at`, `ended_at`)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Stats_id` ON `Stats` (`id`)")
    }
}