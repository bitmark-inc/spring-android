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
import com.bitmark.fbm.data.model.entity.StatsR
import com.bitmark.fbm.data.model.entity.StatsType
import io.reactivex.Completable
import io.reactivex.Single

@Dao
abstract class StatsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun save(stats: StatsR): Completable

    @Query("SELECT * FROM Stats WHERE type = :type AND started_at = :startedAt AND ended_at = :endedAt")
    abstract fun get(type: StatsType, startedAt: Long, endedAt: Long): Single<StatsR>

    @Query("DELETE FROM Stats")
    abstract fun delete(): Completable
}