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
package com.nooblol.smartnoob.core.processing.domain.trying

import com.nooblol.smartnoob.core.domain.model.event.ImageEvent
import com.nooblol.smartnoob.core.processing.domain.IConditionsResult
import com.nooblol.smartnoob.core.processing.domain.ScenarioProcessingListener

internal class ImageEventProcessingTryListener(
    private val triedItem: ImageEventTry,
    private val clientListener: (IConditionsResult) -> Unit,
) : ScenarioProcessingListener {

    override suspend fun onImageEventProcessingCompleted(event: ImageEvent, results: IConditionsResult) {
        if (event.id != triedItem.event.id) return
        clientListener(results)
    }
}