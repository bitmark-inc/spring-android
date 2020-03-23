/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.modelview

import com.bitmark.fbm.data.model.entity.*
import kotlin.math.roundToInt


data class SectionModelView(
    val name: SectionName? = null,
    val period: Period? = null,
    val periodStartedAtSec: Long? = null,
    val quantity: Int = 0,
    val diffFromPrev: Int = 0,
    val groups: List<GroupModelView>? = listOf(),
    val value: Float? = null,
    val income: Float? = null,
    val categories: List<String>? = null
) : ModelView {
    companion object {

        private const val MAX_GROUP_VALUE_COUNT = 4

        fun newEmptyInstance() = SectionModelView()

        fun newInstance(
            name: SectionName,
            period: Period,
            periodStartedAtSec: Long
        ) =
            SectionModelView(
                name = name,
                period = period,
                periodStartedAtSec = periodStartedAtSec
            )

        fun newInstance(period: Period, periodStartedAtSec: Long, income: Float) =
            SectionModelView(
                period = period,
                periodStartedAtSec = periodStartedAtSec,
                income = income
            )

        fun newInstance(categories: List<String>) = SectionModelView(categories = categories)

        fun newInstance(sectionR: SectionR): SectionModelView {
            val sectionName = sectionR.name
            val period = sectionR.period
            val quantity = sectionR.quantity
            val diffFromPrev = sectionR.diffFromPrev
            val groupModelViews = mutableListOf<GroupModelView>()
            val typesCount: Int // total count of stack
            val groups = sectionR.groups

            if (groups != null) {
                val types = sectionR.getGroup<GroupEntity>(GroupName.TYPE)
                typesCount = when (sectionName) {
                    SectionName.POST -> 4
                    SectionName.REACTION -> 6
                    else -> error("unsupported section")
                }

                var data = types.data
                if (sectionName == SectionName.REACTION) {
                    data =
                        data.filterNot { d -> Reaction.fromString(d.key) in Reaction.UNSUPPORTED_TYPE }
                }
                val entries = (0 until typesCount).map { i ->
                    val xVal = when (sectionName) {
                        SectionName.POST -> PostType.fromIndex(i).value
                        SectionName.REACTION -> Reaction.fromIndex(i).value
                        else -> error("unsupported section $sectionName")
                    }
                    val yVal = if (data.containsKey(xVal)) data[xVal] else 0
                    Entry(arrayOf(xVal), floatArrayOf(yVal!!.toFloat()))
                }

                groupModelViews.add(
                    GroupModelView(
                        period,
                        sectionName,
                        GroupName.TYPE,
                        typesCount,
                        entries
                    )
                )

                for (gEntry in sectionR.groups.entries) {
                    val groupName = GroupName.fromString(gEntry.key)
                    if (groupName == GroupName.TYPE) continue
                    var groupEntities = sectionR.getArrayGroup<GroupEntity>(groupName)
                    if (sectionName == SectionName.REACTION) {
                        // filter out unsupported reactions
                        groupEntities = groupEntities.map { grs ->
                            GroupEntity(grs.name, grs.data.filterNot { d ->
                                Reaction.fromString(d.key) in Reaction.UNSUPPORTED_TYPE
                            })
                        }
                    }
                    if (groupEntities.isEmpty()) continue
                    if (groupName != GroupName.SUB_PERIOD) {
                        groupEntities =
                            groupEntities.sortedByDescending { g -> g.data.entries.sumBy { e -> e.value } }
                    } else {
                        val dateRange = period.toSubPeriodCollectionSec(sectionR.periodStartedAtSec)
                        if (groupEntities.size < dateRange.size) {
                            // fill missing sub-period records
                            val newGroupEntities = dateRange.map { dateSec ->
                                val name = dateSec.toString()
                                GroupEntity(
                                    name,
                                    groupEntities.find { e -> e.name == name }?.data ?: mapOf()
                                )
                            }
                            groupEntities = newGroupEntities
                        }
                    }

                    val needAggregate =
                        groupName != GroupName.SUB_PERIOD && groupEntities.size > MAX_GROUP_VALUE_COUNT
                    val topGroupEntities =
                        if (needAggregate) groupEntities.take(MAX_GROUP_VALUE_COUNT) else groupEntities
                    val topGroupEntries = topGroupEntities.map { g ->
                        val xVals = g.name
                        val yVals = FloatArray(typesCount) { 0f }
                        val dataEntries = g.data.entries
                        dataEntries.forEach { e ->
                            val key = e.key
                            val index = when (sectionName) {
                                SectionName.POST -> PostType.indexOf(key)
                                SectionName.REACTION -> Reaction.indexOf(key)
                                else -> error("unsupported section")
                            }
                            yVals[index] += e.value.toFloat()
                        }
                        Entry(arrayOf(xVals), yVals)
                    }.toMutableList()

                    if (needAggregate) {
                        val topGroupName = topGroupEntities.map { g -> g.name }
                        val aggregateData = groupEntities.filterNot { g -> g.name in topGroupName }
                        val xVals = aggregateData.map { d -> d.name }.toTypedArray()
                        val yVals = FloatArray(typesCount) { 0f }
                        aggregateData.forEach { g ->
                            g.data.entries.forEach { e ->
                                val key = e.key
                                val index = when (sectionName) {
                                    SectionName.POST -> PostType.indexOf(key)
                                    SectionName.REACTION -> Reaction.indexOf(key)
                                    else -> error("unsupported section")
                                }
                                yVals[index] += e.value.toFloat()
                            }
                        }
                        topGroupEntries.add(Entry(xVals, yVals))
                    }

                    groupModelViews.add(
                        GroupModelView(
                            period,
                            sectionName,
                            groupName,
                            typesCount,
                            topGroupEntries
                        )
                    )
                }

            }

            groupModelViews.sortWith(Comparator { o1, o2 -> o2.order().compareTo(o1.order()) })

            return SectionModelView(
                sectionName,
                period,
                sectionR.periodStartedAtSec,
                quantity,
                (diffFromPrev * 100).roundToInt(),
                groupModelViews,
                sectionR.value
            )
        }

        fun newInstance(
            period: Period,
            periodStartedAtSec: Long,
            statsList: List<StatsR>
        ): SectionModelView {
            val sectionName = SectionName.STATS
            val groupModelViews = statsList.map { stats ->
                val groupName =
                    if (stats.type == StatsType.POST) GroupName.POST_STATS else GroupName.REACTION_STATS
                val typeCount = when (groupName) {
                    GroupName.POST_STATS -> 4
                    GroupName.REACTION_STATS -> 6
                    else -> error("invalid group name $groupName")
                }
                val data = stats.data
                val entries = (0 until typeCount).map { i ->
                    val xVal = when (groupName) {
                        GroupName.POST_STATS -> PostType.fromIndex(i).value
                        GroupName.REACTION_STATS -> Reaction.fromIndex(i).value
                        else -> error("unsupported group $groupName")
                    }
                    val yVal =
                        if (data.containsKey(xVal)) {
                            data.getValue(xVal).toArray()
                        } else {
                            floatArrayOf(0f, 0f)
                        }
                    Entry(arrayOf(xVal), yVal)
                }
                GroupModelView(period, sectionName, groupName, typeCount, entries)
            }

            return SectionModelView(
                name = sectionName,
                period = period,
                periodStartedAtSec = periodStartedAtSec,
                groups = groupModelViews
            )

        }
    }
}

fun SectionModelView.isEmptyGroups() = groups!!.isEmpty()

fun SectionModelView.hasAnyGroupWithFullData() = groups?.any { g -> g.hasAnyWithFullData() }

fun SectionModelView.hasAnyGroupWithData() = groups?.any { g -> g.hasAnyWithData() }