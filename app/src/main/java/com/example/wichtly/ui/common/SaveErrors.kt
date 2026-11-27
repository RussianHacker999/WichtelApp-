package com.example.wichtly.ui.common

import android.database.sqlite.SQLiteFullException
import androidx.annotation.StringRes
import com.example.wichtly.R
import java.io.IOException

/**
 * Error text for a failed write. If the device storage is full, the app says so specifically;
 * the generic text ("Error while saving") does not help the user in that case.
 *
 * @param e the caught exception.
 * @param fallback generic error text of the action.
 */
@StringRes
fun saveErrorMessage(e: Throwable, @StringRes fallback: Int): Int = if (e.isStorageFull()) R.string.error_storage_full else fallback

/**
 * True if the database is full (SQLite) or a file cannot be written because there is no space left (ENOSPC).
 *
 * Also checks the cause chain, because Room and file operations tend to wrap errors.
 */
internal fun Throwable.isStorageFull(): Boolean = generateSequence(this) { it.cause }.any { cause ->
    cause is SQLiteFullException || (cause is IOException && cause.message?.contains("ENOSPC") == true)
}
