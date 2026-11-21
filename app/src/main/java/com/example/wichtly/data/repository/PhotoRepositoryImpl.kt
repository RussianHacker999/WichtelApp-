package com.example.wichtly.data.repository

import com.example.wichtly.data.local.storage.LocalPhotoStorageManager
import com.example.wichtly.domain.model.PhotoOwnerType
import com.example.wichtly.domain.repository.PhotoRepository
import java.io.File

class PhotoRepositoryImpl(
    private val storageManager: LocalPhotoStorageManager,
) : PhotoRepository {

    override fun getFileForRelativePath(relativePath: String?): File? {
        return storageManager.getFileForRelativePath(relativePath)
    }

    override suspend fun savePhoto(sourceFile: File, ownerType: PhotoOwnerType): String {
        return storageManager.savePhotoFile(sourceFile, ownerType)
    }

    override suspend fun deletePhoto(relativePath: String?) {
        storageManager.deletePhotoFile(relativePath)
    }
}
