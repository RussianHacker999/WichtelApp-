package com.example.wichtly.ui.common

import android.database.sqlite.SQLiteFullException
import androidx.annotation.StringRes
import com.example.wichtly.R
import java.io.IOException

/**
 * Fehlertext für einen gescheiterten Schreibvorgang. Ist der Gerätespeicher voll, sagt die App das
 * konkret; der allgemeine Text ("Fehler beim Speichern") hilft dem Nutzer dann nicht weiter.
 *
 * @param e Die gefangene Exception.
 * @param fallback Allgemeiner Fehlertext der Aktion.
 */
@StringRes
fun saveErrorMessage(e: Throwable, @StringRes fallback: Int): Int =
    if (e.isStorageFull()) R.string.error_storage_full else fallback

/**
 * Datenbank voll (SQLite) oder Datei nicht schreibbar, weil kein Platz mehr ist (ENOSPC).
 * Prüft auch die Ursachenkette, weil Room und Dateioperationen Fehler gern einpacken.
 */
internal fun Throwable.isStorageFull(): Boolean =
    generateSequence(this) { it.cause }.any { cause ->
        cause is SQLiteFullException || (cause is IOException && cause.message?.contains("ENOSPC") == true)
    }
