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
package com.nooblol.smartnoob.feature.qstile.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.nooblol.smartnoob.core.base.data.AppComponentsProvider
import com.nooblol.smartnoob.core.base.di.Dispatcher
import com.nooblol.smartnoob.core.base.di.HiltCoroutineDispatchers.IO
import com.nooblol.smartnoob.core.domain.IRepository
import com.nooblol.smartnoob.core.dumb.domain.DumbRepository
import com.nooblol.smartnoob.core.common.permissions.PermissionsController
import com.nooblol.smartnoob.core.common.permissions.model.PermissionAccessibilityService
import com.nooblol.smartnoob.core.common.permissions.model.PermissionOverlay
import com.nooblol.smartnoob.core.common.permissions.model.PermissionPostNotification
import com.nooblol.smartnoob.core.settings.SettingsRepository
import com.nooblol.smartnoob.feature.qstile.domain.QSTileRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class QSTileLauncherViewModel @Inject constructor(
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val qsTileRepository: QSTileRepository,
    private val permissionController: PermissionsController,
    private val smartRepository: IRepository,
    private val dumbRepository: DumbRepository,
    private val settingsRepository: SettingsRepository,
    private val appComponentsProvider: AppComponentsProvider,
) : ViewModel() {


    fun startPermissionFlowIfNeeded(activity: AppCompatActivity, onAllGranted: () -> Unit, onMandatoryDenied: () -> Unit) {
        permissionController.startPermissionsUiFlow(
            activity = activity,
            permissions = listOf(
                PermissionOverlay(),
                PermissionAccessibilityService(
                    componentName = appComponentsProvider.klickrServiceComponentName,
                    isServiceRunning = { qsTileRepository.isAccessibilityServiceStarted() },
                ),
                PermissionPostNotification(optional = true),
            ),
            onAllGranted = onAllGranted,
            onMandatoryDenied = onMandatoryDenied,
        )
    }

    fun startSmartScenario(resultCode: Int, data: Intent, scenarioId: Long) {
        viewModelScope.launch(ioDispatcher) {
            val scenario = smartRepository.getScenario(scenarioId) ?: return@launch
            qsTileRepository.startSmartScenario(resultCode, data, scenario)
        }
    }

    fun startDumbScenario(scenarioId: Long) {
        viewModelScope.launch(ioDispatcher) {
            val scenario = dumbRepository.getDumbScenario(scenarioId) ?: return@launch
            qsTileRepository.startDumbScenario(scenario)
        }
    }

    fun isEntireScreenCaptureForced(): Boolean =
        settingsRepository.isEntireScreenCaptureForced()
}