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
package com.nooblol.smartnoob.core.processing.domain

import android.content.Context

import com.nooblol.smartnoob.core.domain.model.condition.ImageCondition
import com.nooblol.smartnoob.core.domain.model.event.ImageEvent
import com.nooblol.smartnoob.core.domain.model.event.TriggerEvent
import com.nooblol.smartnoob.core.domain.model.scenario.Scenario

interface ScenarioProcessingListener {

    suspend fun onSessionStarted(
        context: Context,
        scenario: Scenario,
        imageEvents: List<ImageEvent>,
        triggerEvents: List<TriggerEvent>,
    ) = Unit

    suspend fun onTriggerEventProcessingStarted(event: TriggerEvent) = Unit
    suspend fun onTriggerEventProcessingCompleted(event: TriggerEvent, results: List<ConditionResult>) = Unit

    suspend fun onImageEventsProcessingStarted() = Unit

    suspend fun onImageEventProcessingStarted(event: ImageEvent) = Unit

    suspend fun onImageConditionProcessingStarted(condition: ImageCondition) = Unit
    suspend fun onImageConditionProcessingCompleted(result: ConditionResult) = Unit
    suspend fun onImageConditionProcessingCancelled() = Unit

    suspend fun onImageEventProcessingCompleted(event: ImageEvent, results: IConditionsResult) = Unit
    suspend fun onImageEventProcessingCancelled() = Unit

    suspend fun onImageEventsProcessingCompleted() = Unit

    suspend fun onSessionEnded() = Unit
}