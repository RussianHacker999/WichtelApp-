package com.example.wichtly.ui.draws

import com.example.wichtly.domain.model.DrawType
import java.time.LocalDate

/**
 * TEMPORÄRER PLACEHOLDER:
 * Diese Data Class dient als vorübergehendes Datenmodell für einen Auslosungs-Durchlauf
 * während der UI-Entwicklung.
 *
 * HINWEIS:
 * Sie wird zu einem späteren Zeitpunkt durch das reale Domain-Modell aus dem Domain-Layer (Person B) ersetzt.
 *
 * @property id Eindeutige ID der Auslosung.
 * @property groupId ID der Wichtelgruppe.
 * @property groupName Name der Wichtelgruppe zum Zeitpunkt der Auslosung (Snapshot).
 * @property createdAt Datum der Auslosung.
 * @property drawType Gewählte Wichtelart (z. B. NORMAL oder JUNK).
 * @property assignments Liste der Zuordnungen (Wer schenkt wem) dieses Durchlaufs.
 */
data class TemporaryDraw(
    val id: String,
    val groupId: String,
    val groupName: String,
    val createdAt: LocalDate,
    val drawType: DrawType,
    val assignments: List<TemporaryDrawAssignment>,
)

/**
 * TEMPORÄRER PLACEHOLDER:
 * Diese Data Class stellt eine einzelne Ziehungs-Zuordnung (Schenker -> Beschenkter)
 * inklusive der historischen Wunschliste des Beschenkten dar.
 *
 * HINWEIS:
 * Sie wird zu einem späteren Zeitpunkt durch das reale Domain-Modell aus dem Domain-Layer (Person B) ersetzt.
 *
 * @property giverId ID der schenkenden Person.
 * @property giverName Name der schenkenden Person.
 * @property receiverId ID der beschenkten Person.
 * @property receiverName Name der beschenkten Person.
 * @property wishes Liste der Wunschtexte der beschenkten Person zum Zeitpunkt der Auslosung (Snapshot).
 */
data class TemporaryDrawAssignment(
    val giverId: String,
    val giverName: String,
    val receiverId: String,
    val receiverName: String,
    val wishes: List<String>
)
