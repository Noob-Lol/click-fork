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
package com.nooblol.smartnoob.feature.dumb.config.di

import com.nooblol.smartnoob.core.common.overlays.di.OverlayComponent
import com.nooblol.smartnoob.feature.dumb.config.ui.DumbMainMenuModel
import com.nooblol.smartnoob.feature.dumb.config.ui.actions.click.DumbClickViewModel
import com.nooblol.smartnoob.feature.dumb.config.ui.actions.copy.DumbActionCopyModel
import com.nooblol.smartnoob.feature.dumb.config.ui.actions.pause.DumbPauseViewModel
import com.nooblol.smartnoob.feature.dumb.config.ui.actions.swipe.DumbSwipeViewModel
import com.nooblol.smartnoob.feature.dumb.config.ui.brief.DumbScenarioBriefViewModel
import com.nooblol.smartnoob.feature.dumb.config.ui.scenario.DumbScenarioViewModel
import com.nooblol.smartnoob.feature.dumb.config.ui.scenario.actionlist.DumbActionListViewModel
import com.nooblol.smartnoob.feature.dumb.config.ui.scenario.config.DumbScenarioConfigContentViewModel

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@EntryPoint
@InstallIn(OverlayComponent::class)
interface DumbConfigViewModelsEntryPoint {
    fun dumbActionCopyModel(): DumbActionCopyModel
    fun dumbActionListViewModel(): DumbActionListViewModel
    fun dumbClickViewModel(): DumbClickViewModel
    fun dumbMainMenuModel(): DumbMainMenuModel
    fun dumbPauseViewModel(): DumbPauseViewModel
    fun dumbScenarioBriefViewModel(): DumbScenarioBriefViewModel
    fun dumbScenarioViewModel(): DumbScenarioViewModel
    fun dumbScenarioConfigContentViewModel(): DumbScenarioConfigContentViewModel
    fun dumbSwipeViewModel(): DumbSwipeViewModel
}