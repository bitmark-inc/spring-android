/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.account

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import com.bitmark.fbm.BuildConfig
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.BaseAppCompatActivity
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.NONE
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.biometricauth.BiometricAuthActivity
import com.bitmark.fbm.feature.increaseprivacy.IncreasePrivacyActivity
import com.bitmark.fbm.feature.recovery.RecoveryContainerActivity
import com.bitmark.fbm.feature.unlink.UnlinkContainerActivity
import com.bitmark.fbm.feature.whatsnew.WhatsNewActivity
import com.bitmark.fbm.util.ext.openBrowser
import com.bitmark.fbm.util.ext.openIntercom
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import com.bitmark.fbm.util.view.WebViewActivity
import kotlinx.android.synthetic.main.activity_account.*
import javax.inject.Inject


class AccountActivity : BaseAppCompatActivity() {

    companion object {

        private const val SURVEY_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLScL41kNU6SBzo7ndcraUf7O-YJ_JrPqg_rlI588UjLK-_sGtQ/viewform?usp=sf_link"

        private const val GOTO_FAQ = "goto_faq"

        fun getBundle(goToFaq: Boolean = false): Bundle {
            val bundle = Bundle()
            bundle.putBoolean(GOTO_FAQ, goToFaq)
            return bundle
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_account

    override fun viewModel(): BaseViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val goToFaq = intent?.extras?.getBoolean(GOTO_FAQ) ?: false
        if (goToFaq) {
            goToFaq()
        }
    }

    override fun initComponents() {
        super.initComponents()

        tvVersion.text = getString(R.string.version_format).format(BuildConfig.VERSION_NAME)

        val tosAndPpString = getString(R.string.tos_and_pp)
        val spannableString = SpannableString(tosAndPpString)
        val tosString = getString(R.string.term_of_service)
        val ppString = getString(R.string.privacy_policy)

        var startIndex = tosAndPpString.indexOf(tosString)
        var endIndex = startIndex + tosString.length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigator.anim(NONE).openBrowser(BuildConfig.TERM_OF_SERVICE)
                }

            }, startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.ITALIC),
            startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        startIndex = tosAndPpString.indexOf(ppString)
        endIndex = startIndex + ppString.length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigator.anim(NONE).openBrowser(BuildConfig.PRIVACY_POLICY)
                }

            }, startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.ITALIC),
            startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        tvToSandPP.text = spannableString
        tvToSandPP.movementMethod = LinkMovementMethod.getInstance()
        tvToSandPP.setLinkTextColor(getColor(R.color.black))
        tvToSandPP.highlightColor = Color.TRANSPARENT

        ivBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        tvUnlink.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(UnlinkContainerActivity::class.java)
        }

        tvBiometricAuth.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(BiometricAuthActivity::class.java)
        }

        tvRecoveryKey.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(RecoveryContainerActivity::class.java)
        }

        tvExportData.setSafetyOnclickListener {
            toastComingSoon()
        }

        tvDeleteAccount.setSafetyOnclickListener {
            toastComingSoon()
        }

        tvIncPrivacy.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(IncreasePrivacyActivity::class.java)
        }

        tvFbDeleteAccount.setSafetyOnclickListener {
            toastComingSoon()
        }

        tvAbout.setSafetyOnclickListener {
            toastComingSoon()
        }

        tvFaq.setSafetyOnclickListener {
            goToFaq()
        }

        tvHelp.setSafetyOnclickListener {
            navigator.openIntercom()
        }

        tvWhatsNew.setSafetyOnclickListener {
            val bundle = WhatsNewActivity.getBundle(true)
            navigator.anim(RIGHT_LEFT).startActivity(WhatsNewActivity::class.java, bundle)
        }

        tvTellUs.setSafetyOnclickListener {
            navigator.anim(NONE).openBrowser(SURVEY_URL)
        }

    }

    private fun toastComingSoon() {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun goToFaq() {
        val bundle = WebViewActivity.getBundle(BuildConfig.FAQ, getString(R.string.faq))
        navigator.anim(RIGHT_LEFT).startActivity(WebViewActivity::class.java, bundle)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigator.anim(RIGHT_LEFT).finishActivity()
    }
}