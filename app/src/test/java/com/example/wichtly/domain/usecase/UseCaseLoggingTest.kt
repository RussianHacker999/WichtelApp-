package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.logging.AppLog
import com.example.wichtly.logging.LogLevel
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.FakeWishlistRepository
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
 * Checks the log levels of the use cases and the privacy rule (no names in logs).
 */
class UseCaseLoggingTest {

    private lateinit var sink: RecordingLogSink

    private val group = GiftGroup(id = "g1", name = "Secret Family", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24))

    @Before
    fun setUp() {
        sink = RecordingLogSink().also { AppLog.install(it) }
    }

    @After
    fun tearDown() {
        AppLog.reset()
    }

    @Test
    fun rejectedInputIsDebugNotWarning() = runTest {
        val useCase = SaveGiftGroupUseCase(FakeGiftGroupRepository(), today = { LocalDate.of(2026, 11, 25) })

        useCase(null, GiftGroupInput(name = "", budget = "", drawDay = "", drawType = DrawType.NORMAL, photoPath = null))

        assertTrue(sink.atLevel(LogLevel.WARN).isEmpty())
        assertTrue(sink.atLevel(LogLevel.DEBUG).any { "EmptyName" in it.message })
    }

    @Test
    fun savedGroupIsInfoWithoutGroupName() = runTest {
        val useCase = SaveGiftGroupUseCase(FakeGiftGroupRepository(), today = { LocalDate.of(2026, 11, 25) })

        useCase(null, GiftGroupInput(name = "Secret Family", budget = "20", drawDay = "24.12.2026", drawType = DrawType.NORMAL, photoPath = null))

        assertEquals(1, sink.atLevel(LogLevel.INFO).size)
        assertFalse(sink.entries.any { "Secret Family" in it.message })
    }

    @Test
    fun drawLogsOnlyCountsAndNoNames() = runTest {
        val participants = listOf("Anna", "Ben", "Clara").mapIndexed { i, name -> Participant(id = "p$i", groupId = "g1", name = name) }
        val useCase = PerformDrawUseCase(
            FakeDrawRepository().withSources(FakeGiftGroupRepository().apply { setGroups(listOf(group)) }, FakeParticipantRepository().apply { setParticipants(participants) }, FakeWishlistRepository()),
            today = { LocalDate.of(2026, 11, 25) },
        )

        useCase("g1")

        val info = sink.atLevel(LogLevel.INFO).single()
        assertTrue("3 assignments" in info.message)
        assertFalse(sink.entries.any { entry -> participants.any { it.name in entry.message } })
    }

    @Test
    fun undeletablePhotoIsWarningWithCause() = runTest {
        val useCase = DeleteGiftGroupUseCase(
            FakeGiftGroupRepository().apply { setGroups(listOf(group.copy(photoPath = "photos/groups/g.jpg"))) },
            FakeParticipantRepository(),
            FakePhotoRepository().apply { shouldThrowOnDelete = true },
        )

        useCase("g1")

        val warning = sink.atLevel(LogLevel.WARN).single()
        assertTrue("photos/groups/g.jpg" in warning.message)
        assertTrue(warning.throwable is IllegalStateException)
    }
}