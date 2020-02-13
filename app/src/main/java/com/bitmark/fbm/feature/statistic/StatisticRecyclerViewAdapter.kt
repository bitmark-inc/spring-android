/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.statistic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.entity.SectionName
import com.bitmark.fbm.util.ext.gone
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import com.bitmark.fbm.util.ext.visible
import com.bitmark.fbm.util.modelview.SectionModelView
import com.bitmark.fbm.util.view.statistic.GroupView
import com.bitmark.fbm.util.view.statistic.SectionView
import kotlinx.android.synthetic.main.item_data_coming.view.*
import kotlinx.android.synthetic.main.item_sentiment.view.*
import kotlin.math.roundToInt


class StatisticRecyclerViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val SENTIMENT = 0x01

        private const val STATISTIC = 0x02

        private const val DATA_COMING = 0x03

    }

    private val items = mutableListOf<Item>()

    private var chartClickListener: GroupView.ChartClickListener? = null

    private var itemClickListener: ItemClickListener? = null

    fun setChartClickListener(listener: GroupView.ChartClickListener) {
        this.chartClickListener = listener
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    fun set(sections: List<SectionModelView>, notificationEnabled: Boolean? = null) {
        items.clear()
        items.addAll(sections.map { s ->
            val type = when (s.name) {
                SectionName.SENTIMENT -> SENTIMENT
                SectionName.POST, SectionName.REACTION, SectionName.STATS -> STATISTIC
                else -> DATA_COMING
            }
            Item(type, s, notificationEnabled)
        })
        notifyDataSetChanged()
    }

    fun setNotificationEnable(enable: Boolean) {
        val index = items.indexOfFirst { i -> i.notificationEnabled != null }
        if (index != -1) {
            items[index].notificationEnabled = enable
            notifyItemChanged(index)
        }
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    fun isEmpty() = itemCount == 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            STATISTIC -> {
                val sectionView = SectionView(parent.context)
                if (chartClickListener != null) {
                    sectionView.setChartClickListener(chartClickListener!!)
                }
                StatisticVH(sectionView)
            }

            SENTIMENT -> SentimentVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_sentiment,
                    parent,
                    false
                )
            )

            DATA_COMING -> DataComingVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_data_coming,
                    parent,
                    false
                ), itemClickListener
            )

            else -> error("invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            STATISTIC -> (holder as? StatisticVH)?.bind(items[position])
            SENTIMENT -> (holder as? SentimentVH)?.bind(items[position])
            DATA_COMING -> (holder as? DataComingVH)?.bind(items[position])
        }

    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    class SentimentVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            val value = item.section?.value
            with(itemView) {
                if (value != null) {
                    tvNoData.gone()
                    val rounded = value.roundToInt()
                    ivSeekbar.setImageResource(
                        when (rounded) {
                            0, 1 -> R.drawable.ic_seek_bar_1
                            2 -> R.drawable.ic_seek_bar_2
                            3 -> R.drawable.ic_seek_bar_3
                            4 -> R.drawable.ic_seek_bar_4
                            5 -> R.drawable.ic_seek_bar_5
                            6 -> R.drawable.ic_seek_bar_6
                            7 -> R.drawable.ic_seek_bar_7
                            8 -> R.drawable.ic_seek_bar_8
                            9 -> R.drawable.ic_seek_bar_9
                            10 -> R.drawable.ic_seek_bar_10
                            else -> R.drawable.ic_seek_bar_0
                        }
                    )

                    ivSentiment.setImageResource(
                        when {
                            rounded in 0..1 -> R.drawable.ic_cry_bw
                            rounded in 2..3 -> R.drawable.ic_sad_bw
                            rounded in 4..5 -> R.drawable.ic_no_feeling_bw
                            rounded in 6..7 -> R.drawable.ic_smile_bw
                            rounded >= 8 -> R.drawable.ic_happy_bw
                            else -> R.drawable.ic_wow_bw
                        }
                    )
                } else {
                    tvNoData.visible()
                    ivSentiment.setImageResource(R.drawable.ic_wow_bw)
                    ivSeekbar.setImageResource(R.drawable.ic_seek_bar_0)
                }
            }
        }
    }

    class StatisticVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            (itemView as SectionView).bind(item.section!!)
        }
    }

    class DataComingVH(view: View, listener: ItemClickListener?) : RecyclerView.ViewHolder(view) {

        init {
            with(view) {
                tvNotifyMe.setSafetyOnclickListener {
                    listener?.onNotifyMeClicked()
                }
            }
        }

        fun bind(item: Item) {
            with(itemView) {
                tvDataComingTitle.setText(R.string.personal_analytics_are_coming)
                tvDataComingSubtitle.setText(
                    if (item.notificationEnabled == true) {
                        R.string.your_fb_data_archive_is_being_processed_2
                    } else {
                        R.string.your_fb_data_archive_is_being_processed_1
                    }
                )
                tvNotifyMe.visibility =
                    if (item.notificationEnabled == true) View.GONE else View.VISIBLE
            }
        }

    }

    data class Item(
        val type: Int,
        val section: SectionModelView?,
        var notificationEnabled: Boolean?
    )

    interface ItemClickListener {

        fun onNotifyMeClicked()
    }
}