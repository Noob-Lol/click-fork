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
package com.nooblol.smartnoob.feature.smart.config.ui.condition.trigger.broadcast

import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.nooblol.smartnoob.core.ui.bindings.dialogs.DialogNavigationButton
import com.nooblol.smartnoob.core.ui.bindings.dialogs.setButtonEnabledState
import com.nooblol.smartnoob.core.ui.bindings.fields.setError
import com.nooblol.smartnoob.core.ui.bindings.fields.setLabel
import com.nooblol.smartnoob.core.ui.bindings.fields.setOnCheckboxClickedListener
import com.nooblol.smartnoob.core.ui.bindings.fields.setOnTextChangedListener
import com.nooblol.smartnoob.core.ui.bindings.fields.setText
import com.nooblol.smartnoob.core.ui.bindings.fields.setTextValue
import com.nooblol.smartnoob.core.ui.bindings.fields.setup
import com.nooblol.smartnoob.core.common.overlays.base.viewModels
import com.nooblol.smartnoob.core.common.overlays.dialog.OverlayDialog
import com.nooblol.smartnoob.feature.smart.config.R
import com.nooblol.smartnoob.feature.smart.config.databinding.DialogConfigConditionBroadcastBinding
import com.nooblol.smartnoob.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.intent.IntentActionsSelectionDialog
import com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import com.nooblol.smartnoob.feature.smart.config.ui.condition.OnConditionConfigCompleteListener

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch

class BroadcastReceivedConditionDialog(
    private val listener: OnConditionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    /** The view model for this dialog. */
    private val viewModel: BroadcastReceivedConditionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { broadcastReceivedConditionViewModel() },
    )
    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigConditionBroadcastBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigConditionBroadcastBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_broadcast_received)

                buttonDismiss.setDebouncedOnClickListener { back() }
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener {
                        listener.onConfirmClicked()
                        super.back()
                    }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener {
                        listener.onDeleteClicked()
                        super.back()
                    }
                }
            }

            fieldName.apply {
                setLabel(R.string.generic_name)
                setOnTextChangedListener { viewModel.setName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }
            hideSoftInputOnFocusLoss(fieldName.textField)

            editBroadcastAction.apply {
                setup(R.string.field_intent_broadcast_action_label, R.drawable.ic_search, false)
                setOnTextChangedListener { viewModel.setIntentAction(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
                setOnCheckboxClickedListener { showBroadcastActionSelectionDialog() }
            }
            hideSoftInputOnFocusLoss(editBroadcastAction.textField)
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingCondition.collect(::onConditionEditingStateChanged) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.name.collect(viewBinding.fieldName::setText) }
                launch { viewModel.nameError.collect(viewBinding.fieldName::setError)}
                launch { viewModel.intentAction.collect(viewBinding.editBroadcastAction::setTextValue) }
                launch { viewModel.intentActionError.collect(viewBinding.editBroadcastAction::setError)}
                launch { viewModel.conditionCanBeSaved.collect(::updateSaveButton) }
            }
        }
    }

    override fun back() {
        if (viewModel.hasUnsavedModifications()) {
            context.showCloseWithoutSavingDialog {
                listener.onDismissClicked()
                super.back()
            }
            return
        }

        listener.onDismissClicked()
        super.back()
    }

    private fun updateSaveButton(canBeSaved: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, canBeSaved)
    }

    private fun showBroadcastActionSelectionDialog() {
        overlayManager.navigateTo(
            context = context,
            newOverlay = IntentActionsSelectionDialog(
                currentAction = viewModel.getIntentAction(),
                onConfigComplete = { newAction ->
                    viewModel.setIntentAction(newAction ?: "")
                    viewBinding.editBroadcastAction.textField.setText(newAction)
                },
                forBroadcastReception = true,
            ),
            hideCurrent = true,
        )
    }

    private fun onConditionEditingStateChanged(isEditing: Boolean) {
        if (!isEditing) finish()
    }
}