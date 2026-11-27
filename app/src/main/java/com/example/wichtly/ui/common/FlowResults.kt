package com.example.wichtly.ui.common

import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Turns a read Flow into a Flow of [Result]: data as success, an error as failure.
 * The error is logged as ERROR; the Flow then completes as before.
 *
 * The loading error belongs to the state of the Flow. As a flag in the local state it would stay
 * set, even when the screen subscribes again later and the data loads this time.
 *
 * @param tag log tag of the ViewModel.
 * @param logMessage description for the log (without personal data).
 */
fun <T> Flow<T>.asResult(tag: String, logMessage: String): Flow<Result<T>> =
    map { Result.success(it) }
        .catch { e ->
            if (e is CancellationException) throw e
            AppLog.e(tag, logMessage, e)
            emit(Result.failure(e))
        }
