package com.evening.dailylife.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BlurDialogBuilder : MaterialAlertDialogBuilder {
    constructor(context: Context) : super(context)

    constructor(context: Context, overrideThemeResId: Int) : super(context, overrideThemeResId)

    override fun create(): AlertDialog {
        val dialog = super.create()
        addWindowBlurListener(dialog)
        return dialog
    }

    private fun addWindowBlurListener(dialog: AlertDialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val window = dialog.window
            window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window?.decorView?.addOnAttachStateChangeListener(object :
                View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    window.windowManager?.addCrossWindowBlurEnabledListener { enabled ->
                        updateWindowBlur(window, enabled)
                    }
                }

                @SuppressLint("ImplicitSamInstance")
                override fun onViewDetachedFromWindow(v: View) {
                    window.windowManager?.removeCrossWindowBlurEnabledListener { enabled ->
                        updateWindowBlur(window, enabled)
                    }
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun updateWindowBlur(window: Window, enabled: Boolean) {
        val mDimAmountWithBlur = 0.1f
        val mDimAmountNoBlur = 0.35f
        window.setDimAmount(if (enabled) mDimAmountWithBlur else mDimAmountNoBlur)
        window.attributes.blurBehindRadius = 20
        window.attributes = window.attributes
    }
}