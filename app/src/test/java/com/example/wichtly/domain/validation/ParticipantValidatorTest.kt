package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.Participant
import org.junit.Assert.assertTrue
import org.junit.Test

class ParticipantValidatorTest {

    private val validator = ParticipantValidator()
    private val anna = Participant(id = "p1", groupId = "g1", name = "Anna")

    @Test
    fun validParticipantHasNoErrors() {
        assertTrue(validator.validate(anna, emptyList()).isEmpty())
    }

    @Test
    fun emptyNameIsError() {
        val errors = validator.validate(anna.copy(name = ""), emptyList())
        assertTrue(ParticipantValidationError.EmptyName in errors)
    }

    @Test
    fun nameOver50CharactersIsError() {
        val errors = validator.validate(anna.copy(name = "b".repeat(51)), emptyList())
        assertTrue(ParticipantValidationError.NameTooLong in errors)
    }

    @Test
    fun duplicateNameInGroupIsError() {
        val existing = listOf(Participant(id = "p2", groupId = "g1", name = " anna "))
        val errors = validator.validate(anna, existing)
        assertTrue(ParticipantValidationError.DuplicateName in errors)
    }

    @Test
    fun ownNameWhenEditingIsNotDuplicate() {
        assertTrue(validator.validate(anna, listOf(anna)).isEmpty())
    }
}