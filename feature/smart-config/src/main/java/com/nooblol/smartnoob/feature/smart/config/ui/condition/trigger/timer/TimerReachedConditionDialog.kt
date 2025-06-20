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
package com.nooblol.smartnoob.feature.smart.config.ui.condition.trigger.timer

import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.nooblol.smartnoob.core.ui.bindings.dialogs.DialogNavigationButton
import com.nooblol.smartnoob.core.ui.bindings.dropdown.setItems
import com.nooblol.smartnoob.core.ui.bindings.dropdown.setSelectedItem
import com.nooblol.smartnoob.core.ui.bindings.dropdown.timeUnitDropdownItems
import com.nooblol.smartnoob.core.ui.bindings.dialogs.setButtonEnabledState
import com.nooblol.smartnoob.core.ui.bindings.fields.setError
import com.nooblol.smartnoob.core.ui.bindings.fields.setLabel
import com.nooblol.smartnoob.core.ui.bindings.fields.setOnTextChangedListener
import com.nooblol.smartnoob.core.ui.bindings.fields.setText
import com.nooblol.smartnoob.core.common.overlays.base.viewModels
import com.nooblol.smartnoob.core.common.overlays.dialog.OverlayDialog
import com.nooblol.smartnoob.core.ui.bindings.fields.setChecked
import com.nooblol.smartnoob.core.ui.bindings.fields.setDescription
import com.nooblol.smartnoob.core.ui.bindings.fields.setOnClickListener
import com.nooblol.smartnoob.core.ui.bindings.fields.setTitle
import com.nooblol.smartnoob.core.ui.bindings.fields.setupDescriptions
import com.nooblol.smartnoob.core.ui.utils.MinMaxInputFilter
import com.nooblol.smartnoob.feature.smart.config.R
import com.nooblol.smartnoob.feature.smart.config.databinding.DialogConfigConditionTimerBinding
import com.nooblol.smartnoob.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import com.nooblol.smartnoob.feature.smart.config.ui.condition.OnConditionConfigCompleteListener

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch

class TimerReachedConditionDialog(
    private val listener: OnConditionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    /** The view model for this dialog. */
    private val viewModel: TimerReachedConditionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { timerReachedConditionViewModel() },
    )
    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigConditionTimerBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigConditionTimerBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_timer_reached)

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

            editDurationLayout.apply {
                textField.filters = arrayOf(MinMaxInputFilter(min = 1))
                setLabel(R.string.input_field_label_timer_duration_no_unit)
                setOnTextChangedListener {
                    viewModel.setDuration(if (it.isNotEmpty()) it.toString().toLong() else null)
                }
            }
            hideSoftInputOnFocusLoss(editDurationLayout.textField)

            timeUnitField.setItems(
                label = context.getString(R.string.dropdown_label_time_unit),
                items = timeUnitDropdownItems,
                onItemSelected = viewModel::setTimeUnit,
            )

            fieldIsReset.apply {
                setTitle(context.getString(R.string.field_timer_restart_title))
                setupDescriptions(
                    listOf(
                        context.getString(R.string.field_timer_restart_desc_off),
                        context.getString(R.string.field_timer_restart_desc_on),
                    )
                )
                setOnClickListener(viewModel::toggleRestartWhenReached)
            }
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
                launch { viewModel.duration.collect(::updateDuration) }
                launch { viewModel.durationError.collect(viewBinding.editDurationLayout::setError)}
                launch { viewModel.selectedUnitItem.collect(viewBinding.timeUnitField::setSelectedItem) }
                launch { viewModel.restartWhenReached.collect(::updateIsResetField) }
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

    private fun updateDuration(newDuration: String?) {
        viewBinding.editDurationLayout.setText(newDuration, InputType.TYPE_CLASS_NUMBER)
    }

    private fun updateSaveButton(canBeSaved: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, canBeSaved)
    }

    private fun updateIsResetField(resetWhenReached: Boolean) {
        viewBinding.fieldIsReset.apply {
            setChecked(resetWhenReached)
            setDescription(if (resetWhenReached) 1 else 0)
        }
    }

    private fun onConditionEditingStateChanged(isEditing: Boolean) {
        if (!isEditing) finish()
    }
}