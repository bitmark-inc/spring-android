/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.usagetimeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.entity.Period
import com.bitmark.fbm.util.DateTimeUtil
import com.bitmark.fbm.util.modelview.ReactionModelView
import com.bitmark.fbm.util.modelview.getDrawRes
import kotlinx.android.synthetic.main.item_reaction.view.*

class ReactionTimelineRecyclerViewAdapter(period: Period) :
    UsageTimelineRecyclerViewAdapter(period) {

    fun add(reactionModelView: List<ReactionModelView>) {
        if (reactionModelView.isEmpty()) return
        val pos = this.data.size
        val sortedData = reactionModelView.sortedByDescending { it.timestamp }
        val groupedData = sortedData.groupBy { groupKeyBy(period, it.timestamp) }
        val reactionItems = mutableListOf<ReactionItem>()
        groupedData.forEach { (key, value) ->
            val headerPos = reactionItems.size
            value.forEach { v ->
                reactionItems.add(ReactionItem(ITEM, headerPos, key, v))
            }
        }
        this.data.addAll(reactionItems)
        notifyItemRangeInserted(pos, reactionItems.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM -> ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_reaction,
                    parent,
                    false
                ),
                period
            )
            else -> error("invalid viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = this.data[position] as ReactionItem
        val type = data.type
        if (type == ITEM) {
            (holder as? ItemViewHolder)?.bind(data)
        }
    }

    override fun getNumColumns(): Int = 1

    class ItemViewHolder(view: View, private val period: Period) : RecyclerView.ViewHolder(view) {

        fun bind(item: ReactionItem) {
            val reaction = item.reaction!!
            with(itemView) {
                val context = itemView.context!!
                val date = DateTimeUtil.millisToString(
                    reaction.timestamp, if (period == Period.DECADE) {
                        DateTimeUtil.DATE_FORMAT_2
                    } else {
                        DateTimeUtil.DATE_FORMAT_3
                    }, DateTimeUtil.defaultTimeZone()
                )
                val time =
                    DateTimeUtil.millisToString(
                        reaction.timestamp,
                        DateTimeUtil.TIME_FORMAT_1,
                        DateTimeUtil.defaultTimeZone()
                    )
                val dateTime =
                    StringBuilder(context.getString(R.string.date_format_1).format(date, time))
                tvTime.text = dateTime

                tvContent.text = reaction.title
                ivType.setImageResource(reaction.getDrawRes())
            }
        }
    }

    class ReactionItem(
        type: Int,
        headerPos: Int,
        groupedKey: String,
        val reaction: ReactionModelView?
    ) : Item(type, headerPos, groupedKey)
}