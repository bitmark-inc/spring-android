/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.main

import android.app.AlarmManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.*
import com.bitmark.fbm.feature.connectivity.ConnectivityHandler
import com.bitmark.fbm.feature.insights.InsightsContainerFragment
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_INSIGHT
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_SETTINGS
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_USAGE
import com.bitmark.fbm.feature.notification.buildSimpleNotificationBundle
import com.bitmark.fbm.feature.notification.cancelNotification
import com.bitmark.fbm.feature.notification.pushDailyRepeatingNotification
import com.bitmark.fbm.feature.splash.SplashActivity
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.util.Constants
import com.bitmark.fbm.util.ext.*
import com.bitmark.sdk.features.Account
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_bottom_notification.*
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: MainViewModel

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    private val handler = Handler()

    private lateinit var account: Account

    private lateinit var vpAdapter: MainViewPagerAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var firstTimeLaunch = false

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

    companion object {
        private const val ACCOUNT_SEED = "account_seed"

        private const val FIRST_TIME_LAUNCH = "first_time_launch"

        fun getBundle(encodedSeed: String, firstTimeLaunch: Boolean = false): Bundle {
            val bundle = Bundle()
            bundle.putString(ACCOUNT_SEED, encodedSeed)
            bundle.putBoolean(FIRST_TIME_LAUNCH, firstTimeLaunch)
            return bundle
        }
    }

    private val connectivityChangeListener =
        object : ConnectivityHandler.NetworkStateChangeListener {
            override fun onChange(connected: Boolean) {
                if (!connected) {
                    if (layoutNoNetwork.isVisible) return
                    layoutNoNetwork.visible(true)
                    handler.postDelayed({ layoutNoNetwork.gone(true) }, 2000)
                } else {
                    layoutNoNetwork.gone(true)
                    handler.removeCallbacksAndMessages(null)
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val seed = intent?.extras?.getString(ACCOUNT_SEED) ?: error("missing ACCOUNT_SEED")
        firstTimeLaunch =
            intent?.extras?.getBoolean(FIRST_TIME_LAUNCH) ?: false
        account = Account.fromSeed(seed)

        viewModel.checkWaitingForArchive()
        viewModel.checkNotificationPermissionRequested()
        viewModel.startArchiveIssuanceProcessor(account)

    }

    override fun onDestroy() {
        viewModel.stopArchiveIssuanceProcessor()
        super.onDestroy()
    }

    override fun initComponents() {
        super.initComponents()

        vpAdapter = MainViewPagerAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = vpAdapter.count
        viewPager.adapter = vpAdapter
        viewPager.setCurrentItem(TAB_INSIGHT, false)

        bottomNav.setActiveItem(TAB_INSIGHT)
        bottomNav.setIndicatorWidth(screenWidth / vpAdapter.count.toFloat())

        bottomNav.onItemSelected = { pos ->
            viewPager.setCurrentItem(pos, true)

            val color = when (pos) {
                TAB_USAGE -> R.color.cognac
                TAB_INSIGHT -> R.color.international_klein_blue
                TAB_SETTINGS -> R.color.olive
                else -> error("invalid tab pos")
            }

            bottomNav.setActiveColor(color)
        }

        bottomNav.onItemReselected = {
            (vpAdapter.currentFragment as? BehaviorComponent)?.refresh()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomNotification)

        ivClose.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.serviceUnsupportedLiveData.observe(this, Observer { url ->
            dialogController.showUpdateRequired {
                if (url.isEmpty()) {
                    navigator.goToPlayStore()
                } else {
                    navigator.goToUpdateApp(url)
                }
                navigator.exitApp()
            }
        })

        viewModel.checkWaitingForArchiveLiveData.asLiveData()
            .observe(this, Observer { res ->
                when {
                    res.isSuccess() -> {
                        val waiting = res.data()!!
                        if (!firstTimeLaunch && waiting) {
                            showNotification()
                        }
                    }
                }
            })

        viewModel.setNotificationEnabledLiveData.asLiveData().observe(this, Observer { res ->
            when {

                res.isSuccess() -> {
                    scheduleNotification()
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "set notification enable error")
                }
            }
        })

        viewModel.checkNotificationPermissionRequestedLiveData.asLiveData()
            .observe(this, Observer { res ->
                when {
                    res.isSuccess() -> {
                        val requested = res.data()!!
                        if (!requested) requestNotificationPermission()
                    }

                    res.isError() -> {
                        logger.logSharedPrefError(
                            res.throwable(),
                            "check notification permission requested error"
                        )
                        requestNotificationPermission()
                    }
                }
            })
    }

    private fun showNotification() {
        val title =
            getString(R.string.still_waiting)
        val message =
            getString(R.string.sorry_fb_is_still_prepare)
        tvNotifTitle.text = title
        tvNotifMsg.text = message

        handler.postDelayed(
            { bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED },
            Constants.UI_READY_DELAY
        )

    }

    private fun scheduleNotification() {
        cancelNotification(this, Constants.REMINDER_NOTIFICATION_ID)
        val bundle = buildSimpleNotificationBundle(
            this,
            R.string.spring,
            R.string.just_remind_you,
            Constants.REMINDER_NOTIFICATION_ID,
            SplashActivity::class.java
        )
        pushDailyRepeatingNotification(
            this,
            bundle,
            System.currentTimeMillis() + 3 * AlarmManager.INTERVAL_DAY,
            Constants.REMINDER_NOTIFICATION_ID
        )
    }

    private fun requestNotificationPermission() {
        dialogController.confirm(
            R.string.enable_push_notification,
            R.string.allow_spring_send_you,
            false,
            "notification_request",
            R.string.enable,
            { viewModel.setNotificationEnabled(true) },
            R.string.cancel,
            { viewModel.setNotificationEnabled(false) }
        )
    }

    override fun onResume() {
        super.onResume()
        connectivityHandler.addNetworkStateChangeListener(
            connectivityChangeListener
        )
    }

    override fun onPause() {
        super.onPause()
        connectivityHandler.removeNetworkStateChangeListener(
            connectivityChangeListener
        )
    }

    override fun onBackPressed() {
        val currentFragment = vpAdapter.currentFragment as? BehaviorComponent
        if (currentFragment is InsightsContainerFragment && !currentFragment.onBackPressed())
            super.onBackPressed()
        else if (currentFragment?.onBackPressed() == false) {
            bottomNav.setActiveItem(TAB_INSIGHT, R.color.international_klein_blue)
            viewPager.setCurrentItem(TAB_INSIGHT, false)
        }
    }
}