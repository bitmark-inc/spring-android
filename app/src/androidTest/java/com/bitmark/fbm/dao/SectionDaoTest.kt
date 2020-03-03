/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.dao

import com.bitmark.fbm.data.model.entity.Period
import com.bitmark.fbm.data.model.entity.SectionR
import com.bitmark.fbm.data.source.local.api.dao.SectionDao
import io.reactivex.observers.TestObserver
import org.junit.Test


class SectionDaoTest : DaoTest<SectionDao>() {

    override fun dao(): SectionDao = database.sectionDao()

    @Test
    fun testSaveSingleRec() {
        val observer1 = TestObserver<Any>()
        dao().save(SECTION_1).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<SectionR>>()
        dao().listBy(arrayOf(SECTION_1.name), Period.WEEK, 315532800L).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { sections -> sections.size == 1 && sections[0] == SECTION_1 }
        observer2.assertTerminated()
    }

    @Test
    fun testSaveMultipleRec() {
        val observer1 = TestObserver<Any>()
        dao().save(SECTIONS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<SectionR>>()
        dao().listBy(arrayOf(SECTION_1.name, SECTION_2.name), Period.WEEK, 315532800L)
            .subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue(SECTIONS)
        observer2.assertTerminated()
    }

    @Test
    fun testDelete() {
        val observer1 = TestObserver<Any>()
        dao().save(SECTIONS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<Any>()
        dao().delete().subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertTerminated()

        val observer3 = TestObserver<List<SectionR>>()
        dao().listBy(arrayOf(SECTION_1.name, SECTION_2.name), Period.WEEK, 315532800L)
            .subscribe(observer3)

        observer3.assertComplete()
        observer3.assertNoErrors()
        observer3.assertValue { sections -> sections.isEmpty() }
        observer3.assertTerminated()
    }

}