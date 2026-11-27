package com.example.wichtly.di

import android.content.Context
import com.example.wichtly.data.local.database.AppDatabase
import com.example.wichtly.data.local.storage.LocalPhotoStorageManager
import com.example.wichtly.data.repository.DrawRepositoryImpl
import com.example.wichtly.data.repository.GiftGroupRepositoryImpl
import com.example.wichtly.data.repository.ParticipantRepositoryImpl
import com.example.wichtly.data.repository.PhotoRepositoryImpl
import com.example.wichtly.data.repository.WishlistRepositoryImpl
import com.example.wichtly.domain.algorithm.SecretSantaDrawAlgorithm
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.WishlistRepository
import com.example.wichtly.domain.usecase.CleanUpOrphanPhotosUseCase
import com.example.wichtly.domain.usecase.DeleteGiftGroupUseCase
import com.example.wichtly.domain.usecase.DeleteParticipantUseCase
import com.example.wichtly.domain.usecase.DeleteWishlistItemUseCase
import com.example.wichtly.domain.usecase.PerformDrawUseCase
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase
import com.example.wichtly.domain.usecase.SaveParticipantUseCase
import com.example.wichtly.domain.usecase.SaveWishlistItemUseCase
import com.example.wichtly.domain.validation.GiftGroupValidator
import com.example.wichtly.domain.validation.ParticipantValidator
import com.example.wichtly.domain.validation.WishlistValidator

/**
 * Composition root of the app: creates and holds all repositories and use cases.
 *
 * Reading goes through the repositories, which are pure persistence.
 *
 * Writing goes exclusively through the use cases (create and update with validation, draw, delete).
 * When a group or participant is deleted they also coordinate the database and the photo files.
 * Deleting a wish has no rules, but it also goes through a use case for consistency. ViewModels
 * therefore use repositories for reading only.
 *
 * Photo files are handled separately: the photo ViewModels (group form, group detail) save, resolve
 * and discard draft photos directly via the PhotoRepository. A photo is entered in the database only
 * when the group or participant is saved (via the use case).
 */
interface AppContainer {
    val giftGroupRepository: GiftGroupRepository
    val participantRepository: ParticipantRepository
    val wishlistRepository: WishlistRepository
    val drawRepository: DrawRepository
    val photoRepository: PhotoRepository

    val saveGiftGroupUseCase: SaveGiftGroupUseCase
    val deleteGiftGroupUseCase: DeleteGiftGroupUseCase
    val saveParticipantUseCase: SaveParticipantUseCase
    val deleteParticipantUseCase: DeleteParticipantUseCase
    val saveWishlistItemUseCase: SaveWishlistItemUseCase
    val deleteWishlistItemUseCase: DeleteWishlistItemUseCase
    val performDrawUseCase: PerformDrawUseCase
    val cleanUpOrphanPhotosUseCase: CleanUpOrphanPhotosUseCase
}

/**
 * Production implementation of [AppContainer].
 *
 * Every dependency is created lazily on first access and then lives as long as the process.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.create(context)
    }
    private val photoStorageManager: LocalPhotoStorageManager by lazy {
        LocalPhotoStorageManager(context)
    }

    override val giftGroupRepository: GiftGroupRepository by lazy {
        GiftGroupRepositoryImpl(giftGroupDao = database.giftGroupDao())
    }
    override val participantRepository: ParticipantRepository by lazy {
        ParticipantRepositoryImpl(participantDao = database.participantDao())
    }
    override val wishlistRepository: WishlistRepository by lazy {
        WishlistRepositoryImpl(wishlistItemDao = database.wishlistItemDao())
    }
    override val drawRepository: DrawRepository by lazy {
        DrawRepositoryImpl(drawDao = database.drawDao())
    }
    override val photoRepository: PhotoRepository by lazy {
        PhotoRepositoryImpl(
            storageManager = photoStorageManager,
        )
    }

    override val saveGiftGroupUseCase: SaveGiftGroupUseCase by lazy {
        SaveGiftGroupUseCase(
            repository = giftGroupRepository,
            validator = GiftGroupValidator(),
        )
    }
    override val deleteGiftGroupUseCase: DeleteGiftGroupUseCase by lazy {
        DeleteGiftGroupUseCase(
            giftGroupRepository = giftGroupRepository,
            participantRepository = participantRepository,
            photoRepository = photoRepository,
        )
    }
    override val saveParticipantUseCase: SaveParticipantUseCase by lazy {
        SaveParticipantUseCase(
            participantRepository = participantRepository,
            giftGroupRepository = giftGroupRepository,
            validator = ParticipantValidator(),
        )
    }
    override val deleteParticipantUseCase: DeleteParticipantUseCase by lazy {
        DeleteParticipantUseCase(
            participantRepository = participantRepository,
            photoRepository = photoRepository,
        )
    }
    override val saveWishlistItemUseCase: SaveWishlistItemUseCase by lazy {
        SaveWishlistItemUseCase(
            repository = wishlistRepository,
            validator = WishlistValidator(),
        )
    }
    override val deleteWishlistItemUseCase: DeleteWishlistItemUseCase by lazy {
        DeleteWishlistItemUseCase(repository = wishlistRepository)
    }
    override val performDrawUseCase: PerformDrawUseCase by lazy {
        PerformDrawUseCase(
            drawRepository = drawRepository,
            algorithm = SecretSantaDrawAlgorithm(),
        )
    }
    override val cleanUpOrphanPhotosUseCase: CleanUpOrphanPhotosUseCase by lazy {
        CleanUpOrphanPhotosUseCase(
            giftGroupRepository = giftGroupRepository,
            participantRepository = participantRepository,
            photoRepository = photoRepository,
        )
    }
}
