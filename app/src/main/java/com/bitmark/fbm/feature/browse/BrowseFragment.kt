/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.browse

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.ArchiveType
import com.bitmark.fbm.feature.BaseSupportFragment
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.feature.archiveuploading.UploadArchiveActivity
import com.bitmark.fbm.feature.archiveuploading.service.UploadArchiveService
import com.bitmark.fbm.feature.archiveuploading.service.UploadArchiveServiceHandler
import com.bitmark.fbm.feature.realtime.ArchiveStateBus
import com.bitmark.fbm.feature.usagetimeline.UsageTimelineContainerFragment
import com.bitmark.fbm.feature.usagetimeline.UsageType
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.util.DateTimeUtil
import com.bitmark.fbm.util.ext.*
import kotlinx.android.synthetic.main.fragment_browse.*
import kotlinx.android.synthetic.main.layout_archive_progress.*
import kotlinx.android.synthetic.main.layout_archive_requested_info.*
import kotlinx.android.synthetic.main.layout_categories.*
import kotlinx.android.synthetic.main.layout_get_fb_data.*
import javax.inject.Inject


class BrowseFragment : BaseSupportFragment() {

    companion object {

        private const val UPLOAD_ARCHIVE_REQUEST_CODE = 0x0F

        fun newInstance() = BrowseFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: BrowseViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var serviceHandler: UploadArchiveServiceHandler

    @Inject
    internal lateinit var archiveStateBus: ArchiveStateBus

    private val handler = Handler()

    private val uploadArchiveListener = object : UploadArchiveService.StateListener {
        override fun onStarted(fileName: String, byteTotal: Long) {
            showUploadStartedState(fileName, byteTotal)
        }

        override fun onProgressChanged(fileName: String, byteRead: Long, byteTotal: Long) {
            showUploadingState(fileName, byteRead, byteTotal)
        }

        override fun onFinished() {
            viewModel.startArchiveStateBus()
            showProcessingState()
        }

        override fun onError(e: Throwable) {
            // do nothing
        }
    }

    private val actionClickListener = object : ArchiveStateBus.ActionClickListener {
        override fun onTryAgainClicked() {
            openUploadArchive()
        }

    }

    override fun layoutRes(): Int = R.layout.fragment_browse

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.prepareData()

        bindService()
    }

    override fun onDestroyView() {
        unbindService()
        super.onDestroyView()
    }

