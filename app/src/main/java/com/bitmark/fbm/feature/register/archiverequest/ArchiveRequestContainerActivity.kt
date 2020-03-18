/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.register.archiverequest

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.*
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.register.archiverequest.archiverequest.ArchiveRequestFragment
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.util.ext.*
import javax.inject.Inject

class ArchiveRequestContainerActivity : BaseAppCompatActivity() {

    companion object {
        private const val ACCOUNT_REGISTERED = "account_registered"

        private const val ACCOUNT_SEED = "account_seed"

        private const val FIRST_LAUNCH = "first_launch"

        fun getBundle(
            accountRegistered: Boolean = false,
            accountSeed: String? = null,
            firstLaunch: Boolean = true
        ): Bundle {
            val bundle = Bundle()
            bundle.putBoolean(ACCOUNT_REGISTERED, accountRegistered)
            bundle.putBoolean(FIRST_LAUNCH, firstLaunch)
            if (accountSeed != null) bundle.putString(ACCOUNT_SEED, accountSeed)
            return bundle
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: ArchiveRequestContainerViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    private var accountRegistered = false

    private var accountSeed: String? = null

    private var firstLaunch = true

    override fun layoutRes(): Int = R.layout.activity_archive_request_container

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountRegistered = intent?.extras?.getBoolean(ACCOUNT_REGISTERED) ?: false
        accountSeed = intent?.extras?.getString(ACCOUNT_SEED)
        firstLaunch = intent?.extras?.getBoolean(FIRST_LAUNCH) ?: true

        viewModel.getArchiveRequestedAt()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val isFromNotification =
            intent?.getBooleanExtra("direct_from_notification", false) ?: false
        if (isFromNotification) {
            viewModel.getArchiveRequestedAt()
        }
    }

    override fun observe() {
        super.observe()

        viewModel.getArchiveRequestedAt.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val requestedAt = res.data()!!
                    navigator.anim(Navigator.NONE)
                        .replaceFragment(
                            R.id.layoutRoot,
                            ArchiveRequestFragment.newInstance(
                                requestedAt = requestedAt,
                                accountRegistered = accountRegistered,
                                accountSeed = accountSeed,
                                firstLaunch = firstLaunch
                            ),
                            false
                        )
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "could not get archive requested at")
                    dialogController.unexpectedAlert { navigator.openIntercom(true) }
                }
            }
        })

        viewModel.serviceUnsupportedLiveData.observe(this, Observer { url ->
            dialogController.showUpdateRequired {
                if (url.isEmpty()) {
                    navigator.goToPlayStore()
                } else {
                    navigator.goToUpdateApp(url)
                }
                navigator.exitApp()
            }
        })
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.layoutRoot)
        if (currentFragment != null && currentFragment is BehaviorComponent) {
            if (!currentFragment.onBackPressed()) {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.layoutRoot)
        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }

}