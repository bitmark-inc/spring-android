/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.data.repo

import com.bitmark.fbm.data.source.AppRepository
import com.bitmark.fbm.data.source.local.AppLocalDataSource
import com.bitmark.fbm.data.source.remote.AppRemoteDataSource
import com.bitmark.fbm.ut.data.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock

class AppRepositoryTest : DataTest() {

    @Mock
    lateinit var localDataSource: AppLocalDataSource

    @Mock
    lateinit var remoteDataSource: AppRemoteDataSource

    @InjectMocks
    lateinit var repository: AppRepository

    @Test
    fun testRegisterNotificationService() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.registerNotificationService(any())).thenReturn(Completable.complete())

        repository.registerNotificationService(anyString()).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testRegisterNotificationServiceError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.registerNotificationService(any())).thenReturn(
            Completable.error(
                RANDOM_ERROR
            )
        )

        repository.registerNotificationService(anyString()).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSetNotificationEnabled() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.setNotificationEnabled(any())).thenReturn(Completable.complete())

        repository.setNotificationEnabled(true).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSetNotificationEnabledError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.setNotificationEnabled(any())).thenReturn(
            Completable.error(
                RANDOM_ERROR
            )
        )

        repository.setNotificationEnabled(true).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testCheckNotificationEnabled() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkNotificationEnabled()).thenReturn(Single.just(false))

        repository.checkNotificationEnabled().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(false)
        observer.assertTerminated()
    }

    @Test
    fun testCheckNotificationEnabledError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkNotificationEnabled()).thenReturn(Single.error(RANDOM_ERROR))

        repository.checkNotificationEnabled().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testCheckDataReady() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkDataReady()).thenReturn(Single.just(true))

        repository.checkDataReady().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(true)
        observer.assertTerminated()
    }

    @Test
    fun testCheckDataReadyError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.checkDataReady()).thenReturn(Single.error(RANDOM_ERROR))

        repository.checkDataReady().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSetDataReady() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.setDataReady()).thenReturn(Completable.complete())

        repository.setDataReady().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSetDataReadyError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.setDataReady()).thenReturn(Completable.error(RANDOM_ERROR))

        repository.setDataReady().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetAutomationScript() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getAutomationScript()).thenReturn(
            Single.just(
                AUTOMATION_SCRIPT_DATA
            )
        )

        repository.getAutomationScript().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(AUTOMATION_SCRIPT_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testGetAutomationScriptError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getAutomationScript()).thenReturn(
            Single.error(HTTP_ERROR)
        )

        repository.getAutomationScript().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetUpdateAppUrl() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getAppInfo()).thenReturn(
            Single.just(
                APP_INFO_DATA
            )
        )

        repository.getUpdateAppUrl().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue("https://android.com")
        observer.assertTerminated()
    }

    @Test
    fun testGetUpdateAppUrlError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getAppInfo()).thenReturn(
            Single.error(
                NETWORK_ERROR
            )
        )

        repository.getUpdateAppUrl().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(NETWORK_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetAppInfo() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getAppInfo()).thenReturn(
            Single.just(
                APP_INFO_DATA
            )
        )

        repository.getAppInfo().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(APP_INFO_DATA)
        observer.assertTerminated()
    }

    @Test
    fun testGetAppInfoError() {
        val observer = TestObserver<Any>()

        whenever(remoteDataSource.getAppInfo()).thenReturn(
            Single.error(
                HTTP_ERROR
            )
        )

        repository.getAppInfo().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(HTTP_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testDeleteAppData() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.deleteDb()).thenReturn(Completable.complete())
        whenever(localDataSource.deleteSharePref()).thenReturn(Completable.complete())
        whenever(localDataSource.deleteFileStorage()).thenReturn(Completable.complete())

        repository.deleteAppData().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testDeleteAppDataError1() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.deleteDb()).thenReturn(Completable.error(RANDOM_ERROR))
        whenever(localDataSource.deleteSharePref()).thenReturn(Completable.complete())
        whenever(localDataSource.deleteFileStorage()).thenReturn(Completable.complete())

        repository.deleteAppData().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testDeleteAppDataError2() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.deleteDb()).thenReturn(Completable.error(RANDOM_ERROR))
        whenever(localDataSource.deleteSharePref()).thenReturn(Completable.error(RANDOM_ERROR))
        whenever(localDataSource.deleteFileStorage()).thenReturn(Completable.error(RANDOM_ERROR))

        repository.deleteAppData().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testGetLastVersionCode() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getLastVersionCode()).thenReturn(Single.just(1))

        repository.getLastVersionCode().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(1)
        observer.assertTerminated()
    }

    @Test
    fun testGetLastVersionCodeError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.getLastVersionCode()).thenReturn(Single.error(RANDOM_ERROR))

        repository.getLastVersionCode().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSaveLastVersionCode() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveLastVersionCode(any())).thenReturn(Completable.complete())

        repository.saveLastVersionCode(1).subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSaveLastVersionCodeError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveLastVersionCode(any())).thenReturn(
            Completable.error(
                RANDOM_ERROR
            )
        )

        repository.saveLastVersionCode(1).subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testListLinkClicked() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.listLinkClicked()).thenReturn(Single.just(listOf("test.abc")))

        repository.listLinkClicked().subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue(listOf("test.abc"))
        observer.assertTerminated()
    }

    @Test
    fun testListLinkClickedError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.listLinkClicked()).thenReturn(Single.error(RANDOM_ERROR))

        repository.listLinkClicked().subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSaveLinkClicked() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveLinkClicked(any())).thenReturn(Completable.complete())

        repository.saveLinkClicked("test.abc").subscribe(observer)

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertNoValues()
        observer.assertTerminated()
    }

    @Test
    fun testSaveLinkClickedError() {
        val observer = TestObserver<Any>()

        whenever(localDataSource.saveLinkClicked(any())).thenReturn(Completable.error(RANDOM_ERROR))

        repository.saveLinkClicked("test.abc").subscribe(observer)

        observer.assertNotComplete()
        observer.assertError(RANDOM_ERROR)
        observer.assertNoValues()
        observer.assertTerminated()
    }

}