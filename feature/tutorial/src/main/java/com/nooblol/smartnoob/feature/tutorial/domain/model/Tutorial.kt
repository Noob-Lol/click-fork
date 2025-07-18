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
package com.nooblol.smartnoob.feature.tutorial.domain.model

import androidx.annotation.StringRes

import com.nooblol.smartnoob.feature.tutorial.data.TutorialInfo

data class Tutorial(
    @StringRes val nameResId: Int,
    @StringRes val descResId: Int,
    val isUnlocked: Boolean,
)

internal fun TutorialInfo.toDomain(isUnlocked: Boolean): Tutorial =
    Tutorial(
        nameResId = nameResId,
        descResId = descResId,
        isUnlocked = isUnlocked,
    )