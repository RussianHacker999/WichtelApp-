package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.result.GiftGroupResult
import com.example.wichtly.domain.validation.GiftGroupValidationError
import com.example.wichtly.domain.validation.GiftGroupValidator
import com.example.wichtly.domain.validation.parseBudgetToCents
import com.example.wichtly.domain.validation.parseDate
import com.example.wichtly.logging.AppLog
import java.time.LocalDate

private const val TAG = "SaveGiftGroupUseCase"

/**
 * Raw form input of a gift group, exactly as the user typed it.
 *
 * Budget and draw day stay text, so format errors and business errors are checked in one place.
 *
 * Convention for all use cases: from three business input fields on there is a separate `…Input`
 * object (five fields here); below that, single parameters (SaveParticipantUseCase, SaveWishlistItemUseCase).
 *
 * @property budget budget in euros as typed, e.g. "25" or "25,50".
 * @property drawDay draw day as typed, in the format [com.example.wichtly.domain.validation.DRAW_DAY_INPUT_PATTERN].
 * @property photoPath relative path of the photo, or null.
 */
data class GiftGroupInput(val name: String, val budget: String, val drawDay: String, val drawType: DrawType, val photoPath: String?)

/**
 * Creates or updates a gift group. The only place where groups are validated.
 *
 * Flow: load existing groups → when editing, check that the group exists → parse the input and
 * check all rules in one pass → write only if there are no errors. That way the form reports
 * all errors on the first save (including the empty required name next to format errors).
 *
 * @param today supplies the current date; replaced by a fixed date in tests.
 */
class SaveGiftGroupUseCase(
    private val repository: GiftGroupRepository,
    private val validator: GiftGroupValidator = GiftGroupValidator(),
    private val today: () -> LocalDate = { LocalDate.now() },
) {
    /**
     * @param groupId null creates a new group; otherwise the group with this ID is updated.
     */
    suspend operator fun invoke(groupId: String?, input: GiftGroupInput): GiftGroupResult {
        AppLog.d(TAG) { if (groupId == null) "Saving new group" else "Saving group $groupId" }
        val existingGroups = repository.getGiftGroups()
        val stored = groupId?.let { id ->
            existingGroups.firstOrNull { it.id == id } ?: return notFound(id)
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
            AppLog.d(TAG) { "Input rejected: ${errors.joinToString()}" }
            return GiftGroupResult.ValidationError(errors)
        }

        return if (stored == null) {
            // New group: the domain model creates the UUID via its default parameter
            val group = GiftGroup(
                name = name,
                budgetCents = budgetCents,
                drawDay = drawDay,
                drawType = input.drawType,
                photoPath = input.photoPath,
            )
            repository.insertGiftGroup(group)
            AppLog.i(TAG, "Group ${group.id} created")
            GiftGroupResult.Success(group)
        } else {
            // Editing: the id is kept via copy()
            val group = stored.copy(
                name = name,
                budgetCents = budgetCents,
                drawDay = drawDay,
                drawType = input.drawType,
                photoPath = input.photoPath,
            )
            // Deleted between loading and writing: nothing was changed, so do not report success
            if (!repository.updateGiftGroup(group)) return notFound(group.id)
            AppLog.i(TAG, "Group ${group.id} updated")
            GiftGroupResult.Success(group)
        }
    }

    private fun notFound(groupId: String): GiftGroupResult {
        AppLog.w(TAG, "Group $groupId not found, nothing saved")
        return GiftGroupResult.ValidationError(listOf(GiftGroupValidationError.GroupNotFound))
    }
}
