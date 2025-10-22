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
package com.nooblol.smartnoob

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

import com.nooblol.smartnoob.core.base.Dumpable
import com.nooblol.smartnoob.core.base.data.AppComponentsProvider
import com.nooblol.smartnoob.core.base.extensions.requestFilterKeyEvents
import com.nooblol.smartnoob.core.base.extensions.startForegroundMediaProjectionServiceCompat
import com.nooblol.smartnoob.core.bitmaps.BitmapRepository
import com.nooblol.smartnoob.core.common.overlays.manager.OverlayManager
import com.nooblol.smartnoob.core.common.quality.domain.QualityMetricsMonitor
import com.nooblol.smartnoob.core.common.quality.domain.QualityRepository
import com.nooblol.smartnoob.core.display.config.DisplayConfigManager
import com.nooblol.smartnoob.core.domain.model.scenario.Scenario
import com.nooblol.smartnoob.core.dumb.domain.model.DumbScenario
import com.nooblol.smartnoob.core.dumb.engine.DumbEngine
import com.nooblol.smartnoob.core.processing.domain.DetectionRepository
import com.nooblol.smartnoob.core.settings.SettingsRepository
import com.nooblol.smartnoob.core.base.notifications.NotificationIds
import com.nooblol.smartnoob.core.common.actions.AndroidActionExecutor
import com.nooblol.smartnoob.feature.qstile.domain.QSTileActionHandler
import com.nooblol.smartnoob.feature.qstile.domain.QSTileRepository
import com.nooblol.smartnoob.feature.revenue.IRevenueRepository
import com.nooblol.smartnoob.feature.review.ReviewRepository
import com.nooblol.smartnoob.feature.smart.debugging.domain.DebuggingRepository
import com.nooblol.smartnoob.localservice.LocalService
import com.nooblol.smartnoob.localservice.LocalServiceProvider

import dagger.hilt.android.AndroidEntryPoint
import java.io.FileDescriptor
import java.io.PrintWriter
import javax.inject.Inject

/**
 * AccessibilityService implementation for the SmartAutoClicker.
 *
 * Started automatically by Android once the user has defined this service has an accessibility service, it provides
 * an API to start and stop the DetectorEngine correctly in order to display the overlay UI and record the screen for
 * clicks detection.
 * This API is offered through the [LocalService] class, which is instantiated in the [LocalServiceProvider] object.
 * This system is used instead of the usual binder interface because an [AccessibilityService] already has its own
 * binder and it can't be changed. To access this local service, use [LocalServiceProvider].
 *
 * We need this service to be an accessibility service in order to inject the detected event on the currently
 * displayed activity. This injection is made by the [dispatchGesture] method, which is called everytime an event has
 * been detected.
 */
@AndroidEntryPoint
class NoobService : AccessibilityService() {

    private val localServiceProvider = LocalServiceProvider

    private val localService: LocalService?
        get() = localServiceProvider.localServiceInstance as? LocalService

    @Inject lateinit var overlayManager: OverlayManager
    @Inject lateinit var displayConfigManager: DisplayConfigManager
    @Inject lateinit var detectionRepository: DetectionRepository
    @Inject lateinit var dumbEngine: DumbEngine
    @Inject lateinit var bitmapManager: BitmapRepository
    @Inject lateinit var qualityRepository: QualityRepository
    @Inject lateinit var qualityMetricsMonitor: QualityMetricsMonitor
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var revenueRepository: IRevenueRepository
    @Inject lateinit var tileRepository: QSTileRepository
    @Inject lateinit var debugRepository: DebuggingRepository
    @Inject lateinit var reviewRepository: ReviewRepository
    @Inject lateinit var appComponentsProvider: AppComponentsProvider
    @Inject lateinit var actionExecutor: AndroidActionExecutor

    override fun onServiceConnected() {
        super.onServiceConnected()

        qualityMetricsMonitor.onServiceConnected()
        actionExecutor.init(this)

        tileRepository.setTileActionHandler(
            object : QSTileActionHandler {
                override fun isRunning(): Boolean = localServiceProvider.isServiceStarted()
                override fun startDumbScenario(dumbScenario: DumbScenario) {
                    localServiceProvider.localServiceInstance?.startDumbScenario(dumbScenario)
                }
                override fun startSmartScenario(resultCode: Int, data: Intent, scenario: Scenario) {
                    localServiceProvider.localServiceInstance?.startSmartScenario(resultCode, data, scenario)
                }
                override fun stop() {
                    localServiceProvider.localServiceInstance?.stop()
                }
            }
        )

        localServiceProvider.setLocalService(
            LocalService(
                context = this,
                overlayManager = overlayManager,
                appComponentsProvider = appComponentsProvider,
                detectionRepository = detectionRepository,
                dumbEngine = dumbEngine,
                debugRepository = debugRepository,
                revenueRepository = revenueRepository,
                settingsRepository = settingsRepository,
                onStart = ::onLocalServiceStarted,
                onStop = ::onLocalServiceStopped,
            )
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        localServiceProvider.localServiceInstance?.apply {
            stop()
            release()
        }
        localServiceProvider.setLocalService(null)

        qualityMetricsMonitor.onServiceUnbind()
        actionExecutor.clear()
        return super.onUnbind(intent)
    }

    private fun onLocalServiceStarted(scenarioId: Long, isSmart: Boolean, serviceNotification: Notification?) {
        reviewRepository.onUserSessionStarted()
        qualityMetricsMonitor.onServiceForegroundStart()

        serviceNotification?.let {
            startForegroundMediaProjectionServiceCompat(NotificationIds.FOREGROUND_SERVICE_NOTIFICATION_ID, it)
        }
        requestFilterKeyEvents(true)

        displayConfigManager.startMonitoring(this)
        tileRepository.setTileScenario(scenarioId = scenarioId, isSmart = isSmart)
    }

    private fun onLocalServiceStopped() {
        qualityMetricsMonitor.onServiceForegroundEnd()
        reviewRepository.onUserSessionStopped()
        actionExecutor.resetState()

        if (reviewRepository.isUserCandidateForReview()) {
            Log.i(TAG, "User is candidate for review, ")

            reviewRepository.getReviewActivityIntent(this)?.let { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        requestFilterKeyEvents(false)
        stopForeground(STOP_FOREGROUND_REMOVE)

        displayConfigManager.stopMonitoring()
        bitmapManager.clearCache()
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean =
        localService?.onKeyEvent(event) ?: super.onKeyEvent(event)

    /**
     * Dump the state of the service via adb.
     * adb shell "dumpsys activity service com.nooblol.smartnoob"
     */
    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        if (writer == null) return

        writer.append("* NoobService:").println()
        writer.append(Dumpable.DUMP_DISPLAY_TAB)
            .append("- isStarted=").append("${localService?.isStarted ?: false}; ")
            .println()

        displayConfigManager.dump(writer)
        bitmapManager.dump(writer)
        overlayManager.dump(writer)
        detectionRepository.dump(writer)
        dumbEngine.dump(writer)
        actionExecutor.dump(writer)
        qualityRepository.dump(writer)

        revenueRepository.dump(writer)
        reviewRepository.dump(writer)
    }

    override fun onInterrupt() { /* Unused */ }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Unused */ }
}

/** Tag for the logs. */
private const val TAG = "NoobService"