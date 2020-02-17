/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.deleteaccount

import android.app.Activity
import android.content.Intent
import android.webkit.CookieManager
import androidx.lifecycle.Observer
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.feature.BaseAppCompatActivity
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.NONE
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.splash.SplashActivity
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.util.ext.*
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.delete_account_activity.*
import javax.inject.Inject


class DeleteAccountActivity : BaseAppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 0xAD
    }

    @Inject
    internal lateinit var viewModel: DeleteAccountViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    private lateinit var accountData: AccountData

    private var blocked = false

    override fun layoutRes(): Int = R.layout.delete_account_activity

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        ivBack.setOnClickListener {
            if (blocked) return@setOnClickListener
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        btnDelete.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            dialogController.confirm(
                R.string.delete_account,
                R.string.all_of_your_data,
                false,
                "delete_account",
                R.string.confirm,
                {
                    viewModel.getAccountInfo()
                },
                R.string.cancel
            )
        }
    }

    override fun observe() {
        super.observe()

        viewModel.deleteAccountLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    CookieManager.getInstance().removeAllCookies {
                        CookieManager.getInstance().flush()
                        OneSignal.setSubscription(false)
                        blocked = false
                        navigator.anim(NONE).startActivityAsRoot(SplashActivity::class.java)
                    }
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.ACCOUNT_DELETE_ERROR, res.throwable())
                    dialogController.alert(R.string.error, R.string.could_not_delete_account) {
                        navigator.anim(RIGHT_LEFT).finishActivity()
                    }
                    blocked = false
                }

                res.isLoading() -> {
                    progressBar.visible()
                    blocked = true
                }
            }
        })

        viewModel.getAccountInfoLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    accountData = res.data()!!
                    if (!accountData.authRequired) {
                        navigator.openKeyGuardConfirmation(
                            this,
                            R.string.authentication,
                            R.string.your_authorization_is_required,
                            REQUEST_CODE
                        )
                    } else {
                        deleteAccount(accountData) {
                            viewModel.deleteAccount()
                        }
                    }

                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "get account data error")
                    dialogController.unexpectedAlert { navigator.openIntercom(true) }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            deleteAccount(accountData) {
                viewModel.deleteAccount()
            }
        }
    }

    private fun deleteAccount(accountData: AccountData, success: () -> Unit) {
        val spec =
            KeyAuthenticationSpec.Builder(this).setKeyAlias(accountData.keyAlias)
                .setAuthenticationDescription(getString(R.string.your_authorization_is_required))
                .setAuthenticationRequired(accountData.authRequired).build()
        this.removeAccount(accountData.id,
            spec,
            dialogController,
            success,
            setupRequiredAction = { navigator.gotoSecuritySetting() },
            canceledAction = {},
            invalidErrorAction = { e ->
                logger.logError(Event.ACCOUNT_DELETE_ERROR, e)
                dialogController.alert(e) { navigator.anim(RIGHT_LEFT).finishActivity() }
            })
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
    }
}