/*
 * Copyright (C) 2023 Kevin Buzeau
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
package com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.intent

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration

import com.nooblol.smartnoob.core.common.overlays.base.viewModels
import com.nooblol.smartnoob.core.common.overlays.dialog.OverlayDialog
import com.nooblol.smartnoob.core.ui.bindings.dialogs.DialogNavigationButton
import com.nooblol.smartnoob.core.ui.bindings.dialogs.setButtonVisibility
import com.nooblol.smartnoob.feature.smart.config.R
import com.nooblol.smartnoob.feature.smart.config.databinding.DialogConfigActionIntentActionsBinding
import com.nooblol.smartnoob.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.nooblol.smartnoob.feature.smart.config.ui.common.starters.newWebBrowserStarterOverlay

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch

class IntentActionsSelectionDialog (
    private val currentAction: String?,
    private val onConfigComplete: (action: String?) -> Unit,
    private val forBroadcastReception: Boolean = false,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    /** The view model for this dialog. */
    private val viewModel: IntentActionsSelectionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { intentActionsSelectionViewModel() },
    )

    private lateinit var viewBinding: DialogConfigActionIntentActionsBinding
    private lateinit var actionsAdapter: IntentActionsSelectionAdapter

    override fun onCreateView(): ViewGroup {
        viewModel.setRequestedActionsType(forBroadcastReception)

        actionsAdapter = IntentActionsSelectionAdapter(
            onActionCheckClicked = viewModel::setActionSelectionState,
            onActionHelpClicked = { uri -> debounceUserInteraction { onActionHelpClicked(uri) } },
        )

        viewBinding = DialogConfigActionIntentActionsBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_intent_actions)

                setButtonVisibility(DialogNavigationButton.SAVE, View.GONE)
                setButtonVisibility(DialogNavigationButton.DELETE, View.GONE)
                buttonDismiss.setOnClickListener {
                    debounceUserInteraction {
                        onConfigComplete(viewModel.getSelectedAction())
                        back()
                    }
                }
            }

            actionsList.apply {
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = actionsAdapter
            }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        viewModel.setSelectedAction(currentAction)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.actionsItems.collect(actionsAdapter::submitList) }
            }
        }
    }

    private fun onActionHelpClicked(uri: Uri) {
        overlayManager.navigateTo(
            context = context,
            newOverlay = newWebBrowserStarterOverlay(uri),
            hideCurrent = true,
        )
    }
}
