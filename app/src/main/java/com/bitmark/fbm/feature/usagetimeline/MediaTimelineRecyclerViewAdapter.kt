/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.usagetimeline

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.fbm.BuildConfig
import com.bitmark.fbm.R
import com.bitmark.fbm.data.model.entity.Period
import com.bitmark.fbm.util.ext.*
import com.bitmark.fbm.util.modelview.MediaModelView
import com.bitmark.fbm.util.modelview.timestamp
import kotlinx.android.synthetic.main.item_media.view.*
import java.net.URLEncoder


class MediaTimelineRecyclerViewAdapter(period: Period) :
    UsageTimelineRecyclerViewAdapter(period, false) {

    private var loadingImageListener: LoadingImageListener? = null

    private var itemClickListener: ItemClickListener? = null

    fun setLoadingImageListener(listener: LoadingImageListener) {
        this.loadingImageListener = listener
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    fun add(mediaModelView: List<MediaModelView>) {
        if (mediaModelView.isEmpty()) return
        val pos = data.size
        val sortedData = mediaModelView.sortedByDescending { it.timestampSec }
        val groupedData = sortedData.groupBy { groupKeyBy(period, it.timestamp) }
        val mediaItem = mutableListOf<MediaItem>()
        groupedData.forEach { (key, value) ->
            val headerPos = mediaItem.size
            value.forEach { v ->
                mediaItem.add(
                    MediaItem(
                        ITEM,
                        headerPos,
                        key,
                        v.id,
                        v.source,
                        v.thumbnail,
                        v.uri,
                        v.isVideo
                    )
                )
            }
        }
        this.data.addAll(mediaItem)
        notifyItemRangeInserted(pos, mediaItem.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM -> ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_media,
                    parent,
                    false
                ),
                loadingImageListener,
                itemClickListener
            )
            else -> error("invalid viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = this.data[position]
        val type = data.type
        if (type == ITEM) {
            (holder as? ItemViewHolder)?.bind(data as MediaItem)
        }
    }

    override fun getNumColumns(): Int = 3

    class ItemViewHolder(
        view: View,
        private val loadingImageListener: LoadingImageListener?,
        itemClickListener: ItemClickListener?
    ) :
        RecyclerView.ViewHolder(view) {

        private val handler = Handler()

        private lateinit var mediaItem: MediaItem

        init {
            with(itemView) {
                layoutRoot.setSafetyOnclickListener {
                    itemClickListener?.onClicked(mediaItem)
                }
            }
        }

        fun bind(mediaItem: MediaItem) {
            this.mediaItem = mediaItem
            with(itemView) {
                val cache = mediaItem.uri + "_thumbnail"
                vBottomGradient.invisible()
                ivThumbnail.load(mediaItem.thumbnail, cache, error = {
                    this@ItemViewHolder.handler.post {
                        ivThumbnail.load(mediaItem.absoluteUrl, cache, success = {
                            vBottomGradient.visible()
                            loadingImageListener?.onLoadingAlternativeUri(
                                mediaItem.id,
                                mediaItem.absoluteUrl
                            )
                        })
                    }
                }, success = {
                    vBottomGradient.visible()
                })
                if (mediaItem.isVideo) {
                    ivVideo.visible()
                } else {
                    ivVideo.gone()
                }
            }
        }

    }

    class MediaItem(
        type: Int,
        headerPos: Int,
        groupedKey: String,
        val id: String,
        val source: String,
        val thumbnail: String,
        val uri: String,
        val isVideo: Boolean
    ) : Item(type, headerPos, groupedKey)

    interface LoadingImageListener {
        fun onLoadingAlternativeUri(mediaId: String, absoluteUrl: String)
    }

    interface ItemClickListener {
        fun onClicked(mediaItem: MediaItem)
    }
}

val MediaTimelineRecyclerViewAdapter.MediaItem.absoluteUrl: String
    get() = BuildConfig.FBM_ASSET_ENDPOINT + "?key=${URLEncoder.encode(uri, "UTF-8") ?: ""}"