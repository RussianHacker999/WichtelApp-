package com.example.wichtly.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wichtly.data.local.dao.DrawDao
import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.local.dao.ParticipantDao
import com.example.wichtly.data.local.dao.WishlistItemDao
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawEntity
import com.example.wichtly.data.local.entity.DrawWishlistItemSnapshotEntity
import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.data.local.entity.ParticipantEntity
import com.example.wichtly.data.local.entity.WishlistItemEntity
import com.example.wichtly.logging.AppLog

private const val TAG = "AppDatabase"

/**
 * Room database of the app.
 *
 * Schema export: Room writes the schema of every version to app/schemas/ (configured in
 * app/build.gradle.kts via room.schemaLocation). The file for version 4 is checked in and
 * records the state of the submission.
 *
 * Migrations: from version 4 on, schema changes are applied with AutoMigration, so the data
 * (groups, participants, wishlists and the persisted draw history) is kept. Room generates the
 * migration at compile time from the checked-in JSON files of the old and the new version.
 *
 * The database is only recreated destructively where no migration is possible:
 * - Versions 1 to 3: no schema was ever exported for them, so Room cannot derive a migration.
 *   Such databases exist only on old development devices.
 * - Downgrade (older app installed over a newer one): there is no backwards migration.
 *
 * To change the schema in the future:
 * 1. Change the entity and increase `version` (e.g. to 5). Without the increase Room refuses to open
 *    an existing database, and every screen of the app only shows a loading error.
 * 2. Add an entry to `autoMigrations`: AutoMigration(from = 4, to = 5).
 * 3. Build; Room writes 5.json to app/schemas/. Check the new JSON file in as well.
 * 4. Room cannot detect renamed or deleted tables/columns on its own: provide an
 *    AutoMigrationSpec with @RenameColumn, @RenameTable, @DeleteColumn or @DeleteTable
 *    (AutoMigration(from = 4, to = 5, spec = ...)). Without it the build fails with a message.
 * If the migration for a new version is missing, Room does not open the database (no silent data loss).
 */
@Database(
    entities = [
        GiftGroupEntity::class,
        ParticipantEntity::class,
        WishlistItemEntity::class,
        DrawEntity::class,
        DrawAssignmentEntity::class,
        DrawWishlistItemSnapshotEntity::class,
    ],
    version = 4,
    exportSchema = true,
    // Version 4 is the first exported version; add new versions here,
    // e.g. AutoMigration(from = 4, to = 5)
    autoMigrations = [],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun giftGroupDao(): GiftGroupDao
    abstract fun participantDao(): ParticipantDao
    abstract fun wishlistItemDao(): WishlistItemDao
    abstract fun drawDao(): DrawDao

    companion object {
        /** Logs database lifecycle events; a destructive recreation is always logged as a warning. */
        private object LoggingCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                AppLog.i(TAG, "Database created (version ${db.version})")
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                AppLog.d(TAG) { "Database opened (version ${db.version})" }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                AppLog.w(TAG, "Database recreated destructively (version without schema, or downgrade): all data deleted")
            }
        }

        /** Builds the single database instance of the app (called once by the AppContainer). */
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "wichtly_database",
            )
                // Only for versions without an exported schema; every later version migrates automatically
                .fallbackToDestructiveMigrationFrom(true, 1, 2, 3)
                // There is no backwards migration
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .addCallback(LoggingCallback)
                .build()
        }
    }
}
