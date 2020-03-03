/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.dao

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.bitmark.fbm.BaseTest
import com.bitmark.fbm.data.source.local.api.DatabaseGateway


abstract class DaoTest<T> : BaseTest() {

    protected lateinit var database: DatabaseGateway

    override fun before() {
        super.before()
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            DatabaseGateway::class.java
        ).allowMainThreadQueries().build()
    }

    override fun after() {
        database.clearAllTables()
        database.close()
        super.after()
    }

    abstract fun dao(): T

}