package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.WishlistItem
import org.junit.Assert.assertTrue
import org.junit.Test

class WishlistValidatorTest {

    private val validator = WishlistValidator()
    private val wunsch = WishlistItem(id = "w1", participantId = "p1", text = "Buch")

    @Test
    fun gueltigerWunschHatKeineFehler() {
        assertTrue(validator.validate(wunsch, emptyList()).isEmpty())
    }

    @Test
    fun leererTextIstFehler() {
        val fehler = validator.validate(wunsch.copy(text = "   "), emptyList())
        assertTrue(WishlistValidationError.EmptyText in fehler)
    }

    @Test
    fun genau200ZeichenSindErlaubt() {
        assertTrue(validator.validate(wunsch.copy(text = "x".repeat(200)), emptyList()).isEmpty())
    }

    @Test
    fun textUeber200ZeichenIstFehler() {
        val fehler = validator.validate(wunsch.copy(text = "x".repeat(201)), emptyList())
        assertTrue(WishlistValidationError.TextTooLong in fehler)
    }

    @Test
    fun gleicherWunschIstDuplikatAuchMitAndererSchreibweise() {
        val vorhanden = listOf(WishlistItem(id = "w2", participantId = "p1", text = " BUCH "))
        assertTrue(WishlistValidationError.DuplicateText in validator.validate(wunsch, vorhanden))
    }

    @Test
    fun eigenerWunschBeimBearbeitenIstKeinDuplikat() {
        assertTrue(validator.validate(wunsch, listOf(wunsch)).isEmpty())
    }
}
