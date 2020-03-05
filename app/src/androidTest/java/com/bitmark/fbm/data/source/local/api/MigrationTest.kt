/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.local.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.bitmark.fbm.BaseTest
import com.bitmark.fbm.data.model.entity.PostType
import com.bitmark.fbm.data.model.entity.Reaction
import com.bitmark.fbm.data.model.entity.StatsType
import com.bitmark.fbm.data.model.entity.value
import com.bitmark.fbm.data.source.local.api.Migration.Companion.MIGRATION_1_2
import com.bitmark.fbm.data.source.local.api.Migration.Companion.MIGRATION_2_3
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MigrationTest : BaseTest() {

    companion object {
        private const val DB_TEST = "db_test"
    }

    @Rule
    @JvmField
    var migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        DatabaseGateway::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun testMigrate1_2() {
        val database = migrationTestHelper.createDatabase(DB_TEST, 1)

        val values = ContentValues()

        // insert post
        values.put("content", "content")
        values.put("timestamp", 315532800L)
        values.put("title", "title")
        values.put("type", PostType.UPDATE.value)
        database.insert("Post", SQLiteDatabase.CONFLICT_IGNORE, values)
        values.clear()

        // insert reaction
        values.put("id", 1)
        values.put("actor", "actor")
        values.put("reaction", Reaction.LOVE.value)
        values.put("timestamp", 315532800L)
        values.put("title", "title")
        database.insert("Reaction", SQLiteDatabase.CONFLICT_IGNORE, values)
        values.clear()

        // migrate to ver 2
        migrationTestHelper.runMigrationsAndValidate(
            DB_TEST,
            2,
            true,
            MIGRATION_1_2
        )

        // insert Stats
        values.put("id", 1)
        values.put("type", StatsType.POST.value)
        values.put("started_at", 315532800L)
        values.put("ended_at", 347155200L)
        values.put("data", "{\"link\":{\"count\":0,\"sys_avg\":42}")
        database.insert("Stats", SQLiteDatabase.CONFLICT_IGNORE, values)
        values.clear()

        // verify stats is saved
        var cursor = database.query(
            "SELECT * FROM Stats WHERE type = ? AND started_at = ? AND ended_at = ? LIMIT 1",
            arrayOf("post", 315532800L, 347155200L)
        )

        assertTrue(cursor != null)
        assertTrue(cursor.moveToFirst())

        val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        assertEquals(1L, id)
        cursor.close()

        // verify post is saved
        cursor = database.query(
            "SELECT * FROM Post WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC LIMIT 1",
            arrayOf(315532800L, System.currentTimeMillis() / 1000)
        )

        assertTrue(cursor != null)
        assertTrue(cursor.moveToFirst())

        var timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
        assertEquals(315532800L, timestamp)
        cursor.close()

        // verify reaction is saved

        cursor = database.query(
            "SELECT * FROM Reaction WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC LIMIT 1",
            arrayOf(315532800L, System.currentTimeMillis() / 1000)
        )

        assertTrue(cursor != null)
        assertTrue(cursor.moveToFirst())

        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
        assertEquals(315532800L, timestamp)
        cursor.close()

        database.close()

    }

    @Test
    fun testMigrate2_3() {
        val database = migrationTestHelper.createDatabase(DB_TEST, 2)

        val values = ContentValues()

        // insert post
        values.put("content", "content")
        values.put("timestamp", 315532800L)
        values.put("title", "title")
        values.put("type", PostType.UPDATE.value)
        database.insert("Post", SQLiteDatabase.CONFLICT_IGNORE, values)
        values.clear()

        // insert reaction
        values.put("id", 1)
        values.put("actor", "actor")
        values.put("reaction", Reaction.LOVE.value)
        values.put("timestamp", 315532800L)
        values.put("title", "title")
        database.insert("Reaction", SQLiteDatabase.CONFLICT_IGNORE, values)
        values.clear()

        // insert comment
        values.put("id", 1)
        values.put("post_id", 315532800L)
        values.put("timestamp", 315532800L)
        values.put("content", "content")
        values.put("author", "author")
        database.insert("Comment", SQLiteDatabase.CONFLICT_IGNORE, values)
        values.clear()

        // insert criteria
        values.put("type", "post")
        values.put("started_at", 315532800L)
        values.put("ended_at", 347155200L)
        values.put("query", "post?started_at=315532800&ended_at=347155200")
        database.insert("Criteria", SQLiteDatabase.CONFLICT_IGNORE, values)
        values.clear()

        values.put("type", "statistic")
        values.put("started_at", 315532800L)
        values.put("ended_at", 347155200L)
        values.put("query", "statistic?started_at=315532800&ended_at=347155200&type=usage")
        database.insert("Criteria", SQLiteDatabase.CONFLICT_IGNORE, values)
        values.clear()

        // migrate to ver 3
        migrationTestHelper.runMigrationsAndValidate(
            DB_TEST,
            3,
            true,
            MIGRATION_2_3
        )

        // verify post is deleted
        var cursor = database.query("SELECT * FROM Post")

        assertTrue(cursor != null)
        assertFalse(cursor.moveToFirst())
        cursor.close()

        // verify reaction is deleted
        cursor = database.query("SELECT * FROM Reaction")

        assertTrue(cursor != null)
        assertFalse(cursor.moveToFirst())
        cursor.close()

        // verify comment is deleted
        cursor = database.query("SELECT * FROM Comment")

        assertTrue(cursor != null)
        assertFalse(cursor.moveToFirst())
        cursor.close()

        // verify criteria is deleted
        cursor = database.query("SELECT * FROM Criteria")

        assertTrue(cursor != null)
        assertTrue(cursor.moveToFirst())

        val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
        assertEquals("statistic", type)

        // verify no more records
        assertFalse(cursor.moveToNext())
        cursor.close()

        database.close()

    }
}