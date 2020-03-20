/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.usagetimeline

import androidx.lifecycle.Lifecycle
import com.bitmark.fbm.data.model.entity.PostType
import com.bitmark.fbm.data.source.UsageRepository
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.util.livedata.CompositeLiveData
import com.bitmark.fbm.util.livedata.RxLiveDataTransformer
import com.bitmark.fbm.util.modelview.MediaModelView
import com.bitmark.fbm.util.modelview.PostModelView
import com.bitmark.fbm.util.modelview.ReactionModelView
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class UsageTimelineViewModel(
    lifecycle: Lifecycle,
    private val usageRepo: UsageRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listPostLiveData = CompositeLiveData<List<PostModelView>>()

    internal val listReactionLiveData = CompositeLiveData<List<ReactionModelView>>()

    internal val listMediaLiveData = CompositeLiveData<List<MediaModelView>>()

    internal val getVideoPresignedUrl = CompositeLiveData<String>()

    private var lastEndedAtSec = -1L

    fun listReaction(endedAtSec: Long) {
        listReactionLiveData.add(
            rxLiveDataTransformer.single(
                listReactionStream(
                    endedAtSec
                )
            )
        )
    }

    fun listNextReaction() {
        listReactionLiveData.add(
            rxLiveDataTransformer.single(
                listReactionStream(
                    lastEndedAtSec - 1
                )
            )
        )
    }

    private fun listReactionStream(
        endedAtSec: Long
    ): Single<List<ReactionModelView>> =
        usageRepo.listReaction(
            0,
            endedAtSec
        ).map { reactions ->
            if (reactions.isNotEmpty()) {
                lastEndedAtSec = reactions.last().timestampSec
            }
            reactions.map { r -> ReactionModelView.newInstance(r) }
        }

    fun listPost(endedAtSec: Long) {
        listPostLiveData.add(
            rxLiveDataTransformer.single(
                listPostStream(endedAtSec)
            )
        )
    }

    fun listNextPost() {
        listPostLiveData.add(
            rxLiveDataTransformer.single(
                listPostStream(lastEndedAtSec - 1)
            )
        )
    }

    private fun listPostStream(endedAtSec: Long) =
        usageRepo.listPost(
            0,
            endedAtSec
        ).observeOn(Schedulers.computation()).map { posts ->
            if (posts.isNotEmpty()) {
                lastEndedAtSec = posts.last().timestampSec
            }
            posts.filter { p -> p.type != PostType.UNSPECIFIED }
                .map { p -> PostModelView.newInstance(p) }
        }

    fun listMedia(endedAtSec: Long) {
        listMediaLiveData.add(
            rxLiveDataTransformer.single(
                listMediaStream(endedAtSec)
            )
        )
    }

    fun listNextMedia() {
        listMediaLiveData.add(
            rxLiveDataTransformer.single(
                listMediaStream(lastEndedAtSec - 1)
            )
        )
    }

    private fun listMediaStream(endedAtSec: Long) =
        usageRepo.listMedia(
            0,
            endedAtSec
        ).observeOn(Schedulers.computation()).map { media ->
            if (media.isNotEmpty()) {
                lastEndedAtSec = media.last().timestampSec
            }
            media.map { m -> MediaModelView.newInstance(m) }
        }

    fun updateThumbnailUri(mediaId: String, thumbnailUri: String) {
        subscribe(
            usageRepo.updateThumbnailUri(
                mediaId,
                thumbnailUri
            ).subscribe({}, {})
        )
    }

    fun getPresignedUrl(uri: String) {
        getVideoPresignedUrl.add(rxLiveDataTransformer.single(usageRepo.getPresignUrl(uri)))
    }

}