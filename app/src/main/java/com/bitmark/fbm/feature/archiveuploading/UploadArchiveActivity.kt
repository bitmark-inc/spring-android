/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.archiveuploading

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.lifecycle.Observer
import com.bitmark.fbm.R
import com.bitmark.fbm.data.ext.isServiceUnsupportedError
import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.model.ArchiveType
import com.bitmark.fbm.data.model.isRegistered
import com.bitmark.fbm.data.source.remote.api.error.UnknownException
import com.bitmark.fbm.feature.BaseAppCompatActivity
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.fbm.feature.Navigator.Companion.FADE_IN
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.archiveuploading.service.UploadArchiveService
import com.bitmark.fbm.feature.connectivity.ConnectivityHandler
import com.bitmark.fbm.feature.main.MainActivity
import com.bitmark.fbm.feature.register.archiverequest.ArchiveRequestContainerActivity
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.util.ext.*
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.features.Account
import kotlinx.android.synthetic.main.activity_upload_archive.*
import javax.inject.Inject


class UploadArchiveActivity : BaseAppCompatActivity() {

    companion object {

        private const val FB_URL = "https://fb.com/dyi"

        private const val BROWSE_DOC_REQUEST_CODE = 0xEA

        private const val AUTOMATE_REQUEST_CODE = 0xEB

        private const val MAX_FILE_SIZE = 5 * 1024 * 1024 * 1024L // 5GB

        private const val FIRST_LAUNCH = "first_launch"

        private const val UPLOAD_TYPE = "upload_type"

        private const val ACCOUNT_SEED = "account_seed"

        fun getBundle(firstLaunch: Boolean = true, accountSeed: String? = null): Bundle {
            val bundle = Bundle()
            bundle.putBoolean(FIRST_LAUNCH, firstLaunch)
            if (accountSeed != null) bundle.putString(ACCOUNT_SEED, accountSeed)
            return bundle
        }

        fun extractUploadType(intent: Intent?): String {
            return intent?.extras?.getString(UPLOAD_TYPE) ?: error("missing type")
        }
    }

    @Inject
    internal lateinit var viewModel: UploadArchiveViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    @Inject
    internal lateinit var logger: EventLogger

    private var blocked = false

    private var account: Account? = null

    private var archiveUrl: String? = null

    private var archiveUri: String? = null

    private var firstLaunch = true

    override fun layoutRes(): Int = R.layout.activity_upload_archive

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        firstLaunch = intent?.extras?.getBoolean(FIRST_LAUNCH) ?: true
        val seed = intent?.extras?.getString(ACCOUNT_SEED)
        if(seed != null) account = Account.fromSeed(seed)

        val spannableMsg = SpannableString(Html.fromHtml(getString(R.string.option_2_manual)))
        val startIndex = spannableMsg.indexOf(FB_URL)
        val endIndex = startIndex + FB_URL.length
        spannableMsg.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigator.anim(Navigator.NONE).openBrowser(FB_URL)
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
        tvManual.movementMethod = LinkMovementMethod.getInstance()
        tvManual.setLinkTextColor(getColor(R.color.black))
        tvManual.highlightColor = Color.TRANSPARENT
        tvManual.text = spannableMsg

        tvAutomate.text = Html.fromHtml(getString(R.string.option_1_automated))

        ivBack.setOnClickListener {
            exit()
        }

