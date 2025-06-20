/*
 * Copyright (C) 2023 Kevin Buzeau
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
package com.nooblol.smartnoob.core.base

import android.accessibilityservice.GestureDescription

/** Execute the actions related to Android. */
interface AndroidExecutor {

    /** Execute the provided gesture. */
    suspend fun executeGesture(gestureDescription: GestureDescription)
}

/** The maximum supported duration for a gesture. This limitation comes from Android GestureStroke API.  */
const val GESTURE_DURATION_MAX_VALUE = 59_999L