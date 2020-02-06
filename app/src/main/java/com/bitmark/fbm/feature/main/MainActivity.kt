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
import com.bitmark.fbm.feature.insights.InsightsContainerFragment
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_INSIGHT
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_LENS
import com.bitmark.fbm.feature.main.MainViewPagerAdapter.Companion.TAB_USAGE
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

        viewModel.checkAppState()

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
                TAB_LENS -> R.color.olive
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

        viewModel.checkAppStateLiveData.asLiveData()
            .observe(this, Observer { res ->
                when {
                    res.isSuccess() -> {
                        val data = res.data()!!
                        val accountRegistered = data.first
                        val dataReady = data.second
                        if (accountRegistered) {
                            viewModel.startArchiveIssuanceProcessor(account)
                        }
                        showAppState(accountRegistered, dataReady)
                    }
                }
            })
    }

    private fun showAppState(accountRegistered: Boolean, dataReady: Boolean) {
        if (dataReady) return

        val title =
            getString(if (accountRegistered) R.string.processing_data else R.string.still_waiting)
        val message =
            getString(if (accountRegistered) R.string.your_fb_data_archive_has_been_successfully else R.string.sorry_fb_is_still_prepare)
        tvTitle.text = title
        tvMsg.text = message
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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