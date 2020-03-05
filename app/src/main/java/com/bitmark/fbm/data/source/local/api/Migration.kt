/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.local.api

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// add new table Stats
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `Stats` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `type` TEXT NOT NULL, `started_at` INTEGER NOT NULL, `ended_at` INTEGER NOT NULL, `data` TEXT NOT NULL)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Stats_started_at_ended_at` ON `Stats` (`started_at`, `ended_at`)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Stats_id` ON `Stats` (`id`)")
    }
}

// change id type to uuid (TEXT) from tables Post, Reaction, Comment
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // drop table Post
        database.execSQL("DROP TABLE `Post`")
        database.execSQL("DROP INDEX IF EXISTS `index_Post_id`")
        database.execSQL("DROP INDEX IF EXISTS `index_Post_timestamp`")
        database.execSQL("DROP INDEX IF EXISTS `index_Post_type`")
        database.execSQL("DROP INDEX IF EXISTS `index_Post_location_id`")

        // drop table Reaction
        database.execSQL("DROP TABLE `Reaction`")
        database.execSQL("DROP INDEX IF EXISTS `index_Reaction_id`")
        database.execSQL("DROP INDEX IF EXISTS `index_Reaction_timestamp`")
        database.execSQL("DROP INDEX IF EXISTS `index_Reaction_reaction`")

        // drop table Comment
        database.execSQL("DROP TABLE `Comment`")
        database.execSQL("DROP INDEX IF EXISTS `index_Comment_id`")
        database.execSQL("DROP INDEX IF EXISTS `index_Comment_timestamp`")
        database.execSQL("DROP INDEX IF EXISTS `index_Comment_post_id`")

        // delete data from table Criteria
        database.execSQL("DELETE FROM `Criteria` WHERE `type` IN (`post`, `reaction`)")

        // create table Post
        database.execSQL("CREATE TABLE IF NOT EXISTS `Post` (`id` TEXT NOT NULL, `content` TEXT, `timestamp` INTEGER NOT NULL, `title` TEXT, `type` TEXT NOT NULL, `location_id` TEXT, `tags` TEXT, `media_data` TEXT, `url` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`location_id`) REFERENCES `Location`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Post_id` ON `Post` (`id`)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Post_timestamp` ON `Post` (`timestamp`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Post_type` ON `Post` (`type`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Post_location_id` ON `Post` (`location_id`)")

        // create table Reaction
        database.execSQL("CREATE TABLE IF NOT EXISTS `Reaction` (`id` TEXT NOT NULL, `actor` TEXT NOT NULL, `reaction` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `title` TEXT NOT NULL, PRIMARY KEY(`id`))")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Reaction_id` ON `Reaction` (`id`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Reaction_timestamp` ON `Reaction` (`timestamp`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Reaction_reaction` ON `Reaction` (`reaction`)")

        // create table Comment
        database.execSQL("CREATE TABLE IF NOT EXISTS `Comment` (`id` TEXT NOT NULL, `post_id` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `content` TEXT NOT NULL, `author` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`post_id`) REFERENCES `Post`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Comment_id` ON `Comment` (`id`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Comment_timestamp` ON `Comment` (`timestamp`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Comment_post_id` ON `Comment` (`post_id`)")
    }

}