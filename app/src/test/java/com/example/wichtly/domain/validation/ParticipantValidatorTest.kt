package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.Participant
import org.junit.Assert.assertTrue
import org.junit.Test

class ParticipantValidatorTest {

    private val validator = ParticipantValidator()
    private val anna = Participant(id = "p1", groupId = "g1", name = "Anna")

    @Test
    fun gueltigerTeilnehmerHatKeineFehler() {
        assertTrue(validator.validate(anna, emptyList()).isEmpty())
    }

    @Test
    fun leererNameIstFehler() {
        val fehler = validator.validate(anna.copy(name = ""), emptyList())
        assertTrue(ParticipantValidationError.EmptyName in fehler)
    }

    @Test
    fun nameUeber50ZeichenIstFehler() {
        val fehler = validator.validate(anna.copy(name = "b".repeat(51)), emptyList())
        assertTrue(ParticipantValidationError.NameTooLong in fehler)
    }

    @Test
    fun doppelterNameInGruppeIstFehler() {
        val vorhanden = listOf(Participant(id = "p2", groupId = "g1", name = " anna "))
        val fehler = validator.validate(anna, vorhanden)
        assertTrue(ParticipantValidationError.DuplicateName in fehler)
    }

    @Test
    fun eigenerNameBeimBearbeitenIstKeinDuplikat() {
        assertTrue(validator.validate(anna, listOf(anna)).isEmpty())
    }
}
