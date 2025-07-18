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
package com.nooblol.smartnoob.core.processing.tests

import android.os.Build

import androidx.test.ext.junit.runners.AndroidJUnit4

import com.nooblol.smartnoob.core.processing.data.processor.state.EventsState
import com.nooblol.smartnoob.core.processing.utils.ProcessingData

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.annotation.Config

/** Test the [EventsState] class. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class ScenarioStateTests {

    @Test
    fun all_events_enabled_on_start() {
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = true),
            ProcessingData.newEvent(id = 2L, enableOnStart = true),
            ProcessingData.newEvent(id = 3L, enableOnStart = true),
        )

        val scenarioState = EventsState(eventList, emptyList())

        Assert.assertEquals("Invalid enabled events count", eventList.size, scenarioState.getEnabledImageEvents().size)
    }

    @Test
    fun all_events_disabled_on_start() {
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = false),
            ProcessingData.newEvent(id = 2L, enableOnStart = false),
            ProcessingData.newEvent(id = 3L, enableOnStart = false),
        )

        val scenarioState = EventsState(eventList, emptyList())

        Assert.assertEquals("Invalid enabled events count", 0, scenarioState.getEnabledImageEvents().size)
    }

    @Test
    fun mixed_events_enabled_disabled_on_start() {
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = false),
            ProcessingData.newEvent(id = 2L, enableOnStart = true),
            ProcessingData.newEvent(id = 3L, enableOnStart = false),
        )

        val scenarioState = EventsState(eventList, emptyList())

        Assert.assertEquals("Invalid enabled events count", 1, scenarioState.getEnabledImageEvents().size)
    }

    @Test
    fun change_state_enabled_from_disabled() {
        val changingEvent = ProcessingData.newEvent(id = 2L, enableOnStart = false)
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = false),
            changingEvent,
            ProcessingData.newEvent(id = 3L, enableOnStart = false),
        )

        val scenarioState = EventsState(eventList, emptyList()).apply {
            enableEvent(changingEvent.getDatabaseId())
        }

        Assert.assertTrue("Event not enabled", scenarioState.getEnabledImageEvents().contains(changingEvent))
    }

    @Test
    fun change_state_enabled_from_enabled() {
        val changingEvent = ProcessingData.newEvent(id = 2L, enableOnStart = true)
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = false),
            changingEvent,
            ProcessingData.newEvent(id = 3L, enableOnStart = false),
        )

        val scenarioState = EventsState(eventList, emptyList()).apply {
            enableEvent(changingEvent.getDatabaseId())
        }

        Assert.assertTrue("Event not enabled", scenarioState.getEnabledImageEvents().contains(changingEvent))
    }

    @Test
    fun change_state_disabled_from_enabled() {
        val changingEvent = ProcessingData.newEvent(id = 2L, enableOnStart = true)
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = false),
            changingEvent,
            ProcessingData.newEvent(id = 3L, enableOnStart = false),
        )

        val scenarioState = EventsState(eventList, emptyList()).apply {
            disableEvent(changingEvent.getDatabaseId())
        }

        Assert.assertFalse("Event should not be enabled", scenarioState.getEnabledImageEvents().contains(changingEvent))
    }

    @Test
    fun change_state_disabled_from_disabled() {
        val changingEvent = ProcessingData.newEvent(id = 2L, enableOnStart = false)
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = false),
            changingEvent,
            ProcessingData.newEvent(id = 3L, enableOnStart = false),
        )

        val scenarioState = EventsState(eventList, emptyList()).apply {
            disableEvent(changingEvent.getDatabaseId())
        }

        Assert.assertFalse("Event should not be enabled", scenarioState.getEnabledImageEvents().contains(changingEvent))
    }

    @Test
    fun change_state_toggle_from_enabled() {
        val changingEvent = ProcessingData.newEvent(id = 2L, enableOnStart = true)
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = false),
            changingEvent,
            ProcessingData.newEvent(id = 3L, enableOnStart = false),
        )

        val scenarioState = EventsState(eventList, emptyList()).apply {
            toggleEvent(changingEvent.getDatabaseId())
        }

        Assert.assertFalse("Event should not be enabled", scenarioState.getEnabledImageEvents().contains(changingEvent))
    }

    @Test
    fun change_state_toggle_from_disabled() {
        val changingEvent = ProcessingData.newEvent(id = 2L, enableOnStart = false)
        val eventList = listOf(
            ProcessingData.newEvent(id = 1L, enableOnStart = false),
            changingEvent,
            ProcessingData.newEvent(id = 3L, enableOnStart = false),
        )

        val scenarioState = EventsState(eventList, emptyList()).apply {
            toggleEvent(changingEvent.getDatabaseId())
        }

        Assert.assertTrue("Event not enabled", scenarioState.getEnabledImageEvents().contains(changingEvent))
    }
}