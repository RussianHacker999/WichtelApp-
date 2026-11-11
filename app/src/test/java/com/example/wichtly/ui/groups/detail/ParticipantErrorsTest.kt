package com.example.wichtly.ui.groups.detail

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

        assertEquals(EMPTY_PARTICIPANT_NAME_ERROR, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Testet die Übersetzung von NameTooLong in den entsprechenden nameError.
     */
    @Test
    fun mapParticipantValidationErrors_nameTooLong_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.NameTooLong)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(PARTICIPANT_NAME_TOO_LONG_ERROR, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Testet die Übersetzung von DuplicateNameInGroup in den entsprechenden nameError.
     */
    @Test
    fun mapParticipantValidationErrors_duplicateNameInGroup_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.DuplicateNameInGroup)
        val result = mapParticipantValidationErrors(errors)

        assertEquals("Ein Teilnehmer mit diesem Namen existiert bereits in dieser Gruppe.", result.nameError)
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
        assertEquals("Der Teilnehmer wurde nicht gefunden.", result.generalError)
    }

    /**
     * Testet die Übersetzung von GroupNotFound in den allgemeinen Fehler (generalError).
     */
    @Test
    fun mapParticipantValidationErrors_groupNotFound_returnsCorrectGeneralError() {
        val errors = listOf(ParticipantValidationError.GroupNotFound)
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertEquals("Die Wichtelgruppe wurde nicht gefunden.", result.generalError)
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

        assertEquals(EMPTY_PARTICIPANT_NAME_ERROR, result.nameError)
        assertNull(result.generalError)
    }
}
