/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.di

import com.bitmark.fbm.feature.archiveuploading.service.UploadArchiveService
import com.bitmark.fbm.feature.archiveuploading.service.UploadArchiveModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector(modules = [UploadArchiveModule::class])
    @ServiceScope
    internal abstract fun bindUploadArchiveService(): UploadArchiveService
}