        btnAddZip.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            navigator.anim(BOTTOM_UP).browseDocument(BROWSE_DOC_REQUEST_CODE)
        }

        btnAutomate.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.getAccountData()
        }

        etUrl.setOnEditorActionListener { _, actionId, _ ->
            when {
                blocked -> false
                actionId == EditorInfo.IME_ACTION_DONE -> {
                    val url = etUrl.text.toString().trim()
                    if (url.isEmpty()) {
                        false
                    } else {
                        if (!URLUtil.isValidUrl(url)) {
                            etUrl.text?.clear()
                            dialogController.confirm(
                                R.string.invalid_url,
                                R.string.the_url_provided_was_invalid,
                                true,
                                "invalid_url",
                                R.string.contact_us,
                                { navigator.openIntercom() },
                                R.string.try_again
                            )
                        } else {
                            archiveUrl = url
                            registerAccount()
                        }
                        hideKeyBoard()
                        true
                    }

                }
                else -> false
            }
        }
    }

    override fun observe() {
        super.observe()

        viewModel.uploadArchiveUrlLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    blocked = false
                    navigate(firstLaunch)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.ARCHIVE_URL_UPLOAD_ERROR, res.throwable())
                    if (!connectivityHandler.isConnected()) {
                        dialogController.showNoInternetConnection()
                    } else if (!res.throwable()!!.isServiceUnsupportedError()) {
                        dialogController.alert(
                            R.string.error,
                            R.string.could_not_upload_archive_url
                        ) { navigator.openIntercom(true) }
                    }
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })

        viewModel.registerAccountLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    upload()
                    blocked = false
                    navigate(firstLaunch)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(
                        Event.ARCHIVE_UPLOAD_REGISTER_ACCOUNT_ERROR,
                        res.throwable() ?: UnknownException("unknown")
                    )

                    if (!connectivityHandler.isConnected()) {
                        dialogController.showNoInternetConnection()
                    } else if (!res.throwable()!!.isServiceUnsupportedError()) {
                        dialogController.alert(
                            R.string.error,
                            R.string.could_not_register_account
                        ) { navigator.openIntercom(true) }
                    }
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })

        viewModel.getAccountDataLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val accountData = res.data()!!
                    var bundle: Bundle? = null
                    if (accountData.isRegistered()) {
                        loadAccount(accountData) { account ->
                            bundle = ArchiveRequestContainerActivity.getBundle(
                                true,
                                account.seed.encodedSeed,
                                firstLaunch
                            )

                        }
                    } else {
                        bundle = ArchiveRequestContainerActivity.getBundle(
                            false,
                            firstLaunch = firstLaunch
                        )
                    }

                    navigator.anim(RIGHT_LEFT)
                        .startActivityForResult(
                            ArchiveRequestContainerActivity::class.java,
                            AUTOMATE_REQUEST_CODE,
                            bundle
                        )
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "could not get account data")
                    dialogController.unexpectedAlert { navigator.openIntercom() }
                }
            }
        })
    }

    private fun upload() {
        if (archiveUrl != null) {
            viewModel.uploadArchiveUrl(archiveUrl!!)
        } else {
            val bundle = UploadArchiveService.getBundle(archiveUri!!)
            val intent = Intent(this, UploadArchiveService::class.java)
            intent.putExtras(bundle)
            startService(intent)
        }
    }

    private fun navigate(firstLaunch: Boolean) {
        if (firstLaunch) {
            goToMain()
        } else {
            val type = if (archiveUrl != null) ArchiveType.URL else ArchiveType.FILE
            exit(true, type)
        }
    }

    override fun onBackPressed() {
        exit()
        super.onBackPressed()
    }

    private fun exit(withResult: Boolean = false, type: String? = null) {
        if (withResult && type != null) {
            val bundle = Bundle()
            bundle.putString(UPLOAD_TYPE, type)
            val intent = Intent()
            intent.putExtras(bundle)
            navigator.anim(RIGHT_LEFT).finishActivityForResult(intent)
        } else {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

    }

    private fun goToMain() {
        val bundle = MainActivity.getBundle(account!!.seed.encodedSeed)
        navigator.anim(FADE_IN).startActivityAsRoot(MainActivity::class.java, bundle)
    }

    private fun saveAccount(
        account: Account,
        successAction: (String) -> Unit
    ) {
        val keyAlias = account.generateKeyAlias()
        val spec = KeyAuthenticationSpec.Builder(this)
            .setKeyAlias(keyAlias)
            .setAuthenticationRequired(false).build()

        saveAccount(
            account,
            spec,
            dialogController,
            successAction = { successAction(keyAlias) },
            setupRequiredAction = { navigator.gotoSecuritySetting() },
            invalidErrorAction = { e ->
                logger.logError(Event.ACCOUNT_SAVE_TO_KEY_STORE_ERROR, e)
                dialogController.alert(
                    getString(R.string.error),
                    e?.message ?: getString(R.string.unexpected_error)
                ) { navigator.openIntercom(true) }
            })
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

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == BROWSE_DOC_REQUEST_CODE) {
                val uri = data?.data ?: return
                archiveUri = uri.toString()
                val size = getFileSize(uri)
                val ext =
                    MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(contentResolver.getType(uri))
                if (!ext.equals("zip", true)) {
                    dialogController.confirm(
                        R.string.invalid_file_format,
                        R.string.the_file_you_uploaded_was_not_a_fb_archive,
                        true,
                        "invalid_format",
                        R.string.contact_us,
                        {
                            navigator.openIntercom()
                        },
                        R.string.try_again,
                        {})
                } else if (size > MAX_FILE_SIZE) {
                    dialogController.alert(
                        R.string.file_size_exceeded,
                        R.string.your_file_is_larger_than
                    )
                } else if (firstLaunch) {
                    registerAccount()
                } else {
                    upload()
                    navigate(false)
                }
            } else if (requestCode == AUTOMATE_REQUEST_CODE) {
                exit(true, ArchiveType.SESSION)
            }
        }
    }

    private fun registerAccount() {
        if(account == null) account = Account()
        saveAccount(account!!) { alias ->
            viewModel.registerAccount(account!!, alias)
        }
    }
}