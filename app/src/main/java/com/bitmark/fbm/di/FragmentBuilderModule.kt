/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.di

import com.bitmark.fbm.feature.insights.InsightsContainerFragment
import com.bitmark.fbm.feature.insights.InsightsContainerModule
import com.bitmark.fbm.feature.insights.InsightsFragment
import com.bitmark.fbm.feature.insights.InsightsModule
import com.bitmark.fbm.feature.postdetail.PostDetailFragment
import com.bitmark.fbm.feature.postdetail.PostDetailModule
import com.bitmark.fbm.feature.reactiondetail.ReactionDetailFragment
import com.bitmark.fbm.feature.reactiondetail.ReactionDetailModule
import com.bitmark.fbm.feature.recovery.access.RecoveryAccessFragment
import com.bitmark.fbm.feature.recovery.access.RecoveryAccessModule
import com.bitmark.fbm.feature.recovery.notice.RecoveryNoticeFragment
import com.bitmark.fbm.feature.recovery.notice.RecoveryNoticeModule
import com.bitmark.fbm.feature.register.archiverequest.archiverequest.ArchiveRequestFragment
import com.bitmark.fbm.feature.register.archiverequest.archiverequest.ArchiveRequestModule
import com.bitmark.fbm.feature.settings.SettingsContainerFragment
import com.bitmark.fbm.feature.settings.SettingsContainerModule
import com.bitmark.fbm.feature.settings.SettingsFragment
import com.bitmark.fbm.feature.settings.SettingsModule
import com.bitmark.fbm.feature.statistic.StatisticFragment
import com.bitmark.fbm.feature.statistic.StatisticModule
import com.bitmark.fbm.feature.unlink.notice.UnlinkNoticeFragment
import com.bitmark.fbm.feature.unlink.notice.UnlinkNoticeModule
import com.bitmark.fbm.feature.unlink.unlink.UnlinkFragment
import com.bitmark.fbm.feature.unlink.unlink.UnlinkModule
import com.bitmark.fbm.feature.usage.UsageContainerFragment
import com.bitmark.fbm.feature.usage.UsageContainerModule
import com.bitmark.fbm.feature.usage.UsageFragment
import com.bitmark.fbm.feature.usage.UsageModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector(modules = [UsageContainerModule::class])
    @FragmentScope
    internal abstract fun bindUsageContainerFragment(): UsageContainerFragment

    @ContributesAndroidInjector(modules = [UsageModule::class])
    @FragmentScope
    internal abstract fun bindUsageFragment(): UsageFragment

    @ContributesAndroidInjector(modules = [InsightsContainerModule::class])
    @FragmentScope
    internal abstract fun bindInsightsContainerFragment(): InsightsContainerFragment

    @ContributesAndroidInjector(modules = [InsightsModule::class])
    @FragmentScope
    internal abstract fun bindInsightsFragment(): InsightsFragment

    @ContributesAndroidInjector(modules = [UnlinkNoticeModule::class])
    @FragmentScope
    internal abstract fun bindUnlinkNoticeFragment(): UnlinkNoticeFragment

    @ContributesAndroidInjector(modules = [UnlinkModule::class])
    @FragmentScope
    internal abstract fun bindUnlinkFragment(): UnlinkFragment

    @ContributesAndroidInjector(modules = [RecoveryAccessModule::class])
    @FragmentScope
    internal abstract fun bindRecoveryAccessFragment(): RecoveryAccessFragment

    @ContributesAndroidInjector(modules = [RecoveryNoticeModule::class])
    @FragmentScope
    internal abstract fun bindRecoveryNoticeFragment(): RecoveryNoticeFragment

    @ContributesAndroidInjector(modules = [ArchiveRequestModule::class])
    @FragmentScope
    internal abstract fun bindArchiveRequestFragment(): ArchiveRequestFragment

    @ContributesAndroidInjector(modules = [StatisticModule::class])
    @FragmentScope
    internal abstract fun bindStatisticFragment(): StatisticFragment

    @ContributesAndroidInjector(modules = [PostDetailModule::class])
    @FragmentScope
    internal abstract fun bindPostDetailFragment(): PostDetailFragment

    @ContributesAndroidInjector(modules = [ReactionDetailModule::class])
    @FragmentScope
    internal abstract fun bindReactionDetailFragment(): ReactionDetailFragment

    @ContributesAndroidInjector(modules = [SettingsModule::class])
    @FragmentScope
    internal abstract fun bindSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector(modules = [SettingsContainerModule::class])
    @FragmentScope
    internal abstract fun bindSettingsContainerFragment(): SettingsContainerFragment

}