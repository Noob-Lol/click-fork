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
package com.nooblol.smartnoob.core.processing.tests.processor

import com.nooblol.smartnoob.core.detection.DetectionResult
import com.nooblol.smartnoob.core.detection.ImageDetector
import com.nooblol.smartnoob.core.domain.model.EXACT
import com.nooblol.smartnoob.core.domain.model.IN_AREA
import com.nooblol.smartnoob.core.domain.model.WHOLE_SCREEN
import com.nooblol.smartnoob.core.domain.model.event.ImageEvent
import com.nooblol.smartnoob.core.domain.model.event.TriggerEvent
import com.nooblol.smartnoob.core.processing.data.processor.ConditionsResult
import com.nooblol.smartnoob.core.processing.domain.ScenarioProcessingListener
import com.nooblol.smartnoob.core.processing.tests.processor.ProcessingTests.BitmapSupplier
import com.nooblol.smartnoob.core.processing.utils.anyNotNull
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify


internal suspend fun BitmapSupplier.mockBitmapProviding(testCondition: TestImageCondition) {
    `when`(getBitmap(testCondition.imageCondition)).thenReturn(testCondition.mockedBitmap)
}

internal fun ImageDetector.mockAllDetectionResult(testConditions: List<TestImageCondition>, areAllDetected: Boolean) {
    testConditions.forEach { testCondition ->
        mockDetectionResult(testCondition, areAllDetected)
    }
}

internal fun ImageDetector.mockDetectionResult(testCondition: TestImageCondition, isDetected: Boolean) {
    when (testCondition.imageCondition.detectionType) {
        EXACT -> `when`(
            detectCondition(
                testCondition.mockedBitmap,
                testCondition.imageCondition.area,
                testCondition.imageCondition.threshold,
            )
        ).thenReturn(DetectionResult(isDetected))

        WHOLE_SCREEN -> `when`(
            detectCondition(
                testCondition.mockedBitmap,
                testCondition.imageCondition.threshold,
            )
        ).thenReturn(DetectionResult(isDetected))

        IN_AREA -> `when`(
            detectCondition(
                testCondition.mockedBitmap,
                testCondition.imageCondition.detectionArea!!,
                testCondition.imageCondition.threshold,
            )
        ).thenReturn(DetectionResult(isDetected))
    }
}

internal suspend fun ScenarioProcessingListener.verifyImageConditionProcessed(
    condition: TestImageCondition,
    detected: Boolean,
    processedCount: Int = 1,
): Unit = verify(this, times(processedCount))
    .onImageConditionProcessingCompleted(condition.expectedResult(detected))

internal suspend fun ScenarioProcessingListener.monitorImageEventProcessing(
    events: List<ImageEvent>,
): List<Boolean> {
    val results = mutableListOf<Boolean>()

    events.forEach { event ->
        `when`(onImageEventProcessingCompleted(eq(event), anyNotNull())).doAnswer { invocationOnMock ->
            results.add((invocationOnMock.arguments[1] as ConditionsResult).fulfilled == true)
            Unit
        }
    }

    return results
}

internal fun ImageDetector.verifyConditionNeverProcessed(testCondition: TestImageCondition) {
    when (testCondition.imageCondition.detectionType) {
        EXACT -> verify(this, never())
            .detectCondition(
                testCondition.mockedBitmap,
                testCondition.imageCondition.area,
                testCondition.imageCondition.threshold,
            )

        WHOLE_SCREEN -> verify(this, never())
            .detectCondition(
                testCondition.mockedBitmap,
                testCondition.imageCondition.threshold,
            )

        IN_AREA -> verify(this, never())
            .detectCondition(
                testCondition.mockedBitmap,
                testCondition.imageCondition.detectionArea!!,
                testCondition.imageCondition.threshold,
            )
    }
}

internal suspend fun ScenarioProcessingListener.verifyTriggerEventProcessed(
    event: TriggerEvent,
    expectedResult: Boolean,
    processedCount: Int = 1,
): Unit = verify(this, times(processedCount))
    .onTriggerEventProcessingCompleted(event, event.expectedResult(expectedResult))

internal suspend fun ScenarioProcessingListener.verifyTriggerEventNotProcessed(event: TriggerEvent) {
    verify(this, never()).onTriggerEventProcessingCompleted(event, event.expectedResult(true))
    verify(this, never()).onTriggerEventProcessingCompleted(event, event.expectedResult(false))
}