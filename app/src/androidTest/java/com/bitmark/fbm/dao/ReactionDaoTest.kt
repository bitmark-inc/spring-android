/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.dao

import com.bitmark.fbm.data.model.entity.ReactionR
import com.bitmark.fbm.data.source.local.api.dao.ReactionDao
import io.reactivex.observers.TestObserver
import org.junit.Test


class ReactionDaoTest : DaoTest<ReactionDao>() {

    override fun dao(): ReactionDao = database.reactionDao()

    @Test
    fun testSaveMultiRec() {
        val observer1 = TestObserver<Any>()
        dao().save(REACTIONS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<ReactionR>>()
        dao().listOrdered(0, System.currentTimeMillis() / 1000, 100).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { reactions -> reactions.size == REACTIONS.size && reactions[0] == REACTION_2 && reactions[1] == REACTION_1 }
        observer2.assertTerminated()
    }

    @Test
    fun testDelete() {
        val observer1 = TestObserver<Any>()
        dao().save(REACTIONS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<Any>()
        dao().delete().subscribe(observer2)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer3 = TestObserver<List<ReactionR>>()
        dao().listOrdered(0, System.currentTimeMillis() / 1000, 100).subscribe(observer3)

        observer3.assertComplete()
        observer3.assertNoErrors()
        observer3.assertValue { reactions -> reactions.isEmpty() }
        observer3.assertTerminated()
    }

    @Test
    fun testListOrderedExcept() {
        val observer1 = TestObserver<Any>()
        dao().save(REACTIONS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<ReactionR>>()
        dao().listOrderedExcept(
            arrayOf(REACTION_1.reaction, REACTION_2.reaction),
            0,
            System.currentTimeMillis() / 1000,
            100
        ).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { reactions -> reactions.isEmpty() }
        observer2.assertTerminated()
    }

    @Test
    fun testListOrderedByType() {
        val observer1 = TestObserver<Any>()
        dao().save(REACTIONS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<ReactionR>>()
        dao().listOrderedByType(
            REACTION_1.reaction,
            0,
            System.currentTimeMillis() / 1000,
            100
        ).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { reactions -> reactions.size == 1 && reactions[0] == REACTION_1 }
        observer2.assertTerminated()
    }

}