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
    val name: SectionName?,
    val period: Period?,
    val periodStartedAtSec: Long?,
    val quantity: Int,
    val diffFromPrev: Int?,
    val average: Int,
    val groups: List<GroupModelView>,
    val value: Float?
) : ModelView {
    companion object {

        private const val THRESHOLD_VALS_COUNT = 4

        fun newEmptyInstance() = SectionModelView(null, null, null, 0, null, 0, listOf(), null)

        fun newDefaultInstance(
            name: SectionName?,
            period: Period?,
            periodStartedAtSec: Long?
        ) =
            SectionModelView(
                name,
                period,
                periodStartedAtSec,
                0,
                null,
                0,
                listOf(),
                null
            )

        fun newInstance(sectionR: SectionR, avg: Int): SectionModelView {
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
                        else -> error("unsupported section")
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
                        groupName != GroupName.SUB_PERIOD && groupEntities.size > THRESHOLD_VALS_COUNT
                    val topGroupEntities =
                        if (needAggregate) groupEntities.take(THRESHOLD_VALS_COUNT) else groupEntities
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
                avg,
                groupModelViews,
                sectionR.value
            )
        }
    }

    fun isNoData() = groups.isEmpty()
}

fun SectionModelView.order() = when (name) {
    SectionName.SENTIMENT -> 0
    SectionName.POST -> -1
    SectionName.REACTION -> -2
    else -> error("unsupported section name")
}