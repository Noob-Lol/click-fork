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
package com.nooblol.smartnoob.feature.revenue.ui.paywall

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.annotation.AttrRes

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.nooblol.smartnoob.core.base.extensions.getThemeColor
import com.nooblol.smartnoob.core.ui.bindings.buttons.LoadableButtonState
import com.nooblol.smartnoob.core.ui.utils.hideProgress
import com.nooblol.smartnoob.core.ui.utils.showProgress
import com.nooblol.smartnoob.feature.revenue.R
import com.nooblol.smartnoob.feature.revenue.databinding.FragmentAdsLoadingDialogBinding

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
internal class PaywallFragment : DialogFragment() {

    companion object {
        /** Tag for ads loading dialog fragment. */
        const val FRAGMENT_TAG = "AdsLoadingDialog"
    }

    /** ViewModel providing the click scenarios data to the UI. */
    private val viewModel: AdsLoadingViewModel by viewModels()
    /** The view binding on the views of this dialog. */
    private lateinit var viewBinding: FragmentAdsLoadingDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.dialogState.collect(::updateDialogState) }
            }
        }

        viewModel.loadAdIfNeeded(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = FragmentAdsLoadingDialogBinding.inflate(layoutInflater).apply {
            buttonTrial.setOnClickListener {
                viewModel.requestTrial()
                dismiss()
            }
            buttonWatchAd.setOnClickListener {
                activity?.let(viewModel::showAd)
            }
            buttonBuy.setOnClickListener { activity?.let(viewModel::launchPlayStoreBillingFlow) }
        }

        return BottomSheetDialog(requireContext()).apply {
            setContentView(viewBinding.root)
            setCancelable(false)
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    this@PaywallFragment.dismiss()
                    true
                } else {
                    false
                }
            }

            create()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    private fun updateDialogState(state: DialogState) {
        when (state) {
            is DialogState.NotPurchased -> toNotPurchasedState(state)
            DialogState.Purchased -> toPurchasedState()
            DialogState.AdShowing -> toAdShowingState()
            DialogState.AdWatched -> toAdWatchedState()
        }
    }

    private fun toNotPurchasedState(state: DialogState.NotPurchased) {
        viewBinding.apply {
            purchaseText.visibility = View.VISIBLE
            purchasedText.visibility = View.INVISIBLE
            textAdWatched.visibility = View.INVISIBLE
            progressAdWatched.visibility = View.INVISIBLE

            buttonTrial.visibility = View.VISIBLE
            buttonTrial.setState(state.trialButtonState, R.attr.colorPrimary)

            buttonWatchAd.visibility = View.VISIBLE
            buttonWatchAd.setState(state.adButtonState, R.attr.colorPrimary)

            buttonBuy.visibility = View.VISIBLE
            buttonBuy.setState(state.purchaseButtonState, R.attr.colorOnPrimary)
            buttonBuy.setOnClickListener { activity?.let(viewModel::launchPlayStoreBillingFlow) }
        }
    }

    private fun toPurchasedState() {
        viewBinding.apply {
            purchaseText.visibility = View.INVISIBLE
            purchasedText.visibility = View.VISIBLE
            textAdWatched.visibility = View.INVISIBLE
            progressAdWatched.visibility = View.INVISIBLE

            buttonTrial.visibility = View.INVISIBLE
            buttonWatchAd.visibility = View.INVISIBLE

            buttonBuy.visibility = View.VISIBLE
            buttonBuy.setState(
                LoadableButtonState.Loaded.Enabled(requireContext().getString(R.string.button_text_understood)),
                R.attr.colorOnPrimary,
            )
            buttonBuy.setOnClickListener { dismiss() }
        }
    }

    private fun toAdShowingState() {
        viewBinding.apply {
            purchaseText.visibility = View.INVISIBLE
            purchasedText.visibility = View.INVISIBLE
            textAdWatched.visibility = View.INVISIBLE
            progressAdWatched.show()

            buttonTrial.visibility = View.INVISIBLE
            buttonWatchAd.visibility = View.INVISIBLE
            buttonBuy.visibility = View.INVISIBLE
        }
    }

    private fun toAdWatchedState() {
        viewBinding.apply {
            purchaseText.visibility = View.INVISIBLE
            purchasedText.visibility = View.INVISIBLE
            textAdWatched.visibility = View.VISIBLE

            textAdWatched.animateShow()
            progressAdWatched.show()

            buttonTrial.visibility = View.INVISIBLE
            buttonWatchAd.visibility = View.INVISIBLE
            buttonBuy.visibility = View.INVISIBLE
        }

        lifecycleScope.launch {
            delay(1.seconds)
            dismiss()
        }
    }

    private fun View.animateShow() {
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f)
            .setDuration(250)
    }
}

private fun MaterialButton.setState(state: LoadableButtonState, @AttrRes iconColor: Int) {
    text = state.text

    when (state) {
        is LoadableButtonState.Loading -> {
            showProgress(context.getThemeColor(iconColor))
            alpha = 0.75f
            isClickable = false
        }

        is LoadableButtonState.Loaded -> {
            hideProgress()
            when (state) {
                is LoadableButtonState.Loaded.Enabled -> {
                    alpha = 1f
                    isClickable = true
                }
                is LoadableButtonState.Loaded.Disabled -> {
                    alpha = 0.75f
                    isClickable = false
                }
            }
        }
    }
}