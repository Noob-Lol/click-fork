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
package com.NoobLol.smartnoob.feature.smart.debugging.di

import com.NoobLol.smartnoob.core.common.overlays.di.OverlayComponent
import com.NoobLol.smartnoob.feature.smart.debugging.ui.overlay.DebugModel
import com.NoobLol.smartnoob.feature.smart.debugging.ui.overlay.TryElementViewModel
import com.NoobLol.smartnoob.feature.smart.debugging.ui.report.DebugReportModel

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@EntryPoint
@InstallIn(OverlayComponent::class)
interface DebuggingViewModelsEntryPoint {
    fun debugModel(): DebugModel
    fun debugReportModel(): DebugReportModel
    fun tryElementViewModel(): TryElementViewModel
}