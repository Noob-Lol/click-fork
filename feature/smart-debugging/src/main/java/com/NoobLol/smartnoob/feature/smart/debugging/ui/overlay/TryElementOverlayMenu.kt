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
package com.NoobLol.smartnoob.feature.smart.debugging.ui.overlay

import android.util.Size
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.NoobLol.smartnoob.core.base.isStopScenarioKey

import com.NoobLol.smartnoob.core.domain.model.scenario.Scenario
import com.NoobLol.smartnoob.core.common.overlays.base.viewModels
import com.NoobLol.smartnoob.core.common.overlays.menu.OverlayMenu
import com.NoobLol.smartnoob.feature.smart.debugging.R
import com.NoobLol.smartnoob.feature.smart.debugging.databinding.OverlayTryElementMenuBinding
import com.NoobLol.smartnoob.feature.smart.debugging.di.DebuggingViewModelsEntryPoint

import kotlinx.coroutines.launch

class TryElementOverlayMenu(
    private val scenario: Scenario,
    private val triedElement: Any,
) : OverlayMenu() {

    /** The view model for this dialog. */
    private val viewModel: TryElementViewModel by viewModels(
        entryPoint = DebuggingViewModelsEntryPoint::class.java,
        creator = { tryElementViewModel() },
    )

    private lateinit var viewBinding: OverlayTryElementMenuBinding

    override fun onCreateMenu(layoutInflater: LayoutInflater): ViewGroup {
        viewModel.setTriedElement(scenario, triedElement)

        viewBinding = OverlayTryElementMenuBinding.inflate(LayoutInflater.from(context))

        return viewBinding.root
    }

    override fun onCreateOverlayView(): DebugOverlayView = DebugOverlayView(context)

    override fun onStart() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.displayResults.collect(::updateDetectionResults) }
            }
        }

        viewModel.startTry(context)
    }

    override fun onStop() {
        viewModel.stopTry()
    }

    override fun getWindowMaximumSize(backgroundView: ViewGroup): Size {
        val bgSize = super.getWindowMaximumSize(backgroundView)
        return Size(
            bgSize.width + context.resources.getDimensionPixelSize(R.dimen.overlay_debug_text_width),
            bgSize.height,
        )
    }

    override fun onMenuItemClicked(viewId: Int) {
        when (viewId) {
            R.id.btn_back -> {
                viewModel.stopTry()
                back()
            }
        }
    }

    override fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        if (!keyEvent.isStopScenarioKey()) return false

        if (keyEvent.action == KeyEvent.ACTION_DOWN) {
            viewModel.stopTry()
            back()
        }

        return true
    }

    private fun updateDetectionResults(results: ResultsDisplay?) {
        (screenOverlayView as? DebugOverlayView)?.setResults(results?.detectionResults ?: emptyList())
        viewBinding.textResult.text = results?.resultText
    }
}