    override fun initComponents() {
        super.initComponents()

        tvNotifyMe.setSafetyOnclickListener {
            dialogController.confirm(
                R.string.enable_push_notification,
                R.string.allow_spring_send_you,
                false,
                "notification",
                R.string.enable,
                {
                    viewModel.setNotificationEnable()
                },
                R.string.cancel
            )
        }

        tvGetStarted.setSafetyOnclickListener {
            openUploadArchive()
        }

        layoutPost.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).replaceChildFragment(
                R.id.layoutContainer,
                UsageTimelineContainerFragment.newInstance(UsageType.POST)
            )
        }

        layoutMedia.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).replaceChildFragment(
                R.id.layoutContainer,
                UsageTimelineContainerFragment.newInstance(UsageType.MEDIA)
            )
        }

        layoutReaction.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).replaceChildFragment(
                R.id.layoutContainer,
                UsageTimelineContainerFragment.newInstance(UsageType.REACTION)
            )
        }

        archiveStateBus.addActionClickListener(actionClickListener)
    }

    override fun deinitComponents() {
        archiveStateBus.removeActionClickListener(actionClickListener)
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.setNotificationEnableLiveData.asLiveData().observe(this, Observer { res ->
            when {

                res.isSuccess() -> {
                    tvNotifyMe.invisible()
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "set notification enable error")
                    dialogController.unexpectedAlert { navigator.openIntercom() }
                }
            }
        })

        viewModel.prepareDataLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val data = res.data()!!
                    val dataReady = data.first
                    val notificationEnabled = data.second
                    val archiveRequestedAt = data.third
                    if (dataReady) {
                        showCategories()
                    } else if (archiveRequestedAt != -1L) {
                        showArchiveRequestedAt(archiveRequestedAt)
                    } else {
                        showProcessingState()
                        if (notificationEnabled) {
                            tvNotifyMe.invisible()
                        } else {
                            tvNotifyMe.visible()
                        }
                    }
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "prepare data error")
                    dialogController.unexpectedAlert { navigator.openIntercom() }
                }
            }
        })

        viewModel.dataReadyLiveData.observe(this, Observer {
            showCategories()
        })

        viewModel.archiveInvalidLiveData.observe(this, Observer {
            showGetFbData()
        })

        viewModel.getArchiveRequestedAtLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val archiveRequestedAt = res.data()!!
                    showArchiveRequestedAt(archiveRequestedAt)
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "could not get archive requested at")
                }
            }
        })
    }

    private fun showArchiveRequestedAt(archiveRequestedAt: Long) {
        if (archiveRequestedAt == -1L) {
            tvFbArchiveRequest2.gone()
        } else {
            tvFbArchiveRequest2.visible()
            tvFbArchiveRequest2.text =
                getString(R.string.you_requested_your_fb_archive_format).format(
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
                )
        }

        layoutArchiveRequestInfo.visible()
        layoutProgress.gone()
        layoutGetFbData.gone()
        layoutCategories.gone()
    }

    override fun refresh() {
        super.refresh()
        sv.scrollToTop()
    }

    private fun showUploadStartedState(fileName: String, byteTotal: Long) {
        progressBar1.progress = 0
        tvState.setText(R.string.uploading)
        tvProgress.text =
            String.format("%d of %s", 0, byteTotal.formatByteString())
        tvArchiveName.text = fileName
        tvArchiveName.visible()
        tvProgress.visible()
        progressBar1.visible()
        progressBar2.gone()

        layoutProgress.visible()
        layoutCategories.gone()
        layoutGetFbData.gone()
        layoutArchiveRequestInfo.gone()
    }

    private fun showUploadingState(fileName: String, byteRead: Long, byteTotal: Long) {
        progressBar1.progress = (byteRead * 100 / byteTotal).toInt()
        tvState.setText(R.string.uploading)
        tvProgress.text =
            String.format("%s of %s", byteRead.formatByteString(), byteTotal.formatByteString())
        tvArchiveName.text = fileName
        tvArchiveName.visible()
        tvProgress.visible()
        progressBar1.visible()
        progressBar2.gone()

        layoutProgress.visible()
        layoutCategories.gone()
        layoutGetFbData.gone()
        layoutArchiveRequestInfo.gone()
    }

    private fun showProcessingState() {
        tvState.setText(R.string.processing)
        tvProgress.invisible()
        tvArchiveName.invisible()
        progressBar1.gone()
        progressBar2.visible()

        layoutProgress.visible()
        layoutCategories.gone()
        layoutGetFbData.gone()
        layoutArchiveRequestInfo.gone()
    }

    private fun showCategories() {
        layoutCategories.visible()
        layoutProgress.gone()
        layoutGetFbData.gone()
        layoutArchiveRequestInfo.gone()
    }

    private fun showGetFbData() {
        layoutGetFbData.visible()
        layoutCategories.gone()
        layoutProgress.gone()
        layoutArchiveRequestInfo.gone()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UPLOAD_ARCHIVE_REQUEST_CODE) {
            when (UploadArchiveActivity.extractUploadType(data)) {
                ArchiveType.URL -> {
                    showProcessingState()
                    viewModel.startArchiveStateBus()
                }
                ArchiveType.FILE -> bindService()
                ArchiveType.SESSION -> viewModel.getArchiveRequestedAt()
            }
        }

    }

    private fun bindService() {
        serviceHandler.bind()
        serviceHandler.setListener(uploadArchiveListener)
    }

    private fun unbindService() {
        serviceHandler.unbind()
    }

    private fun openUploadArchive() {
        val bundle = UploadArchiveActivity.getBundle(false)
        navigator.anim(RIGHT_LEFT).startActivityForResult(
            UploadArchiveActivity::class.java,
            UPLOAD_ARCHIVE_REQUEST_CODE,
            bundle
        )
    }

}