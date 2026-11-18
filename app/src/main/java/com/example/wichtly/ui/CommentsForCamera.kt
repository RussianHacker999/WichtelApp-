package com.example.wichtly.ui

/*
================================================================================
  ANLEITUNG FÜR UI-DEVELOPER: KAMERA- UND FOTO-INTEGRATION (DOMAIN / DATA LAYER)
================================================================================

Diese Anleitung erklärt, wie die UI-Layer (ViewModels, Screens und Dialoge) mit dem
Domain/Data-Layer für Gruppenfotos und Teilnehmerfotos interagiert.

--------------------------------------------------------------------------------
1. ZUDRANG UND DEPENDENCY INJECTION
--------------------------------------------------------------------------------
Das PhotoRepository befindet sich unter:
  domain/repository/PhotoRepository.kt

In Composables/Routes wird es über den AppContainer bezogen:
  val context = LocalContext.current
  val photoRepository = (context.applicationContext as WichtlyApplication).container.photoRepository

--------------------------------------------------------------------------------
2. KERNTHEMA: RELATIVE DATEIPFADE (photoPath: String?)
--------------------------------------------------------------------------------
- In Room und in allen Domain-Modellen (GiftGroup.photoPath, Participant.photoPath)
  wird AUSSCHLIESSLICH ein relativer Pfad gespeichert.
  - Für Gruppen:      "photos/groups/groups_<uuid>.jpg"
  - Für Teilnehmer:  "photos/participants/participants_<uuid>.jpg"

- WICHTIG: Speicher NIEMALS absolute Dateipfade (z. B. "/data/user/0/...") in Room!

- Um ein Foto in der UI (z. B. via BitmapFactory oder Coil) anzuzeigen:
    val file: File? = photoRepository.getFileForRelativePath(photoPath)
    if (file != null && file.exists()) {
        // Lade das Bild aus file.absolutePath
    } else {
        // Zeige Platzhalter-Icon (z. B. "＋" oder Avatar)
    }

--------------------------------------------------------------------------------
3. INTERAKTION ZWISCHEN VIEWMODEL UND PHOTO-REPOSITORY
--------------------------------------------------------------------------------

A) Neues Foto nach CameraX-Aufnahme speichern:
   1. CameraX speichert die Aufnahme in eine temporäre Datei (tempFile in cacheDir).
   2. Das ViewModel ruft das Repository auf:
        // Für Gruppen:
        val relativePath = photoRepository.savePhoto(tempFile, PhotoOwnerType.GROUPS)
        // Für Teilnehmer:
        val relativePath = photoRepository.savePhoto(tempFile, PhotoOwnerType.PARTICIPANTS)
   3. Das ViewModel aktualisiert seinen UI-State:
        uiState = uiState.copy(photoPath = relativePath)
   4. Beim Klick auf "Speichern" wird die Entität mit dem photoPath in Room gesichert.

B) Vorhandenes Foto entfernen:
   1. Nutzer klickt auf "Foto entfernen".
   2. Das ViewModel ruft auf:
        photoRepository.deletePhoto(currentPhotoPath)
   3. Das ViewModel aktualisiert seinen UI-State:
        uiState = uiState.copy(photoPath = null)

--------------------------------------------------------------------------------
4. EMPFOHLENE GEMEINSAME UI-KOMPONENTEN (SHARED UI CODE)
--------------------------------------------------------------------------------
Da Gruppenfotos (GroupFormScreen) und Teilnehmerfotos (ParticipantEditorDialog)
exakt denselben Ablauf nutzen, sollten sich die Entwickler beide Komponenten teilen:

1) PhotoTile (Composable):
   - Formular-Kachel mit Vorschaubild/Platzhalter, Beschriftung und Buttons
     ("Auswählen" / "Ändern" sowie "Foto entfernen").

2) PhotoSourceSheet (Modal Bottom Sheet):
   - Menü zur Quellauswahl ("Foto aufnehmen" / "Vorhandenes Foto entfernen").

3) CameraDeniedFallbackDialog (AlertDialog):
   - Hinweis-Dialog bei verweigerter KAMERA-Berechtigung ("Die Kamera wurde nicht
     freigegeben. Du kannst ohne Foto fortfahren.").

4) CameraCaptureScreen (Composable):
   - Vollbild-CameraX-Preview mit Auslöser-Button.
*/
