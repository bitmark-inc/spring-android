/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.dao

import com.bitmark.fbm.data.model.FriendData
import com.bitmark.fbm.data.model.entity.*

val CRITERIA_1 = CriteriaR.fromPostWType(PostType.UPDATE, 315532800000L, 347155200000L)

val CRITERIA_2 = CriteriaR.fromPostWRange(315532800000L, 347155200000L)

val CRITERIA_3 = CriteriaR.fromReactionWType(Reaction.LOVE, 315532800000L, 347155200000L)

val CRITERIA_4 = CriteriaR.fromStatisticWType("usage", 315532800000L, 347155200000L)

val POST_1 =
    PostR(
        "uuid1",
        "content1",
        315532800L,
        "title1",
        PostType.UPDATE,
        null,
        listOf(FriendData("uuid1", "user1")),
        null,
        null
    )

val POST_2 =
    PostR(
        "uuid2",
        "content2",
        347155200L,
        "title2",
        PostType.LINK,
        null,
        listOf(FriendData("uuid1", "user1"), FriendData("uuid2", "user2")),
        null,
        "https://url.com"
    )

val POST_3 =
    PostR(
        "uuid3",
        "content3",
        347155200L,
        "title2",
        PostType.LINK,
        "location_1",
        null,
        null,
        "https://url.com"
    )

val POSTS = listOf(POST_1, POST_2)

val LOCATION_1 = LocationR("location_1", "location_name_1", null, "address_1", null, 315532800L)

val REACTION_1 = ReactionR("uuid1", "actor_1", Reaction.LIKE, 315532800L, "title_1")

val REACTION_2 = ReactionR("uuid2", "actor_2", Reaction.LOVE, 347155200L, "title_2")

val REACTIONS = listOf(REACTION_1, REACTION_2)

val SECTION_1 = SectionR(1, SectionName.POST, Period.WEEK, 315532800L, 0.5f, 10, null, 0.5f)

val SECTION_2 = SectionR(2, SectionName.REACTION, Period.WEEK, 315532800L, -0.5f, 0, null, 5f)

val SECTIONS = listOf(SECTION_1, SECTION_2)

val STATS = StatsR(null, StatsType.POST, 315532800L, 347155200L, mapOf("test" to Stats(0.1f, 0.2f)))

