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
package com.nooblol.smartnoob.feature.smart.config.ui.common.bindings

import com.nooblol.smartnoob.core.domain.model.condition.TriggerCondition
import com.nooblol.smartnoob.feature.smart.config.databinding.ItemTriggerConditionBinding
import com.nooblol.smartnoob.feature.smart.config.ui.common.model.condition.UiTriggerCondition

/**
 * Bind this view holder as a condition item.
 *
 * @param uiCondition the condition to be represented by this item.
 * @param conditionClickedListener listener notified upon user click on this item.
 */
fun ItemTriggerConditionBinding.bind(
    uiCondition: UiTriggerCondition,
    conditionClickedListener: (TriggerCondition) -> Unit,
) {
    conditionName.text = uiCondition.name
    conditionDetails.text = uiCondition.description
    conditionTypeIcon.setImageResource(uiCondition.iconRes)
    root.setOnClickListener { conditionClickedListener(uiCondition.condition) }
}
