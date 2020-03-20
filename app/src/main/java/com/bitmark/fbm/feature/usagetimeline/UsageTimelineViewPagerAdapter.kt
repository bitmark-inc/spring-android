/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.usagetimeline

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.entity.Period
import com.bitmark.fbm.util.view.ViewPagerAdapter


class UsageTimelineViewPagerAdapter(
    private val context: Context,
    type: Int,
    fm: FragmentManager
) :
    ViewPagerAdapter(fm) {

    companion object {
        const val MONTH = 0x00
        const val YEAR = 0x01
        const val DECADE = 0x02
    }

    init {
        add(
            UsageTimelineFragment.newInstance(Period.MONTH, type),
            UsageTimelineFragment.newInstance(Period.YEAR, type),
            UsageTimelineFragment.newInstance(Period.DECADE, type)
        )
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(
            when (position) {
                MONTH -> R.string.month
                YEAR -> R.string.year
                DECADE -> R.string.decade
                else -> throw IllegalArgumentException("invalid tab pos")
            }
        )
    }

}