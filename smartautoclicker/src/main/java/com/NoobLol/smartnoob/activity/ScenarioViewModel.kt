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
package com.NoobLol.smartnoob.activity

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.NoobLol.smartnoob.NoobService
import com.NoobLol.smartnoob.core.common.quality.domain.QualityRepository
import com.NoobLol.smartnoob.core.domain.model.scenario.Scenario
import com.NoobLol.smartnoob.core.dumb.domain.model.DumbScenario
import com.NoobLol.smartnoob.core.common.permissions.PermissionsController
import com.NoobLol.smartnoob.core.common.permissions.model.PermissionAccessibilityService
import com.NoobLol.smartnoob.core.common.permissions.model.PermissionOverlay
import com.NoobLol.smartnoob.core.common.permissions.model.PermissionPostNotification
import com.NoobLol.smartnoob.feature.revenue.IRevenueRepository
import com.NoobLol.smartnoob.feature.revenue.UserConsentState
import com.NoobLol.smartnoob.localservice.ILocalService

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** AndroidViewModel for create/delete/list click scenarios from an LifecycleOwner. */
@HiltViewModel
class ScenarioViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val revenueRepository: IRevenueRepository,
    private val qualityRepository: QualityRepository,
    private val permissionController: PermissionsController,
) : ViewModel() {

    /** Callback upon the availability of the [SmartAutoClickerService]. */
    private val serviceConnection: (ILocalService?) -> Unit = { localService ->
        clickerService = localService
    }

    /**
     * Reference on the [SmartAutoClickerService].
     * Will be not null only if the Accessibility Service is enabled.
     */
    private var clickerService: ILocalService? = null
    /** The Android notification manager. Initialized only if needed.*/
    private val notificationManager: NotificationManager?

    val userConsentState: StateFlow<UserConsentState> = revenueRepository.userConsentState
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserConsentState.UNKNOWN)

    init {
        NoobService.getLocalService(serviceConnection)

        notificationManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                context.getSystemService(NotificationManager::class.java)
            else null
    }

    override fun onCleared() {
        NoobService.getLocalService(null)
        super.onCleared()
    }

    fun requestUserConsentIfNeeded(activity: Activity) {
        revenueRepository.refreshPurchases()
        revenueRepository.startUserConsentRequestUiFlowIfNeeded(activity)
    }

    fun refreshPurchaseState() {
        revenueRepository.refreshPurchases()
    }

    fun startPermissionFlowIfNeeded(activity: AppCompatActivity, onAllGranted: () -> Unit) {
        permissionController.startPermissionsUiFlow(
            activity = activity,
            permissions = listOf(
                PermissionOverlay(),
                PermissionAccessibilityService(
                    componentName = ComponentName(activity, NoobService::class.java),
                    isServiceRunning = { NoobService.isServiceStarted() },
                ),
                PermissionPostNotification(optional = true),
            ),
            onAllGranted = onAllGranted,
        )
    }

    fun startTroubleshootingFlowIfNeeded(activity: FragmentActivity, onCompleted: () -> Unit) {
        qualityRepository.startTroubleshootingUiFlowIfNeeded(activity, onCompleted)
    }

    /**
     * Start the overlay UI and instantiates the detection objects for a given scenario.
     *
     * This requires the media projection permission code and its data intent, they both can be retrieved using the
     * results of the activity intent provided by
     * [android.media.projection.MediaProjectionManager.createScreenCaptureIntent] (this Intent shows the dialog
     * warning about screen recording privacy). Any attempt to call this method without the correct screen capture
     * intent result will leads to a crash.
     *
     * @param resultCode the result code provided by the screen capture intent activity result callback
     * [android.app.Activity.onActivityResult]
     * @param data the data intent provided by the screen capture intent activity result callback
     * [android.app.Activity.onActivityResult]
     * @param scenario the identifier of the scenario of clicks to be used for detection.
     */
    fun loadSmartScenario(context: Context, resultCode: Int, data: Intent, scenario: Scenario): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val foregroundPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE)
            if (foregroundPermission != PermissionChecker.PERMISSION_GRANTED) return false
        }

        clickerService?.startSmartScenario(resultCode, data, scenario)
        return true
    }

    fun loadDumbScenario(context: Context, scenario: DumbScenario): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val foregroundPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE)
            if (foregroundPermission != PermissionChecker.PERMISSION_GRANTED) return false
        }

        clickerService?.startDumbScenario(scenario)
        return true
    }

    /** Stop the overlay UI and release all associated resources. */
    fun stopScenario() {
        clickerService?.stop()
    }
}
