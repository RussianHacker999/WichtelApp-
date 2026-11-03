package com.example.wichtly.di

import android.content.Context
import com.example.wichtly.data.local.AppDatabase
import com.example.wichtly.data.repository.GiftGroupRepositoryImpl
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.validation.GiftGroupValidator


interface AppContainer {
    val giftGroupRepository: GiftGroupRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
    private val giftGroupValidator: GiftGroupValidator by lazy {
        GiftGroupValidator()
    }
    override val giftGroupRepository: GiftGroupRepository by lazy {
        GiftGroupRepositoryImpl(
            giftGroupDao = database.giftGroupDao(),
            validator = giftGroupValidator
        )
    }
}
