/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.view.statistic

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.entity.SectionName
import com.bitmark.fbm.util.ext.decimalFormat
import com.bitmark.fbm.util.ext.getDimensionPixelSize
import com.bitmark.fbm.util.ext.gone
import com.bitmark.fbm.util.ext.visible
import com.bitmark.fbm.util.modelview.GroupModelView
import com.bitmark.fbm.util.modelview.SectionModelView
import com.bitmark.fbm.util.modelview.hasAnyGroupWithData
import com.bitmark.fbm.util.modelview.hasAnyGroupWithFullData
import kotlinx.android.synthetic.main.layout_legend.view.*
import kotlinx.android.synthetic.main.layout_section.view.*
import kotlinx.android.synthetic.main.layout_section_header.view.*


class SectionView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private var chartClickListener: GroupView.ChartClickListener? = null

    init {
        inflate(context, R.layout.layout_section, this)

        orientation = VERTICAL
        val paddingHorizontal = context.getDimensionPixelSize(R.dimen.dp_18)
        val paddingVertical = context.getDimensionPixelSize(R.dimen.dp_30)
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams = params
    }

    fun setChartClickListener(listener: GroupView.ChartClickListener) {
        this.chartClickListener = listener
    }

    fun bind(section: SectionModelView) {
        when (section.name) {
            SectionName.POST -> {
                tvOverview.text = context.getString(R.string.posts_format)
                    .format(section.quantity!!.decimalFormat())
                tvOverviewSuffix.text = context.getString(R.string.you_made).toLowerCase()
            }
            SectionName.REACTION -> {
                tvOverview.text =
                    context.getString(R.string.reactions_format)
                        .format(section.quantity!!.decimalFormat())
                tvOverviewSuffix.text = context.getString(R.string.you_gave).toLowerCase()
            }
            SectionName.STATS -> {
                tvOverview.text = context.getString(R.string.all_of_spring)
                tvOverviewSuffix.text = context.getString(R.string.aggregate_analysis)
            }
            else -> {
                // do nothing
            }
        }

        val groups = section.groups
        val isNoData = section.isNoData()
        val isStatsSection = section.name == SectionName.STATS
        val defaultChildCount = if (isStatsSection && !isNoData) 2 else 1

        tvEmpty.visibility = if (isNoData) View.VISIBLE else View.GONE
        removeGroups(defaultChildCount)
        if (isNoData) return

        // init chart legend
        if (isStatsSection) {
            ivIcon1.setImageResource(R.drawable.bg_indian_khaki_circle)
            ivIcon2.setImageResource(R.drawable.bg_cognac_circle)
            tvName1.setText(R.string.spring_user_avg)
            tvName2.setText(R.string.your_posts)
            if (section.hasAnyGroupWithData()) {
                ivIcon1.visible()
                tvName1.visible()
                if (section.hasAnyGroupWithFullData()) {
                    ivIcon2.visible()
                    tvName2.visible()
                } else {
                    ivIcon2.gone()
                    tvName2.gone()
                }
            } else {
                ivIcon1.gone()
                tvName1.gone()
                ivIcon2.gone()
                tvName2.gone()
            }

            layoutLegend.visible()
        }

        addGroups(groups, defaultChildCount)
        bindGroupsData(groups, defaultChildCount)
    }

    private fun removeGroups(defaultChildCount: Int) {
        val count = children.filter { v -> v is GroupView }.count()
        if (count == 0) return
        removeViews(defaultChildCount, count)
    }

    private fun addGroups(groups: List<GroupModelView>, defaultChildCount: Int) {
        for (index in groups.indices) {
            val groupView = GroupView(context)
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            params.setMargins(0, context.getDimensionPixelSize(R.dimen.dp_25), 0, 0)
            groupView.layoutParams = params
            if (chartClickListener != null) {
                groupView.setChartClickListener(chartClickListener!!)
            }
            addView(groupView, index + defaultChildCount)
        }
    }

    private fun bindGroupsData(groups: List<GroupModelView>, defaultChildCount: Int) {
        for (index in defaultChildCount until childCount) {
            val view = getChildAt(index) as? GroupView
            view?.bind(groups[index - defaultChildCount])
        }
    }
}