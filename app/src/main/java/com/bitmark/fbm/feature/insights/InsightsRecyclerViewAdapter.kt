/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.insights

import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.fbm.R
import com.bitmark.fbm.util.DateTimeUtil
import com.bitmark.fbm.util.ext.getDimensionPixelSize
import com.bitmark.fbm.util.ext.gone
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import com.bitmark.fbm.util.ext.visible
import com.bitmark.fbm.util.modelview.InsightModelView
import kotlinx.android.synthetic.main.item_categories.view.*
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_data_coming.view.*
import kotlinx.android.synthetic.main.item_how_u_r_tracked.view.*
import kotlinx.android.synthetic.main.item_income.view.*


class InsightsRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val INCOME = 0x00
        private const val CATEGORY = 0x01
        private const val USER_TRACKED = 0x02
        private const val DATA_COMING = 0x03
    }

    private val items = mutableListOf<Item>()

    private var itemClickListener: ItemClickListener? = null

    fun set(insights: List<InsightModelView>) {
        this.items.clear()
        val items = insights.map { i ->
            val type = when {
                i.income != null -> INCOME
                i.categories != null -> CATEGORY
                i.notificationEnabled != null -> DATA_COMING
                else -> USER_TRACKED
            }
            Item(type, i)
        }
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun setNotificationEnable(enable: Boolean) {
        val index = items.indexOfFirst { i -> i.insight.notificationEnabled != null }
        if (index != -1) {
            items[index].insight.notificationEnabled = enable
            notifyItemChanged(index)
        }
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    fun isEmpty() = itemCount == 0

    fun setItemClickListener(listener: ItemClickListener?) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            INCOME -> IncomeVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_income,
                    parent,
                    false
                ), itemClickListener
            )
            CATEGORY -> CategoryVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_categories,
                    parent,
                    false
                )
            )
            USER_TRACKED -> UserTrackedVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_how_u_r_tracked,
                    parent,
                    false
                ), itemClickListener
            )
            DATA_COMING -> DataComingVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_data_coming,
                    parent,
                    false
                ), itemClickListener
            )
            else -> error("unsupported view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IncomeVH -> holder.bind(items[position])
            is CategoryVH -> holder.bind(items[position])
            is UserTrackedVH -> { // do nothing
            }
            is DataComingVH -> holder.bind(items[position])
            else -> error("unsupported holder")
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].type

    class IncomeVH(view: View, listener: ItemClickListener?) : RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                ivInfo.setOnClickListener { listener?.onIncomeInfoClicked() }
            }
        }

        fun bind(item: Item) {
            with(itemView) {
                val insight = item.insight
                if (insight.income == null || insight.income <= 0f) {
                    tvIncome.text = "--"
                    tvMsg.text = context.getString(R.string.sorry_no_data)
                } else {
                    tvIncome.text = String.format("$%.2f", insight.income)
                    tvMsg.text = context.getString(R.string.income_fb_made_from_you_format)
                        .format(
                            DateTimeUtil.millisToString(
                                insight.incomeFrom!! * 1000,
                                DateTimeUtil.DATE_FORMAT_11,
                                DateTimeUtil.defaultTimeZone()
                            )
                        )
                }
            }
        }
    }

    class CategoryVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            val insight = item.insight
            if (insight.categories == null) error("categories is null")
            with(itemView) {
                val root = itemView as LinearLayout
                root.removeViews(3, root.childCount - 3)

                if (insight.categories.isEmpty()) {
                    tvNoData.visible()
                } else {
                    tvNoData.gone()
                    insight.categories.forEachIndexed { i, category ->
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

    class UserTrackedVH(view: View, listener: ItemClickListener?) : RecyclerView.ViewHolder(view) {

        init {
            with(view) {
                tvReadMore.setSafetyOnclickListener {
                    listener?.onReadMoreClicked()
                }

                val string = context.getString(R.string.read_more_arrow)
                val spannableString = SpannableString(string)
                spannableString.setSpan(
                    UnderlineSpan(),
                    0,
                    string.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                tvReadMore.text = spannableString
            }
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
                tvDataComingTitle.setText(R.string.more_insights_is_coming)
                tvDataComingSubtitle.setText(
                    if (item.insight.notificationEnabled == true) {
                        R.string.your_fb_data_archive_is_being_processed_2
                    } else {
                        R.string.your_fb_data_archive_is_being_processed_1
                    }
                )
                tvNotifyMe.visibility =
                    if (item.insight.notificationEnabled == true) View.GONE else View.VISIBLE
            }
        }

    }

    data class Item(
        val type: Int,
        val insight: InsightModelView
    )

    interface ItemClickListener {

        fun onIncomeInfoClicked()

        fun onReadMoreClicked()

        fun onNotifyMeClicked()
    }
}