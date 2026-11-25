package com.example.wichtly.ui.common

import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Macht aus einem Lese-Flow einen Flow von [Result]: Daten als success, ein Fehler als failure.
 * Der Fehler wird als ERROR geloggt, der Flow endet danach wie zuvor.
 *
 * Warum: Der Ladefehler gehört zum Stand des Flows. Als Flag im lokalen Zustand blieb er stehen,
 * auch wenn der Screen später erneut abonniert und die Daten diesmal geladen werden.
 *
 * @param tag Log-Tag des ViewModels.
 * @param logMessage Beschreibung für das Log (ohne personenbezogene Daten).
 */
fun <T> Flow<T>.asResult(tag: String, logMessage: String): Flow<Result<T>> =
    map { Result.success(it) }
        .catch { e ->
            if (e is CancellationException) throw e
            AppLog.e(tag, logMessage, e)
            emit(Result.failure(e))
        }
