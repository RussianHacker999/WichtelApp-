package com.example.wichtly.ui.groups.detail.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.ParticipantValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit-Tests für ParticipantErrors.kt.
 *
 * Warum diese Tests wichtig sind:
 * Verifiziert die korrekte Übersetzung aller [ParticipantValidationError]-Typen in nutzerfreundliche
 * deutsche Fehlertexte sowie die strikte Trennung von Feldfehlern und allgemeinen Fehlern.
 */
class ParticipantErrorsTest {

    /**
     * Testet, dass bei einer leeren Fehlerliste weder Feld- noch allgemeine Fehler zurückgegeben werden.
     */
    @Test
    fun mapParticipantValidationErrors_emptyList_returnsNullErrors() {
        val errors = emptyList<ParticipantValidationError>()
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Testet die Übersetzung von EmptyName in den entsprechenden nameError.
     */
    @Test
    fun mapParticipantValidationErrors_emptyName_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.EmptyName)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_empty, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Testet die Übersetzung von NameTooLong in den entsprechenden nameError.
     */
    @Test
    fun mapParticipantValidationErrors_nameTooLong_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.NameTooLong)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_too_long, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Testet die Übersetzung von DuplicateNameInGroup in den entsprechenden nameError.
     */
    @Test
    fun mapParticipantValidationErrors_duplicateNameInGroup_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.DuplicateNameInGroup)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_duplicate, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Testet die Übersetzung von ParticipantNotFound in den allgemeinen Fehler (generalError).
     */
    @Test
    fun mapParticipantValidationErrors_participantNotFound_returnsCorrectGeneralError() {
        val errors = listOf(ParticipantValidationError.ParticipantNotFound)
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertEquals(R.string.error_participant_missing, result.generalError)
    }

    /**
     * Testet die Übersetzung von GroupNotFound in den allgemeinen Fehler (generalError).
     */
    @Test
    fun mapParticipantValidationErrors_groupNotFound_returnsCorrectGeneralError() {
        val errors = listOf(ParticipantValidationError.GroupNotFound)
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertEquals(R.string.error_group_not_found, result.generalError)
    }

    /**
     * Testet, dass bei mehreren Namensfehlern in der Liste nur der erste Fehler als nameError übernommen wird (genau ein Text).
     */
    @Test
    fun mapParticipantValidationErrors_multipleNameErrors_returnsSingleFirstNameError() {
        val errors = listOf(
            ParticipantValidationError.EmptyName,
            ParticipantValidationError.NameTooLong
        )
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_empty, result.nameError)
        assertNull(result.generalError)
    }
}
