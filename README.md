# Wichtly

Geniale App für Wichteln

von Merve Oztuerk, Viktoria Ludchenko, Diana Folz und Melissa Meyer :)

Wichtly ist eine Android-App für die Person, die ein Wichteln organisiert. Sie läuft komplett offline;
alle Daten bleiben auf dem Gerät.

## Was die App kann

| Feature | Inhalt |
|---|---|
| Gruppen | Wichtelgruppen mit Name, Budget, Wichteltag, Wichtel-Typ (normal oder Schrottwichteln) und Foto anlegen, bearbeiten und löschen |
| Teilnehmer | Teilnehmer mit Name und Foto je Gruppe verwalten, je Teilnehmer eine Wunschliste |
| Auslosung | Losen ab drei Teilnehmern bis einschließlich Wichteltag; niemand zieht sich selbst, jeder beschenkt und wird genau einmal beschenkt; erneutes Losen legt einen neuen Durchlauf an |
| Historie | Alle Auslosungen bleiben gespeichert, mit Namen und Wünschen von damals (Snapshot) |
| Kamera | Gruppen- und Teilnehmerfotos mit CameraX, inklusive Berechtigungsdialog |

## Bauen und testen

Voraussetzungen: Android Studio (aktuelle stabile Version, bringt ein passendes JDK mit), Android SDK 37. Mindestens Android 8.0 (API 26).

```bash
./gradlew :app:assembleDebug        # Debug-APK bauen
./gradlew :app:testDebugUnitTest    # Unit-Tests (JVM, ohne Gerät)
./gradlew :app:lintDebug            # Android Lint
```

Nach größeren Umbenennungen (z. B. nur Groß-/Kleinschreibung von Dateinamen) einmal `./gradlew clean`
ausführen; sonst kann der inkrementelle Build veraltete Klassen verwenden.

## Architektur

Ein Gradle-Modul, Kotlin, Jetpack Compose mit Material 3, MVVM und Clean Architecture.

```
ui (Compose-Screens, ViewModels)  ──►  domain (Modelle, Regeln, Use Cases)  ◄──  data (Room, Dateien)
```

- **domain** kennt weder Android-UI noch Room. Hier liegen die Modelle, die Fachregeln (`DrawRules`,
  Validatoren), der Los-Algorithmus und die Use Cases.
- **Use Cases sind die einzige Stelle für Validierung.** Schreiben läuft immer über einen Use Case
  (`SaveGiftGroupUseCase`, `SaveParticipantUseCase`, `SaveWishlistItemUseCase`, `PerformDrawUseCase`,
  `DeleteGiftGroupUseCase`). Sie liefern ein `…Result` mit Erfolg oder einer Liste fachlicher Fehler.
- **Repositories sind reine Persistenz.** Lesende Methoden heißen `observe…` und liefern einen `Flow`;
  sie prüfen keine Fachregeln.
- **ViewModels** übersetzen Fehler in Texte (`…Errors.kt`) und halten den Zustand als `StateFlow`
  (`combine` + `stateIn(WhileSubscribed(5_000))`). Einmal-Ereignisse wie „gespeichert“ stehen im UiState
  und werden von der Route nach der Navigation quittiert.
- **Dependency Injection** von Hand: `di/AppContainer` erzeugt Repositories und Use Cases; jedes ViewModel
  hat eine `factory(container, …)`, die Routes rufen sie über `appContainer()` auf.

### Paketregeln

- Ein Paket enthält entweder Dateien oder Unterpakete, nie beides.
- Jeder Screen hat `screen/` (zustandsloser Screen, Dialoge, UiState, UI-Modelle, Actions) und
  `state/` (Route, ViewModel, Fehler-Übersetzung). `screen/` importiert nie aus `state/`.
- Gemeinsame Bausteine liegen in `ui/components/{layout,controls,display,photo}`, Hilfsfunktionen für
  ViewModels und Routes in `ui/common`, Farben, Typografie und Abstände (`Spacing`) in `ui/theme`.
- Tests spiegeln die Paketstruktur von `main`.

## Entscheidungen

- **Snapshot-Historie:** Eine Auslosung speichert Namen und Wünsche der Empfänger als Kopie. Spätere
  Änderungen an Teilnehmern oder Wunschlisten verfälschen alte Ergebnisse nicht.
- **Auslosung in einer Transaktion:** `PerformDrawUseCase` liest Gruppe, Teilnehmer und Wünsche mit einer
  einzigen Room-Abfrage (`DrawDao.getDrawSource`) und speichert das Ergebnis atomar.
- **Wichteltag:** Losen ist bis einschließlich Wichteltag erlaubt. Gruppen bleiben auch danach
  bearbeitbar (z. B. Tippfehler im Namen); ein geändertes Datum muss in der Zukunft liegen.
- **Grenzen:** Namen bis 50 Zeichen, Wünsche bis 200 Zeichen, Budget unter 999 999 €, Wichteltag
  höchstens 100 Jahre in der Zukunft. Keine doppelten Gruppennamen, Teilnehmernamen je Gruppe oder
  Wünsche je Teilnehmer (Groß-/Kleinschreibung zählt nicht).
- **Datenbank:** Room mit exportiertem Schema (`app/schemas`). Ab Version 4 werden Änderungen als
  `AutoMigration` ergänzt; Versionen 1 bis 3 stammen aus der Entwicklung und werden verworfen.
- **Fotos** liegen im privaten App-Speicher (`filesDir/photos`), die Datenbank speichert nur relative
  Pfade. Pfade außerhalb dieses Ordners werden abgelehnt. Fotos ohne Verweis räumt die App beim Start auf.
- **Prozesstod:** Formular- und Editor-Eingaben liegen im `SavedStateHandle` und überstehen es, wenn
  Android die App im Hintergrund beendet.
- **Logging** über `AppLog` mit den Stufen DEBUG, INFO, WARN und ERROR; Tag ist der Klassenname mit dem
  Präfix `Wichtly/`. Release-Builds loggen ab INFO. Namen, Wünsche und Zuordnungen werden nie geloggt.
- **Coroutines:** In `try`/`catch` innerhalb von Coroutines wird `CancellationException` zuerst gefangen
  und weitergeworfen; in `Flow.catch` mit `if (e is CancellationException) throw e`; in normalen
  Funktionen nicht.
- **Backup:** `allowBackup="false"`. Die Regeln für die Gerät-zu-Gerät-Übertragung (Android 12+) sind noch
  nicht angepasst, siehe Gesamtreview (G-01).

## Bekannte Grenzen

Offene Punkte stehen mit Priorität im Gesamtreview unter `docs/review/Wichtly_Gesamtreview_2026-11-25.html`.
Formatierungsregeln für Android Studio stehen in `.editorconfig`.
