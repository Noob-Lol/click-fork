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
package com.nooblol.smartnoob.feature.smart.config.ui.common.starters

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

import com.nooblol.smartnoob.core.base.data.getNotificationSettingsIntent
import com.nooblol.smartnoob.core.base.data.getOpenWebBrowserIntent
import com.nooblol.smartnoob.core.base.data.getOpenWebBrowserPickerIntent
import com.nooblol.smartnoob.core.common.overlays.menu.implementation.ActivityStarterOverlayMenu


internal fun newWebBrowserStarterOverlay(uri: Uri) = ActivityStarterOverlayMenu(
    intent = getOpenWebBrowserIntent(uri),
    fallbackIntent = getOpenWebBrowserPickerIntent(uri),
)

internal fun newRestartMediaProjectionStarterOverlay(context: Context) = ActivityStarterOverlayMenu(
    intent = RestartMediaProjectionActivity.getStartIntent(context),
)

internal fun newNotificationPermissionStarterOverlay(context: Context) = ActivityStarterOverlayMenu(
    intent = RequestNotificationPermissionActivity.getStartIntent(context)
)

@RequiresApi(Build.VERSION_CODES.O)
internal fun newNotificationSettingsStarterOverlay() = ActivityStarterOverlayMenu(
    intent = getNotificationSettingsIntent(),
)