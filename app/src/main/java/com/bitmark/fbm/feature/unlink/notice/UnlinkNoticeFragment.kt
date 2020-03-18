/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.unlink.notice

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.BaseSupportFragment
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.unlink.unlink.UnlinkFragment
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import kotlinx.android.synthetic.main.fragment_unlink_notice.*
import javax.inject.Inject


class UnlinkNoticeFragment : BaseSupportFragment() {

    companion object {
        fun newInstance() = UnlinkNoticeFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.fragment_unlink_notice

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val msg = SpannableString(getString(R.string.if_you_unlink_your_account))
        val linkText = getString(R.string.pls_do_so_now)
        val startIndex = msg.indexOf(linkText)
        val endIndex = startIndex + linkText.length

        msg.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigator.anim(RIGHT_LEFT).finishActivityForResult()
                }

            }, startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        tvMsg.text = msg
        tvMsg.movementMethod = LinkMovementMethod.getInstance()
        tvMsg.setLinkTextColor(context!!.getColor(R.color.black))
        tvMsg.highlightColor = Color.TRANSPARENT

        btnContinue.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT)
                .replaceFragment(R.id.layoutRoot, UnlinkFragment.newInstance(), true)
        }

        ivBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    override fun onBackPressed(): Boolean {
        navigator.anim(RIGHT_LEFT).finishActivity()
        return true
    }
}