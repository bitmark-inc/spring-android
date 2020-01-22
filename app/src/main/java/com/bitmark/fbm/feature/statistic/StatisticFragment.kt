/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.statistic

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Range
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bitmark.fbm.R
import com.bitmark.fbm.data.ext.isServiceUnsupportedError
import com.bitmark.fbm.data.model.entity.*
import com.bitmark.fbm.feature.BaseSupportFragment
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.postdetail.PostDetailFragment
import com.bitmark.fbm.feature.reactiondetail.ReactionDetailFragment
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.logging.Tracer
import com.bitmark.fbm.util.Constants
import com.bitmark.fbm.util.DateTimeUtil
import com.bitmark.fbm.util.formatPeriod
import com.bitmark.fbm.util.formatSubPeriod
import com.bitmark.fbm.util.view.TopVerticalItemDecorator
import com.bitmark.fbm.util.view.statistic.GroupView
import kotlinx.android.synthetic.main.fragment_statistic.*
import javax.inject.Inject
import kotlin.math.abs


class StatisticFragment : BaseSupportFragment() {

    companion object {

        private const val TAG = "StatisticFragment"

        private const val PERIOD = "period"

        fun newInstance(period: Period): StatisticFragment {
            val fragment = StatisticFragment()
            val bundle = Bundle()
            bundle.putString(PERIOD, period.value)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    internal lateinit var viewModel: StatisticViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    private lateinit var period: Period

    private var currentStartedAtSec = -1L

    private var periodGap = -1

    private val handler = Handler()

    private lateinit var adapter: StatisticRecyclerViewAdapter

    override fun layoutRes(): Int = R.layout.fragment_statistic

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        period = Period.fromString(
            arguments?.getString(PERIOD) ?: throw IllegalArgumentException("missing period")
        )

        periodGap = 0
        currentStartedAtSec = getStartOfPeriodSec(period)
    }

    override fun onResume() {
        super.onResume()
        if (adapter.itemCount > 0) return
        handler.postDelayed({
            viewModel.listUsageStatistic(period, currentStartedAtSec)
        }, Constants.MASTER_DELAY_TIME)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getLastActivityTimestamp()
    }

    override fun initComponents() {
        super.initComponents()

        ivNextPeriod.isEnabled = currentStartedAtSec != getStartOfPeriodSec(period)
        showPeriod(period, currentStartedAtSec, periodGap)

        adapter = StatisticRecyclerViewAdapter()
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvStatistic.layoutManager = layoutManager
        val dividerDrawable =
            ContextCompat.getDrawable(context!!, R.drawable.double_divider_white_black_stroke)
        val itemDecoration = TopVerticalItemDecorator(dividerDrawable)
        rvStatistic.addItemDecoration(itemDecoration)
        (rvStatistic.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        rvStatistic.isNestedScrollingEnabled = false
        rvStatistic.adapter = adapter

        adapter.setChartClickListener(object : GroupView.ChartClickListener {
            override fun onClick(chartItem: GroupView.ChartItem) {
                handleChartClicked(chartItem)
            }
        })

        ivNextPeriod.setOnClickListener {
            nextPeriod()
        }

        ivPrevPeriod.setOnClickListener {
            prevPeriod()
        }

    }

    private fun nextPeriod() {
        periodGap++
        currentStartedAtSec =
            getStartOfPeriodSec(period, currentStartedAtSec, 1)
        showPeriod(period, currentStartedAtSec, periodGap)
        viewModel.listUsageStatistic(period, currentStartedAtSec)
    }

    private fun prevPeriod() {
        periodGap--
        currentStartedAtSec =
            getStartOfPeriodSec(period, currentStartedAtSec, -1)
        showPeriod(period, currentStartedAtSec, periodGap)
        viewModel.listUsageStatistic(period, currentStartedAtSec)
    }

    private fun showPeriod(period: Period, periodStartedAtSec: Long, periodGap: Int) {
        ivNextPeriod.isEnabled = periodStartedAtSec != getStartOfPeriodSec(period)
        ivPrevPeriod.isEnabled = getStartOfPeriodSec(period, periodStartedAtSec, -1) >= 0L
        val periodStartedAtMillis = periodStartedAtSec * 1000
        tvTime.text = DateTimeUtil.formatPeriod(period, periodStartedAtMillis)
        val defaultPeriodGap = 0
        if (defaultPeriodGap == periodGap) {
            tvType.text = getString(
                when (period) {
                    Period.WEEK -> R.string.this_week
                    Period.YEAR -> R.string.this_year
                    Period.DECADE -> R.string.this_decade
                }
            )
        } else {
            val plural = abs(periodGap) > 1
            tvType.text = getString(
                when (period) {
                    Period.WEEK -> if (plural) R.string.last_week_format_plural else R.string.last_week
                    Period.YEAR -> if (plural) R.string.last_year_format_plural else R.string.last_year
                    Period.DECADE -> if (plural) R.string.last_decade_format_plural else R.string.last_decade
                }
            ).format(abs(periodGap))
        }

    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.listUsageStatisticLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val data = res.data() ?: return@Observer
                    if (data.any { s -> s.periodStartedAtSec != currentStartedAtSec }) return@Observer
                    adapter.set(data)
                }

                res.isError() -> {
                    Tracer.ERROR.log(TAG, res.throwable()?.message ?: "unknown")
                    logger.logError(
                        Event.LOAD_STATISTIC_ERROR,
                        res.throwable()?.message ?: "unknown"
                    )
                    if (!res.throwable()!!.isServiceUnsupportedError()) {
                        adapter.clear()
                        dialogController.alert(
                            R.string.error,
                            R.string.there_was_error_when_loading_statistic
                        )
                    }
                }

                res.isLoading() -> {
                }
            }
        })

        viewModel.getLastActivityTimestampLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val lastActivityMillis = res.data()!! * 1000
                    if (lastActivityMillis == 0L) return@Observer
                    val startOfPeriodMillis = getStartOfPeriodSec(period) * 1000
                    periodGap = calculateGap(period, startOfPeriodMillis, lastActivityMillis)
                    currentStartedAtSec =
                        getStartOfPeriodSec(period, currentStartedAtSec, periodGap)
                    showPeriod(period, currentStartedAtSec, periodGap)
                }
            }
        })
    }

    private fun calculateGap(
        period: Period,
        startOfPeriodMillis: Long,
        lastActivityMillis: Long
    ): Int {
        val periodDateRange = when (period) {
            Period.WEEK -> DateTimeUtil.getDateRangeOfWeek(startOfPeriodMillis)
            Period.YEAR -> DateTimeUtil.getDateRangeOfYear(startOfPeriodMillis)
            Period.DECADE -> DateTimeUtil.getDateRangeOfDecade(startOfPeriodMillis)
        }
        val periodMillisRange = Range(periodDateRange.first.time, periodDateRange.second.time)
        return if (lastActivityMillis in periodMillisRange) {
            0
        } else {
            val gapMillis = abs(lastActivityMillis - startOfPeriodMillis)
            val gap = when (period) {
                Period.WEEK -> (gapMillis / DateTimeUtil.WEEK_MILLIS + 1).toInt()
                Period.YEAR -> (gapMillis / DateTimeUtil.YEAR_MILLIS + 1).toInt()
                Period.DECADE -> (gapMillis / DateTimeUtil.DECADE_MILLIS + 1).toInt()
            }
            if (lastActivityMillis < periodMillisRange.lower) -gap else gap
        }
    }

    private fun handleChartClicked(chartItem: GroupView.ChartItem) {
        if (chartItem.yVal == 0f) return
        val periodStartedAtMillis = currentStartedAtSec * 1000
        val endedAtSec = when (period) {
            Period.WEEK -> DateTimeUtil.getEndOfWeekMillis(periodStartedAtMillis)
            Period.YEAR -> DateTimeUtil.getEndOfYearMillis(periodStartedAtMillis)
            Period.DECADE -> DateTimeUtil.getEndOfDecadeMillis(periodStartedAtMillis)
        } / 1000
        val startedAtSec = currentStartedAtSec

        val title = getString(
            when (chartItem.sectionName) {
                SectionName.POST -> chartItem.stringRes ?: R.string.posts
                SectionName.REACTION -> chartItem.stringRes ?: R.string.reactions
                else -> R.string.empty
            }
        )

        val titleSuffix = when (chartItem.sectionName) {
            SectionName.POST -> {
                when (chartItem.groupName) {
                    GroupName.FRIEND -> getString(R.string.tagged_with_lower_format).format(
                        chartItem.xVal
                    )
                    GroupName.PLACE -> getString(R.string.tagged_at_lower_format).format(chartItem.xVal)
                    else -> ""
                }
            }

            SectionName.REACTION -> {
                when (chartItem.groupName) {
                    GroupName.FRIEND -> getString(R.string.reacted_to_lower_format).format(chartItem.xVal)
                    else -> ""
                }
            }

            else -> ""
        }

        val periodDetail = if (chartItem.groupName == GroupName.SUB_PERIOD) {
            DateTimeUtil.formatSubPeriod(
                period,
                chartItem.periodRange!!.first * 1000,
                DateTimeUtil.defaultTimeZone()
            )
        } else {
            DateTimeUtil.formatPeriod(
                period,
                periodStartedAtMillis,
                DateTimeUtil.defaultTimeZone()
            )
        }

        when (chartItem.sectionName) {
            SectionName.POST -> {
                navigator.anim(RIGHT_LEFT).replaceChildFragment(
                    R.id.layoutContainer,
                    PostDetailFragment.newInstance(
                        title,
                        periodDetail,
                        period,
                        titleSuffix,
                        startedAtSec,
                        endedAtSec,
                        chartItem
                    )
                )
            }

            SectionName.REACTION -> {
                navigator.anim(RIGHT_LEFT).replaceChildFragment(
                    R.id.layoutContainer,
                    ReactionDetailFragment.newInstance(
                        title,
                        periodDetail,
                        period,
                        titleSuffix,
                        startedAtSec,
                        endedAtSec,
                        chartItem
                    )
                )
            }

            else -> {
                // do nothing
            }

        }
    }

    private fun getStartOfPeriodSec(period: Period) = when (period) {
        Period.WEEK -> DateTimeUtil.getStartOfThisWeekMillis()
        Period.YEAR -> DateTimeUtil.getStartOfThisYearMillis()
        Period.DECADE -> DateTimeUtil.getStartOfThisDecadeMillis()
    } / 1000

    private fun getStartOfPeriodSec(period: Period, sec: Long, gap: Int): Long {
        val millis = sec * 1000
        return when (period) {
            Period.WEEK -> DateTimeUtil.getStartOfWeekMillis(millis, gap)
            Period.YEAR -> DateTimeUtil.getStartOfYearMillis(millis, gap)
            Period.DECADE -> DateTimeUtil.getStartOfDecadeMillis(millis, gap)
        } / 1000
    }

}