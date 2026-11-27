package com.example.wichtly.ui.common

import com.example.wichtly.R
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveErrorsTest {

    @Test
    fun fullStorageHasOwnMessage() {
        val full = IOException("write failed: ENOSPC (No space left on device)")
        assertEquals(R.string.error_storage_full, saveErrorMessage(full, R.string.error_photo_save))
    }

    @Test
    fun wrappedStorageErrorIsDetected() {
        val wrapped = IllegalStateException("Room", IOException("ENOSPC"))
        assertEquals(R.string.error_storage_full, saveErrorMessage(wrapped, R.string.error_group_save))
    }

    @Test
    fun otherErrorsKeepGenericMessage() {
        assertEquals(R.string.error_group_save, saveErrorMessage(IllegalStateException("broken"), R.string.error_group_save))
    }
}