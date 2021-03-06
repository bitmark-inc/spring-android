/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.register.trustnotice

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.BaseAppCompatActivity
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.register.onboarding.OnboardingActivity
import com.bitmark.fbm.util.Constants.FAQ_URL
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import com.bitmark.fbm.util.view.WebViewActivity
import kotlinx.android.synthetic.main.activity_trust_notice.*
import javax.inject.Inject


class TrustNoticeActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_trust_notice

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()
        val msg = SpannableString(getString(R.string.spring_is_from_bitmark))
        val linkText = getString(R.string.faq)
        val startIndex = msg.indexOf(linkText)
        val endIndex = startIndex + linkText.length

        msg.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val bundle =
                        WebViewActivity.getBundle(FAQ_URL, getString(R.string.faq))
                    navigator.anim(Navigator.RIGHT_LEFT)
                        .startActivity(WebViewActivity::class.java, bundle)
                }

            }, startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        tvMsg.text = msg
        tvMsg.movementMethod = LinkMovementMethod.getInstance()
        tvMsg.setLinkTextColor(getColor(R.color.black))
        tvMsg.highlightColor = Color.TRANSPARENT

        ivBack.setOnClickListener {
            navigator.anim(Navigator.RIGHT_LEFT)
                .finishActivity()
        }

        btnContinue.setSafetyOnclickListener {
            navigator.anim(Navigator.RIGHT_LEFT)
                .startActivity(OnboardingActivity::class.java)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigator.anim(Navigator.RIGHT_LEFT).finishActivity()
    }
}