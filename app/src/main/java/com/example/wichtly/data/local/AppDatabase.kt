package com.example.wichtly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.local.entity.GiftGroupEntity


@Database(
    entities = [GiftGroupEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun giftGroupDao(): GiftGroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wichtly_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
