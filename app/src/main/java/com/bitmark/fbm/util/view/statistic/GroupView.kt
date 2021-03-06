/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.view.statistic

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.entity.*
import com.bitmark.fbm.util.DateTimeUtil
import com.bitmark.fbm.util.ext.getDimensionPixelSize
import com.bitmark.fbm.util.ext.gone
import com.bitmark.fbm.util.ext.visible
import com.bitmark.fbm.util.modelview.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.android.synthetic.main.layout_group_header.view.*
import kotlinx.android.synthetic.main.layout_section.view.*
import java.util.*
import kotlin.Comparator


class GroupView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {

    private var chartClickListener: ChartClickListener? = null

    companion object {

        private const val MAX_HORIZONTAL_COUNT = 5

        private const val MAX_VERTICAL_COUNT = 12

        private val stringResLabelMap = mapOf(
            R.string.updates to PostType.UPDATE.value,
            R.string.photos_videos to PostType.MEDIA.value,
            R.string.stories to PostType.STORY.value,
            R.string.links to PostType.LINK.value,
            R.string.like to Reaction.LIKE.value,
            R.string.love to Reaction.LOVE.value,
            R.string.haha to Reaction.HAHA.value,
            R.string.wow to Reaction.WOW.value,
            R.string.sad to Reaction.SAD.value,
            R.string.angry to Reaction.ANGRY.value
        )
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        inflate(context, R.layout.layout_group, this)

        orientation = VERTICAL
    }

    fun setChartClickListener(listener: ChartClickListener) {
        this.chartClickListener = listener
    }

    fun bind(g: GroupModelView) {
        val group = g.copy()
        if (group.name == GroupName.SUB_PERIOD) {
            // sort the sub-period by ascending
            val sortedEntries = group.entries.toMutableList()
            sortedEntries.sortWith(Comparator { o1, o2 ->
                o1.xValue[0].toLong().compareTo(o2.xValue[0].toLong())
            })
            group.entries = sortedEntries
        }
        tvName.text = context.getString(
            when (group.sectionName) {
                SectionName.POST -> {
                    when (group.name) {
                        GroupName.TYPE -> R.string.by_type
                        GroupName.SUB_PERIOD -> when (group.period) {
                            Period.WEEK -> R.string.by_day
                            Period.YEAR -> R.string.by_month
                            Period.DECADE -> R.string.by_year
                            else -> error("unsupported period")
                        }
                        GroupName.FRIEND -> R.string.by_friends_tagged
                        GroupName.PLACE -> R.string.by_place_tagged
                        else -> error("invalid group name")
                    }
                }
                SectionName.REACTION -> {
                    when (group.name) {
                        GroupName.TYPE -> R.string.by_type
                        GroupName.SUB_PERIOD -> when (group.period) {
                            Period.WEEK -> R.string.by_day
                            Period.YEAR -> R.string.by_month
                            Period.DECADE -> R.string.by_year
                            else -> error("unsupported period")
                        }
                        GroupName.FRIEND -> R.string.by_friend
                        else -> error("invalid group name")
                    }
                }
                SectionName.STATS -> {
                    when (group.name) {
                        GroupName.POST_STATS -> R.string.posts_by_type
                        GroupName.REACTION_STATS -> R.string.reaction_by_type
                        else -> error("invalid group name")
                    }
                }
                else -> error("invalid section name")
            }
        ).toUpperCase(Locale.getDefault())
        tvNameSuffix.text = context.getString(
            when (group.sectionName) {
                SectionName.REACTION -> {
                    when (group.name) {
                        GroupName.SUB_PERIOD -> R.string.you_reacted
                        GroupName.FRIEND -> R.string.you_reacted_to
                        else -> R.string.empty
                    }
                }
                else -> R.string.empty
            }
        ).toLowerCase(Locale.getDefault())

        removeChartView()

        if (!group.hasAnyWithData()) {
            tvEmpty.visible()
        } else {
            tvEmpty.gone()
            val vertical = group.name == GroupName.SUB_PERIOD

            if (!vertical) {
                group.reverse()
            }

            val isStatsChart = group.sectionName == SectionName.STATS

            val barXValues = getBarXValues(group)
            val chartView = if (isStatsChart) {
                buildMultiBarChart(barXValues)
            } else {
                buildSingleBarChart(group, barXValues)
            }

            val width = if (vertical) {
                calculateVerticalWidth(barXValues.size)
            } else {
                LayoutParams.MATCH_PARENT
            }
            val h = context.getDimensionPixelSize(R.dimen.dp_180)
            val height =
                if (vertical) h else calculateHorizontalHeight(barXValues.size)
            val params = LayoutParams(width, height)
            val margin = context.getDimensionPixelSize(R.dimen.dp_8)
            params.marginStart = margin
            params.marginEnd = margin

            addView(chartView, params)

            val data = if (isStatsChart) {
                getMultiBarData(group, barXValues, chartView)
            } else {
                getSingleBarData(group, barXValues)
            }
            chartView.data = data
            chartView.notifyDataSetChanged()
        }
    }

