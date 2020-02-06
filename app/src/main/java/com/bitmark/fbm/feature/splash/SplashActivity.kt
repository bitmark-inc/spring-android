/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.splash

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.lifecycle.Observer
import com.bitmark.fbm.BuildConfig
import com.bitmark.fbm.R
import com.bitmark.fbm.data.ext.isServiceUnsupportedError
import com.bitmark.fbm.data.model.*
import com.bitmark.fbm.data.source.remote.api.error.UnknownException
import com.bitmark.fbm.feature.BaseAppCompatActivity
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.fbm.feature.Navigator.Companion.FADE_IN
import com.bitmark.fbm.feature.Navigator.Companion.NONE
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.main.MainActivity
import com.bitmark.fbm.feature.register.archiverequest.ArchiveRequestContainerActivity
import com.bitmark.fbm.feature.register.dataprocessing.DataProcessingActivity
import com.bitmark.fbm.feature.register.onboarding.OnboardingActivity
import com.bitmark.fbm.feature.signin.SignInActivity
import com.bitmark.fbm.feature.whatsnew.WhatsNewActivity
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.logging.Tracer
import com.bitmark.fbm.util.ext.*
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.features.Account
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.concurrent.Executors
import javax.inject.Inject

class SplashActivity : BaseAppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"

        private const val WHATS_NEW_REQ_CODE = 0xA7
    }

    @Inject
    internal lateinit var viewModel: SplashViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    private val handler = Handler()

    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var account: Account

    override fun layoutRes(): Int = R.layout.activity_splash

    override fun viewModel(): BaseViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkVersionOutOfDate()
    }

    override fun initComponents() {
        super.initComponents()

        val tosAndPpString = getString(R.string.by_continuing)
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
        tvToSandPP.setLinkTextColor(getColor(R.color.white))
        tvToSandPP.highlightColor = Color.TRANSPARENT

        btnGetStarted.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(OnboardingActivity::class.java)
        }

        tvLogin.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(SignInActivity::class.java)
        }
    }

    override fun deinitComponents() {
        executor.shutdown()
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.checkVersionOutOfDateLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val data = res.data()!!
                    val versionOutOfDate = data.first
                    if (versionOutOfDate) {
                        dialogController.showUpdateRequired {
                            val updateUrl = data.second
                            navigator.goToUpdateApp(updateUrl)
                            navigator.exitApp()
                        }
                    } else {
                        viewModel.checkFirstTimeEnterNewVersion(BuildConfig.VERSION_CODE)
                    }
                }

                res.isError() -> {
                    logger.logError(
                        Event.SPLASH_VERSION_CHECK_ERROR,
                        res.throwable() ?: UnknownException()
                    )
                    viewModel.checkFirstTimeEnterNewVersion(BuildConfig.VERSION_CODE)
                }

            }
        })

        viewModel.checkFirstTimeEnterNewVersionLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val firstTimeEnter = res.data() ?: false
                    if (firstTimeEnter) {
                        val bundle = WhatsNewActivity.getBundle(false)
                        navigator.anim(BOTTOM_UP).startActivityForResult(
                            WhatsNewActivity::class.java,
                            WHATS_NEW_REQ_CODE,
                            bundle
                        )
                    } else {
                        viewModel.getAccountInfo()
                    }
                }

                res.isError() -> {
                    logger.logSharedPrefError(
                        res.throwable(),
                        "check first time enter new version error"
                    )
                    viewModel.getAccountInfo()
                }
            }
        })

        viewModel.getAccountInfoLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val data = res.data()!!
                    val accountData = data.first
                    val archiveRequestedAt = data.second
                    val fbCredentialExisting = data.third

                    if (fbCredentialExisting) {
                        CredentialData.delete(this, executor, {
                            handleAppState(accountData, archiveRequestedAt)
                        }, { e ->
                            Tracer.ERROR.log(TAG, "delete fb credential error ${e?.message}")
                            logger.logError(Event.DELETE_FB_CREDENTIAL_ERROR, e)
                            handleAppState(accountData, archiveRequestedAt)
                        })
                    } else {
                        handleAppState(accountData, archiveRequestedAt)
                    }
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "get account info error")
                    dialogController.unexpectedAlert { navigator.exitApp() }
                }
            }
        })

        viewModel.prepareDataLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val invalidArchives = res.data() ?: false
                    if (invalidArchives) {
                        goToMain(account.seed.encodedSeed)
                    } else {
                        viewModel.checkDataReady()
                    }
                }

                res.isError() -> {
                    val error = res.throwable()!!
                    logger.logError(
                        Event.SPLASH_PREPARE_DATA_ERROR,
                        "$TAG: prepare data error: ${error.message ?: "unknown"}"
                    )
                    if (!error.isServiceUnsupportedError()) {
                        if (error is UnknownException) {
                            dialogController.unexpectedAlert { navigator.exitApp() }
                        } else {
                            dialogController.alert(error) { navigator.exitApp() }
                        }
                    }
                }
            }
        })

        viewModel.checkDataReadyLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val dataReady = res.data()!!.first
                    val categoryReady = res.data()!!.second

                    handler.postDelayed({
                        if (dataReady) {
                            if (categoryReady) {
                                val bundle = MainActivity.getBundle(account.seed.encodedSeed)
                                navigator.anim(FADE_IN)
                                    .startActivityAsRoot(MainActivity::class.java, bundle)
                            } else {
                                val bundle = ArchiveRequestContainerActivity.getBundle(
                                    true,
                                    account.seed.encodedSeed
                                )
                                navigator.anim(FADE_IN)
                                    .startActivityAsRoot(
                                        ArchiveRequestContainerActivity::class.java,
                                        bundle
                                    )
                            }
                        } else {
                            goToMain(account.seed.encodedSeed)
                        }
                    }, 250)
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "check data ready error")
                    dialogController.unexpectedAlert { navigator.exitApp() }
                }
            }
        })

    }

    private fun handleAppState(accountData: AccountData, archiveRequestedAt: Long) {
        val archiveRequested = archiveRequestedAt != -1L
        val loggedIn = accountData.isCreatedRemotely() && !archiveRequested
        val uri = intent?.data
        val loginFromDeepLink =
            uri != null && uri.scheme == getString(R.string.scheme) && uri.host == "login"

        if (accountData.isCreatedLocally()) {
            loadAccount(accountData) { account ->
                this.account = account

                when {
                    // account already registered remotely
                    loggedIn -> viewModel.prepareData(this.account)

                    // requested archive
                    archiveRequested -> handler.postDelayed({
                        val bundle = DataProcessingActivity.getBundle(
                            archiveRequestedAt,
                            account.seed.encodedSeed
                        )
                        navigator.anim(FADE_IN)
                            .startActivityAsRoot(DataProcessingActivity::class.java, bundle)
                    }, 250)

                    loginFromDeepLink -> {
                        try {
                            val phrase = uri!!.getQueryParameter("phrases")!!
                            val phraseArray = phrase.split("-").toTypedArray()
                            if (phraseArray.size in arrayOf(12, 24)) {
                                val bundle = SignInActivity.getBundle(phraseArray)
                                navigator.anim(RIGHT_LEFT)
                                    .startActivityAsRoot(SignInActivity::class.java, bundle)
                            } else {
                                logger.logError(
                                    Event.ACCOUNT_DEEPLINK_INVALID_PHRASE_ERROR,
                                    "invalid deeplink phrase $phrase"
                                )
                                showOnboarding()
                            }
                        } catch (e: Throwable) {
                            logger.logError(
                                Event.ACCOUNT_DEEPLINK_INVALID_PHRASE_ERROR,
                                "invalid deeplink parsing ${e.message}"
                            )
                            showOnboarding()
                        }
                    }
                }
            }
        } else {
            showOnboarding()
        }
    }

    private fun goToMain(accountSeed: String) {
        val bundle = MainActivity.getBundle(accountSeed)
        navigator.anim(RIGHT_LEFT).startActivityAsRoot(MainActivity::class.java, bundle)
    }

    private fun showOnboarding() {
        tvToSandPP.visible(true)
        btnGetStarted.visible(true)
        tvLogin.visible(true)
    }

    private fun loadAccount(accountData: AccountData, action: (Account) -> Unit) {
        val spec =
            KeyAuthenticationSpec.Builder(this).setKeyAlias(accountData.keyAlias)
                .setAuthenticationDescription(getString(R.string.your_authorization_is_required))
                .setAuthenticationRequired(accountData.authRequired).build()
        loadAccount(
            accountData.id,
            spec,
            dialogController,
            successAction = action,
            setupRequiredAction = { navigator.gotoSecuritySetting() },
            canceledAction = {
                dialogController.showAuthRequired {
                    loadAccount(accountData, action)
                }
            },
            invalidErrorAction = { e ->
                logger.logError(Event.ACCOUNT_LOAD_KEY_STORE_ERROR, e)
                dialogController.alert(e) { navigator.exitApp() }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == WHATS_NEW_REQ_CODE) {
            viewModel.getAccountInfo()
        }
    }
}