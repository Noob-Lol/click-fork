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
package com.nooblol.smartnoob.core.common.overlays.dialog

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

import androidx.annotation.CallSuper
import androidx.annotation.StyleRes
import androidx.coordinatorlayout.widget.CoordinatorLayout

import com.nooblol.smartnoob.core.base.addDumpTabulationLvl
import com.nooblol.smartnoob.core.common.overlays.base.BaseOverlay
import com.nooblol.smartnoob.core.common.overlays.manager.OverlayManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

import java.io.PrintWriter

/**
 * Controller for a dialog opened from a service as an overlay.
 *
 * This class ensure that all dialogs opened from a service will have the same behaviour. It provides basic lifecycle
 * alike methods to ease the view initialization/cleaning.
 */
abstract class OverlayDialog(@StyleRes theme: Int? = null) : BaseOverlay(theme, recreateOnRotation = true) {

    /** The Android InputMethodManger, for ensuring the keyboard dismiss on dialog dismiss. */
    private lateinit var inputMethodManager: InputMethodManager
    /** Touch listener hiding the software keyboard and propagating the touch event normally. */
    protected val hideSoftInputTouchListener = { view: View, _: MotionEvent ->
        hideSoftInput()
        view.performClick()
    }

    /** Tells if the dialog is visible. */
    private var isShown = false

    /**
     * The dialog currently displayed by this controller.
     * Null until [onDialogCreated] is called, or if it has been dismissed.
     */
    protected var dialog: BottomSheetDialog? = null
        private set

    /**
     * The coordinator layout of the dialog.
     * Null until [onDialogCreated] is called, or if the dialog has been dismissed.
     */
    protected var dialogCoordinatorLayout: CoordinatorLayout? = null
        private set

    /**
     * Creates the dialog shown by this controller.
     * Note that the cancelable value and the dismiss listener will be overridden with internal values once, so any
     * values for them defined here will not be kept.
     *
     * @return the builder for the dialog to be created.
     */
    protected abstract fun onCreateView(): ViewGroup

    /**
     * Setup the dialog view.
     * Called once the dialog is created and first show, it allows the implementation to initialize the content views.
     *
     * @param dialog the newly created dialog.
     */
    protected abstract fun onDialogCreated(dialog: BottomSheetDialog)

    final override fun onCreate() {
        inputMethodManager = context.getSystemService(InputMethodManager::class.java)

        dialog = BottomSheetDialog(context).apply {
            val view = onCreateView()

            setContentView(view)
            setCancelable(false)
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    this@OverlayDialog.back()
                    true
                } else {
                    false
                }
            }
            create()

            window?.apply {
                setType(OverlayManager.OVERLAY_WINDOW_TYPE)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                decorView.setOnTouchListener(hideSoftInputTouchListener)
            }

            dialogCoordinatorLayout = (view.parent.parent as CoordinatorLayout)

            behavior.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                isDraggable = false
            }
        }

        onDialogCreated(dialog!!)
    }

    @CallSuper
    override fun onStart() {
        if (isShown) return

        isShown = true
        dialog?.show()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        hideSoftInput()
    }

    @CallSuper
    override fun onStop() {
        if (!isShown) return

        hideSoftInput()
        dialog?.hide()
        isShown = false
    }

    @CallSuper
    override fun onDestroy() {
        dialog?.dismiss()
        dialog = null
    }

    /** Hide automatically the software keyboard when the provided view lose the focus. */
    fun hideSoftInputOnFocusLoss(view: View) {
        view.setOnFocusChangeListener { v, hasFocus ->
            if (view.id == v.id && !hasFocus) {
                hideSoftInput()
            }
        }
    }

    /** Hide the software keyboard. */
    private fun hideSoftInput() {
        dialog?.let {
            inputMethodManager.hideSoftInputFromWindow(it.window!!.decorView.windowToken, 0)
        }
    }

    override fun dump(writer: PrintWriter, prefix: CharSequence) {
        super.dump(writer, prefix)
        val contentPrefix = prefix.addDumpTabulationLvl()

        writer.append(contentPrefix)
            .append("isDialogShown=$isShown; ")
            .println()
    }
}