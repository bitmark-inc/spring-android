/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source

import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.model.isProcessed
import com.bitmark.fbm.data.model.isValid
import com.bitmark.fbm.data.model.mergeWith
import com.bitmark.fbm.data.source.local.AccountLocalDataSource
import com.bitmark.fbm.data.source.remote.AccountRemoteDataSource
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.io.InputStream


class AccountRepository(
    private val remoteDataSource: AccountRemoteDataSource,
    private val localDataSource: AccountLocalDataSource
) {

    fun sendArchiveDownloadRequest(
        archiveUrl: String,
        cookie: String,
        startedAtSec: Long,
        endedAtSec: Long
    ) = remoteDataSource.sendArchiveDownloadRequest(archiveUrl, cookie, startedAtSec, endedAtSec)

    fun registerFbmServerAccount(
        timestamp: String,
        signature: String,
        requester: String,
        encPubKey: String
    ) = registerFbmServerJwt(
        timestamp,
        signature,
        requester
    ).andThen(remoteDataSource.registerFbmServerAccount(encPubKey))

    fun registerFbmServerJwt(
        timestamp: String,
        signature: String,
        requester: String
    ) = remoteDataSource.registerFbmServerJwt(
        timestamp,
        signature,
        requester
    )

    fun checkJwtExpired() = localDataSource.checkJwtExpired()

    fun saveAccountData(accountData: AccountData) =
        localDataSource.saveAccountData(accountData)

    fun getAccountData() = localDataSource.getAccountData()

    fun syncAccountData() = localDataSource.getAccountData().flatMap { localAccount ->
        remoteDataSource.getAccountInfo().flatMap { account ->
            saveAccountData(account.mergeWith(localAccount)).andThen(Single.just(account))
        }
    }

    fun registerIntercomUser(id: String) = remoteDataSource.registerIntercomUser(id)

    fun setArchiveRequestedAt(timestamp: Long) =
        localDataSource.setArchiveRequestedAt(timestamp)

    fun clearArchiveRequestedAt() = localDataSource.clearArchiveRequestedAt()

    fun getArchiveRequestedAt() = localDataSource.getArchiveRequestedAt()

    fun checkFbCredentialExisting() = localDataSource.checkFbCredentialExisting()

    fun checkInvalidArchives() = remoteDataSource.getArchives().map { archives ->
        archives.isNotEmpty() && archives.none { a -> a.isValid() }
    }

    fun checkArchiveProcessed() = listProcessedArchive().map { archives -> archives.isNotEmpty() }

    fun listProcessedArchive() =
        remoteDataSource.getArchives().map { archives -> archives.filter { a -> a.isProcessed() } }

    fun checkArchivesEmptyOrInvalid() =
        remoteDataSource.getArchives().map { archives -> archives.isEmpty() || archives.none { a -> a.isValid() } }

    fun saveAccountKeyData(alias: String, authRequired: Boolean) =
        localDataSource.saveAccountKeyAlias(alias, authRequired)

    fun saveAdsPrefCategories(categories: List<String>) =
        localDataSource.saveAdsPrefCategories(categories)

    fun listAdsPrefCategory() = localDataSource.listAdsPrefCategory()

    fun checkAdsPrefCategoryReady() = localDataSource.checkAdsPrefCategoryReady()

    fun updateMetadata(metadata: Map<String, String>) = Single.zip(
        remoteDataSource.updateMetadata(metadata),
        localDataSource.getAccountData(),
        BiFunction<AccountData, AccountData, AccountData> { remoteAccountData, localAccountData ->
            remoteAccountData.mergeWith(localAccountData)
        }).flatMap { accountData ->
        saveAccountData(accountData).andThen(Single.just(accountData))
    }

    fun getLastActivityTimestamp() = getAccountData().map { accountData ->
        accountData.metadata?.get("latest_activity_timestamp")?.toLong()
            ?: error("do not contains latest_activity_timestamp")
    }

    fun deleteAccount() = remoteDataSource.deleteAccount()

    fun uploadArchiveUrl(url: String) = remoteDataSource.uploadArchiveUrl(url)

    fun uploadArchive(
        fileInputStream: InputStream,
        fileSize: Long,
        progress: (Pair<Long, Long>) -> Unit
    ) =
        remoteDataSource.uploadArchive(fileInputStream, fileSize, progress)

    fun saveLatestArchiveType(type: String) = updateMetadata(mapOf("latest_archive_type" to type))

    fun getLatestArchiveType() = getAccountData().map { accountData ->
        accountData.metadata?.get("latest_archive_type") ?: ""
    }
}