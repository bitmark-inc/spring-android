/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.dao

import androidx.room.EmptyResultSetException
import com.bitmark.fbm.data.model.entity.StatsR
import com.bitmark.fbm.data.source.local.api.dao.StatsDao
import io.reactivex.observers.TestObserver
import org.junit.Test


class StatsDaoTest : DaoTest<StatsDao>() {

    override fun dao(): StatsDao = database.statsDao()

    @Test
    fun testSaveAndGet() {
        val observer1 = TestObserver<Any>()
        dao().save(STATS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<StatsR>()
        dao().get(STATS.type, STATS.startedAt, STATS.endedAt).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { stats -> stats.type == STATS.type && stats.startedAt == STATS.startedAt && stats.endedAt == STATS.endedAt }
        observer2.assertTerminated()
    }

    @Test
    fun testDelete() {
        val observer1 = TestObserver<Any>()
        dao().save(STATS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<StatsR>()
        dao().delete().subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertTerminated()

        val observer3 = TestObserver<StatsR>()
        dao().get(STATS.type, STATS.startedAt, STATS.endedAt).subscribe(observer3)

        observer3.assertNotComplete()
        observer3.assertError(EmptyResultSetException::class.java)
        observer3.assertTerminated()
    }
}