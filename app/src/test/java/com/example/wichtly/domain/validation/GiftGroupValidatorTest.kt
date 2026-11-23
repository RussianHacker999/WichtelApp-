package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.GiftGroup
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GiftGroupValidatorTest {

    private val validator = GiftGroupValidator()

    // festes "Heute", damit der Test an jedem Tag gleich funktioniert
    private val heute = LocalDate.of(2026, 11, 23)

    // gültige Gruppe als Vorlage; in den Tests wird jeweils nur ein Wert verändert
    private val gueltigeGruppe = GiftGroup(
        id = "g1",
        name = "Familie",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 6),
    )

    @Test
    fun gueltigeGruppeHatKeineFehler() {
        val fehler = validator.validate(gueltigeGruppe, emptyList(), currentDate = heute)
        assertTrue(fehler.isEmpty())
    }

    @Test
    fun leererNameIstFehler() {
        val gruppe = gueltigeGruppe.copy(name = "   ")
        val fehler = validator.validate(gruppe, emptyList(), currentDate = heute)
        assertTrue(GiftGroupValidationError.EmptyName in fehler)
    }

    @Test
    fun nameUeber50ZeichenIstFehler() {
        val gruppe = gueltigeGruppe.copy(name = "a".repeat(51))
        val fehler = validator.validate(gruppe, emptyList(), currentDate = heute)
        assertTrue(GiftGroupValidationError.NameTooLong in fehler)
    }

    @Test
    fun doppelterNameIstFehlerAuchMitAndererGrossschreibung() {
        val vorhanden = listOf(gueltigeGruppe.copy(id = "g2", name = "FAMILIE"))
        val fehler = validator.validate(gueltigeGruppe, vorhanden, currentDate = heute)
        assertTrue(GiftGroupValidationError.DuplicateName in fehler)
    }

    @Test
    fun eigenerNameBeimBearbeitenIstKeinDuplikat() {
        // dieselbe Gruppe (gleiche id) steht schon in der Liste -> Bearbeiten, kein Duplikat
        val fehler = validator.validate(gueltigeGruppe, listOf(gueltigeGruppe), currentDate = heute)
        assertTrue(fehler.isEmpty())
    }

    @Test
    fun budgetNullOderNegativIstFehler() {
        val nullBudget = validator.validate(gueltigeGruppe.copy(budgetCents = 0), emptyList(), currentDate = heute)
        val negativ = validator.validate(gueltigeGruppe.copy(budgetCents = -100), emptyList(), currentDate = heute)
        assertTrue(GiftGroupValidationError.InvalidBudget in nullBudget)
        assertTrue(GiftGroupValidationError.InvalidBudget in negativ)
    }

    @Test
    fun datumHeuteOderVergangenheitIstFehler() {
        val heuteFehler = validator.validate(gueltigeGruppe.copy(drawDay = heute), emptyList(), currentDate = heute)
        val gesternFehler = validator.validate(gueltigeGruppe.copy(drawDay = heute.minusDays(1)), emptyList(), currentDate = heute)
        assertTrue(GiftGroupValidationError.DateInPast in heuteFehler)
        assertTrue(GiftGroupValidationError.DateInPast in gesternFehler)
    }

    @Test
    fun altesDatumErlaubtWennZukunftNichtVerlangt() {
        val gruppe = gueltigeGruppe.copy(drawDay = heute.minusDays(10))
        val fehler = validator.validate(gruppe, emptyList(), requireFutureDate = false, currentDate = heute)
        assertTrue(fehler.isEmpty())
    }
}