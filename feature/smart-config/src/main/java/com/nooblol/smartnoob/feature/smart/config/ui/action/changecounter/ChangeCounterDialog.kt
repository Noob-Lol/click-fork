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
package com.nooblol.smartnoob.feature.smart.config.ui.action.changecounter

import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.nooblol.smartnoob.core.ui.bindings.dialogs.DialogNavigationButton
import com.nooblol.smartnoob.core.ui.bindings.dropdown.setItems
import com.nooblol.smartnoob.core.ui.bindings.dropdown.setSelectedItem
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
import com.nooblol.smartnoob.core.domain.model.CounterOperationValue
import com.nooblol.smartnoob.core.ui.bindings.buttons.MultiStateButtonConfig
import com.nooblol.smartnoob.core.ui.bindings.buttons.setChecked
import com.nooblol.smartnoob.core.ui.bindings.buttons.setOnCheckedListener
import com.nooblol.smartnoob.core.ui.bindings.buttons.setup
import com.nooblol.smartnoob.core.ui.utils.MinMaxInputFilter
import com.nooblol.smartnoob.feature.smart.config.R
import com.nooblol.smartnoob.feature.smart.config.databinding.DialogConfigActionChangeCounterBinding
import com.nooblol.smartnoob.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.nooblol.smartnoob.feature.smart.config.ui.action.OnActionConfigCompleteListener
import com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.counter.CounterNameSelectionDialog
import com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch

class ChangeCounterDialog(
    private val listener: OnActionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    /** The view model for this dialog. */
    private val viewModel: ChangeCounterViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { changeCounterViewModel() }
    )
    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigActionChangeCounterBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigActionChangeCounterBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_change_counter)

                buttonDismiss.setDebouncedOnClickListener { back() }
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener { onSaveButtonClicked() }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener { onDeleteButtonClicked() }
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

            editCounterNameLayout.apply {
                setup(R.string.field_counter_name_label, R.drawable.ic_search, false)
                setOnTextChangedListener { viewModel.setCounterName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
                setOnCheckboxClickedListener { showCounterSelectionDialog(viewModel::setCounterName) }
            }
            hideSoftInputOnFocusLoss(editCounterNameLayout.textField)

            operatorField.setItems(
                label = context.getString(R.string.dropdown_comparison_operator_label),
                items = viewModel.operatorDropdownItems,
                onItemSelected = viewModel::setOperationItem,
            )

            valueTypeMultiStateButton.apply {
                setup(
                    MultiStateButtonConfig(
                        icons = listOf(R.drawable.ic_numbers, R.drawable.ic_change_counter),
                        singleSelection = true,
                        selectionRequired = true,
                    )
                )
                setOnCheckedListener { checkedId ->
                    viewModel.setOperationValue(
                        if (checkedId == 0) {
                            CounterOperationValue.Number(
                                if (editValueLayout.textField.text.isNullOrEmpty()) 0
                                else editValueLayout.textField.text.toString().toInt()
                            )
                        } else {
                            CounterOperationValue.Counter(
                                if (editValueCounterName.textField.text.isNullOrEmpty()) ""
                                else editValueCounterName.textField.text.toString()
                            )
                        }
                    )
                }
            }

            editValueLayout.apply {
                textField.filters = arrayOf(MinMaxInputFilter(0, Int.MAX_VALUE))
                setLabel(R.string.field_counter_operation_value_label)
                setOnTextChangedListener {
                    viewModel.setOperationValue(
                        CounterOperationValue.Number(
                            if (editValueLayout.textField.text.isNullOrEmpty()) 0
                            else editValueLayout.textField.text.toString().toInt()
                        )
                    )
                }
            }
            hideSoftInputOnFocusLoss(editValueLayout.textField)

            editValueCounterName.apply {
                setup(R.string.field_counter_name_label, R.drawable.ic_search, false)
                setOnTextChangedListener {
                    viewModel.setOperationValue(CounterOperationValue.Counter(it.toString()))
                }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
                setOnCheckboxClickedListener {
                    showCounterSelectionDialog { counterName ->
                        viewModel.setOperationValue(CounterOperationValue.Counter(counterName))
                    }
                }
            }
            hideSoftInputOnFocusLoss(editValueCounterName.textField)
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingAction.collect(::onActionEditingStateChanged) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.name.collect(viewBinding.fieldName::setText) }
                launch { viewModel.nameError.collect(viewBinding.fieldName::setError) }
                launch { viewModel.counterName.collect(viewBinding.editCounterNameLayout::setTextValue) }
                launch { viewModel.counterNameError.collect(viewBinding.editCounterNameLayout::setError) }
                launch { viewModel.operatorDropdownState.collect(viewBinding.operatorField::setSelectedItem) }
                launch { viewModel.isNumberValue.collect(::updateOperationValueVisibility) }
                launch { viewModel.numberValueText.collect(::updateNumberValue) }
                launch { viewModel.counterNameValueText.collect(::updateCounterValue) }
                launch { viewModel.isValidAction.collect(::updateSaveButton) }
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

    private fun onSaveButtonClicked() {
        listener.onConfirmClicked()
        super.back()
    }

    private fun onDeleteButtonClicked() {
        listener.onDeleteClicked()
        super.back()
    }

    private fun updateOperationValueVisibility(isNumberValue: Boolean) {
        viewBinding.apply {
            if (isNumberValue) {
                editValueCounterName.root.visibility = View.GONE
                editValueLayout.root.visibility = View.VISIBLE
                valueTypeMultiStateButton.setChecked(0)
            } else {
                editValueCounterName.root.visibility = View.VISIBLE
                editValueLayout.root.visibility = View.GONE
                valueTypeMultiStateButton.setChecked(1)
            }
        }
    }

    private fun updateNumberValue(newValue: String?) {
        viewBinding.editValueLayout.setText(newValue, InputType.TYPE_CLASS_NUMBER)
    }

    private fun updateCounterValue(newValue: String?) {
        viewBinding.editValueCounterName.setTextValue(newValue)
    }

    private fun updateSaveButton(isValidAction: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, isValidAction)
    }

    private fun showCounterSelectionDialog(onCounterSelected: (String) -> Unit) {
        overlayManager.navigateTo(
            context = context,
            newOverlay = CounterNameSelectionDialog(onCounterSelected),
            hideCurrent = true,
        )
    }

    private fun onActionEditingStateChanged(isEditingAction: Boolean) {
        if (!isEditingAction) {
            Log.e(TAG, "Closing ChangeCounterDialog because there is no action edited")
            finish()
        }
    }
}

private const val TAG = "ChangeCounterDialog"