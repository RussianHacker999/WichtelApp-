package com.example.wichtly.di

import android.content.Context
import com.example.wichtly.data.local.AppDatabase
import com.example.wichtly.data.repository.GiftGroupRepositoryImpl
import com.example.wichtly.data.repository.ParticipantRepositoryImpl
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.validation.GiftGroupValidator
import com.example.wichtly.domain.validation.ParticipantValidator

interface AppContainer {
    val giftGroupRepository: GiftGroupRepository
    val participantRepository: ParticipantRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.create(context)
    }
    private val giftGroupValidator: GiftGroupValidator by lazy {
        GiftGroupValidator()
    }
    private val participantValidator: ParticipantValidator by lazy {
        ParticipantValidator()
    }
    override val giftGroupRepository: GiftGroupRepository by lazy {
        GiftGroupRepositoryImpl(
            giftGroupDao = database.giftGroupDao(),
            validator = giftGroupValidator,
        )
    }
    override val participantRepository: ParticipantRepository by lazy {
        ParticipantRepositoryImpl(
            participantDao = database.participantDao(),
            validator = participantValidator,
        )
    }
}
