/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "Stats",
    indices = [Index(value = ["started_at", "ended_at"], unique = false), Index(
        value = ["id"],
        unique = true
    )]
)
data class StatsR(

    @Expose
    @SerializedName("id")
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long?,

    @Expose
    @SerializedName("type")
    @ColumnInfo(name = "type")
    val type: StatsType,

    @Expose
    @SerializedName("started_at")
    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @Expose
    @SerializedName("ended_at")
    @ColumnInfo(name = "ended_at")
    val endedAt: Long,

    @Expose
    @SerializedName("data")
    @ColumnInfo(name = "data")
    val data: Map<String, Stats>
) : Record {
    companion object
}

enum class StatsType {
    @Expose
    @SerializedName("post")
    POST,

    @Expose
    @SerializedName("reaction")
    REACTION;

    companion object
}

val StatsType.value
    get() = when (this) {
        StatsType.POST -> "post"
        StatsType.REACTION -> "reaction"
    }

fun StatsType.Companion.fromString(type: String) = when (type) {
    "post" -> StatsType.POST
    "reaction" -> StatsType.REACTION
    else -> error("invalid $type")
}

fun StatsR.Companion.newEmptyInstance(type: StatsType, startedAt: Long, endedAt: Long) =
    StatsR(null, type, startedAt, endedAt, mapOf())

fun StatsR.isEmpty() = data.isEmpty()

data class Stats(
    @Expose
    @SerializedName("sys_avg")
    val systemAvg: Float,

    @Expose
    @SerializedName("count")
    val count: Float?
) {
    companion object
}

fun Stats.toArray() = floatArrayOf(systemAvg, count ?: 0f)