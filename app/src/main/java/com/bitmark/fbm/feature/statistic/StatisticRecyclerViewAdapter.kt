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
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.entity.SectionName
import com.bitmark.fbm.util.DateTimeUtil
import com.bitmark.fbm.util.ext.getDimensionPixelSize
import com.bitmark.fbm.util.ext.gone
import com.bitmark.fbm.util.ext.visible
import com.bitmark.fbm.util.modelview.SectionModelView
import com.bitmark.fbm.util.view.statistic.GroupView
import com.bitmark.fbm.util.view.statistic.SectionView
import kotlinx.android.synthetic.main.item_archive_uploading.view.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_income.view.*
import kotlinx.android.synthetic.main.item_sentiment.view.*


class StatisticRecyclerViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val ADS_CATEGORIES = 0x01

        private const val STATISTIC = 0x02

        private const val FB_INCOME = 0x03

        private const val ARCHIVE_UPLOAD = 0x04

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

    fun set(sections: List<SectionModelView>) {
        items.clear()
        items.addAll(sections.map { s ->
            val type = when {
                s.name in arrayOf(
                    SectionName.POST,
                    SectionName.REACTION,
                    SectionName.STATS
                ) -> STATISTIC
                s.categories != null -> ADS_CATEGORIES
                s.income != null && s.incomeFrom != null -> FB_INCOME
                else -> ARCHIVE_UPLOAD
            }
            Item(type, s)
        })

        notifyDataSetChanged()
    }

    fun addArchiveUploadSection() {
        val index = items.indexOfFirst { i -> i.type == ARCHIVE_UPLOAD }
        if (index == -1) {
            val pos = items.size
            items.add(Item(ARCHIVE_UPLOAD, SectionModelView.newEmptyInstance()))
            notifyItemInserted(pos)
        }
    }

    fun removeArchiveUploadSection() {
        val index = items.indexOfFirst { i -> i.type == ARCHIVE_UPLOAD }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
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

            ADS_CATEGORIES -> CategoryVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_categories,
                    parent,
                    false
                )
            )

            FB_INCOME -> IncomeVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_income,
                    parent,
                    false
                ), itemClickListener
            )

            ARCHIVE_UPLOAD -> ArchiveUploadingVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_archive_uploading,
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
            ADS_CATEGORIES -> (holder as? CategoryVH)?.bind(items[position])
            FB_INCOME -> (holder as? IncomeVH)?.bind(items[position])
            ARCHIVE_UPLOAD -> {
                // do nothing
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    class StatisticVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            (itemView as SectionView).bind(item.section)
        }
    }

    class CategoryVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            with(itemView) {
                val root = itemView as LinearLayout
                root.removeViews(3, root.childCount - 3)
                val categories = item.section.categories!!
                if (categories.isEmpty()) {
                    tvNoData.visible()
                } else {
                    tvNoData.gone()
                    categories.forEachIndexed { i, category ->
                        val categoryView =
                            LayoutInflater.from(context).inflate(R.layout.item_category, null)
                        categoryView.tvCategory.text = category
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        val marginTop =
                            context.getDimensionPixelSize(if (i == 0) R.dimen.dp_24 else R.dimen.dp_16)
                        params.setMargins(0, marginTop, 0, 0)
                        root.addView(categoryView, params)
                    }
                }

            }
        }
    }

    class IncomeVH(view: View, listener: ItemClickListener?) : RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                ivInfo.setOnClickListener { listener?.onIncomeInfoClicked() }
            }
        }

        fun bind(item: Item) {
            with(itemView) {
                val income = item.section.income
                val incomeFrom = item.section.incomeFrom
                if (income == null || income <= 0f) {
                    tvIncome.text = "--"
                    tvMsg.text = context.getString(R.string.sorry_no_data)
                } else {
                    tvIncome.text = String.format("$%.2f", income)
                    tvMsg.text = context.getString(R.string.income_fb_made_from_you_format)
                        .format(
                            DateTimeUtil.millisToString(
                                incomeFrom!! * 1000,
                                DateTimeUtil.DATE_FORMAT_11,
                                DateTimeUtil.defaultTimeZone()
                            )
                        )
                }
            }
        }
    }

    class ArchiveUploadingVH(view: View, listener: ItemClickListener?) :
        RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                tvViewProgress.setOnClickListener {
                    listener?.onViewProgressClicked()
                }
            }
        }

    }

    data class Item(
        val type: Int,
        val section: SectionModelView
    )

    interface ItemClickListener {
        fun onIncomeInfoClicked()

        fun onViewProgressClicked()
    }
}