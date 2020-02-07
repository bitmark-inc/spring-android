/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.insights

import android.app.AlarmManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bitmark.fbm.R
import com.bitmark.fbm.data.ext.isServiceUnsupportedError
import com.bitmark.fbm.feature.BaseSupportFragment
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.account.AccountActivity
import com.bitmark.fbm.feature.notification.buildSimpleNotificationBundle
import com.bitmark.fbm.feature.notification.cancelNotification
import com.bitmark.fbm.feature.notification.pushDailyRepeatingNotification
import com.bitmark.fbm.feature.splash.SplashActivity
import com.bitmark.fbm.feature.support.SupportActivity
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.logging.Tracer
import com.bitmark.fbm.util.Constants
import com.bitmark.fbm.util.ext.logSharedPrefError
import com.bitmark.fbm.util.ext.scrollToTop
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import com.bitmark.fbm.util.view.TopVerticalItemDecorator
import kotlinx.android.synthetic.main.fragment_insights.*
import javax.inject.Inject


class InsightsFragment : BaseSupportFragment() {

    companion object {

        private const val TAG = "InsightsFragment"

        private const val INCOME_QUATERLY_EARNING_LINK =
            "https://investor.fb.com/financials/?section=quarterlyearnings"

        fun newInstance() = InsightsFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: InsightsViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var adapter: InsightsRecyclerViewAdapter

    private val handler = Handler()

    override fun layoutRes(): Int = R.layout.fragment_insights

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler.postDelayed({ viewModel.listInsight() }, Constants.UI_READY_DELAY)
    }

    override fun initComponents() {
        super.initComponents()

        adapter = InsightsRecyclerViewAdapter()
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvInsights.layoutManager = layoutManager
        val dividerDrawable = context!!.getDrawable(R.drawable.double_divider_white_black_stroke)
        val itemDecoration = TopVerticalItemDecorator(dividerDrawable)
        rvInsights.addItemDecoration(itemDecoration)
        (rvInsights.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        rvInsights.isNestedScrollingEnabled = false
        rvInsights.adapter = adapter

        adapter.setItemClickListener(object : InsightsRecyclerViewAdapter.ItemClickListener {

            override fun onNotifyMeClicked() {
                viewModel.setNotificationEnable()
            }

            override fun onReadMoreClicked() {
                val bundle = SupportActivity.getBundle(
                    getString(R.string.how_u_r_tracked),
                    getString(R.string.how_u_r_tracked_content)
                )
                navigator.anim(RIGHT_LEFT).startActivity(SupportActivity::class.java, bundle)
            }

            override fun onIncomeInfoClicked() {
                val bundle = SupportActivity.getBundle(
                    getString(R.string.how_much_your_worth_to_fb),
                    getString(R.string.avg_revenue_per_user),
                    getString(R.string.quarterly_earning_reports),
                    INCOME_QUATERLY_EARNING_LINK
                )
                navigator.anim(RIGHT_LEFT).startActivity(SupportActivity::class.java, bundle)
            }

        })

        ivAccount.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(AccountActivity::class.java)
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.listInsightLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val insights = res.data() ?: return@Observer
                    adapter.set(insights)
                }

                res.isError() -> {
                    Tracer.ERROR.log(TAG, res.throwable()?.message ?: "unknown")
                    logger.logError(
                        Event.INSIGHTS_LOADING_ERROR,
                        res.throwable()?.message ?: "unknown"
                    )
                    if (!res.throwable()!!.isServiceUnsupportedError()) {
                        adapter.clear()
                        dialogController.alert(
                            R.string.error,
                            R.string.there_was_error_when_loading_insights
                        )
                    }
                }
            }
        })

        viewModel.setNotificationEnableLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    adapter.markNotificationEnable()
                    scheduleNotification()
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "set notification enable error")
                }
            }
        })
    }

    private fun scheduleNotification() {
        if (context == null) return
        cancelNotification(context!!, Constants.REMINDER_NOTIFICATION_ID)
        val bundle = buildSimpleNotificationBundle(
            context!!,
            R.string.spring,
            R.string.just_remind_you,
            Constants.REMINDER_NOTIFICATION_ID,
            SplashActivity::class.java
        )
        pushDailyRepeatingNotification(
            context!!,
            bundle,
            System.currentTimeMillis() + 3 * AlarmManager.INTERVAL_DAY
        )
    }

    override fun refresh() {
        super.refresh()
        sv.scrollToTop()
    }

}