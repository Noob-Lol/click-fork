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
<<<<<<<< HEAD:feature/notifications/src/main/java/com/nooblol/smartnoob/feature/notifications/service/receivers/NotificationActionsReceiver.kt
package com.nooblol.smartnoob.feature.notifications.service.receivers
========
package com.buzbuz.smartautoclicker.feature.notifications.receivers
>>>>>>>> upstream/master:feature/notifications/src/main/java/com/nooblol/smartnoob/feature/notifications/receivers/NotificationActionsReceiver.kt

import android.content.Context
import android.content.Intent
import android.util.Log

<<<<<<<< HEAD:feature/notifications/src/main/java/com/nooblol/smartnoob/feature/notifications/service/receivers/NotificationActionsReceiver.kt
import com.nooblol.smartnoob.core.base.SafeBroadcastReceiver
import com.nooblol.smartnoob.feature.notifications.service.model.ServiceNotificationAction
import com.nooblol.smartnoob.feature.notifications.service.model.getAllActionsBroadcastIntentFilter
import com.nooblol.smartnoob.feature.notifications.service.model.toServiceNotificationAction
========
import com.buzbuz.smartautoclicker.core.base.SafeBroadcastReceiver
import com.buzbuz.smartautoclicker.feature.notifications.model.ServiceNotificationAction
import com.buzbuz.smartautoclicker.feature.notifications.model.getAllActionsBroadcastIntentFilter
import com.buzbuz.smartautoclicker.feature.notifications.model.toServiceNotificationAction
>>>>>>>> upstream/master:feature/notifications/src/main/java/com/nooblol/smartnoob/feature/notifications/receivers/NotificationActionsReceiver.kt


internal class NotificationActionsReceiver(
    private val onReceived: (ServiceNotificationAction) -> Unit,
): SafeBroadcastReceiver(getAllActionsBroadcastIntentFilter()) {

    override fun onReceive(context: Context, intent: Intent) {
        intent.toServiceNotificationAction()?.let { action ->
            Log.i(TAG, "Notification action received: ${intent.action}")
            onReceived(action)
        }
    }
}

private const val TAG = "NotificationActionsReceiver"