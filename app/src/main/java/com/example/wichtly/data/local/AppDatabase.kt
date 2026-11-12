package com.example.wichtly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.local.dao.ParticipantDao
import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.data.local.entity.ParticipantEntity

@Database(
    entities = [
        GiftGroupEntity::class,
        ParticipantEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun giftGroupDao(): GiftGroupDao
    abstract fun participantDao(): ParticipantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wichtly_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
