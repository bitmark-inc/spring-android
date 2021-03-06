/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.data.source.local

import com.bitmark.fbm.data.ext.fromJson
import com.bitmark.fbm.data.ext.newGsonInstance
import com.bitmark.fbm.data.model.AccountData
import com.bitmark.fbm.data.model.isRegistered
import com.bitmark.fbm.data.model.keyFileName
import com.bitmark.fbm.data.source.local.api.DatabaseApi
import com.bitmark.fbm.data.source.local.api.FileStorageApi
import com.bitmark.fbm.data.source.local.api.SharedPrefApi
import com.bitmark.fbm.data.source.local.event.DataReadyListener
import io.reactivex.Completable
import javax.inject.Inject


class AppLocalDataSource @Inject constructor(
    databaseApi: DatabaseApi,
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : LocalDataSource(databaseApi, sharedPrefApi, fileStorageApi) {

    private var dataReadyListener: DataReadyListener? = null

    internal fun setDataReadyListener(listener: DataReadyListener) {
        this.dataReadyListener = listener
    }

    fun setNotificationEnabled(enabled: Boolean) =
        sharedPrefApi.rxCompletable { sharedPrefGateway ->
            sharedPrefGateway.put(SharedPrefApi.NOTIFICATION_ENABLED, enabled)
        }

    fun checkNotificationEnabled() = sharedPrefApi.rxSingle { sharedPrefGateway ->
        sharedPrefGateway.get(SharedPrefApi.NOTIFICATION_ENABLED, Boolean::class)
    }

    fun checkDataReady() = sharedPrefApi.rxSingle { sharedPrefGateway ->
        sharedPrefGateway.get(SharedPrefApi.DATA_READY, Boolean::class)
    }

    fun setDataReady() = sharedPrefApi.rxCompletable { sharedPrefGateway ->
        sharedPrefGateway.put(SharedPrefApi.DATA_READY, true)
    }.doOnComplete { dataReadyListener?.onDataReady() }

    fun deleteDb() = databaseApi.rxCompletable { databaseGateway ->
        databaseGateway.locationDao().delete()
        databaseGateway.postDao().delete()
        databaseGateway.sectionDao().delete()
        databaseGateway.commentDao().delete()
        databaseGateway.reactionDao().delete()
        databaseGateway.criteriaDao().delete()
        databaseGateway.statsDao().delete()
        databaseGateway.mediaDao().delete()
    }

    fun deleteSharePref(keepAccountData: Boolean = false) =
        sharedPrefApi.rxCompletable { sharedPrefGateway ->
            if (keepAccountData) {
                val accountData = sharedPrefGateway.get(SharedPrefApi.ACCOUNT_DATA, String::class)
                sharedPrefGateway.clear()
                sharedPrefGateway.put(SharedPrefApi.ACCOUNT_DATA, accountData)
            } else {
                sharedPrefGateway.clear()
            }
        }

    fun deleteFileStorage(keepAccountData: Boolean = false) =
        sharedPrefApi.rxSingle { sharedPrefGateway ->
            val rawAccountData = sharedPrefGateway.get(SharedPrefApi.ACCOUNT_DATA, String::class)
            newGsonInstance().fromJson<AccountData>(rawAccountData)
                ?: AccountData.newEmptyInstance()
        }.flatMapCompletable { accountData ->
            fileStorageApi.rxCompletable { fileStorageGateway ->
                if (keepAccountData && accountData.isRegistered()) {
                    fileStorageGateway.deleteFileDir(accountData.keyFileName)
                } else {
                    fileStorageGateway.deleteFileDir()
                }
            }
        }

    fun getLastVersionCode() = sharedPrefApi.rxSingle { sharedPrefGateway ->
        sharedPrefGateway.get(SharedPrefApi.LAST_VERSION_CODE, Int::class, -1)
    }

    fun saveLastVersionCode(code: Int) = sharedPrefApi.rxCompletable { sharedPrefGateway ->
        sharedPrefGateway.put(SharedPrefApi.LAST_VERSION_CODE, code)
    }

    fun listLinkClicked() = sharedPrefApi.rxSingle { sharedPrefGateway ->
        val links = sharedPrefGateway.get(
            SharedPrefApi.LINK_CLICKED,
            String::class
        )
        if (links.isEmpty()) {
            listOf()
        } else {
            newGsonInstance().fromJson<List<String>>(links)
        }

    }

    fun saveLinkClicked(link: String) = listLinkClicked().flatMapCompletable { savedLinks ->
        if (savedLinks.contains(link)) {
            Completable.complete()
        } else {
            sharedPrefApi.rxCompletable { sharedPrefGateway ->
                val links = savedLinks.toMutableList()
                links.add(link)
                sharedPrefGateway.put(SharedPrefApi.LINK_CLICKED, links)
            }
        }
    }

    fun setArchiveUploaded() = sharedPrefApi.rxCompletable { sharedPrefGateway ->
        sharedPrefGateway.put(SharedPrefApi.ARCHIVE_UPLOADED, true)
    }

    fun checkArchiveUploaded() = sharedPrefApi.rxSingle { sharedPrefGateway ->
        sharedPrefGateway.get(SharedPrefApi.ARCHIVE_UPLOADED, Boolean::class)
    }

}