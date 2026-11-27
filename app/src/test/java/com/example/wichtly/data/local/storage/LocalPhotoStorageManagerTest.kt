package com.example.wichtly.data.local.storage

import com.example.wichtly.domain.model.photo.PhotoOwnerType
import java.io.File
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Tests for [LocalPhotoStorageManager] on a temporary folder instead of filesDir.
 *
 * Focus: paths from the database must not leave the photo folder (G-23).
 */
class LocalPhotoStorageManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun manager() = LocalPhotoStorageManager(tempFolder.root)

    @Test
    fun gespeichertesFotoLiegtImFotoordnerUndQuelleIstVerschoben() = runTest {
        val source = tempFolder.newFile("photo_tmp.jpg").apply { writeText("jpeg") }

        val relativePath = manager().savePhotoFile(source, PhotoOwnerType.GROUPS)

        assertTrue(relativePath.startsWith("photos/groups/groups_"))
        val saved = manager().getFileForRelativePath(relativePath)
        assertNotNull(saved)
        assertEquals("jpeg", saved!!.readText())
        assertFalse(source.exists())
    }

    @Test
    fun pfadAusserhalbDesFotoordnersWirdWederGelesenNochGeloescht() = runTest {
        val fremd = File(tempFolder.root, "databases/wichtly.db").apply {
            parentFile!!.mkdirs()
            writeText("db")
        }
        val boeserPfad = "photos/../databases/wichtly.db"

        assertNull(manager().getFileForRelativePath(boeserPfad))
        manager().deletePhotoFile(boeserPfad)

        assertTrue(fremd.exists())
    }

    @Test
    fun nichtAufloesbarerPfadLiefertNullStattZuWerfen() = runTest {
        // A NUL character makes File.getCanonicalFile() fail with an IOException ("Invalid file path")
        val ungueltigerPfad = "photos/groups/groups_\u0000.jpg"

        assertNull(manager().getFileForRelativePath(ungueltigerPfad))
    }

    @Test
    fun geloeschtesFotoIstWeg() = runTest {
        val source = tempFolder.newFile("photo_tmp.jpg").apply { writeText("jpeg") }
        val relativePath = manager().savePhotoFile(source, PhotoOwnerType.PARTICIPANTS)

        manager().deletePhotoFile(relativePath)

        assertNull(manager().getFileForRelativePath(relativePath))
    }

    @Test
    fun auflistenLiefertNurFotosAelterAlsDieGrenze() = runTest {
        val alt = manager().savePhotoFile(tempFolder.newFile("a.jpg"), PhotoOwnerType.GROUPS)
        val neu = manager().savePhotoFile(tempFolder.newFile("b.jpg"), PhotoOwnerType.PARTICIPANTS)
        File(tempFolder.root, alt).setLastModified(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli())
        File(tempFolder.root, neu).setLastModified(Instant.parse("2026-06-01T00:00:00Z").toEpochMilli())

        val pfade = manager().listPhotoPaths(olderThan = Instant.parse("2026-03-01T00:00:00Z"))

        assertEquals(listOf(alt), pfade)
    }
}
