/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.browse

import android.content.Intent
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.BaseSupportFragment
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.BehaviorComponent
import com.bitmark.fbm.feature.Navigator
import javax.inject.Inject


class BrowseContainerFragment : BaseSupportFragment() {

    companion object {
        fun newInstance() = BrowseContainerFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.fragment_browse_container

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        navigator.replaceChildFragment(
            R.id.layoutContainer,
            BrowseFragment.newInstance(), false
        )
    }

    override fun onBackPressed(): Boolean {
        super.onBackPressed()
        val currentFragment = currentFragment() as? BehaviorComponent
            ?: return false
        return currentFragment.onBackPressed()
    }

    override fun refresh() {
        super.refresh()
        val currentFragment = currentFragment()
        if (currentFragment !is BrowseFragment) {
            navigator.popChildFragmentToRoot()
        } else {
            (currentFragment as? BehaviorComponent)?.refresh()
        }
    }

    private fun currentFragment() =
        childFragmentManager.findFragmentById(R.id.layoutContainer)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        currentFragment()?.onActivityResult(requestCode, resultCode, data)
    }
}