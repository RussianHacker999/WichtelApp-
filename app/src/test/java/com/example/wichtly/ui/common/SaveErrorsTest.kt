package com.example.wichtly.ui.common

import com.example.wichtly.R
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveErrorsTest {

    @Test
    fun vollerSpeicherHatEigenenText() {
        val voll = IOException("write failed: ENOSPC (No space left on device)")
        assertEquals(R.string.error_storage_full, saveErrorMessage(voll, R.string.error_photo_save))
    }

    @Test
    fun eingepackterSpeicherfehlerWirdErkannt() {
        val eingepackt = IllegalStateException("Room", IOException("ENOSPC"))
        assertEquals(R.string.error_storage_full, saveErrorMessage(eingepackt, R.string.error_group_save))
    }

    @Test
    fun andereFehlerBehaltenDenAllgemeinenText() {
        assertEquals(R.string.error_group_save, saveErrorMessage(IllegalStateException("kaputt"), R.string.error_group_save))
    }
}
