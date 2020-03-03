/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.dao

import android.database.sqlite.SQLiteConstraintException
import com.bitmark.fbm.data.model.PostData
import com.bitmark.fbm.data.source.local.api.dao.PostDao
import io.reactivex.observers.TestObserver
import org.junit.Test


class PostDaoTest : DaoTest<PostDao>() {

    override fun dao(): PostDao = database.postDao()

    @Test
    fun testSaveAndGetSingleRec() {
        val observer1 = TestObserver<Any>()
        dao().save(POST_1).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<PostData>>()
        dao().listOrderedPost(
            0,
            System.currentTimeMillis() / 1000,
            100
        ).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { res ->
            // assert primary key only
            res.size == 1 && res[0].timestampSec == POST_1.timestampSec
        }
        observer2.assertTerminated()
    }

    @Test
    fun testSaveAndGetMultipleRec() {
        val observer1 = TestObserver<Any>()
        dao().save(POSTS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<PostData>>()
        dao().listOrderedPost(0, System.currentTimeMillis() / 1000, 100).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { res ->
            res.size == POSTS.size && res[0].timestampSec == POST_2.timestampSec && res[1].timestampSec == POST_1.timestampSec
        }
        observer2.assertTerminated()
    }

    @Test
    fun testSaveWForeignKeyRequired() {
        val observer1 = TestObserver<Any>()
        dao().save(POST_3).subscribe(observer1)

        observer1.assertNotComplete()
        observer1.assertNoValues()
        observer1.assertError(SQLiteConstraintException::class.java)
        observer1.assertTerminated()

        val observer2 = TestObserver<Any>()
        database.locationDao().save(LOCATION_1).subscribe()
        dao().save(POST_3).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertTerminated()

        val observer3 = TestObserver<List<PostData>>()
        dao().listOrderedPost(0, System.currentTimeMillis() / 1000, 100).subscribe(observer3)

        observer3.assertComplete()
        observer3.assertNoErrors()
        observer3.assertValue { res -> res.size == 1 && res[0].timestampSec == POST_3.timestampSec }
        observer3.assertTerminated()
    }

    @Test
    fun testDelete() {
        val observer1 = TestObserver<Any>()
        dao().save(POSTS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<Any>()
        dao().delete().subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoValues()
        observer2.assertNoErrors()
        observer2.assertTerminated()

        val observer3 = TestObserver<List<PostData>>()
        dao().listOrderedPost(0, System.currentTimeMillis() / 1000, 100).subscribe(observer3)

        observer3.assertComplete()
        observer3.assertNoErrors()
        observer3.assertValue { res -> res.isEmpty() }
        observer3.assertTerminated()
    }

    @Test
    fun testListOrderedByType() {
        val observer1 = TestObserver<Any>()
        dao().save(POST_1).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<PostData>>()
        dao().listOrderedPostByType(POST_1.type, 0L, System.currentTimeMillis() / 1000, 100)
            .subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { res -> res.size == 1 && res[0].timestampSec == POST_1.timestampSec }
        observer2.assertTerminated()
    }

    @Test
    fun testListOrderedByTag() {
        val observer1 = TestObserver<Any>()
        dao().save(POSTS).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<PostData>>()
        dao().listOrderedPostByTag("user1", 0L, System.currentTimeMillis() / 1000, 100)
            .subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { res -> res.size == POSTS.size && res[0].timestampSec == POST_2.timestampSec && res[1].timestampSec == POST_1.timestampSec }
        observer2.assertTerminated()
    }

    @Test
    fun testListOrderedByLocation() {
        val observer1 = TestObserver<Any>()
        database.locationDao().save(LOCATION_1).subscribe()
        dao().save(POST_3).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        val observer2 = TestObserver<List<PostData>>()
        dao().listOrderedPostByLocations(
            listOf(LOCATION_1.id),
            0L,
            System.currentTimeMillis() / 1000,
            100
        )
            .subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { res -> res.size == 1 && res[0].timestampSec == POST_3.timestampSec }
        observer2.assertTerminated()
    }

    @Test
    fun testLocationDeleted() {
        val observer1 = TestObserver<Any>()
        database.locationDao().save(LOCATION_1).subscribe()
        dao().save(POST_3).subscribe(observer1)

        observer1.assertComplete()
        observer1.assertNoErrors()
        observer1.assertTerminated()

        database.locationDao().delete().subscribe()

        val observer2 = TestObserver<List<PostData>>()
        dao().listOrderedPost(0, System.currentTimeMillis(), 100).subscribe(observer2)

        observer2.assertComplete()
        observer2.assertNoErrors()
        observer2.assertValue { res -> res.isEmpty() }
        observer2.assertTerminated()
    }
}