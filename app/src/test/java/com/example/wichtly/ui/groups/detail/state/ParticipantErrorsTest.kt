package com.example.wichtly.ui.groups.detail.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.ParticipantValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for ParticipantErrors.kt.
 *
 * Why these tests matter:
 * Verifies the correct mapping of all [ParticipantValidationError] types to user-friendly
 * error texts, as well as the strict separation of field errors and general errors.
 */
class ParticipantErrorsTest {

    /**
     * Tests that an empty error list returns neither field errors nor general errors.
     */
    @Test
    fun mapParticipantValidationErrors_emptyList_returnsNullErrors() {
        val errors = emptyList<ParticipantValidationError>()
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Tests mapping EmptyName to the corresponding nameError.
     */
    @Test
    fun mapParticipantValidationErrors_emptyName_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.EmptyName)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_empty, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Tests mapping NameTooLong to the corresponding nameError.
     */
    @Test
    fun mapParticipantValidationErrors_nameTooLong_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.NameTooLong)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_too_long, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Tests mapping DuplicateName to the corresponding nameError.
     */
    @Test
    fun mapParticipantValidationErrors_duplicateNameInGroup_returnsCorrectNameError() {
        val errors = listOf(ParticipantValidationError.DuplicateName)
        val result = mapParticipantValidationErrors(errors)

        assertEquals(R.string.error_participant_name_duplicate, result.nameError)
        assertNull(result.generalError)
    }

    /**
     * Tests mapping ParticipantNotFound to the general error (generalError).
     */
    @Test
    fun mapParticipantValidationErrors_participantNotFound_returnsCorrectGeneralError() {
        val errors = listOf(ParticipantValidationError.ParticipantNotFound)
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertEquals(R.string.error_participant_missing, result.generalError)
    }

    /**
     * Tests mapping GroupNotFound to the general error (generalError).
     */
    @Test
    fun mapParticipantValidationErrors_groupNotFound_returnsCorrectGeneralError() {
        val errors = listOf(ParticipantValidationError.GroupNotFound)
        val result = mapParticipantValidationErrors(errors)

        assertNull(result.nameError)
        assertEquals(R.string.error_group_not_found, result.generalError)
    }

    /**
     * Tests that with multiple name errors in the list, only the first one is used as nameError (exactly one text).
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