/*
 * Copyright (C) 2025 Kevin Buzeau
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
<<<<<<<< HEAD:feature/notifications/src/main/java/com/nooblol/smartnoob/feature/notifications/user/UserNotificationsController.kt
package com.nooblol.smartnoob.feature.notifications.user
========
package com.buzbuz.smartautoclicker.core.common.actions.notification
>>>>>>>> upstream/master:core/common/actions/src/main/java/com/buzbuz/smartautoclicker/core/common/actions/notification/NotificationRequestExecutor.kt

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.os.Build
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

<<<<<<<< HEAD:feature/notifications/src/main/java/com/nooblol/smartnoob/feature/notifications/user/UserNotificationsController.kt
import com.nooblol.smartnoob.core.base.di.Dispatcher
import com.nooblol.smartnoob.core.base.di.HiltCoroutineDispatchers
import com.nooblol.smartnoob.core.common.permissions.model.PermissionPostNotification
import com.nooblol.smartnoob.core.domain.model.NotificationRequest
import com.nooblol.smartnoob.feature.notifications.common.NotificationIds
import com.nooblol.smartnoob.feature.notifications.common.createUserScenarioNotificationChannelDefault
import com.nooblol.smartnoob.feature.notifications.common.createUserScenarioNotificationChannelGroup
import com.nooblol.smartnoob.feature.notifications.common.createUserScenarioNotificationChannelHigh
import com.nooblol.smartnoob.feature.notifications.common.createUserScenarioNotificationChannelLow
import com.nooblol.smartnoob.feature.notifications.common.getUserScenarioNotificationChannelId
import com.nooblol.smartnoob.feature.notifications.common.notificationIconResId
import com.nooblol.smartnoob.feature.notifications.user.group.UserNotificationGroups
========
import com.buzbuz.smartautoclicker.core.common.permissions.model.PermissionPostNotification
import com.buzbuz.smartautoclicker.core.base.notifications.NotificationIds
import com.buzbuz.smartautoclicker.core.common.actions.model.ActionNotificationRequest
import com.buzbuz.smartautoclicker.core.ui.utils.notificationIconResId
>>>>>>>> upstream/master:core/common/actions/src/main/java/com/buzbuz/smartautoclicker/core/common/actions/notification/NotificationRequestExecutor.kt

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NotificationRequestExecutor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationIds: NotificationIds,
    private val scheduler: NotificationScheduler,
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    private val notificationGroups: ActionNotificationGroups =
        ActionNotificationGroups(notificationIds)

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.apply {
                createActionNotificationChannelGroup(context)
                createActionNotificationChannelLow(context)
                createActionNotificationChannelDefault(context)
                createActionNotificationChannelHigh(context)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun postNotification(notificationRequest: ActionNotificationRequest) {
        if (!PermissionPostNotification().checkIfGranted(context)) return

        val channelId = getActionNotificationChannelId(notificationRequest.importance) ?: let {
            Log.w(LOG_TAG, "Invalid channel id for importance ${notificationRequest.importance}")
            return
        }
        val notificationGroup = notificationGroups.getGroup(context, notificationRequest) ?: let {
            Log.w(LOG_TAG, "Cannot find notification group for request $notificationRequest")
            return
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(notificationRequest.title)
            .setContentText(notificationRequest.message)
            .setSmallIcon(notificationIconResId())
            .setCategory(Notification.CATEGORY_REMINDER)
            .setGroup(notificationGroup.groupName)

        scheduler.schedule(
            id = notificationIds.getUserNotificationId(notificationRequest.actionId),
            notification = builder.build(),
            group = notificationGroup,
        )
    }

    fun clear() {
        // We don't want to remove another notification like the foreground service one
        notificationIds.resetDynamicIdsCache().forEach { notificationId ->
            notificationManager.cancel(notificationId)
        }
    }
}

private const val LOG_TAG = "NotificationRequestExecutor"