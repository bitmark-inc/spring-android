/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.usagetimeline

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.entity.Period
import com.bitmark.fbm.data.model.entity.fromString
import com.bitmark.fbm.data.model.entity.value
import com.bitmark.fbm.feature.BaseSupportFragment
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.DialogController
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.connectivity.ConnectivityHandler
import com.bitmark.fbm.logging.Event
import com.bitmark.fbm.logging.EventLogger
import com.bitmark.fbm.util.Constants
import com.bitmark.fbm.util.EndlessScrollListener
import com.bitmark.fbm.util.ext.getDimensionPixelSize
import com.bitmark.fbm.util.ext.gone
import com.bitmark.fbm.util.ext.openVideoPlayer
import com.bitmark.fbm.util.ext.visible
import com.bitmark.fbm.util.view.SpaceItemDecoration
import com.bitmark.fbm.util.view.stickyheader.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.fragment_usage_timeline.*
import javax.inject.Inject


class UsageTimelineFragment : BaseSupportFragment() {

    companion object {
        private const val PERIOD = "period"

        private const val TYPE = "type"

        fun newInstance(period: Period, type: Int): UsageTimelineFragment {
            val fragment = UsageTimelineFragment()
            val bundle = Bundle()
            bundle.putString(PERIOD, period.value)
            bundle.putInt(TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    internal lateinit var viewModel: UsageTimelineViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var period: Period

    private var type = -1

    private lateinit var adapter: UsageTimelineRecyclerViewAdapter

    private lateinit var endlessScrollListener: EndlessScrollListener

    private val handler = Handler()

    private val connectivityChangeListener =
        object : ConnectivityHandler.NetworkStateChangeListener {
            override fun onChange(connected: Boolean) {
                val runnable = Runnable { layoutNoNetwork.gone(true) }
                if (!connected) {
                    if (layoutNoNetwork.isVisible) return
                    layoutNoNetwork.visible(true)
                    handler.postDelayed(runnable, 2000)
                } else {
                    layoutNoNetwork.gone(true)
                    handler.removeCallbacks(runnable)
                }
            }

        }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        type = arguments?.getInt(TYPE) ?: error("missing type")
        val periodString = arguments?.getString(PERIOD) ?: error("missing period")
        period = Period.fromString(periodString)
    }

    override fun layoutRes(): Int = R.layout.fragment_usage_timeline

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        adapter = when (type) {
            UsageType.POST -> PostTimelineRecyclerViewAdapter(period)
            UsageType.REACTION -> ReactionTimelineRecyclerViewAdapter(period)
            UsageType.MEDIA -> MediaTimelineRecyclerViewAdapter(period)
            else -> error("unsupported type: $type")
        }

        val layoutManager: RecyclerView.LayoutManager
        layoutManager = if (type == UsageType.MEDIA) {
            val itemDecoration = SpaceItemDecoration(context!!.getDimensionPixelSize(R.dimen.dp_1))
            rvTimeline.addItemDecoration(itemDecoration)
            GridLayoutManager(context, 3)
        } else {
            val itemDecoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
            val dividerDrawable =
                ContextCompat.getDrawable(context!!, R.drawable.double_divider_white_black_stroke)
            if (dividerDrawable != null) itemDecoration.setDrawable(dividerDrawable)
            rvTimeline.addItemDecoration(itemDecoration)
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        endlessScrollListener = object : EndlessScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                when (type) {
                    UsageType.POST -> viewModel.listNextPost()
                    UsageType.REACTION -> viewModel.listNextReaction()
                    UsageType.MEDIA -> viewModel.listNextMedia()
                }
            }
        }
        rvTimeline.addOnScrollListener(endlessScrollListener)
        rvTimeline.layoutManager = layoutManager
        rvTimeline.addItemDecoration(StickyRecyclerHeadersDecoration(adapter))
        rvTimeline.adapter = adapter

        if (type == UsageType.MEDIA) {
            val mediaAdapter = adapter as MediaTimelineRecyclerViewAdapter
            mediaAdapter.setLoadingImageListener(object :
                MediaTimelineRecyclerViewAdapter.LoadingImageListener {
                override fun onLoadingAlternativeUri(mediaId: String, absoluteUrl: String) {
                    viewModel.updateThumbnailUri(mediaId, absoluteUrl)
                }

            })

            mediaAdapter.setItemClickListener(object :
                MediaTimelineRecyclerViewAdapter.ItemClickListener {
                override fun onClicked(mediaItem: MediaTimelineRecyclerViewAdapter.MediaItem) {
                    if (mediaItem.isVideo) {
                        viewModel.getPresignedUrl(mediaItem.uri)
                    }
                }
            })
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun onResume() {
        super.onResume()
        connectivityHandler.addNetworkStateChangeListener(
            connectivityChangeListener
        )

        if (adapter.isEmpty()) {
            handler.postDelayed({
                val now = System.currentTimeMillis() / 1000
                when (type) {
                    UsageType.POST -> viewModel.listPost(now)
                    UsageType.REACTION -> viewModel.listReaction(now)
                    UsageType.MEDIA -> viewModel.listMedia(now)
                }
            }, Constants.UI_READY_DELAY)
        }
    }

    override fun onPause() {
        super.onPause()
        connectivityHandler.removeNetworkStateChangeListener(
            connectivityChangeListener
        )
    }

    override fun observe() {
        super.observe()

        viewModel.listPostLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data() ?: return@Observer
                    handleEmptyData(adapter, data)
                    if (data.isNotEmpty()) (adapter as PostTimelineRecyclerViewAdapter).add(data)
                }

                res.isError() -> {
                    progressBar.gone()
                    handleError(res.throwable())
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.listReactionLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data() ?: return@Observer
                    handleEmptyData(adapter, data)
                    if (data.isNotEmpty()) (adapter as ReactionTimelineRecyclerViewAdapter).add(data)
                }

                res.isError() -> {
                    progressBar.gone()
                    handleError(res.throwable())
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.listMediaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data() ?: return@Observer
                    handleEmptyData(adapter, data)
                    if (data.isNotEmpty()) (adapter as MediaTimelineRecyclerViewAdapter).add(data)
                }

                res.isError() -> {
                    progressBar.gone()
                    handleError(res.throwable())
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.getVideoPresignedUrl.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val url = res.data()!!
                    navigator.openVideoPlayer(url, error = { e ->
                        logger.logError(Event.PLAY_VIDEO_ERROR, e)
                        dialogController.alert(R.string.error, R.string.could_not_play_video)
                    })
                }

                res.isError() -> {
                    logger.logError(Event.PLAY_VIDEO_ERROR, res.throwable())
                    dialogController.alert(R.string.error, R.string.could_not_play_video)
                }
            }
        })
    }

    private fun handleError(e: Throwable?) {
        logger.logError(Event.BROWSE_CATEGORIES_ERROR, e)
    }

    private fun handleEmptyData(adapter: UsageTimelineRecyclerViewAdapter, data: List<*>) {
        if (adapter.isEmpty() && data.isEmpty()) {
            tvNoData.visible()
        } else {
            tvNoData.gone()
        }
    }

    override fun refresh() {
        super.refresh()
        rvTimeline.smoothScrollToPosition(0)
    }
}