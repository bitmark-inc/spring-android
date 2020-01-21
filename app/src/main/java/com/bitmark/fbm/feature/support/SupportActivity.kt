/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.support

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.BaseAppCompatActivity
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.NONE
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.util.ext.openBrowser
import kotlinx.android.synthetic.main.activity_support.*
import javax.inject.Inject


class SupportActivity : BaseAppCompatActivity() {

    companion object {
        private const val TITLE = "title"

        private const val MESSAGE = "msg"

        private const val LINK = "link"

        private const val LINK_TEXT = "link_text"

        fun getBundle(
            title: String,
            message: String,
            linkText: String? = null,
            link: String? = null
        ): Bundle {
            val bundle = Bundle()
            bundle.putString(TITLE, title)
            bundle.putString(MESSAGE, message)
            if (link != null) bundle.putString(LINK, link)
            if (linkText != null) bundle.putString(LINK_TEXT, linkText)
            return bundle
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_support

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val bundle = intent?.extras ?: throw IllegalArgumentException("missing intent extras")
        val title = bundle.getString(TITLE) ?: error("missing title")
        val message = bundle.getString(MESSAGE) ?: error("missing message")
        val link = bundle.getString(LINK)
        val linkText = bundle.getString(LINK_TEXT)
        tvTitle.text = title
        val spannableMsg = SpannableString(message)


        if (linkText != null && link != null && message.contains(linkText)) {
            val startIndex = spannableMsg.indexOf(linkText)
            val endIndex = startIndex + linkText.length
            spannableMsg.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        navigator.anim(NONE).openBrowser(link)
                    }

                }, startIndex,
                endIndex,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableMsg.setSpan(
                UnderlineSpan(),
                startIndex,
                endIndex,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        tvMsg.movementMethod = LinkMovementMethod.getInstance()
        tvMsg.setLinkTextColor(getColor(R.color.international_klein_blue))
        tvMsg.highlightColor = Color.TRANSPARENT
        tvMsg.text = spannableMsg

        ivBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigator.anim(RIGHT_LEFT).finishActivity()
    }
}