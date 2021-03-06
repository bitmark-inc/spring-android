/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.main

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.*
import com.bitmark.fbm.feature.connectivity.ConnectivityHandler
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_BROWSE
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_SETTINGS
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_SUMMARY
import com.bitmark.fbm.feature.summary.SummaryContainerFragment
import com.bitmark.fbm.logging.EventLogger
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

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

    companion object {
        private const val ACCOUNT_SEED = "account_seed"

        fun getBundle(encodedSeed: String): Bundle {
            val bundle = Bundle()
            bundle.putString(ACCOUNT_SEED, encodedSeed)
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
        account = Account.fromSeed(seed)

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
        bottomNav.setIndicatorWidth(screenWidth / vpAdapter.count.toFloat())
        switchTab(TAB_SUMMARY)

        bottomNav.onItemSelected = { pos ->
            viewPager.setCurrentItem(pos, true)

            val color = when (pos) {
                TAB_SUMMARY -> R.color.cognac
                TAB_BROWSE -> R.color.international_klein_blue
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
        if (currentFragment is SummaryContainerFragment && !currentFragment.onBackPressed())
            super.onBackPressed()
        else if (currentFragment?.onBackPressed() == false) {
            bottomNav.setActiveItem(TAB_SUMMARY, R.color.cognac)
            viewPager.setCurrentItem(TAB_SUMMARY, false)
        }
    }

    fun switchTab(pos: Int) {
        val color = when (pos) {
            TAB_SUMMARY -> R.color.cognac
            TAB_BROWSE -> R.color.international_klein_blue
            TAB_SETTINGS -> R.color.olive
            else -> error("invalid tab pos")
        }
        viewPager.setCurrentItem(pos, false)
        bottomNav.setActiveItem(pos)
        bottomNav.setActiveColor(color)
    }
}