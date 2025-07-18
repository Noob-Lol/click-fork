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
package com.nooblol.smartnoob.core.ui.monitoring

import android.graphics.Rect
import android.view.View
import com.nooblol.smartnoob.core.display.config.DisplayConfigManager

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitoredViewsManager @Inject constructor(
    private val displayConfigManager: DisplayConfigManager,
) {

    private val monitoredViews: MutableMap<MonitoredViewType, ViewMonitor> = mutableMapOf()
    private val monitoredClicks: MutableMap<MonitoredViewType, () -> Unit> = mutableMapOf()

    fun attach(
        type: MonitoredViewType,
        monitoredView: View,
        positioningType: ViewPositioningType = ViewPositioningType.SCREEN,
    ) {
        if (!monitoredViews.contains(type)) monitoredViews[type] = ViewMonitor(displayConfigManager)
        monitoredViews[type]?.attachView(monitoredView, positioningType)
    }

    fun detach(type: MonitoredViewType) {
        monitoredViews[type]?.detachView()
    }

    fun notifyClick(type: MonitoredViewType) {
        monitoredClicks[type]?.invoke()
    }

    fun setExpectedViews(types: Set<MonitoredViewType>) {
        types.forEach { type ->
            if (!monitoredViews.contains(type)) monitoredViews[type] = ViewMonitor(displayConfigManager)
        }
    }

    fun clearExpectedViews() {
        monitoredViews.clear()
    }

    fun getViewPosition(type: MonitoredViewType): StateFlow<Rect>? =
        monitoredViews[type]?.position

    fun performClick(type: MonitoredViewType): Boolean {
        return monitoredViews[type]?.performClick() ?: false
    }

    fun monitorNextClick(type: MonitoredViewType, listener: () -> Unit) {
        monitoredClicks[type] = {
            monitoredClicks.remove(type)
            listener()
        }
    }
}