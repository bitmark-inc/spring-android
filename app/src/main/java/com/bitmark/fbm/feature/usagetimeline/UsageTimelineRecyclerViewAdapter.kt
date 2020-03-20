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
import com.bitmark.fbm.util.ext.gone
import com.bitmark.fbm.util.ext.invisible
import com.bitmark.fbm.util.ext.visible
import com.bitmark.fbm.util.view.stickyheader.StickyRecyclerHeadersAdapter
import kotlinx.android.synthetic.main.item_usage_timeline_header.view.*


abstract class UsageTimelineRecyclerViewAdapter(
    protected val period: Period,
    private val headerDivider: Boolean = true
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    companion object {
        internal const val HEADER = 0x98
        internal const val ITEM = 0x99
    }

    protected val data = mutableListOf<Item>()

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }

    override fun getHeaderId(position: Int): Long {
        return data[position].headerPos.toLong()
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return HeaderViewHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.item_usage_timeline_header,
                parent,
                false
            ), period, headerDivider
        )
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?, pos: Int) {
        (holder as? HeaderViewHolder)?.bind(data[pos])
    }

    fun isEmpty() = data.isEmpty()

    class HeaderViewHolder(view: View, private val period: Period, hasDivider: Boolean) :
        RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                if (hasDivider) {
                    divider.visible()
                } else {
                    divider.invisible()
                }
            }
        }

        fun bind(item: Item) {
            with(itemView) {

                val groupedKey = item.groupedKey

                when (period) {
                    Period.MONTH -> {
                        tvHeader2.visible()
                        val keyArray = groupedKey.split("-")
                        val month = keyArray[0].trim()
                        val year = keyArray[1].trim()
                        tvHeader1.text = month
                        tvHeader2.text = year
                    }
                    Period.YEAR -> {
                        tvHeader2.gone()
                        tvHeader1.text = groupedKey
                    }
                    Period.DECADE -> {
                        tvHeader2.gone()
                        tvHeader1.text = groupedKey
                    }
                    else -> error("unsupported period")
                }

            }
        }
    }

    protected fun groupKeyBy(period: Period, timestamp: Long): String {
        val timezone = DateTimeUtil.defaultTimeZone()
        val month =
            DateTimeUtil.millisToString(
                timestamp,
                DateTimeUtil.DATE_FORMAT_12,
                timezone
            )
        val year =
            DateTimeUtil.millisToString(
                timestamp,
                DateTimeUtil.DATE_FORMAT_8,
                timezone
            )


        return when (period) {
            Period.MONTH -> {
                "$month - $year"
            }

            Period.YEAR -> {
                year
            }

            Period.DECADE -> {
                val range = DateTimeUtil.getDateRangeOfDecade(timestamp, timezone)
                String.format(
                    "%s - %s",
                    DateTimeUtil.dateToString(
                        range.first,
                        DateTimeUtil.DATE_FORMAT_8,
                        timezone
                    ),
                    DateTimeUtil.dateToString(
                        range.second,
                        DateTimeUtil.DATE_FORMAT_8,
                        timezone
                    )
                )
            }
            else -> error("unsupported period")
        }
    }

    open class Item(
        val type: Int,
        val headerPos: Int,
        val groupedKey: String
    )
}