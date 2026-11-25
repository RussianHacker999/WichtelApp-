package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.group.GiftGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GiftGroupValidatorTest {

    private val validator = GiftGroupValidator()

    // festes "Heute", damit der Test an jedem Tag gleich funktioniert
    private val heute = LocalDate.of(2026, 11, 23)

    private val familie = GiftGroup(
        id = "g1",
        name = "Familie",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 6),
    )

    // ----------------------------------------------------------------- Name

    @Test
    fun gueltigerNameHatKeineFehler() {
        assertTrue(validator.validateName("Familie").isEmpty())
    }

    @Test
    fun leererNameIstFehler() {
        assertTrue(GiftGroupValidationError.EmptyName in validator.validateName("   "))
    }

    @Test
    fun nameMitMaximalLaengeIstErlaubtEinZeichenMehrNicht() {
        val max = GiftGroupValidator.MAX_NAME_LENGTH
        assertTrue(validator.validateName("a".repeat(max)).isEmpty())
        assertTrue(GiftGroupValidationError.NameTooLong in validator.validateName("a".repeat(max + 1)))
    }

    @Test
    fun normalizeNameEntferntLeerraumAmRand() {
        assertEquals("Familie", validator.normalizeName("  Familie \t"))
    }

    // ----------------------------------------------------------------- Duplikat

    @Test
    fun doppelterNameIstFehlerAuchMitAndererGrossschreibung() {
        val vorhanden = listOf(familie.copy(id = "g2", name = "FAMILIE"))
        val fehler = validator.validateUniqueName("Familie", groupId = null, existingGroups = vorhanden)
        assertTrue(GiftGroupValidationError.DuplicateName in fehler)
    }

    @Test
    fun eigenerNameBeimBearbeitenIstKeinDuplikat() {
        val fehler = validator.validateUniqueName("Familie", groupId = "g1", existingGroups = listOf(familie))
        assertTrue(fehler.isEmpty())
    }

    // ----------------------------------------------------------------- Budget

    @Test
    fun budgetNullOderNegativIstFehler() {
        assertTrue(GiftGroupValidationError.InvalidBudget in validator.validateBudget(0))
        assertTrue(GiftGroupValidationError.InvalidBudget in validator.validateBudget(-100))
        assertTrue(validator.validateBudget(1).isEmpty())
    }

    // ----------------------------------------------------------------- Wichteltag

    @Test
    fun neuanlageVerlangtDatumNachHeute() {
        assertTrue(GiftGroupValidationError.DateInPast in validator.validateDrawDay(heute, previousDay = null, today = heute))
        assertTrue(GiftGroupValidationError.DateInPast in validator.validateDrawDay(heute.minusDays(1), null, heute))
        assertTrue(validator.validateDrawDay(heute.plusDays(1), null, heute).isEmpty())
    }

    @Test
    fun bearbeitenMitUnveraendertemVergangenemDatumIstErlaubt() {
        // P1-1: Eine Gruppe muss sich auch am oder nach ihrem Wichteltag noch bearbeiten lassen
        val gespeichert = heute.minusDays(10)
        assertTrue(validator.validateDrawDay(gespeichert, previousDay = gespeichert, today = heute).isEmpty())
        assertTrue(validator.validateDrawDay(heute, previousDay = heute, today = heute).isEmpty())
    }

    @Test
    fun bearbeitenMitGeaendertemDatumVerlangtWiederDatumNachHeute() {
        val fehler = validator.validateDrawDay(heute.minusDays(1), previousDay = heute.minusDays(10), today = heute)
        assertTrue(GiftGroupValidationError.DateInPast in fehler)
    }

    @Test
    fun budgetAb999999EuroIstZuHoch() {
        val grenze = GiftGroupValidator.BUDGET_LIMIT_EUROS * 100
        assertTrue(validator.validateBudget(grenze - 1).isEmpty())
        assertTrue(GiftGroupValidationError.BudgetTooHigh in validator.validateBudget(grenze))
        assertTrue(GiftGroupValidationError.BudgetTooHigh in validator.validateBudget(Long.MAX_VALUE))
    }

    @Test
    fun wichteltagAb100JahrenInDerZukunftIstZuWeit() {
        val grenze = heute.plusYears(GiftGroupValidator.MAX_YEARS_AHEAD)
        assertTrue(validator.validateDrawDay(grenze.minusDays(1), previousDay = null, today = heute).isEmpty())
        assertTrue(GiftGroupValidationError.DateTooFarInFuture in validator.validateDrawDay(grenze, previousDay = null, today = heute))
    }

    @Test
    fun ueberschrittenerWichteltagDarfNurAufEinenTagNachHeuteVerschobenWerden() {
        val vorbei = heute.minusDays(30)
        // auf heute verschieben: abgelehnt (wie beim Anlegen)
        assertTrue(GiftGroupValidationError.DateInPast in validator.validateDrawDay(heute, previousDay = vorbei, today = heute))
        // auf morgen verschieben: erlaubt, damit beginnt die nächste Runde
        assertTrue(validator.validateDrawDay(heute.plusDays(1), previousDay = vorbei, today = heute).isEmpty())
    }
}
