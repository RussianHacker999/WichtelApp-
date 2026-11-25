package com.example.wichtly.di

import android.content.Context
import com.example.wichtly.data.local.database.AppDatabase
import com.example.wichtly.data.local.storage.LocalPhotoStorageManager
import com.example.wichtly.data.repository.DrawRepositoryImpl
import com.example.wichtly.data.repository.GiftGroupRepositoryImpl
import com.example.wichtly.data.repository.ParticipantRepositoryImpl
import com.example.wichtly.data.repository.PhotoRepositoryImpl
import com.example.wichtly.data.repository.WishListRepositoryImpl
import com.example.wichtly.domain.algorithm.SecretSantaDrawAlgorithm
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.WishListRepository
import com.example.wichtly.domain.usecase.DeleteGiftGroupUseCase
import com.example.wichtly.domain.usecase.PerformDrawUseCase
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase
import com.example.wichtly.domain.usecase.SaveParticipantUseCase
import com.example.wichtly.domain.usecase.SaveWishListItemUseCase
import com.example.wichtly.domain.validation.GiftGroupValidator
import com.example.wichtly.domain.validation.ParticipantValidator
import com.example.wichtly.domain.validation.WishListValidator

/**
 * Composition Root der App.
 *
 * Lesen: über die Repositories. Schreiben: ausschließlich über die Use Cases, weil nur sie die
 * Fachregeln prüfen. Die Repositories selbst sind reine Persistenz.
 */
interface AppContainer {
    val giftGroupRepository: GiftGroupRepository
    val participantRepository: ParticipantRepository
    val wishListRepository: WishListRepository
    val drawRepository: DrawRepository
    val photoRepository: PhotoRepository

    val saveGiftGroupUseCase: SaveGiftGroupUseCase
    val deleteGiftGroupUseCase: DeleteGiftGroupUseCase
    val saveParticipantUseCase: SaveParticipantUseCase
    val saveWishListItemUseCase: SaveWishListItemUseCase
    val performDrawUseCase: PerformDrawUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.create(context)
    }
    private val photoStorageManager: LocalPhotoStorageManager by lazy {
        LocalPhotoStorageManager(context)
    }

    // =========================================================================
    // REPOSITORIES (Persistenz)
    // =========================================================================

    override val giftGroupRepository: GiftGroupRepository by lazy {
        GiftGroupRepositoryImpl(giftGroupDao = database.giftGroupDao())
    }
    override val participantRepository: ParticipantRepository by lazy {
        ParticipantRepositoryImpl(participantDao = database.participantDao())
    }
    override val wishListRepository: WishListRepository by lazy {
        WishListRepositoryImpl(wishListItemDao = database.wishListItemDao())
    }
    override val drawRepository: DrawRepository by lazy {
        DrawRepositoryImpl(drawDao = database.drawDao())
    }
    override val photoRepository: PhotoRepository by lazy {
        PhotoRepositoryImpl(
            storageManager = photoStorageManager,
        )
    }

    // =========================================================================
    // USE CASES (Fachlogik und Validierung)
    // =========================================================================

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
    override val saveWishListItemUseCase: SaveWishListItemUseCase by lazy {
        SaveWishListItemUseCase(
            repository = wishListRepository,
            validator = WishListValidator(),
        )
    }
    override val performDrawUseCase: PerformDrawUseCase by lazy {
        PerformDrawUseCase(
            giftGroupRepository = giftGroupRepository,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository,
            drawRepository = drawRepository,
            algorithm = SecretSantaDrawAlgorithm(),
        )
    }
}
