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
    tableName = "Media",
    indices = [Index(value = ["id"], unique = true), Index(value = ["timestamp"])]
)
data class MediaR(
    @Expose
    @SerializedName("id")
    @ColumnInfo(name = "id")
    @PrimaryKey
    val id: String,

    @Expose
    @SerializedName("uri")
    @ColumnInfo(name = "uri")
    val uri: String,

    @Expose
    @SerializedName("source")
    @ColumnInfo(name = "source_uri")
    val source: String,

    @Expose
    @SerializedName("thumbnail")
    @ColumnInfo(name = "thumbnail_uri")
    val thumbnail: String,

    @Expose
    @SerializedName("extension")
    @ColumnInfo(name = "extension")
    val extension: String,

    @Expose
    @SerializedName("timestamp")
    @ColumnInfo(name = "timestamp")
    val timestampSec: Long
) : Record

val MediaR.timestamp: Long
    get() = timestampSec * 1000