    private fun removeChartView() {
        val childCount = childCount
        for (i in 0 until childCount) {
            if (getChildAt(i) is BarChart) {
                removeViewAt(i)
            }
        }
    }

    private fun getSingleBarData(group: GroupModelView, barXValues: List<String>): BarData {
        val font = ResourcesCompat.getFont(context, R.font.grotesk_light_font_family)
        val vertical = group.name == GroupName.SUB_PERIOD
        val gEntries = group.entries
        val barEntries = mutableListOf<BarEntry>()

        for (index in barXValues.indices) {
            val xVal = barXValues[index]
            val entry = gEntries[index]
            val resId =
                if (needResIdAsAdditionalData(group)) {
                    stringResLabelMap.entries.first { e -> context.getString(e.key) == xVal }.key
                } else {
                    null
                }
            val periodRange = if (vertical) {
                val periodStartedAt = group.entries[index].xValue.first().toLong()
                group.period.toSubPeriodRangeSec(periodStartedAt)
            } else {
                null
            }

            val hiddenXVals = if (xVal == context.getString(R.string.others)) {
                group.entries[index].xValue.toList()
            } else {
                null
            }

            val data =
                ChartItem(
                    group.sectionName,
                    group.name,
                    xVal,
                    entry.sum(),
                    resId,
                    periodRange,
                    hiddenXVals
                )

            barEntries.add(
                BarEntry(
                    index.toFloat(),
                    entry.yValues,
                    data
                )
            )
        }
        val dataSet = BarDataSet(barEntries, "")
        val colors =
            context.resources.getIntArray(
                when (group.sectionName) {
                    SectionName.REACTION -> R.array.color_palette_2
                    SectionName.POST -> R.array.color_palette_4
                    else -> R.array.color_palette_3
                }
            )
        if (group.name != GroupName.TYPE) {
            // reverse all stacked chart
            colors.reverse()
        }
        dataSet.colors = colors.toList()
        val barData = BarData(dataSet)
        barData.barWidth = if (vertical) 0.4f else 0.15f
        barData.setValueTextSize(10.5f)
        barData.setValueTypeface(font)
        barData.setValueFormatter(StackedBarValueFormatter(!vertical))
        return barData
    }

    private fun getMultiBarData(
        group: GroupModelView,
        barXValues: List<String>,
        chartView: BarChart
    ): BarData {
        val gEntries = group.entries
        val dataSetCount = gEntries[0].yValues.size
        val valid = gEntries.map { e -> e.yValues.size }.none { size -> size != dataSetCount }
        if (!valid) error("Invalid data set")
        val dataSetEntries = mutableListOf<MutableList<BarEntry>>()
        for (i in 0 until dataSetCount) {
            dataSetEntries.add(mutableListOf())
        }

        for (i in barXValues.indices) {
            val xVal = barXValues[i]
            val entry = gEntries[i]
            val resId =
                if (needResIdAsAdditionalData(group)) {
                    stringResLabelMap.entries.first { e -> context.getString(e.key) == xVal }.key
                } else {
                    null
                }

            for (j in 0 until dataSetCount) {
                val data =
                    ChartItem(
                        group.sectionName,
                        group.name,
                        xVal,
                        entry.yValues[j],
                        resId,
                        null,
                        null
                    )
                dataSetEntries[j].add(BarEntry(i.toFloat(), entry.yValues[j], data))
            }

        }

        val colors =
            context.resources.getIntArray(R.array.color_palette_5)
        val dataSets = dataSetEntries.mapIndexed { i, e ->
            val dataSet = BarDataSet(e, "")
            dataSet.colors = listOf(colors[i])
            dataSet
        }

        val barData = BarData(dataSets.reversed())
        barData.barWidth = 0.14f
        barData.setDrawValues(false)
        barData.groupBars(0f, 0.62f, 0.05f)
        chartView.xAxis.axisMaximum = barData.getGroupWidth(0.62f, 0.05f) * barXValues.size
        return barData
    }

