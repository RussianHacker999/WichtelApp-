package com.example.wichtly.ui.components.photo

import androidx.annotation.StringRes
import com.example.wichtly.R

/**
 * Kennzeichnet den Anwendungsbereich für die Foto-Funktionalität (Wichtelgruppe oder Teilnehmer).
 *
 * Warum als Enum?
 * Erlaubt eine saubere Wiederverwendung derselben zustandslosen UI-Bausteine ([PhotoPickerField], [PhotoSourceDialog])
 * für sowohl Gruppen- als auch Teilnehmerfotos.
 *
 * Hinweis: Seit dem Redesign sehen Fotofeld ("Foto") und Auswahldialog ("Foto hinzufügen") für Gruppe und
 * Teilnehmer gleich aus. Die folgenden Texte werden deshalb aktuell nirgends angezeigt.
 *
 * @property fieldTitle Überschrift des Eingabebereichs im Formular oder Dialog.
 * @property emptyTitle Titel für den leeren Zustand (noch kein Foto vorhanden).
 * @property emptyHint Untertitel/Hinweistext für den leeren Zustand.
 * @property dialogTitle Titel des Quellen-Auswahldialogs.
 */
enum class PhotoArea(
    @StringRes val fieldTitle: Int,
    @StringRes val emptyTitle: Int,
    @StringRes val emptyHint: Int,
    @StringRes val dialogTitle: Int,
) {
    /**
     * Konfiguration für Wichtelgruppen-Fotos.
     */
    GROUP(
        fieldTitle = R.string.photo_area_group_field_title,
        emptyTitle = R.string.photo_area_group_empty_title,
        emptyHint = R.string.photo_area_group_empty_hint,
        dialogTitle = R.string.photo_area_group_dialog_title,
    ),

    /**
     * Konfiguration für Teilnehmer-Fotos.
     */
    PARTICIPANT(
        fieldTitle = R.string.photo_area_participant_field_title,
        emptyTitle = R.string.photo_area_participant_empty_title,
        emptyHint = R.string.photo_area_participant_empty_hint,
        dialogTitle = R.string.photo_area_participant_dialog_title,
    ),
}
