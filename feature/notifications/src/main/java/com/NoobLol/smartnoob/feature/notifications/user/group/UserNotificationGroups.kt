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
package com.NoobLol.smartnoob.feature.notifications.user.group

import android.content.Context
import androidx.core.app.NotificationCompat

import com.NoobLol.smartnoob.core.domain.model.NotificationRequest
import com.NoobLol.smartnoob.feature.notifications.common.NotificationIds
import com.NoobLol.smartnoob.feature.notifications.common.getUserScenarioNotificationChannelId
import com.NoobLol.smartnoob.feature.notifications.common.notificationIconResId


internal class UserNotificationGroups(private val notificationIds: NotificationIds) {

    private val groups: MutableMap<Long, UserNotificationGroup> =
        mutableMapOf()

    fun getGroup(context: Context, notificationRequest: NotificationRequest): UserNotificationGroup =
        groups[notificationRequest.eventId] ?: UserNotificationGroup(
            groupName = notificationRequest.groupName,
            summaryId = notificationIds.getSummaryNotificationId(notificationRequest.eventId),
            summaryBuilder = notificationRequest.createSummaryNotificationBuilder(context)
        )

    private fun NotificationRequest.createSummaryNotificationBuilder(context: Context): NotificationCompat.Builder =
        NotificationCompat.Builder(context, getUserScenarioNotificationChannelId(importance))
            .setContentTitle(groupName)
            .setSmallIcon(notificationIconResId())
            .setGroup(groupName)
            .setGroupSummary(true)
}

