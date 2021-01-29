package com.bsrakdg.trackerappwithgooglemaps.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.bsrakdg.trackerappwithgooglemaps.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog : DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit) {
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(getString(R.string.menu_cancel_tracking_title))
            .setMessage(getString(R.string.menu_cancel_tracking_message))
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(getString(R.string.menu_cancel_tracking_yes)) { _, _ ->
                yesListener?.let { yes ->
                    yes()
                }
            }
            .setNegativeButton(getString(R.string.menu_cancel_tracking_no)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}