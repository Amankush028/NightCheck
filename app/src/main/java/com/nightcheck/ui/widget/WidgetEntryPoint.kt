package com.nightcheck.ui.widget

import com.nightcheck.billing.PremiumCache
import com.nightcheck.domain.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint used by both MarkTaskCompleteAction and the gated widgets.
 * Replaces the one in MarkTaskCompleteAction.kt — delete that interface and
 * use this file as the single source for widget entry points.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun taskRepository(): TaskRepository
    fun premiumCache(): PremiumCache
}