/*
 * Copyright (C) 2024 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.nooblol.smartnoob.feature.smart.config.di

import com.nooblol.smartnoob.core.common.overlays.di.OverlayComponent
import com.nooblol.smartnoob.feature.smart.config.ui.MainMenuModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.selection.ActionTypeSelectionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.brief.SmartActionsBriefViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.changecounter.ChangeCounterViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.click.ClickOffsetViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.click.ClickViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.copy.ActionCopyModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.intent.IntentViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.intent.activities.ActivitySelectionModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.intent.component.ComponentSelectionModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.intent.extras.ExtraConfigModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.intent.flags.FlagsSelectionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.notification.NotificationViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.pause.PauseViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.settext.SetTextViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.swipe.SwipeViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.system.SystemActionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.toggleevent.EventTogglesViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.action.toggleevent.ToggleEventViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.counter.CounterNameSelectionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.intent.IntentActionsSelectionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.copy.ConditionCopyModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.image.CaptureViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.image.ImageConditionAreaSelectorViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.image.ImageConditionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.image.brief.ImageConditionsBriefViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.trigger.TriggerConditionListViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.trigger.broadcast.BroadcastReceivedConditionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.trigger.counter.CounterReachedConditionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.condition.trigger.timer.TimerReachedConditionViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.event.EventDialogViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.event.copy.EventCopyModel
import com.nooblol.smartnoob.feature.smart.config.ui.scenario.ScenarioDialogViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.scenario.config.ScenarioConfigViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.scenario.imageevents.ImageEventListViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.scenario.more.MoreViewModel
import com.nooblol.smartnoob.feature.smart.config.ui.scenario.triggerevents.TriggerEventListViewModel

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@EntryPoint
@InstallIn(OverlayComponent::class)
interface ScenarioConfigViewModelsEntryPoint {

    fun actionCopyViewModel(): ActionCopyModel
    fun actionTypeSelectionViewModel(): ActionTypeSelectionViewModel
    fun activitySelectionViewModel(): ActivitySelectionModel
    fun broadcastReceivedConditionViewModel(): BroadcastReceivedConditionViewModel
    fun captureViewModel(): CaptureViewModel
    fun changeCounterViewModel(): ChangeCounterViewModel
    fun clickOffsetViewModel(): ClickOffsetViewModel
    fun clickViewModel(): ClickViewModel
    fun componentSelectionViewModel(): ComponentSelectionModel
    fun conditionCopyViewModel(): ConditionCopyModel
    fun counterNameSelectionViewModel(): CounterNameSelectionViewModel
    fun counterReachedConditionViewModel(): CounterReachedConditionViewModel
    fun eventCopyModel(): EventCopyModel
    fun eventDialogViewModel(): EventDialogViewModel
    fun eventTogglesViewModel(): EventTogglesViewModel
    fun extraConfigViewModel(): ExtraConfigModel
    fun flagsSelectionViewModel(): FlagsSelectionViewModel
    fun imageConditionAreaSelectorViewModel(): ImageConditionAreaSelectorViewModel
    fun imageConditionsBriefViewModel(): ImageConditionsBriefViewModel
    fun imageConditionViewModel(): ImageConditionViewModel
    fun imageEventListViewModel(): ImageEventListViewModel
    fun intentActionsSelectionViewModel(): IntentActionsSelectionViewModel
    fun intentViewModel(): IntentViewModel
    fun mainMenuViewModel(): MainMenuModel
    fun moreViewModel(): MoreViewModel
    fun notificationViewModel(): NotificationViewModel
    fun pauseViewModel(): PauseViewModel
    fun scenarioConfigViewModel(): ScenarioConfigViewModel
    fun scenarioDialogViewModel(): ScenarioDialogViewModel
    fun setTextViewModel(): SetTextViewModel
    fun systemActionViewModel(): SystemActionViewModel
    fun smartActionsBriefViewModel(): SmartActionsBriefViewModel
    fun swipeViewModel(): SwipeViewModel
    fun timerReachedConditionViewModel(): TimerReachedConditionViewModel
    fun toggleEventViewModel(): ToggleEventViewModel
    fun triggerConditionsViewModel(): TriggerConditionListViewModel
    fun triggerEventListViewModel(): TriggerEventListViewModel
}