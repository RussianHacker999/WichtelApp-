package com.example.wichtly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wichtly.data.local.dao.DrawDao
import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.local.dao.ParticipantDao
import com.example.wichtly.data.local.dao.WishListItemDao
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawEntity
import com.example.wichtly.data.local.entity.DrawWishListItemSnapshotEntity
import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.data.local.entity.ParticipantEntity
import com.example.wichtly.data.local.entity.WishListItemEntity

/**
 * Room-Datenbank der App.
 *
 * Schema-Export: Room schreibt das Schema jeder Version nach app/schemas/ (konfiguriert in
 * app/build.gradle.kts über room.schemaLocation). Die Datei für Version 4 ist eingecheckt und
 * hält den Stand der Abgabe fest.
 *
 * Migrationen: Ab Version 4 werden Schemaänderungen per AutoMigration übernommen, die Daten
 * (Gruppen, Teilnehmer, Wunschlisten und die dauerhaft gespeicherte Auslosungshistorie) bleiben
 * erhalten. Room erzeugt die Migration beim Kompilieren aus den eingecheckten JSON-Dateien von
 * alter und neuer Version.
 *
 * Destruktiv bleibt es nur dort, wo keine Migration möglich ist:
 * - Versionen 1 bis 3: Für sie wurde nie ein Schema exportiert, Room kann daraus nichts ableiten.
 *   Solche Datenbanken gibt es nur auf alten Entwicklungsgeräten.
 * - Downgrade (ältere App über neuere installiert): Rückwärts gibt es keine Migration.
 *
 * Wer das Schema künftig ändert:
 * 1. Entity ändern und version erhöhen (z. B. auf 5). Ohne Erhöhung verweigert Room das Öffnen
 *    einer bestehenden Datenbank, und die App zeigt auf jedem Screen nur noch Ladefehler.
 * 2. In autoMigrations einen Eintrag ergänzen: AutoMigration(from = 4, to = 5).
 * 3. Bauen; Room schreibt 5.json nach app/schemas/. Die neue JSON-Datei mit einchecken.
 * 4. Umbenennen oder Löschen von Tabellen/Spalten kann Room nicht allein erkennen: dafür eine
 *    AutoMigrationSpec mit @RenameColumn, @RenameTable, @DeleteColumn oder @DeleteTable angeben
 *    (AutoMigration(from = 4, to = 5, spec = ...)). Fehlt sie, bricht der Build mit einer Meldung ab.
 * Fehlt für eine neue Version die Migration, öffnet Room die Datenbank nicht (keine stille Löschung).
 */
@Database(
    entities = [
        GiftGroupEntity::class,
        ParticipantEntity::class,
        WishListItemEntity::class,
        DrawEntity::class,
        DrawAssignmentEntity::class,
        DrawWishListItemSnapshotEntity::class,
    ],
    version = 4,
    exportSchema = true,
    // Version 4 ist die erste exportierte Version; neue Versionen hier ergänzen,
    // z. B. AutoMigration(from = 4, to = 5)
    autoMigrations = [],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun giftGroupDao(): GiftGroupDao
    abstract fun participantDao(): ParticipantDao
    abstract fun wishListItemDao(): WishListItemDao
    abstract fun drawDao(): DrawDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "wichtly_database",
            )
                // Nur für Versionen ohne exportiertes Schema; alle späteren Versionen migrieren automatisch
                .fallbackToDestructiveMigrationFrom(true, 1, 2, 3)
                // Rückwärts gibt es keine Migration
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()
        }
    }
}
