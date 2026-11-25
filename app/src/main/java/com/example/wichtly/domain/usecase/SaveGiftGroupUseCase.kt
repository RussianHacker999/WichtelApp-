package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.validation.GiftGroupResult
import com.example.wichtly.domain.validation.GiftGroupValidationError
import com.example.wichtly.domain.validation.GiftGroupValidator
import com.example.wichtly.domain.validation.parseBudgetToCents
import com.example.wichtly.domain.validation.parseDate
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Rohe Formulareingaben einer Wichtelgruppe, so wie der Nutzer sie getippt hat.
 * Budget und Wichteltag bleiben Text, damit Format- und Fachfehler an einer Stelle geprüft werden.
 */
data class GiftGroupInput(
    val name: String,
    val budget: String,
    val drawDay: String,
    val drawType: DrawType,
    val photoPath: String?,
)

/**
 * Legt eine Wichtelgruppe an oder ändert sie. Einzige Stelle, an der Gruppen geprüft werden.
 *
 * Ablauf: Bestand laden → beim Bearbeiten Existenz prüfen → Eingaben parsen und alle Regeln in
 * einem Durchlauf prüfen → nur bei fehlerfreiem Ergebnis schreiben. So meldet das Formular schon
 * beim ersten Speichern sämtliche Fehler (auch den leeren Pflicht-Namen neben Formatfehlern).
 *
 * @param today Liefert das aktuelle Datum; in Tests durch ein festes Datum ersetzbar.
 */
class SaveGiftGroupUseCase(
    private val repository: GiftGroupRepository,
    private val validator: GiftGroupValidator = GiftGroupValidator(),
    private val today: () -> LocalDate = { LocalDate.now() },
) {
    /**
     * @param groupId null legt eine neue Gruppe an, sonst wird die Gruppe mit dieser ID geändert.
     */
    suspend operator fun invoke(groupId: String?, input: GiftGroupInput): GiftGroupResult {
        val existingGroups = repository.getGiftGroups().first()
        val stored = groupId?.let { id ->
            existingGroups.firstOrNull { it.id == id } ?: return notFound()
        }

        val name = validator.normalizeName(input.name)
        val budgetCents = parseBudgetToCents(input.budget)
        val drawDay = parseDate(input.drawDay)

        val errors = buildList {
            addAll(validator.validateName(name))
            addAll(validator.validateUniqueName(name, groupId, existingGroups))
            if (budgetCents == null) {
                add(GiftGroupValidationError.InvalidBudgetFormat)
            } else {
                addAll(validator.validateBudget(budgetCents))
            }
            if (drawDay == null) {
                add(GiftGroupValidationError.InvalidDateFormat)
            } else {
                addAll(validator.validateDrawDay(drawDay, stored?.drawDay, today()))
            }
        }
        if (errors.isNotEmpty() || budgetCents == null || drawDay == null) {
            return GiftGroupResult.ValidationError(errors)
        }

        return if (stored == null) {
            // Neuanlage: Das Domain-Modell erzeugt die UUID über den Standard-Parameter
            val group = GiftGroup(
                name = name,
                budgetCents = budgetCents,
                drawDay = drawDay,
                drawType = input.drawType,
                photoPath = input.photoPath,
            )
            repository.insertGiftGroup(group)
            GiftGroupResult.Success(group)
        } else {
            // Bearbeiten: id bleibt über copy() erhalten
            val group = stored.copy(
                name = name,
                budgetCents = budgetCents,
                drawDay = drawDay,
                drawType = input.drawType,
                photoPath = input.photoPath,
            )
            // Zwischen Laden und Schreiben gelöscht: nichts geändert, also kein Erfolg melden
            if (!repository.updateGiftGroup(group)) return notFound()
            GiftGroupResult.Success(group)
        }
    }

    private fun notFound() = GiftGroupResult.ValidationError(listOf(GiftGroupValidationError.GroupNotFound))
}
