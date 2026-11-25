package com.example.wichtly.ui.draws.detail.screen

import com.example.wichtly.domain.model.group.DrawType
import java.time.LocalDateTime

/**
 * UI-Modell eines gespeicherten Auslosungs-Durchlaufs für die Detailansicht.
 *
 * @property id Eindeutige ID der Auslosung.
 * @property groupId ID der Wichtelgruppe.
 * @property groupName Aktueller Name der Wichtelgruppe (kein Snapshot: er wird mit der Auslosung nicht
 *   gespeichert, sondern nachgeladen). null = Name konnte nicht geladen werden; die UI zeigt dann den
 *   Platzhalter aus strings.xml.
 * @property drawnAt Datum und Uhrzeit der Auslosung (lokale Zeitzone).
 * @property drawType Gewählte Wichtelart (z. B. NORMAL oder JUNK).
 * @property assignments Liste der Zuordnungen (Wer schenkt wem) dieses Durchlaufs.
 */
data class DrawDetailUiModel(
    val id: String,
    val groupId: String,
    val groupName: String?,
    val drawnAt: LocalDateTime,
    val drawType: DrawType,
    val assignments: List<DrawAssignmentUiModel>,
)

/**
 * UI-Modell einer einzelnen Ziehungs-Zuordnung (Schenker -> Beschenkter)
 * inklusive der historischen Wunschliste des Beschenkten.
 *
 * @property id Primärschlüssel der Zuordnung; eindeutiger Listen-Key, auch wenn eine Person doppelt vorkäme.
 * @property giverId ID der schenkenden Person.
 * @property giverName Name der schenkenden Person.
 * @property receiverId ID der beschenkten Person.
 * @property receiverName Name der beschenkten Person.
 * @property wishes Liste der Wunschtexte der beschenkten Person zum Zeitpunkt der Auslosung (Snapshot).
 */
data class DrawAssignmentUiModel(
    val id: String,
    val giverId: String,
    val giverName: String,
    val receiverId: String,
    val receiverName: String,
    val wishes: List<String>
)
