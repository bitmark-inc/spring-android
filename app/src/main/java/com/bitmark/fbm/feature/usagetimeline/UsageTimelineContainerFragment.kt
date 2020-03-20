/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.usagetimeline

import android.content.Context
import android.os.Bundle
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.BaseSupportFragment
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.BehaviorComponent
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_usage_timeline_container.*
import javax.inject.Inject


class UsageTimelineContainerFragment : BaseSupportFragment() {

    companion object {

        private const val TYPE = "type"

        fun newInstance(type: Int): UsageTimelineContainerFragment {
            val fragment = UsageTimelineContainerFragment()
            val bundle = Bundle()
            bundle.putInt(TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    private lateinit var adapter: UsageTimelineViewPagerAdapter

    private var type = -1

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {

        override fun onTabReselected(p0: TabLayout.Tab?) {
            (adapter.currentFragment as? BaseSupportFragment)?.refresh()
        }

        override fun onTabUnselected(p0: TabLayout.Tab?) {
        }

        override fun onTabSelected(p0: TabLayout.Tab?) {
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        type = arguments?.getInt(TYPE) ?: error("missing type")
        adapter = UsageTimelineViewPagerAdapter(context, type, childFragmentManager)
    }

    override fun layoutRes(): Int = R.layout.fragment_usage_timeline_container

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val title = getString(
            when (type) {
                UsageType.POST -> R.string.posts
                UsageType.REACTION -> R.string.reactions
                UsageType.MEDIA -> R.string.photos_videos
                else -> error("unsupported type: $type")
            }
        )
        tvTitle.text = title

        vp.adapter = adapter
        vp.offscreenPageLimit = adapter.count
        tabLayout.setupWithViewPager(vp)
        tabLayout.addOnTabSelectedListener(tabSelectedListener)

        ivBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).popChildFragment()
        }
    }

    override fun deinitComponents() {
        tabLayout.removeOnTabSelectedListener(tabSelectedListener)
        super.deinitComponents()
    }

    override fun refresh() {
        super.refresh()
        if (vp.currentItem != UsageTimelineViewPagerAdapter.MONTH) {
            vp.currentItem = UsageTimelineViewPagerAdapter.MONTH
        } else {
            (adapter.currentFragment as? BehaviorComponent)?.refresh()
        }
    }

    override fun onBackPressed(): Boolean {
        super.onBackPressed()
        return if (vp.currentItem != UsageTimelineViewPagerAdapter.MONTH) {
            vp.currentItem = UsageTimelineViewPagerAdapter.MONTH
            true
        } else {
            navigator.popChildFragment()
        }
    }
}