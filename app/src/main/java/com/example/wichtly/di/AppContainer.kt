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
import com.example.wichtly.domain.usecase.PerformDrawUseCase
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase
import com.example.wichtly.domain.usecase.SaveParticipantUseCase
import com.example.wichtly.domain.usecase.SaveWishlistItemUseCase
import com.example.wichtly.domain.validation.GiftGroupValidator
import com.example.wichtly.domain.validation.ParticipantValidator
import com.example.wichtly.domain.validation.WishlistValidator

/**
 * Composition Root der App.
 *
 * Lesen: über die Repositories. Die Repositories selbst sind reine Persistenz.
 *
 * Schreiben: über die Use Cases, sobald Fachregeln greifen (Anlegen und Ändern mit Validierung, Losen)
 * oder Datenbank und Fotodateien gemeinsam geändert werden (Gruppe oder Teilnehmer löschen).
 * Einzige Ausnahme ist das Löschen eines Wunsches: Es ist eine einzelne Zeile ohne Regeln und ohne
 * Datei, deshalb ruft das ViewModel dafür direkt das WishlistRepository auf.
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
    val performDrawUseCase: PerformDrawUseCase
    val cleanUpOrphanPhotosUseCase: CleanUpOrphanPhotosUseCase
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
