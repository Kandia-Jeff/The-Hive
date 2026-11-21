
package com.example.thehive.ui.common

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/** The four states every list or data screen can be in. */
enum class ScreenState { LOADING, CONTENT, EMPTY, ERROR }

/**
 * Shows only the view that matches [state] and hides the others.
 * Pass the screen's main content view plus the three included state views.
 */
fun setScreenState(
    state: ScreenState,
    content: View,
    loading: View,
    empty: View,
    error: View
) {
    content.visibility = if (state == ScreenState.CONTENT) View.VISIBLE else View.GONE
    loading.visibility = if (state == ScreenState.LOADING) View.VISIBLE else View.GONE
    empty.visibility = if (state == ScreenState.EMPTY) View.VISIBLE else View.GONE
    error.visibility = if (state == ScreenState.ERROR) View.VISIBLE else View.GONE
}

/** Shows a confirmation dialog (e.g. before deleting a listing). */
fun Context.showConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    onConfirm: () -> Unit
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setNegativeButton("Cancel", null)
        .setPositiveButton(confirmText) { _, _ -> onConfirm() }
        .show()
}

/** Shows a short feedback message at the bottom of the screen. */
fun Fragment.showMessage(message: String) {
    view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
}