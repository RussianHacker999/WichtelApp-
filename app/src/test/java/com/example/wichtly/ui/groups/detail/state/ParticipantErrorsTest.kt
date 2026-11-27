package com.example.wichtly.ui.groups.detail.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.ParticipantValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for ParticipantErrors.kt.
 *
 * Covers the translation of every [ParticipantValidationError] type into a user-facing error text
 * and the strict separation of field errors and general errors.
 */
class ParticipantErrorsTest {

    /**
     * An empty error list returns neither a field error nor a general error.
     */
    @Test
    fun mapParticipantValidationErrors_emptyList_returnsNullErrors() {
        val errors = emptyList<ParticipantValidationError>()
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertNull(result.generalError)
    }

    /**
     * EmptyName is translated into the matching nameError.
     */
    @Test
    fun mapParticipantValidationErrors_emptyName_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.EmptyName)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_empty, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * NameTooLong is translated into the matching nameError.
     */
    @Test
    fun mapParticipantValidationErrors_nameTooLong_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.NameTooLong)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_too_long, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * DuplicateName is translated into the matching nameError.
     */
    @Test
    fun mapParticipantValidationErrors_duplicateNameInGroup_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.DuplicateName)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_duplicate, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * ParticipantNotFound is translated into the general error (generalError).
     */
    @Test
    fun mapParticipantValidationErrors_participantNotFound_returnsCorrectGeneralError() {
        val errors = listOf(ParticipantValidationError.ParticipantNotFound)
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertEquals(R.string.error_participant_missing, result.generalError)
    }

    /**
     * GroupNotFound is translated into the general error (generalError).
     */
    @Test
    fun mapParticipantValidationErrors_groupNotFound_returnsCorrectGeneralError() {
        val errors = listOf(ParticipantValidationError.GroupNotFound)
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertEquals(R.string.error_group_not_found, result.generalError)
    }

    /**
     * With several name errors in the list, only the first one becomes the nameError (exactly one text).
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
