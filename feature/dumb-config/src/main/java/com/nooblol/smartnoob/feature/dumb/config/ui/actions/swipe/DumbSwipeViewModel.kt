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
package com.nooblol.smartnoob.feature.dumb.config.ui.actions.swipe

import android.content.Context
import android.graphics.Point

import androidx.lifecycle.ViewModel
import com.nooblol.smartnoob.core.dumb.domain.model.DumbAction

import com.nooblol.smartnoob.feature.dumb.config.R
import com.nooblol.smartnoob.feature.dumb.config.data.getDumbConfigPreferences
import com.nooblol.smartnoob.feature.dumb.config.data.putSwipeDurationConfig
import com.nooblol.smartnoob.feature.dumb.config.data.putSwipeRepeatCountConfig
import com.nooblol.smartnoob.feature.dumb.config.data.putSwipeRepeatDelayConfig
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import javax.inject.Inject

class DumbSwipeViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val _editedDumbSwipe: MutableStateFlow<DumbAction.DumbSwipe?> = MutableStateFlow(null)
    private val editedDumbSwipe: Flow<DumbAction.DumbSwipe> = _editedDumbSwipe.filterNotNull()

    /** Tells if the configured dumb swipe is valid and can be saved. */
    val isValidDumbSwipe: Flow<Boolean> = _editedDumbSwipe
        .map { it != null && it.isValid() }

    /** The name of the swipe. */
    val name: Flow<String> = editedDumbSwipe
        .map { it.name }
        .take(1)
    /** Tells if the action name is valid or not. */
    val nameError: Flow<Boolean> = editedDumbSwipe
        .map { it.name.isEmpty() }

    /** The duration between the press and release of the swipe in milliseconds. */
    val swipeDuration: Flow<String> = editedDumbSwipe
        .map { it.swipeDurationMs.toString() }
        .take(1)
    /** Tells if the press duration value is valid or not. */
    val swipeDurationError: Flow<Boolean> = editedDumbSwipe
        .map { it.swipeDurationMs <= 0 }

    /** The number of times to repeat the action. */
    val repeatCount: Flow<String> = editedDumbSwipe
        .map { it.repeatCount.toString() }
        .take(1)
    /** Tells if the repeat count value is valid or not. */
    val repeatCountError: Flow<Boolean> = editedDumbSwipe
        .map { it.repeatCount <= 0 }
    /** Tells if the action should be repeated infinitely. */
    val repeatInfiniteState: Flow<Boolean> = editedDumbSwipe
        .map { it.isRepeatInfinite }
    /** The delay, in ms, between two repeats of the action. */
    val repeatDelay: Flow<String> = editedDumbSwipe
        .map { it.repeatDelayMs.toString() }
        .take(1)
    /** Tells if the delay is valid or not. */
    val repeatDelayError: Flow<Boolean> = editedDumbSwipe
        .map { !it.isRepeatDelayValid() }

    /** Subtext for the position selector. */
    val swipePositionText: Flow<String> = editedDumbSwipe
        .map { dumbSwipe ->
            context.getString(
                R.string.item_desc_dumb_swipe_positions,
                dumbSwipe.fromPosition.x,
                dumbSwipe.fromPosition.y,
                dumbSwipe.toPosition.x,
                dumbSwipe.toPosition.y,
            )
        }

    fun setEditedDumbSwipe(swipe: DumbAction.DumbSwipe) {
        _editedDumbSwipe.value = swipe.copy()
    }

    fun getEditedDumbSwipe(): DumbAction.DumbSwipe? =
        _editedDumbSwipe.value

    fun setName(newName: String) {
        _editedDumbSwipe.value = _editedDumbSwipe.value?.copy(name = newName)
    }

    fun setPressDurationMs(durationMs: Long) {
        _editedDumbSwipe.value = _editedDumbSwipe.value?.copy(swipeDurationMs = durationMs)
    }

    fun setRepeatCount(repeatCount: Int) {
        _editedDumbSwipe.value = _editedDumbSwipe.value?.copy(repeatCount = repeatCount)
    }

    fun toggleInfiniteRepeat() {
        val currentValue = _editedDumbSwipe.value?.isRepeatInfinite ?: return
        _editedDumbSwipe.value = _editedDumbSwipe.value?.copy(isRepeatInfinite = !currentValue)
    }

    fun setRepeatDelay(delayMs: Long) {
        _editedDumbSwipe.value = _editedDumbSwipe.value?.copy(repeatDelayMs = delayMs)
    }

    fun setPositions(from: Point?, to: Point?) {
        if (from == null || to == null) return
        _editedDumbSwipe.value = _editedDumbSwipe.value?.copy(fromPosition = from, toPosition = to)
    }

    fun saveLastConfig(context: Context) {
        _editedDumbSwipe.value?.let { swipe ->
            context.getDumbConfigPreferences()
                .edit()
                .putSwipeDurationConfig(swipe.swipeDurationMs)
                .putSwipeRepeatCountConfig(swipe.repeatCount)
                .putSwipeRepeatDelayConfig(swipe.repeatDelayMs)
                .apply()
        }
    }
}