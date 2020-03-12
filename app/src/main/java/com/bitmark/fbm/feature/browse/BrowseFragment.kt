/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.browse

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
import com.bitmark.fbm.util.view.TopVerticalItemDecorator
import kotlinx.android.synthetic.main.fragment_browse.*
import javax.inject.Inject


class BrowseFragment : BaseSupportFragment() {

    companion object {

        private const val TAG = "BrowseFragment"

        private const val INCOME_QUATERLY_EARNING_URL =
            "https://investor.fb.com/financials/?section=quarterlyearnings"

        private const val HOW_U_R_TRACKED_URL =
            "https://raw.githubusercontent.com/bitmark-inc/spring/master/how-are-you-tracked.md"

        fun newInstance() = BrowseFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: BrowseViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var adapter: BrowseRecyclerViewAdapter

    private val handler = Handler()

    override fun layoutRes(): Int = R.layout.fragment_browse

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler.postDelayed({ viewModel.listInsight() }, Constants.UI_READY_DELAY)
    }

    override fun initComponents() {
        super.initComponents()

        adapter = BrowseRecyclerViewAdapter()
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvInsights.layoutManager = layoutManager
        val dividerDrawable = context!!.getDrawable(R.drawable.double_divider_white_black_stroke)
        val itemDecoration = TopVerticalItemDecorator(dividerDrawable)
        rvInsights.addItemDecoration(itemDecoration)
        (rvInsights.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        rvInsights.isNestedScrollingEnabled = false
        rvInsights.adapter = adapter

        adapter.setItemClickListener(object : BrowseRecyclerViewAdapter.ItemClickListener {

            override fun onNotifyMeClicked() {
                viewModel.setNotificationEnable()
            }

            override fun onReadMoreClicked() {
                val bundle = SupportActivity.getBundle(
                    getString(R.string.how_r_u_tracked),
                    getString(R.string.fb_constantly_tracks_you),
                    getString(R.string.increase_your_privacy),
                    titleColor = R.color.international_klein_blue,
                    fromHtml = true
                )
                navigator.anim(RIGHT_LEFT).startActivity(SupportActivity::class.java, bundle)
            }

            override fun onIncomeInfoClicked() {
                val bundle = SupportActivity.getBundle(
                    getString(R.string.how_much_your_worth_to_fb),
                    getString(R.string.avg_revenue_per_user),
                    getString(R.string.quarterly_earning_reports),
                    INCOME_QUATERLY_EARNING_URL,
                    R.color.international_klein_blue
                )
                navigator.anim(RIGHT_LEFT).startActivity(SupportActivity::class.java, bundle)
            }

        })
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
                    adapter.setNotificationEnable(true)
                    scheduleNotification()
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "set notification enable error")
                }
            }
        })

        viewModel.notificationStateChangedLiveData.observe(this, Observer { enable ->
            adapter.setNotificationEnable(enable)
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
            System.currentTimeMillis() + 3 * AlarmManager.INTERVAL_DAY,
            Constants.REMINDER_NOTIFICATION_ID
        )
    }

    override fun refresh() {
        super.refresh()
        sv.scrollToTop()
    }

}