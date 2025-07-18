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
package com.nooblol.smartnoob.feature.smart.config.ui.action.intent

import android.util.Log
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.nooblol.smartnoob.core.ui.bindings.dialogs.DialogNavigationButton
import com.nooblol.smartnoob.core.ui.bindings.dialogs.setButtonEnabledState
import com.nooblol.smartnoob.core.common.overlays.base.viewModels
import com.nooblol.smartnoob.core.common.overlays.dialog.implementation.navbar.NavBarDialog
import com.nooblol.smartnoob.core.common.overlays.dialog.implementation.navbar.NavBarDialogContent
import com.nooblol.smartnoob.core.ui.bindings.dialogs.setButtonVisibility
import com.nooblol.smartnoob.feature.smart.config.R
import com.nooblol.smartnoob.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.nooblol.smartnoob.feature.smart.config.ui.action.OnActionConfigCompleteListener
import com.nooblol.smartnoob.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView

import kotlinx.coroutines.launch

class IntentDialog(
    private val listener: OnActionConfigCompleteListener,
) : NavBarDialog(R.style.ScenarioConfigTheme) {

    /** The view model for this dialog. */
    private val viewModel: IntentViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { intentViewModel() },
    )

    override fun onCreateView(): ViewGroup {
        return super.onCreateView().also {
            topBarBinding.apply {
                dialogTitle.setText(R.string.dialog_title_intent)
                setButtonVisibility(DialogNavigationButton.SAVE, View.VISIBLE)
                setButtonVisibility(DialogNavigationButton.DELETE, View.VISIBLE)
            }
        }
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        navBarView.selectedItemId =
            if (viewModel.isAdvanced()) R.id.page_advanced
            else R.id.page_simple

        super.onDialogCreated(dialog)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingAction.collect(::onActionEditingStateChanged) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.isValidAction.collect(::updateSaveButton) }
            }
        }
    }

    override fun inflateMenu(navBarView: NavigationBarView) {
        navBarView.inflateMenu(R.menu.menu_intent_config)
    }

    override fun onCreateContent(navItemId: Int): NavBarDialogContent {
        return when (navItemId) {
            R.id.page_simple -> SimpleIntentContent(context.applicationContext)
            R.id.page_advanced -> AdvancedIntentContent(context.applicationContext)
            else -> throw IllegalArgumentException("Unknown menu id $navItemId")
        }
    }

    override fun onContentViewChanged(navItemId: Int) {
        when (navItemId) {
            R.id.page_simple -> viewModel.setIsAdvancedConfiguration(false)
            R.id.page_advanced -> viewModel.setIsAdvancedConfiguration(true)
        }
    }

    override fun onDialogButtonPressed(buttonType: DialogNavigationButton) {
        when (buttonType) {
            DialogNavigationButton.SAVE -> {
                viewModel.saveLastConfig()
                listener.onConfirmClicked()
                super.back()
            }
            DialogNavigationButton.DISMISS -> back()
            DialogNavigationButton.DELETE -> {
                listener.onDeleteClicked()
                super.back()
            }
            else -> Unit
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

    private fun updateSaveButton(isValidCondition: Boolean) {
        topBarBinding.setButtonEnabledState(DialogNavigationButton.SAVE, isValidCondition)
    }

    private fun onActionEditingStateChanged(isEditingAction: Boolean) {
        if (!isEditingAction) {
            Log.e(TAG, "Closing IntentDialog because there is no action edited")
            finish()
        }
    }
}

private const val TAG = "IntentDialog"