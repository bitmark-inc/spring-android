/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.repo

import com.bitmark.fbm.data.source.AccountRepository
import com.bitmark.fbm.data.source.local.AccountLocalDataSource
import com.bitmark.fbm.data.source.remote.AccountRemoteDataSource
import com.bitmark.fbm.ut.data.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock


class AccountRepositoryTest : DataTest() {

    @Mock
    lateinit var localDataSource: AccountLocalDataSource

    @Mock
    lateinit var remoteDataSource: AccountRemoteDataSource

    @InjectMocks
    lateinit var repository: AccountRepository

    @Test
    fun testSendArchiveDownloadRequest() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.sendArchiveDownloadRequest(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Completable.complete()
        )

        repository.sendArchiveDownloadRequest(anyString(), anyString(), 0, 0).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()

    }

    @Test
    fun testSendArchiveDownloadRequestError() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.sendArchiveDownloadRequest(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Completable.error(NETWORK_ERROR)
        )

        repository.sendArchiveDownloadRequest(anyString(), anyString(), 0, 0).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterFbmServerAccount() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.registerFbmServerJwt(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Completable.complete()
        )

        whenever(
            remoteDataSource.registerFbmServerAccount(
                any()
            )
        ).thenReturn(
            Single.just(ACCOUNT_DATA)
        )

        repository.registerFbmServerAccount(anyString(), anyString(), anyString(), anyString())
            .subscribe(observer)

        observer.assertValue(ACCOUNT_DATA)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterFbmServerAccountError1() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.registerFbmServerJwt(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Completable.error(HTTP_ERROR)
        )

        whenever(
            remoteDataSource.registerFbmServerAccount(
                any()
            )
        ).thenReturn(
            Single.just(ACCOUNT_DATA)
        )

        repository.registerFbmServerAccount(anyString(), anyString(), anyString(), anyString())
            .subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterFbmServerAccountError2() {

        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.registerFbmServerJwt(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Completable.complete()
        )

        whenever(
            remoteDataSource.registerFbmServerAccount(
                any()
            )
        ).thenReturn(
            Single.error(NETWORK_ERROR)
        )

        repository.registerFbmServerAccount(anyString(), anyString(), anyString(), anyString())
            .subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterFbmServerJwt() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.registerFbmServerJwt(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Completable.complete()
        )

        repository.registerFbmServerJwt(anyString(), anyString(), anyString()).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterFbmServerJwtError() {
        val observer = TestObserver<Any>()

        whenever(
            remoteDataSource.registerFbmServerJwt(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Completable.error(HTTP_ERROR)
        )

        repository.registerFbmServerJwt(anyString(), anyString(), anyString()).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testCheckJwtExpired() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkJwtExpired()).thenReturn(Single.just(true))

        repository.checkJwtExpired().subscribe(observer)

        observer.assertValue(true)
        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertTerminated()
    }

    @Test
    fun testCheckJwtExpiredError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkJwtExpired()).thenReturn(Single.error(RANDOM_ERROR))

        repository.checkJwtExpired().subscribe(observer)

        observer.assertNoValues()
        observer.assertError(RANDOM_ERROR)
        observer.assertNotComplete()
        observer.assertTerminated()
    }

    @Test
    fun testSaveAccountData() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.complete())

        repository.saveAccountData(ACCOUNT_DATA).subscribe(observer)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertTerminated()
    }

    @Test
    fun testSaveAccountDataError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.error(RANDOM_ERROR))

        repository.saveAccountData(ACCOUNT_DATA).subscribe(observer)

        observer.assertNoValues()
        observer.assertError(RANDOM_ERROR)
        observer.assertNotComplete()
        observer.assertTerminated()
    }

    @Test
    fun testGetAccountData() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(Single.just(ACCOUNT_DATA))

        repository.getAccountData().subscribe(observer)

        observer.assertValue(ACCOUNT_DATA)
        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertTerminated()
    }

    @Test
    fun testGetAccountDataError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(Single.error(RANDOM_ERROR))

        repository.getAccountData().subscribe(observer)

        observer.assertNoValues()
        observer.assertError(RANDOM_ERROR)
        observer.assertNotComplete()
        observer.assertTerminated()
    }

    @Test
    fun testSyncAccountData1() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(Single.just(ACCOUNT_DATA))
        whenever(remoteDataSource.getAccountInfo()).thenReturn(Single.just(REMOTE_ACCOUNT_DATA))
        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.complete())

        repository.syncAccountData().subscribe(observer)

        observer.assertValue(ACCOUNT_DATA)
        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertTerminated()
    }

    @Test
    fun testSyncAccountData2() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(Single.just(EMPTY_ACCOUNT_DATA))
        whenever(remoteDataSource.getAccountInfo()).thenReturn(Single.just(REMOTE_ACCOUNT_DATA))
        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.complete())

        repository.syncAccountData().subscribe(observer)

        observer.assertValue(REMOTE_ACCOUNT_DATA)
        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertTerminated()
    }

    @Test
    fun testSyncAccountDataError1() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(Single.error(RANDOM_ERROR))

        repository.syncAccountData().subscribe(observer)

        observer.assertNoValues()
        observer.assertError(RANDOM_ERROR)
        observer.assertNotComplete()
        observer.assertTerminated()
    }

    @Test
    fun testSyncAccountDataError2() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(Single.just(EMPTY_ACCOUNT_DATA))
        whenever(remoteDataSource.getAccountInfo()).thenReturn(Single.error(HTTP_ERROR))

        repository.syncAccountData().subscribe(observer)

        observer.assertNoValues()
        observer.assertError(HTTP_ERROR)
        observer.assertNotComplete()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterIntercomUser() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.registerIntercomUser(any())).thenReturn(Completable.complete())

        repository.registerIntercomUser(anyString()).subscribe(observer)

        observer.assertNoValues()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterIntercomUserError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.registerIntercomUser(any())).thenReturn(
            Completable.error(
                RANDOM_ERROR
            )
        )

        repository.registerIntercomUser(anyString()).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSetArchiveRequestedAt() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.setArchiveRequestedAt(any())).thenReturn(Completable.complete())

        repository.setArchiveRequestedAt(123L).subscribe(observer)

        observer.assertNoValues()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testSetArchiveRequestedAtError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.setArchiveRequestedAt(any())).thenReturn(
            Completable.error(
                RANDOM_ERROR
            )
        )

        repository.setArchiveRequestedAt(123L).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testClearArchiveRequestedAt() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.clearArchiveRequestedAt()).thenReturn(Completable.complete())

        repository.clearArchiveRequestedAt().subscribe(observer)

        observer.assertNoValues()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testClearArchiveRequestedAtError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.clearArchiveRequestedAt()).thenReturn(
            Completable.error(
                RANDOM_ERROR
            )
        )

        repository.clearArchiveRequestedAt().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetArchiveRequestedAt() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getArchiveRequestedAt()).thenReturn(Single.just(123L))

        repository.getArchiveRequestedAt().subscribe(observer)

        observer.assertValue(123L)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testGetArchiveRequestedAtError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getArchiveRequestedAt()).thenReturn(Single.error(RANDOM_ERROR))

        repository.getArchiveRequestedAt().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testCheckFbCredentialExisting() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkFbCredentialExisting()).thenReturn(Single.just(false))

        repository.checkFbCredentialExisting().subscribe(observer)

        observer.assertValue(false)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testCheckFbCredentialExistingError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkFbCredentialExisting()).thenReturn(Single.error(RANDOM_ERROR))

        repository.checkFbCredentialExisting().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testCheckInvalidArchives() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getArchives()).thenReturn(Single.just(INVALID_ARCHIVE_DATA))

        repository.checkInvalidArchives().subscribe(observer)

        observer.assertValue(true)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testCheckInvalidArchivesError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getArchives()).thenReturn(Single.error(NETWORK_ERROR))

        repository.checkInvalidArchives().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testCheckArchiveProcessed1() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getArchives()).thenReturn(Single.just(PROCESSED_ARCHIVE_DATA))

        repository.checkArchiveProcessed().subscribe(observer)

        observer.assertValue(true)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testCheckArchiveProcessed2() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getArchives()).thenReturn(Single.just(INVALID_ARCHIVE_DATA))

        repository.checkArchiveProcessed().subscribe(observer)

        observer.assertValue(false)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testCheckArchiveProcessedError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getArchives()).thenReturn(Single.error(NETWORK_ERROR))

        repository.checkArchiveProcessed().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testListProcessedArchive() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getArchives()).thenReturn(Single.just(PROCESSED_ARCHIVE_DATA))

        repository.listProcessedArchive().subscribe(observer)

        observer.assertValue(PROCESSED_ARCHIVE_DATA)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testListProcessedArchiveError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getArchives()).thenReturn(Single.error(NETWORK_ERROR))

        repository.listProcessedArchive().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSaveAccountKeyData() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.complete())

        repository.saveAccountData(ACCOUNT_DATA).subscribe(observer)

        observer.assertNoValues()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testSaveAccountKeyDataError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.error(RANDOM_ERROR))

        repository.saveAccountData(ACCOUNT_DATA).subscribe(observer)

        observer.assertNoValues()
        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertTerminated()
    }

    @Test
    fun testSaveAdsPrefCategories() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveAdsPrefCategories(any())).thenReturn(Completable.complete())

        repository.saveAdsPrefCategories(listOf(anyString())).subscribe(observer)

        observer.assertNoValues()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testSaveAdsPrefCategoriesError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveAdsPrefCategories(any())).thenReturn(
            Completable.error(
                RANDOM_ERROR
            )
        )

        repository.saveAdsPrefCategories(listOf(anyString())).subscribe(observer)

        observer.assertNoValues()
        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertTerminated()
    }

    @Test
    fun testListAdsPrefCategory() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.listAdsPrefCategory()).thenReturn(Single.just(listOf("test")))

        repository.listAdsPrefCategory().subscribe(observer)

        observer.assertValue(listOf("test"))
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testListAdsPrefCategoryError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.listAdsPrefCategory()).thenReturn(Single.error(RANDOM_ERROR))

        repository.listAdsPrefCategory().subscribe(observer)

        observer.assertNoValues()
        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertTerminated()
    }

    @Test
    fun testCheckAdsPrefCategoryReady() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkAdsPrefCategoryReady()).thenReturn(Single.just(true))

        repository.checkAdsPrefCategoryReady().subscribe(observer)

        observer.assertValue(true)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testCheckAdsPrefCategoryReadyError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkAdsPrefCategoryReady()).thenReturn(Single.error(RANDOM_ERROR))

        repository.checkAdsPrefCategoryReady().subscribe(observer)

        observer.assertNoValues()
        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertTerminated()
    }

    @Test
    fun testUpdateMetadata() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.updateMetadata(any())).thenReturn(Single.just(REMOTE_ACCOUNT_DATA))
        whenever(localDataSource.getAccountData()).thenReturn(Single.just(ACCOUNT_DATA))
        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.complete())

        repository.updateMetadata(mapOf()).subscribe(observer)

        observer.assertValue(ACCOUNT_DATA)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testUpdateMetadataError1() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.updateMetadata(any())).thenReturn(Single.error(HTTP_ERROR))
        whenever(localDataSource.getAccountData()).thenReturn(Single.just(ACCOUNT_DATA))
        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.complete())

        repository.updateMetadata(mapOf()).subscribe(observer)

        observer.assertNoValues()
        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertTerminated()
    }

    @Test
    fun testUpdateMetadataError2() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.updateMetadata(any())).thenReturn(Single.error(HTTP_ERROR))
        whenever(localDataSource.getAccountData()).thenReturn(Single.error(RANDOM_ERROR))
        whenever(localDataSource.saveAccountData(any())).thenReturn(Completable.complete())

        repository.updateMetadata(mapOf()).subscribe(observer)

        observer.assertNoValues()
        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertTerminated()
    }

    @Test
    fun testGetLastActivityTimestamp() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(Single.just(ACCOUNT_DATA))

        repository.getLastActivityTimestamp().subscribe(observer)

        observer.assertValue(123L)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertTerminated()
    }

    @Test
    fun testGetLastActivityTimestampError1() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(Single.error(RANDOM_ERROR))

        repository.getLastActivityTimestamp().subscribe(observer)

        observer.assertNoValues()
        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertTerminated()
    }

    @Test
    fun testGetLastActivityTimestampError2() {

        val observer = TestObserver<Any>()

        whenever(localDataSource.getAccountData()).thenReturn(
            Single.just(
                ACCOUNT_DATA_NO_ACTIVITY_TIMESTAMP
            )
        )

        repository.getLastActivityTimestamp().subscribe(observer)

        observer.assertNoValues()
        observer.assertNotComplete()
        observer.assertTerminated()
        observer.assertErrorMessage("do not contains last_activity_timestamp")
    }

}