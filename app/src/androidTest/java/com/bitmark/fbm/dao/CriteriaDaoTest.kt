/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.dao

import androidx.room.EmptyResultSetException
import com.bitmark.fbm.data.source.local.api.dao.CriteriaDao
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Test


class CriteriaDaoTest : DaoTest<CriteriaDao>() {

    override fun dao(): CriteriaDao = database.criteriaDao()

    @Test
    fun testSaveAndGet() {
        val observer1 = TestObserver<Any>()
        dao().save(CRITERIA_1).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<Any>()
        dao().getCriteria(CRITERIA_1.query).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertValue(CRITERIA_1)
        observer2.assertTerminated()
    }

    @Test
    fun testDelete() {
        val observer1 = TestObserver<Any>()
        Completable.mergeArray(
            dao().save(CRITERIA_1),
            dao().save(CRITERIA_2),
            dao().save(CRITERIA_3),
            dao().save(CRITERIA_4)
        ).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<Any>()
        dao().delete().subscribe(observer2)
        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertTerminated()

        val observer3 = TestObserver<Any>()
        dao().getCriteria(CRITERIA_1.query).subscribe(observer3)

        observer3.assertNotComplete()
        observer3.assertError(EmptyResultSetException::class.java)
        observer3.assertTerminated()

        val observer4 = TestObserver<Any>()
        dao().getCriteria(CRITERIA_3.query).subscribe(observer4)

        observer4.assertNotComplete()
        observer4.assertError(EmptyResultSetException::class.java)
        observer4.assertTerminated()
    }

}