package com.example.wichtly.ui.draws

import com.example.wichtly.domain.model.DrawType
import java.time.LocalDate

/**
 * UI-Modell eines gespeicherten Auslosungs-Durchlaufs für die Detailansicht.
 *
 * @property id Eindeutige ID der Auslosung.
 * @property groupId ID der Wichtelgruppe.
 * @property groupName Aktueller Name der Wichtelgruppe (kein Snapshot: er wird mit der Auslosung nicht
 *   gespeichert, sondern nachgeladen). null = Name konnte nicht geladen werden; die UI zeigt dann den
 *   Platzhalter aus strings.xml.
 * @property createdAt Datum der Auslosung.
 * @property drawType Gewählte Wichtelart (z. B. NORMAL oder JUNK).
 * @property assignments Liste der Zuordnungen (Wer schenkt wem) dieses Durchlaufs.
 */
data class DrawDetailUiModel(
    val id: String,
    val groupId: String,
    val groupName: String?,
    val createdAt: LocalDate,
    val drawType: DrawType,
    val assignments: List<DrawAssignmentUiModel>,
)

/**
 * UI-Modell einer einzelnen Ziehungs-Zuordnung (Schenker -> Beschenkter)
 * inklusive der historischen Wunschliste des Beschenkten.
 *
 * @property giverId ID der schenkenden Person.
 * @property giverName Name der schenkenden Person.
 * @property receiverId ID der beschenkten Person.
 * @property receiverName Name der beschenkten Person.
 * @property wishes Liste der Wunschtexte der beschenkten Person zum Zeitpunkt der Auslosung (Snapshot).
 */
data class DrawAssignmentUiModel(
    val giverId: String,
    val giverName: String,
    val receiverId: String,
    val receiverName: String,
    val wishes: List<String>
)
