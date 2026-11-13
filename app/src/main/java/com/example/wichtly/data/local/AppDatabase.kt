package com.example.wichtly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.local.dao.ParticipantDao
import com.example.wichtly.data.local.dao.WishListItemDao
import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.data.local.entity.ParticipantEntity
import com.example.wichtly.data.local.entity.WishListItemEntity

@Database(
    entities = [
        GiftGroupEntity::class,
        ParticipantEntity::class,
        WishListItemEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun giftGroupDao(): GiftGroupDao
    abstract fun participantDao(): ParticipantDao
    abstract fun wishListItemDao(): WishListItemDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "wichtly_database",
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