    private fun calculateHorizontalHeight(xCount: Int) = if (xCount == 0) {
        0
    } else {
        context.getDimensionPixelSize(R.dimen.dp_180) * xCount / MAX_HORIZONTAL_COUNT + context.getDimensionPixelSize(
            R.dimen.dp_28
        ) / xCount
    }

    private fun calculateVerticalWidth(xCount: Int): Int {
        val room =
            resources.displayMetrics.widthPixels - 4 * context.getDimensionPixelSize(R.dimen.dp_18)
        return if (xCount == 0) 0 else (0.85f * room * xCount / MAX_VERTICAL_COUNT).toInt()
    }


    private fun getBarXValues(group: GroupModelView): List<String> {
        val gEntries = group.entries

        return when {
            group.name == GroupName.SUB_PERIOD -> {
                gEntries.map { entry ->
                    val xVal = entry.xValue
                    val periodStartedAt = xVal.first().toLong() * 1000
                    when (group.period) {
                        Period.WEEK -> {
                            val dow = context.resources.getStringArray(R.array.day_of_week).toList()
                            val index = DateTimeUtil.getDoW(periodStartedAt) - 1
                            dow[index]
                        }
                        Period.YEAR -> {
                            val moy =
                                context.resources.getStringArray(R.array.month_of_year).toList()
                            val index = DateTimeUtil.getMoY(periodStartedAt)
                            moy[index]
                        }
                        Period.DECADE -> {
                            DateTimeUtil.getYear(periodStartedAt).toString().takeLast(2)
                        }
                        else -> error("unsupported period")
                    }
                }
            }
            needResIdAsAdditionalData(group) -> {
                gEntries.map { entry ->
                    context.getString(stringResLabelMap.entries.first { e ->
                        e.value.equals(
                            entry.xValue.first(),
                            true
                        )
                    }.key)
                }
            }

            group.hasAggregatedData() -> {
                gEntries.map { e ->
                    if (e.xValue.size > 1) {
                        context.getString(R.string.others)
                    } else {
                        e.xValue.first()
                    }
                }
            }

            else -> {
                gEntries.map { e -> e.xValue.first() }
            }
        }
    }

    private fun needResIdAsAdditionalData(group: GroupModelView) = (group.sectionName in arrayOf(
        SectionName.POST,
        SectionName.REACTION
    ) && group.name == GroupName.TYPE) || group.sectionName == SectionName.STATS

