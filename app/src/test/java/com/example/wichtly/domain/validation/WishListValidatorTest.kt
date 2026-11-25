package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.WishListItem
import org.junit.Assert.assertTrue
import org.junit.Test

class WishListValidatorTest {

    private val validator = WishListValidator()
    private val wunsch = WishListItem(id = "w1", participantId = "p1", text = "Buch")

    @Test
    fun gueltigerWunschHatKeineFehler() {
        assertTrue(validator.validate(wunsch, emptyList()).isEmpty())
    }

    @Test
    fun leererTextIstFehler() {
        val fehler = validator.validate(wunsch.copy(text = "   "), emptyList())
        assertTrue(WishListValidationError.EmptyText in fehler)
    }

    @Test
    fun genau200ZeichenSindErlaubt() {
        assertTrue(validator.validate(wunsch.copy(text = "x".repeat(200)), emptyList()).isEmpty())
    }

    @Test
    fun textUeber200ZeichenIstFehler() {
        val fehler = validator.validate(wunsch.copy(text = "x".repeat(201)), emptyList())
        assertTrue(WishListValidationError.TextTooLong in fehler)
    }

    @Test
    fun gleicherWunschIstDuplikatAuchMitAndererSchreibweise() {
        val vorhanden = listOf(WishListItem(id = "w2", participantId = "p1", text = " BUCH "))
        assertTrue(WishListValidationError.DuplicateText in validator.validate(wunsch, vorhanden))
    }

    @Test
    fun eigenerWunschBeimBearbeitenIstKeinDuplikat() {
        assertTrue(validator.validate(wunsch, listOf(wunsch)).isEmpty())
    }
}
