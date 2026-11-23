package com.example.wichtly.ui.components.photo

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
    val fieldTitle: String,
    val emptyTitle: String,
    val emptyHint: String,
    val dialogTitle: String,
) {
    /**
     * Konfiguration für Wichtelgruppen-Fotos.
     */
    GROUP(
        fieldTitle = "Gruppenfoto (optional)",
        emptyTitle = "Noch kein Gruppenfoto",
        emptyHint = "Die App funktioniert auch ohne Foto.",
        dialogTitle = "Gruppenfoto",
    ),

    /**
     * Konfiguration für Teilnehmer-Fotos.
     */
    PARTICIPANT(
        fieldTitle = "Teilnehmerfoto (optional)",
        emptyTitle = "Noch kein Foto",
        emptyHint = "Die Wunschliste und Verwaltung funktionieren auch ohne Foto.",
        dialogTitle = "Teilnehmerfoto",
    ),
}
