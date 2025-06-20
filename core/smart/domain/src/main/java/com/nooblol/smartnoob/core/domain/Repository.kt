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
package com.nooblol.smartnoob.core.domain

import android.graphics.Bitmap
import android.util.Log

import com.nooblol.smartnoob.core.base.extensions.mapList
import com.nooblol.smartnoob.core.base.identifier.Identifier
import com.nooblol.smartnoob.core.bitmaps.BitmapRepository
import com.nooblol.smartnoob.core.bitmaps.CONDITION_FILE_PREFIX
import com.nooblol.smartnoob.core.bitmaps.TUTORIAL_CONDITION_FILE_PREFIX
import com.nooblol.smartnoob.core.database.ClickDatabase
import com.nooblol.smartnoob.core.database.TutorialDatabase
import com.nooblol.smartnoob.core.database.entity.CompleteScenario
import com.nooblol.smartnoob.core.domain.data.ScenarioDataSource
import com.nooblol.smartnoob.core.domain.model.action.Action
import com.nooblol.smartnoob.core.domain.model.action.toDomain
import com.nooblol.smartnoob.core.domain.model.condition.Condition
import com.nooblol.smartnoob.core.domain.model.condition.ImageCondition
import com.nooblol.smartnoob.core.domain.model.condition.toDomain
import com.nooblol.smartnoob.core.domain.model.event.Event
import com.nooblol.smartnoob.core.domain.model.event.ImageEvent
import com.nooblol.smartnoob.core.domain.model.event.TriggerEvent
import com.nooblol.smartnoob.core.domain.model.event.toDomainImageEvent
import com.nooblol.smartnoob.core.domain.model.event.toDomainTriggerEvent
import com.nooblol.smartnoob.core.domain.model.scenario.Scenario
import com.nooblol.smartnoob.core.domain.model.scenario.toDomain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for the database and bitmap manager.
 * Provide the access to the scenario, events, actions and conditions from the database and the conditions bitmap from
 * the application data folder.
 *
 * @param database the database containing the list of scenario.
 * @param bitmapManager save and loads the bitmap for the conditions.
 */
internal class Repository @Inject internal constructor(
    private val database: ClickDatabase,
    private val tutorialDatabase: TutorialDatabase,
    private val bitmapManager: BitmapRepository,
): IRepository {

    private val dataSource: ScenarioDataSource = ScenarioDataSource(database, bitmapManager)


    override val scenarios: Flow<List<Scenario>> =
        dataSource.scenarios.mapList { it.toDomain() }

    override val allImageEvents: Flow<List<ImageEvent>> =
        dataSource.allImageEvents.mapList { it.toDomainImageEvent() }

    override val allTriggerEvents: Flow<List<TriggerEvent>> =
        dataSource.allTriggerEvents.mapList { it.toDomainTriggerEvent() }

    override val allConditions: Flow<List<Condition>> =
        dataSource.getAllConditions().mapList { it.toDomain() }

    override val allActions: Flow<List<Action>> =
        dataSource.getAllActions().mapList { it.toDomain() }

    override suspend fun getScenario(scenarioId: Long): Scenario? =
        dataSource.getScenario(scenarioId)?.toDomain()

    override fun getScenarioFlow(scenarioId: Long): Flow<Scenario?> =
        dataSource.getScenarioFlow(scenarioId).map { it?.toDomain() }

    override fun getEventsFlow(scenarioId: Long): Flow<List<Event>> =
        getImageEventsFlow(scenarioId).combine(getTriggerEventsFlow(scenarioId)) { imgEvts, trigEvts ->
            buildList {
                addAll(imgEvts)
                addAll(trigEvts)
            }
        }

    override suspend fun getImageEvents(scenarioId: Long): List<ImageEvent> =
        dataSource.getImageEvents(scenarioId).map { it.toDomainImageEvent() }

    override fun getImageEventsFlow(scenarioId: Long): Flow<List<ImageEvent>> =
        dataSource.getImageEventsFlow(scenarioId).mapList { it.toDomainImageEvent() }

    override suspend fun getTriggerEvents(scenarioId: Long): List<TriggerEvent> =
        dataSource.getTriggerEvents(scenarioId).map { it.toDomainTriggerEvent() }

    override fun getTriggerEventsFlow(scenarioId: Long): Flow<List<TriggerEvent>> =
        dataSource.getTriggerEventsFlow(scenarioId).mapList { it.toDomainTriggerEvent() }

    override suspend fun addScenario(scenario: Scenario): Long =
        dataSource.addScenario(scenario)

    override suspend fun deleteScenario(scenarioId: Identifier): Unit =
        dataSource.deleteScenario(scenarioId)

    override suspend fun markAsUsed(scenarioId: Identifier) {
        dataSource.markAsUsed(scenarioId.databaseId)
    }

    override suspend fun addScenarioCopy(completeScenario: CompleteScenario): Long? {
        val (scenario, events) = completeScenario.toDomain(cleanIds = true)
        return dataSource.addCompleteScenario(scenario, events)
    }

    override suspend fun addScenarioCopy(scenarioId: Long, copyName: String): Long? {
        val (scenario, events) = dataSource.getCompleteScenario(scenarioId)
            ?.toDomain(cleanIds = true) ?: return null
        return dataSource.addCompleteScenario(scenario.copy(name = copyName), events)
    }

    override suspend fun updateScenario(scenario: Scenario, events: List<Event>): Boolean =
        dataSource.updateScenario(scenario, events)

    override suspend fun saveConditionBitmap(bitmap: Bitmap): String {
        return bitmapManager.saveImageConditionBitmap(
            bitmap,
            if (dataSource.currentDatabase.value == tutorialDatabase) TUTORIAL_CONDITION_FILE_PREFIX
            else CONDITION_FILE_PREFIX,
        )
    }

    override suspend fun getConditionBitmap(condition: ImageCondition): Bitmap? =
        bitmapManager.getImageConditionBitmap(condition.path, condition.area.width(), condition.area.height())

    override suspend fun cleanupUnusedBitmaps(removedPath: List<String>) {
        dataSource.clearRemovedConditionsBitmaps(removedPath)
    }

    override fun startTutorialMode() {
        Log.d(TAG, "Start tutorial mode, use tutorial database")
        dataSource.currentDatabase.value = tutorialDatabase
    }

    override fun stopTutorialMode() {
        Log.d(TAG, "Stop tutorial mode, use regular database")
        dataSource.currentDatabase.value = database
    }

    override fun isTutorialModeEnabled(): Boolean =
        dataSource.currentDatabase.value == tutorialDatabase

    override fun isTutorialModeEnabledFlow(): Flow<Boolean> =
        dataSource.currentDatabase.map { it == tutorialDatabase }
}

/** Tag for logs. */
private const val TAG = "RepositoryImpl"