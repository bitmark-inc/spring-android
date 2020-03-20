/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.local.api.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bitmark.fbm.data.model.entity.MediaR
import io.reactivex.Completable
import io.reactivex.Single

@Dao
abstract class MediaDao {

    @Query("SELECT * FROM Media WHERE timestamp BETWEEN :startedAtSec AND :endedAtSec ORDER BY timestamp DESC LIMIT :limit")
    abstract fun listOrdered(startedAtSec: Long, endedAtSec: Long, limit: Int): Single<List<MediaR>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun save(mediaRs: List<MediaR>): Completable

    @Query("DELETE FROM Media")
    abstract fun delete(): Completable

    @Query("UPDATE Media SET thumbnail_uri = :thumbnailUri WHERE id = :id")
    abstract fun updateThumbnailUri(id: String, thumbnailUri: String): Completable
}