    private fun buildSingleBarChart(group: GroupModelView, barXValues: List<String>): BarChart {
        val font = ResourcesCompat.getFont(context, R.font.grotesk_light_font_family)
        val vertical = group.name == GroupName.SUB_PERIOD
        val chartView = if (vertical) BarChart(context) else HorizontalBarChart(context)
        chartView.description.isEnabled = false
        val axisLeft = chartView.axisLeft
        val axisRight = chartView.axisRight
        val xAxis = chartView.xAxis
        axisLeft.setDrawLabels(false)
        axisLeft.setDrawGridLines(false)
        axisLeft.setDrawAxisLine(false)
        axisLeft.axisMinimum = 0f
        axisRight.setDrawLabels(false)
        axisRight.setDrawGridLines(false)
        axisRight.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(vertical)
        xAxis.textSize = 12f
        xAxis.typeface = font
        xAxis.position =
            if (vertical) XAxis.XAxisPosition.BOTTOM else XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.valueFormatter = IndexAxisValueFormatter(barXValues)
        xAxis.labelCount = barXValues.size
        xAxis.isGranularityEnabled = true
        chartView.setScaleEnabled(false)
        chartView.isDoubleTapToZoomEnabled = false
        chartView.setPinchZoom(false)
        chartView.isDragEnabled = false
        chartView.legend.isEnabled = false
        chartView.setTouchEnabled(true)
        chartView.isHighlightPerTapEnabled = true
        chartView.isHighlightPerDragEnabled = false
        chartView.animateY(200)
        chartView.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {

            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (chartClickListener != null) {
                    var data = e?.data as? ChartItem ?: return
                    if (data.stringRes != null) {
                        data = ChartItem(
                            data.sectionName,
                            data.groupName,
                            stringResLabelMap[data.stringRes!!]
                                ?: error("could not found item is stringResLabelMap"),
                            data.yVal,
                            stringRes = data.stringRes,
                            periodRange = data.periodRange
                        )
                    }
                    chartClickListener?.onClick(data)
                    postDelayed({ chartView.highlightValues(null) }, 50)
                }
            }
        })
        if (vertical) chartView.setExtraOffsets(
            0f,
            0f,
            0f,
            context.getDimensionPixelSize(R.dimen.dp_12).toFloat()
        )
        return chartView
    }

    private fun buildMultiBarChart(barXValues: List<String>): BarChart {
        val font = ResourcesCompat.getFont(context, R.font.grotesk_light_font_family)
        val chartView = HorizontalBarChart(context)
        chartView.reinitXAxisRenderer(true)
        chartView.description.isEnabled = false
        val axisLeft = chartView.axisLeft
        val axisRight = chartView.axisRight
        val xAxis = chartView.xAxis
        axisLeft.setDrawLabels(false)
        axisLeft.setDrawGridLines(false)
        axisLeft.setDrawAxisLine(false)
        axisLeft.axisMinimum = 0f
        axisRight.setDrawLabels(true)
        axisRight.setDrawGridLines(true)
        axisRight.setDrawAxisLine(false)
        axisRight.typeface = font
        axisRight.textSize = 12f
        axisRight.axisMinimum = 0f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textSize = 12f
        xAxis.typeface = font
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.valueFormatter = IndexAxisValueFormatter(barXValues)
        xAxis.labelCount = barXValues.size
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        xAxis.axisMinimum = 0f
        chartView.setScaleEnabled(false)
        chartView.isDoubleTapToZoomEnabled = false
        chartView.setPinchZoom(false)
        chartView.isDragEnabled = false
        chartView.legend.isEnabled = false
        chartView.setTouchEnabled(true)
        chartView.isHighlightPerTapEnabled = true
        chartView.isHighlightPerDragEnabled = false
        chartView.animateY(200)
        chartView.setFitBars(true)
        chartView.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {

            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (chartClickListener != null) {
                    var data = e?.data as? ChartItem ?: return
                    if (data.stringRes != null) {
                        data = ChartItem(
                            data.sectionName,
                            data.groupName,
                            stringResLabelMap[data.stringRes!!]
                                ?: error("could not found item is stringResLabelMap"),
                            data.yVal,
                            stringRes = data.stringRes,
                            periodRange = data.periodRange
                        )
                    }
                    chartClickListener?.onClick(data)
                    postDelayed({ chartView.highlightValues(null) }, 50)
                }
            }
        })
        return chartView
    }

    interface ChartClickListener {

        fun onClick(chartItem: ChartItem)

    }

    @Parcelize
    data class ChartItem(
        val sectionName: SectionName,

        val groupName: GroupName,

        val xVal: String,

        val yVal: Float,

        val stringRes: Int? = null,

        val periodRange: @RawValue LongRange? = null,

        val aggregateVals: List<String>? = null
    ) : Parcelable
}

fun GroupView.ChartItem.getPostType() =
    if (sectionName != SectionName.POST) null else PostType.fromString(xVal)

fun GroupView.ChartItem.getReaction() =
    if (sectionName != SectionName.REACTION) null else Reaction.fromString(xVal)