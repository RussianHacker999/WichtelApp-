package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.data.local.entity.ParticipantEntity
import com.example.wichtly.data.local.entity.WishlistItemEntity
import com.example.wichtly.data.local.relation.GiftGroupWithParticipants
import com.example.wichtly.data.local.relation.ParticipantWithWishlistEntity
import com.example.wichtly.domain.model.group.DrawType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for the draw read model: Room returns relations unsorted, the mapper sorts them.
 */
class DrawMapperTest {

    private val gruppe = GiftGroupEntity(
        id = "g1",
        name = "Familie",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 24),
        drawType = DrawType.NORMAL,
        photoPath = null,
    )

    private fun teilnehmer(id: String, name: String, vararg wuensche: Pair<String, Int>) = ParticipantWithWishlistEntity(
        participant = ParticipantEntity(id = id, groupId = "g1", name = name, photoPath = null),
        wishlistItems = wuensche.mapIndexed { i, (text, order) ->
            WishlistItemEntity(id = "$id-w$i", participantId = id, text = text, sortOrder = order)
        },
    )

    @Test
    fun teilnehmerNachNameUndWuenscheNachReihenfolge() {
        val quelle = GiftGroupWithParticipants(
            group = gruppe,
            participants = listOf(
                teilnehmer("p2", "ben", "Schal" to 1, "Buch" to 0),
                teilnehmer("p1", "Anna"),
            ),
        ).toDomainModel()

        assertEquals("g1", quelle.group.id)
        assertEquals(listOf("Anna", "ben"), quelle.participants.map { it.participant.name })
        assertEquals(listOf("Buch", "Schal"), quelle.participants[1].wishlist.map { it.text })
    }
}
