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
package com.NoobLol.smartnoob.feature.smart.config.ui.scenario.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.NoobLol.smartnoob.feature.smart.config.domain.EditionRepository
import com.NoobLol.smartnoob.core.processing.domain.DETECTION_QUALITY_MAX
import com.NoobLol.smartnoob.core.processing.domain.DETECTION_QUALITY_MIN

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlin.math.max
import kotlin.math.min

/** View model for the [ScenarioConfigContent]. */
class ScenarioConfigViewModel @Inject constructor(
    private val editionRepository: EditionRepository,
) : ViewModel() {

    /** Currently configured scenario. */
    private val configuredScenario = editionRepository.editionState.scenarioState
        .mapNotNull { it.value }

    /** The event name value currently edited by the user. */
    val scenarioName: Flow<String> = configuredScenario
        .map { it.name }
        .filterNotNull()
        .take(1)
    /** Tells if the scenario name is valid or not. */
    val scenarioNameError: Flow<Boolean> = configuredScenario
        .map { it.name.isEmpty() }

    /** The randomization value for the scenario. */
    val randomization: Flow<Boolean> = configuredScenario
        .map { it.randomize }

    /** The quality of the detection. */
    val detectionQuality: Flow<Int?> = configuredScenario
        .map { it.detectionQuality }

    /** Set a new name for the scenario. */
    fun setScenarioName(name: String) {
        editionRepository.editionState.getScenario()?.let { scenario ->
            viewModelScope.launch {
                editionRepository.updateEditedScenario(scenario.copy(name = name))
            }
        }
    }

    /** Toggle the randomization value. */
    fun toggleRandomization() {
        editionRepository.editionState.getScenario()?.let { scenario ->
            viewModelScope.launch {
                editionRepository.updateEditedScenario(scenario.copy(randomize = !scenario.randomize))
            }
        }
    }

    /** Remove one to the detection quality */
    fun decreaseDetectionQuality() {
        editionRepository.editionState.getScenario()?.let { scenario ->
            viewModelScope.launch {
                editionRepository.updateEditedScenario(
                    scenario.copy(
                        detectionQuality = max(scenario.detectionQuality - 1, DETECTION_QUALITY_MIN.toInt())
                    )
                )
            }
        }
    }

    /** Add one to the detection quality */
    fun increaseDetectionQuality() {
        editionRepository.editionState.getScenario()?.let { scenario ->
            viewModelScope.launch {
                editionRepository.updateEditedScenario(
                    scenario.copy(
                        detectionQuality = min(scenario.detectionQuality + 1, DETECTION_QUALITY_MAX.toInt())
                    )
                )
            }
        }
    }

    /**
     * Set the detection quality for the scenario.
     * @param quality the value from the seekbar.
     */
    fun setDetectionQuality(quality: Int) {
        editionRepository.editionState.getScenario()?.let { scenario ->
            viewModelScope.launch {
                editionRepository.updateEditedScenario(scenario.copy(detectionQuality = quality))
            }
        }
    }
}

/** The minimum value for the seek bar. */
const val SLIDER_QUALITY_MIN = DETECTION_QUALITY_MIN.toFloat()
/** The maximum value for the seek bar. */
const val SLIDER_QUALITY_MAX = DETECTION_QUALITY_MAX.toFloat()