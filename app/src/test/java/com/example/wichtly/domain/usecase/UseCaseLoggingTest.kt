package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.logging.AppLog
import com.example.wichtly.logging.LogLevel
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.FakeWishListRepository
import com.example.wichtly.testutil.RecordingLogSink
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Prüft die Log-Stufen der Use Cases und die Datenschutz-Regel (keine Namen in Logs).
 */
class UseCaseLoggingTest {

    private lateinit var sink: RecordingLogSink

    private val gruppe = GiftGroup(id = "g1", name = "Familie Geheim", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24))

    @Before
    fun setUp() {
        sink = RecordingLogSink().also { AppLog.install(it) }
    }

    @After
    fun tearDown() {
        AppLog.reset()
    }

    @Test
    fun abgelehnteEingabenSindDebugUndKeineWarnung() = runTest {
        val useCase = SaveGiftGroupUseCase(FakeGiftGroupRepository(), today = { LocalDate.of(2026, 11, 25) })

        useCase(null, GiftGroupInput(name = "", budget = "", drawDay = "", drawType = DrawType.NORMAL, photoPath = null))

        assertTrue(sink.atLevel(LogLevel.WARN).isEmpty())
        assertTrue(sink.atLevel(LogLevel.DEBUG).any { "EmptyName" in it.message })
    }

    @Test
    fun gespeicherteGruppeIstInfoOhneGruppennamen() = runTest {
        val useCase = SaveGiftGroupUseCase(FakeGiftGroupRepository(), today = { LocalDate.of(2026, 11, 25) })

        useCase(null, GiftGroupInput(name = "Familie Geheim", budget = "20", drawDay = "24.12.2026", drawType = DrawType.NORMAL, photoPath = null))

        assertEquals(1, sink.atLevel(LogLevel.INFO).size)
        assertFalse(sink.entries.any { "Familie Geheim" in it.message })
    }

    @Test
    fun auslosungLoggtNurAnzahlenUndKeineNamen() = runTest {
        val teilnehmer = listOf("Anna", "Ben", "Clara").mapIndexed { i, name -> Participant(id = "p$i", groupId = "g1", name = name) }
        val useCase = PerformDrawUseCase(
            FakeGiftGroupRepository().apply { setGroups(listOf(gruppe)) },
            FakeParticipantRepository().apply { setParticipants(teilnehmer) },
            FakeWishListRepository(),
            FakeDrawRepository(),
        )

        useCase("g1")

        val info = sink.atLevel(LogLevel.INFO).single()
        assertTrue("3 Zuordnungen" in info.message)
        assertFalse(sink.entries.any { entry -> teilnehmer.any { it.name in entry.message } })
    }

    @Test
    fun nichtLoeschbaresFotoIstWarnungMitUrsache() = runTest {
        val useCase = DeleteGiftGroupUseCase(
            FakeGiftGroupRepository().apply { setGroups(listOf(gruppe.copy(photoPath = "photos/groups/g.jpg"))) },
            FakeParticipantRepository(),
            FakePhotoRepository().apply { shouldThrowOnDelete = true },
        )

        useCase("g1")

        val warnung = sink.atLevel(LogLevel.WARN).single()
        assertTrue("photos/groups/g.jpg" in warnung.message)
        assertTrue(warnung.throwable is IllegalStateException)
    }
}
