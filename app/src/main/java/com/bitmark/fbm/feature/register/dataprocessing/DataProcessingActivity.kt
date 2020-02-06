/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.register.dataprocessing

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.BaseAppCompatActivity
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.register.archiverequest.ArchiveRequestContainerActivity
import com.bitmark.fbm.util.DateTimeUtil
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import kotlinx.android.synthetic.main.activity_data_processing.*
import kotlinx.android.synthetic.main.fragment_archive_request_credential.tvTitle
import javax.inject.Inject

class DataProcessingActivity : BaseAppCompatActivity() {

    companion object {

        private const val ACCOUNT_SEED = "account_seed"

        private const val ARCHIVE_REQUESTED_AT = "archive_requested_at"

        fun getBundle(
            archiveRequestedAt: Long,
            accountSeed: String? = null
        ): Bundle {
            val bundle = Bundle()
            bundle.putLong(ARCHIVE_REQUESTED_AT, archiveRequestedAt)
            if (accountSeed != null) bundle.putString(ACCOUNT_SEED, accountSeed)
            return bundle
        }

    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_data_processing

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val archiveRequestedAt =
            intent?.extras?.getLong(ARCHIVE_REQUESTED_AT) ?: error("missing ARCHIVE_REQUESTED_AT")
        val accountSeed = intent?.extras?.getString(ACCOUNT_SEED)

        val title = getString(R.string.data_requested)
        val msg =
            "${getString(R.string.we_are_waiting_for_fb)}\n\n${getString(R.string.you_requested_your_fb_archive_format).format(
                DateTimeUtil.millisToString(
                    archiveRequestedAt,
                    DateTimeUtil.DATE_FORMAT_3,
                    DateTimeUtil.defaultTimeZone()
                ),
                DateTimeUtil.millisToString(
                    archiveRequestedAt,
                    DateTimeUtil.TIME_FORMAT_1,
                    DateTimeUtil.defaultTimeZone()
                )
            )}"

        val requestedAtMsg = getString(R.string.you_requested_your_fb_archive)
        val requestedAtIndex = msg!!.indexOf(requestedAtMsg)
        val spannableString = SpannableString(msg)
        if (requestedAtIndex != -1) {
            spannableString.setSpan(
                StyleSpan(Typeface.ITALIC),
                requestedAtIndex,
                msg.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        tvTitle.text = title
        tvMsg.text = spannableString

        btnCheckNow.setSafetyOnclickListener {
            val bundle = ArchiveRequestContainerActivity.getBundle(false, accountSeed)
            navigator.anim(RIGHT_LEFT)
                .startActivityAsRoot(ArchiveRequestContainerActivity::class.java, bundle)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigator.anim(RIGHT_LEFT).finishActivity()
    }
}