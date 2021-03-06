/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
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
import androidx.lifecycle.Observer
import com.bitmark.fbm.BuildConfig
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.AppInfoData
import com.bitmark.fbm.feature.BaseSupportFragment
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.NONE
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.biometricauth.BiometricAuthActivity
import com.bitmark.fbm.feature.deleteaccount.DeleteAccountActivity
import com.bitmark.fbm.feature.increaseprivacy.IncreasePrivacyActivity
import com.bitmark.fbm.feature.recovery.RecoveryContainerActivity
import com.bitmark.fbm.feature.unlink.UnlinkContainerActivity
import com.bitmark.fbm.feature.whatsnew.WhatsNewActivity
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.util.Constants
import com.bitmark.fbm.util.Constants.FAQ_URL
import com.bitmark.fbm.util.ext.logSharedPrefError
import com.bitmark.fbm.util.ext.openBrowser
import com.bitmark.fbm.util.ext.openIntercom
import com.bitmark.fbm.util.ext.setSafetyOnclickListener
import com.bitmark.fbm.util.view.WebViewActivity
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject


class SettingsFragment : BaseSupportFragment() {

    companion object {

        private const val SOURCE_CODE_URL = "https://github.com/bitmark-inc/spring-android"

        private const val PERSONAL_API_URL =
            "https://documenter.getpostman.com/view/59304/SzRw2rJn?version=latest"

        private const val SIGN_OUT_REQUEST_CODE = 0x1A

        fun newInstance() = SettingsFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: SettingsViewModel

    @Inject
    internal lateinit var logger: EventLogger

    private lateinit var appInfoData: AppInfoData

    override fun layoutRes(): Int = R.layout.fragment_settings

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAppInfo()
        viewModel.checkDataCanBeDeleted()
    }

    override fun initComponents() {
        super.initComponents()

        tvVersion.text = getString(R.string.version_format).format(BuildConfig.VERSION_NAME)

        val eulaAndPpString = getString(R.string.eula_and_pp)
        val spannableString = SpannableString(eulaAndPpString)
        val eulaString = getString(R.string.eula)
        val ppString = getString(R.string.privacy_policy)

        var startIndex = eulaAndPpString.indexOf(eulaString)
        var endIndex = startIndex + eulaString.length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if (::appInfoData.isInitialized) {
                        navigator.anim(NONE).openBrowser(appInfoData.docs.eula)
                    }
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

        startIndex = eulaAndPpString.indexOf(ppString)
        endIndex = startIndex + ppString.length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigator.anim(NONE).openBrowser(Constants.PRIVACY_URL)
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
        tvToSandPP.setLinkTextColor(context!!.getColor(R.color.black))
        tvToSandPP.highlightColor = Color.TRANSPARENT

        tvUnlink.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT)
                .startActivityForResult(UnlinkContainerActivity::class.java, SIGN_OUT_REQUEST_CODE)
        }

        tvBiometricAuth.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(BiometricAuthActivity::class.java)
        }

        tvRecoveryKey.setSafetyOnclickListener {
            goToRecoveryKey()
        }

        tvPersonalApi.setSafetyOnclickListener {
            navigator.openBrowser(PERSONAL_API_URL)
        }

        tvExportData.setSafetyOnclickListener {
            toastComingSoon()
        }

        tvDeleteData.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(DeleteAccountActivity::class.java)
        }

        tvIncPrivacy.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(IncreasePrivacyActivity::class.java)
        }

        tvSourceCode.setSafetyOnclickListener {
            navigator.openBrowser(SOURCE_CODE_URL)
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
    }

    override fun observe() {
        super.observe()

        viewModel.getAppInfoLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    appInfoData = res.data()!!
                }

                res.isError() -> {
                    logger.logError(Event.GET_APP_INFO_ERROR, res.throwable())
                }
            }
        })

        viewModel.checkDataCanBeDeleted.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val can = res.data() ?: false
                    tvDeleteData.isEnabled = can
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "check data can be deleted error")
                }
            }
        })

        viewModel.dataReadyLiveData.observe(this, Observer {
            tvDeleteData.isEnabled = true
        })
    }

    private fun toastComingSoon() {
        Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun goToFaq() {
        val bundle = WebViewActivity.getBundle(FAQ_URL, getString(R.string.faq))
        navigator.anim(RIGHT_LEFT).startActivity(WebViewActivity::class.java, bundle)
    }

    private fun goToRecoveryKey() {
        navigator.anim(RIGHT_LEFT).startActivity(RecoveryContainerActivity::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == SIGN_OUT_REQUEST_CODE) {
            goToRecoveryKey()
        }
    }

}