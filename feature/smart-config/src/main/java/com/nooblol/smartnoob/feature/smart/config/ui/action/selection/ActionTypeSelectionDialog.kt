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
package com.nooblol.smartnoob.feature.smart.config.ui.action.selection

import android.view.View

import com.nooblol.smartnoob.core.common.overlays.base.viewModels
import com.nooblol.smartnoob.core.common.overlays.dialog.implementation.MultiChoiceDialog
import com.nooblol.smartnoob.feature.smart.config.R
import com.nooblol.smartnoob.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint

class ActionTypeSelectionDialog(
    choices: List<ActionTypeChoice>,
    onChoiceSelectedListener: (ActionTypeChoice) -> Unit,
    onCancelledListener: (() -> Unit)? = null,
) : MultiChoiceDialog<ActionTypeChoice>(
    theme = R.style.ScenarioConfigTheme,
    dialogTitleText = R.string.dialog_title_action_type,
    choices = choices,
    onChoiceSelected = onChoiceSelectedListener,
    onCanceled = onCancelledListener,
) {

    /** View model for this content. */
    private val viewModel: ActionTypeSelectionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { actionTypeSelectionViewModel() },
    )

    override fun onStop() {
        super.onStop()
        viewModel.stopViewMonitoring()
    }

    override fun onChoiceViewBound(choice: ActionTypeChoice, view: View?) {
        if (choice !is ActionTypeChoice.Click) return

        if (view != null) viewModel.monitorCreateClickView(view)
        else viewModel.stopViewMonitoring()
    }
}