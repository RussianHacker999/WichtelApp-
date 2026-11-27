# AI Usage Log Book
This document records every prompt we sent to an AI assistant for the project Wichtly in the course
"Mobile Applications" with Jan Brodbaecker, and what we did with the results.

## Log Entries

#### Default tools 
### Viktoria 
Gemini in Android Studio until 2026-11-17, Claude Code (Claude Opus 5.5) from 2026-11-19.
Entries that used a different tool name it in the heading.

### 2026-11-02 (Viktoria)
**Prompt 1** : "Add all required dependencies needed for the database 
layer with Room, according to our existing Kotlin-Setup/-Version.
Add every dependency required for setup of one simple feature including
AppDatabase class, a DB entity,
a DAO, corresponding Repository classes. State exactly what you added and why."\
**Accepted**: Dependency kinds themselves. \
**Changed**: Some versions were not correct. Auto-corrected with IDE for now.\
**Commits**: 9f2bcce

### 2026-11-03 (Viktoria)
**Prompt 1**: Implement the Room Entity GiftGroup in data/local/entity
for the Table "gift_groups".
Fields are: "id: String as UUID, Primary Key";
"name: String"; "budgetCents: Long";
"drawDay: as LocalDate with ISO String"; "photoPath: "String?".
Summarize your changes.\
**Accepted** as is.\
**Commits**: 9f2bcce

**Prompt 2**: For the next step, do NOT implement, only make suggestions.
Please suggest a contract for GiftGroupdDao and GiftGroupRepository,
including functions, and thoroughly justify any suggestion.
Suggest whether our app needs any further GiftGroup-related classes
in the data layer, and why or why not. /plan\
**Accepted**: getAllGroups() as Flow. getGroupById as Flow. deleteGroupById().\
**To further verify**: insert/update vs. upsert. AI added Class GroupMembers, 
I removed it for now.\
**Commits**: 9f2bcce


**Prompt 3**: 
Check again which implementation makes more sense: 
Separate insert/update DAO functions or one upsert operation. 
Justify in depth and in detail. /plan\
**Decision**: upsert. Note that this decision was reverted later.\
**Commits**: 9f2bcce

**Prompt 4**:
Now implement the GiftGroupDao class ONLY.
Functions: "getAllGiftGroups(): Flow<List<GiftGroup>>";
"getGiftGroupById(id:String): Flow<GiftGroup?>";
" suspend fun upsertGroup(group: GiftGroup)";
"suspend fun deleteGroupById(id: String)" Only these ones for now.\
**Accepted** as is.\
**Commits**: 9f2bcce

**Prompt 5**:
Rename the DB data class GiftGroup to GiftGroupEntity
everywhere where it is required (state where you renamed it)\
**Accepted** as is.\
**Commits**: 9f2bcce

**Prompt 6**:
Please check whether an extra GiftGroup domain class 
(mapped to the DB class GiftGroupEntity) is
advised or not. Thoroughly justify your decision.
/plan\
**Accepted** as is.\
**Changed**: Removed unused helper functions from new GiftGroup class for now.\
**Commits**: 9f2bcce

**Prompt 7**:
Implement a GiftGroupValidator class in the domain layer.
It should include the following domain validation rules:
Group name is not empty. Max Group name length is 50.
No name duplicates allowed.
Group budget is greater than null.
Create operation should only allow dates lying in the future.
Suggest further business rules if you consider them sensible, 
but not implement them yet, only name them.
Keep in mind the difference between UI validation and pure domain validation.\
**Accepted** for now.\
**Commits**: 9f2bcce


**Prompt 8**:
Implement a minimal Dependency-Injection for our Database-related code without Hilt. 
It should be implemented via an AppContainer. 
Make changes to all dependents, such as Manifest etc.
State exactly what you did and why.\
**Accepted** for now.\
**Commits**: 9f2bcce


### 2026-11-05 (Viktoria)
**Prompt 1**:
Add error handling for GiftGroupRepositoryImpl so that it returns an error if 0 
rows are updated. (not found)\
**Accepted** as is.\
**Commits**: ac7b7d2

**Prompt 2**: 
Remove ID generation from Room GiftGroup entity; make GiftGroupRepositoryImpl the owner 
of UUID creation.\
**Accepted** as is.\
**Commits**: ac7b7d2

**Prompt 3**: 
Add GiftGroup Validation rule: Not only create but also Update operation should only be possible
for future date.\
**Accepted** as is.\
**Commits**: ac7b7d2

### 2026-11-08 (Melissa, Meyer), Copilot von Microsoft (kostenlos)
**Prompt 1**: Wo liegt der Unterschied zwischen einem UIState und einem ViewModel? Ist es sinnvoll beides in eine Adroid App mit Composable habe?.\
**Promt 2**: Es soll mehrere Einträge aus einer Datenbank gezogen werden, sodass die eine Liste an Namen auf den Screen geprinted wird. Ich weiß, dass man eine solche Liste im Composable als LazyColumn speichert. Ist das im UISate auch der Fall?.\
**Angenommen:**  Datein und ihr Grundgeruest (mit einer (leeren) List für die Gruppen aus der Datenbank(wird spaeter verbunden))
**Geaendert:**   Datein fuer GroupsViewModel und GroupsUISate eingefuegt
**Commits:**     d3198e9

### 2026-11-08 (Melissa, Meyer), Copilot von Microsoft (kostenlos)
**Prompt 1**: Wie kann ich diese Methode im ViewModel nutzen? Die Methode gibt Daten aus einer Datenbank zurück. Ich möchte diese Daten im ViewModel nutzen. Muss ich einen bei jedem Auslesen ein neues GiftGroupRepositoryImpl-Objekt erstellen? ​‌
class GiftGroupRepositoryImpl(
    private val giftGroupDao: GiftGroupDao,
    private val validator: GiftGroupValidator
) : GiftGroupRepository {
        override fun getGiftGroups(): Flow<List<GiftGroup>> {
        return giftGroupDao.getAllGiftGroups().map { it.toDomainModelList() }
    }} 
**Promt 2**: Bitte erklaere mir die typischen Verwendungen im ViewModel genauer
**Promt 3**: Ich brauche erstmal ausschließliche den Namen der Gruppe. Ist es sinnvoll nur den Namen der Gruppe zuspeichert oder sollte man trotzem alles speichern? fun GiftGroupEntity.toDomainModel(): GiftGroup = GiftGroup(
    id = id,
    name = name,...)
**Angenommen:**  GiftGroupRepository dem GroupsViewModel uebergeben, init
**Geaendert:**  GroupsUiState speichert List<GiftGroup> statt List<string>
**Commits:** c83b0f3

### 2026-11-08 (Diana, Folz), Claude.ai (kostenlos)

**Prompt 1**: 
Lies AGENTS.md und den AP3.3-Vertrag. Plane einen ParticipantWishlistScreen, der nur participantId als Navigationsargument erhält. Der Screen lädt den Teilnehmer 
über ParticipantRepository.observeParticipantById(id) und die Einträge über WishlistRepository.observeWishlistItems(id). Zeige Teilnehmernamen als Überschrift und 
seine Wunschliste. Add/Edit/Delete eines Wunscheintrags. Keine Namen als Schlüssel und keine ParticipantInput/WishlistInput-Klasse. Definiere UiState, Route und Dialoge; noch keine Dateien ändern.
**Accepted**: Zustände Loading/NotFound, Dialog-State im ViewModel, key = item.id.\
**Commits**: ca5020f

**Prompt 2**:
Lies zuerst AGENTS.md. Lege im Paket com.example.wichtly.ui.wishlist (Ordner ui/wishlist) genau diese drei Dateien an: TemporaryWishlistPlaceholders.kt, WishlistUiState.kt und ParticipantWishlistScreen.kt. Ändere keine bestehenden Dateien. Kein ViewModel, kein Repository, keine Navigation, kein AppContainer, keine neuen Dependencies.
TemporaryWishlistPlaceholders.kt: Eine data class TemporaryWishlistItem(id: String, participantId: String, text: String, sortOrder: Int). Kommentiere deutlich, dass sie temporär ist und später durch das Domain-Modell WishlistItem von Person B ersetzt wird.
WishlistUiState.kt: Eine data class WishlistUiState mit isLoading: Boolean = true, participantName: String? = null, items: List<TemporaryWishlistItem> = emptyList(), errorMessage: String? = null, isSaving: Boolean = false. Kommentiere: isLoading false und participantName null bedeutet “Teilnehmer nicht gefunden”; eine leere Liste ist ein gültiger Zustand und kein Fehler.
ParticipantWishlistScreen.kt: Ein zustandsloser Composable. Er bekommt state: WishlistUiState und die Callbacks onBack, onAddClick, onEditClick(itemId: String), onDeleteClick(itemId: String) als Parameter. Er kennt kein ViewModel und kein Repository. Aufbau mit Scaffold, TopAppBar (Titel ist der Teilnehmername, Zurück-Aktion) und LazyColumn mit key = { it.id }. Jeder Eintrag ist eine Card mit Text sowie Bearbeiten- und Löschen-Aktion. Es gibt vier Darstellungen: Laden, Teilnehmer nicht gefunden (mit Zurück-Aktion), Fehler (zeigt errorMessage) und Bereit. Im Zustand Bereit mit leerer Liste: Hinweistext und Aktion “Wunsch hinzufügen”; sonst eine Aktion “Wunsch hinzufügen” über der Liste. Den modifier-Parameter nur am äußersten Element verwenden.
Füge für jeden Zustand eine private @Preview in WichtlyTheme hinzu: Laden, Nicht gefunden, Fehler, Bereit leer, Bereit mit drei Beispielwünschen. Die Fake-Daten gehören nur in die Previews, nicht in den Screen. Orientiere dich am Stil von GroupsScreen (Material 3, Scaffold, Card). Kommentare auf Deutsch, die erklären, warum etwas so gebaut ist. Fasse am Ende zusammen, was du angelegt hast.
**Accepted**: WishlistUiState, TemporaryWishlistItem, ParticipantWishlistScreen mit fünf Previews.\
**Commits**: ca5020f

### 2026-11-08 (Diana, Folz), Claude.ai (kostenlos)

**Prompt 1**:
Lies zuerst AGENTS.md. Ändere nur WishlistUiState.kt im Paket com.example.wichtly.ui.wishlist. Ändere keine anderen Dateien. Kein ViewModel, kein Repository, keine Navigation, keine neuen Dependencies.

Ergänze showEditor: Boolean = false, editingItemId: String? = null, editorText: String = “”, editorError: String? = null und pendingDeleteItemId: String? = null. Aktualisiere die KDoc. editingItemId 
null bedeutet Hinzufügen-Modus, eine ID bedeutet Bearbeiten-Modus. errorMessage bleibt für Ladefehler, die den ganzen Screen ersetzen. Validierungs- und Speicherfehler beim Hinzufügen oder Bearbeiten gehören in editorError. Bestehende Felder und Standardwerte bleiben unverändert. Das Projekt muss weiterhin bauen. Fasse am Ende zusammen, was du geändert hast.
**Accepted**: WishlistUiState variables and values: showEditor, editingItemId, editorText, editorError, pendingDeleteItemId
**Changed**: Comments explaining certain features (for maintainability reasons)
**Commits**: c52d021

**Prompt 2**:
Explain the following code:
```kotlin
data class WishlistUiState(
    val isLoading: Boolean = true,
    val participantName: String? = null,
    val items: List<TemporaryWishlistItem> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val showEditor: Boolean = false,
    val editingItemId: String? = null,
    val editorText: String = "",
    val editorError: String? = null,
    val pendingDeleteItemId: String? = null
)
```
**Accepted**: Accepted val's listed above, after considerung Gemini's explanation.
**Commits**: c52d021

**Prompt 3**:
Lies zuerst AGENTS.md. Lege nur die neue Datei WishlistItemDialog.kt im Paket com.example.wichtly.ui.wishlist an. Ändere keine bestehenden Dateien. Kein ViewModel, kein Repository, keine Navigation, keine neuen Dependencies. 
Ein Wunsch hat nur text, keine Titel, Beschreibungen, Links oder Kategorien.

Ein zustandsloser AlertDialog für Hinzufügen und Bearbeiten. Parameter: isEditMode: Boolean, text: 
String, errorMessage: String?, isSaving: Boolean, onTextChange: (String) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit. Ein einzeiliges OutlinedTextField mit der Fehlermeldung direkt darunter (isError und supportingText). 
Der Speichern-Button ist während isSaving deaktiviert. Die Titel lauten “Wunsch hinzufügen” und “Wunsch bearbeiten”, die Buttons “Speichern” und “Abbrechen”.

Füge private @Preview-Funktionen in WichtlyTheme hinzu: Hinzufügen leer, Bearbeiten mit vorbelegtem Text, mit Fehlermeldung und im Speichern-Zustand. Fake-Daten nur in den Previews. Kommentare auf Deutsch, die das Warum erklären. 
Das Projekt muss weiterhin bauen. Fasse am Ende zusammen, was du angelegt hast.
**Accepted**: Accepted created file "WishlistItemDialog.kt" with very few parameter changes.
**Commits**: b913bd1

**Prompt 4**:
Lies zuerst AGENTS.md. Lege nur die neue Datei WishlistDeleteDialog.kt im Paket com.example.wichtly.ui.wishlist an. Ändere keine bestehenden Dateien. Kein ViewModel, kein Repository, keine Navigation, keine neuen Dependencies.

Ein zustandsloser AlertDialog. Parameter: itemText: String, isSaving: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit. Er zeigt den Wunschtext und fragt, ob er gelöscht werden soll. Der Bestätigen-Button ist während isSaving 
deaktiviert. Füge eine private @Preview-Funktion in WichtlyTheme hinzu, Fake-Daten nur dort. Kommentare auf Deutsch, die das Warum erklären. Das Projekt muss weiterhin bauen. Fasse am Ende zusammen, was du angelegt hast.
**Accepted**: stateless ParticipantWishlistScreen; keeping in mind that actualy data will replace temporary test data
**Commits**: 10db69d

**Prompt 5**:
Lies zuerst AGENTS.md. Ändere nur ParticipantWishlistScreen.kt im Paket com.example.wichtly.ui.wishlist. Kein ViewModel, kein Repository, keine Navigation, keine neuen Dependencies. Der Screen bleibt zustandslos.

Ergänze in der Signatur von `ParticipantWishlistScreen` fünf neue Parameter, über die später bestimmte Aktionen ausgeführt werden können:

- `onEditorTextChange: (String) -> Unit`
- `onSaveClick: () -> Unit`
- `onDismissEditor: () -> Unit`
- `onConfirmDelete: () -> Unit`
- `onDismissDelete: () -> Unit`

Die Reihenfolge muss genau so bleiben. Füge die neuen Parameter nach `onDeleteClick` und vor `modifier` ein.
Gib den neuen Parametern keine Standardwerte. Dadurch soll ein Compilerfehler entstehen, wenn bei einem Aufruf vergessen wird, eine der neuen Funktionen zu übergeben.
Die bereits vorhandenen Funktionen und ihre Aufgaben sollen unverändert bleiben. Die fünf neuen Funktionen werden in diesem Schritt noch nicht verwendet oder aufgerufen. 
Passe alle bestehenden Previews so an, dass sie für die neuen Parameter jeweils eine leere Funktion `{}` übergeben. Dadurch sollen die Previews weiterhin ohne Fehler kompilieren.
Ergänze außerdem KDoc-Kommentare für alle fünf neuen Parameter, die kurz erklären, wofür sie später gedacht sind. Das gesamte Projekt muss danach erfolgreich bauen. Fasse am Ende zusammen, welche Änderungen du vorgenommen hast.
**Commits**: 4e77861

**Prompt 6**:
Lies dir zuerst die `AGENTS.md` durch. Ändere ausschließlich die Datei `ParticipantWishlistScreen.kt` im Paket `com.example.wichtly.ui.wishlist`.
Verändere nichts am ViewModel, Repository oder an der Navigation. Füge auch keine neuen Dependencies hinzu. Der Screen soll weiterhin zustandslos bleiben und seine Informationen ausschließlich über den übergebenen Zustand und die Parameter erhalten.
Zeige den `WishlistItemDialog` an, sobald `state.showEditor` den Wert `true` hat.
Übergebe dem Dialog dabei folgende Werte:

- `isEditMode`: Soll `true` sein, wenn `state.editingItemId != null` ist.
- `text`: Verwende `state.editorText`.
- `errorMessage`: Verwende `state.editorError`.
- `isSaving`: Verwende `state.isSaving`.
- `onTextChange`: Übergib die Funktion `onEditorTextChange`.
- `onSave`: Übergib die Funktion `onSaveClick`.
- `onDismiss`: Übergib die Funktion `onDismissEditor`.

Der Dialog soll über dem normalen Screen-Inhalt angezeigt werden. Baue ihn deshalb nicht direkt in die `when`-Verzweigung für die verschiedenen Screen-Zustände (Laden, Fehler und Nicht gefunden) ein.
Wichtig: Der Screen soll den Dialog nicht selbst öffnen oder entscheiden, wann er geöffnet wird. Er soll nur darauf reagieren, ob `state.showEditor` auf `true` steht.
Füge außerdem eine private `@Preview`-Funktion innerhalb des `WichtlyTheme` hinzu. In dieser Preview soll der Screen mit einem bereits geöffneten Editor im Hinzufügen-Modus angezeigt werden. Verwende dafür Fake-Daten, die ausschließlich in dieser Preview angelegt werden.
Schreibe alle Kommentare auf Deutsch. Das gesamte Projekt muss danach erfolgreich bauen. Fasse am Ende zusammen, welche Änderungen du vorgenommen hast.
**Accepted**: UI-Screen Vorschläge als temporärer Erstversuch. 
**Changed**: Minimale spontane UI-Design Änderungen (Schrift, Anzeige, etc.)
**Commits**: ef1f96c

**Prompt 7**:
Lies zuerst AGENTS.md. Ändere nur ParticipantWishlistScreen.kt im Paket com.example.wichtly.ui.wishlist. Kein ViewModel, kein Repository, keine Navigation, keine neuen Dependencies. Der Screen bleibt zustandslos.

Zeige WishlistDeleteDialog, wenn state.pendingDeleteItemId gesetzt ist und in state.items ein Eintrag mit genau dieser ID existiert. Existiert er nicht, wird kein Löschdialog gezeigt. itemText ist der text dieses Eintrags, 
isSaving ist state.isSaving, onConfirm ist onConfirmDelete, onDismiss ist onDismissDelete. Der Dialog liegt über dem Inhalt und gehört nicht in die when-Verzweigung der Zustände Laden, Fehler und Nicht gefunden. Füge eine 
private @Preview-Funktion in WichtlyTheme hinzu: Screen mit offenem Löschdialog, Fake-Daten nur dort. Kommentare auf Deutsch. Das Projekt muss bauen. Fasse am Ende zusammen, was du geändert hast.
**Accepted**: UI-Screen Vorschläge als temporärer Erstversuch im delete Wishlist item Kontext.
**Commits**: 10930b2

### 2026-11-10 (Viktoria)

**Prompt 1**:
Read AGENTS.md. Read our domain model GiftGroup. Propose a domain model for Participant (as in)
secret santa participant of a GiftGroup. Justify it.
**Accepted**: id, groupId, name, photoPath for now.
**Rejected**: fields wishlist, assignedRecipientId (for now).
**Commits**: 425799c, 823ac40

**Prompt 2**:
Suggest a Repository interface for the Participant model suitable
for CRUD operations, including listing.
**Accepted**: participantvalidator class analogous zu giftgroup validator
**Rejected**: seel follow-up questions in Prompt 3
**Commits**: 425799c, 823ac40

**Prompt 3**:
Two questions: 1) do still need deleteParticipantsByGroupId
even if we have CASCADE deletion for groups,2)
**Accepted**: remove deleteParticipantsByGroupId
**Commits**: 425799c, 823ac40


### 2026-11-10 (Diana, Folz), Claude.ai (pro)

**Prompt 1**:
Lies zuerst AGENTS.md. Ändere nur GroupFormInput.kt im Paket com.example.wichtly.ui.groups.form, nicht die Testdatei. Die Tests in GroupFormInputTest.kt sind die Vorgabe. Mach alle Tests grün, ohne das Verhalten bereits
grüner Tests zu ändern. parseBudgetToCents darf bei keiner Eingabe eine Exception werfen, auch nicht bei Überlauf von Long. Rechne weiter mit BigDecimal, nicht mit Double. Prüfe das Eingabeformat zuerst, bevor du umwandelst:
nach trim nur Ziffern, optional ein Komma oder Punkt mit einer oder zwei Nachkommastellen, sonst null. Kommentiere auf Deutsch, warum. Das Projekt muss bauen. Fasse zusammen, was du geändert hast.
**Accepted**: GroupFormInput.kt (Parsing mit BigDecimal und striktem Datum, nicht nur die Standart-Grenzen/Werte)
**Changed**: Tests um Grenzfälle erweitert, weil Gemini nur Normalfälle getestet hatte (Überlauf, 1e3, Schaltjahr, einstellige Tage)
Checked: 12 Tests grün
**Commits**: 46263aea

**Prompt 2**:
Lies zuerst AGENTS.md. Lege im Paket com.example.wichtly.ui.groups.form die Dateien GroupFormUiState.kt und GroupFormErrors.kt an. Ändere keine bestehenden Dateien. Kein ViewModel, kein Compose-Screen, keine neuen Dependencies.
GroupFormUiState: data class mit isLoading: Boolean, isEditMode: Boolean, notFound: Boolean, name: String, budget: String, drawDay: String, drawType: DrawType, nameError: String?, budgetError: String?, drawDayError: String?,
generalError: String?, isSaving: Boolean, isSaved: Boolean. Sinnvolle Standardwerte. Kommentiere, was jedes Feld bedeutet. Das Foto kommt nicht vor.
GroupFormErrors.kt: Eine Funktion, die eine List<GiftGroupValidationError> aus domain/validation in die Felder des States übersetzt, mit deutschen, nutzerfreundlichen Texten: EmptyName, NameTooLong
und DuplicateName gehören zu nameError, InvalidBudget zu budgetError, DateInPast zu drawDayError, GroupNotFound zu generalError. Zwei Fehler auf demselben Feld ergeben einen Text, nicht zwei. Nimm
die Klassen aus dem Projekt, erfinde keine. Füge einen JUnit-Test hinzu. Das Projekt muss bauen. Fasse zusammen.
Die Fehlermeldung für ein nicht lesbares Datum soll das Format TT.MM.JJJJ nennen, die für ein nicht lesbares Budget ein Beispiel wie 25,50. Diese Formatfehler entstehen im ViewModel vor dem Aufruf des Repositorys, lege dafür Texte als Konstanten an.
**Accepted**: GroupFormUiState.kt, GroupFormErrors.kt, GroupFormErrorsTest.kt
**Commits**: ca5d2602

### 2026-11-10 (Diana, Folz), Gemini in Android Studio, Claude.ai (pro)

**Prompt 3**:
/plan Lies zuerst AGENTS.md. Ändere keine Dateien. Untersuche app/build.gradle.kts, gradle/libs.versions.toml, WichtlyApplication.kt, MainActivity.kt und di/AppContainer.kt. Beantworte: (1) Welche der Libraries lifecycle-viewmodel-compose, lifecycle-runtime-compose und navigation-compose sind bereits als Dependency vorhanden, mit Versionen? (2) Wie ist der AppContainer von außen erreichbar, zum Beispiel über die Application-Klasse? (3) Welche minSdk und welche Kotlin- und Compose-Version sind gesetzt? (4) Gibt es schon ViewModels, Factories oder Navigation? Nenne nur, was wirklich existiert, und erfinde nichts.\
**Accepted**: Bestandsaufnahme (keine der drei Libraries vorhanden, Zugriff auf den AppContainer über `WichtlyApplication.container`, minSdk 26, noch keine ViewModels/Factories/Navigation).\
**Changed**: Nur `lifecycle-viewmodel-compose` ergänzt. `lifecycle-runtime-compose` war nicht nötig, weil `collectAsStateWithLifecycle` bereits verfügbar war. navigation-compose bleibt Aufgabe von Person A. Vorher im Gruppenchat angekündigt, weil libs.versions.toml und build.gradle.kts dem ganzen Team gehören. Die beiden Dateien wurden getrennt vom Rest committet.\
**Commits**: 19030c4, c0252ff

**Prompt 4**:
/plan Konzept für ein WishlistViewModel: combine aus Teilnehmer-Flow, Wunsch-Flow und lokalem Dialog-Zustand, stateIn mit WhileSubscribed(5_000), Validierung und Fehlerübersetzung, Schutz vor doppeltem Speichern, Verhalten bei gelöschtem Teilnehmer, Factory über den AppContainer. Noch keine Dateien ändern.\
**Accepted**: Plan als implementation_plan.artifact.md (lokaler Zustand in eigener data class, Editor schließt automatisch, wenn der Teilnehmer verschwindet). Umgesetzt am 13.11.\
**Commits**: -

**Prompt 5**:
GroupFormInput aufräumen und die Fehlertests um Grenzfälle erweitern (Überlauf, Exponentenschreibweise, Schaltjahr, einstellige Tage).\
**Accepted**: As is.
**Commits**: 58dbd7b

**Prompt 6**:
GroupFormViewModel samt Factory für Anlegen und Bearbeiten und zustandsloser GroupFormScreen. Gruppe beim Bearbeiten nur einmal laden (`first()` statt dauerhaftem Beobachten), beim Speichern `copy()` auf der geladenen Gruppe (damit `photoPath` nicht verloren geht), Schutz vor Doppelklick, kein stilles `catch`, Format- und Validierungsfehler über GroupFormErrors. Previews für Anlegen, Bearbeiten, Fehler, Laden und Nicht gefunden.\
**Accepted**: Repository holen, ViewModel erzeugen, State sammeln, Speicher-Muster. Als Basis für die folgenden Prompts committet.\
**Commits**: a082b5d, 1ba5aff

### 2026-11-11 (Diana, Folz), Gemini in Android Studio, Claude.ai (pro)

**Prompt 1**:
GroupFormRoute, die AppContainer, Factory und Screen verbindet und nur groupId (null = neu) sowie onBack und onSaved bekommt.\
**Accepted**: Route.\
**Changed**: In der Route die Erzeugung von Factory und ViewModel korrigiert (Fehler aus der KI-Version). Zum Testen GroupFormRoute vorübergehend in MainActivity aufgerufen, nicht committet. Kommentare aufgeräumt.\
**Checked**: Emulator: Gruppe anlegen, im Database Inspector geprüft. Bearbeiten-Modus mit der echten UUID aus dem Inspector: Formular zeigt die gespeicherten Werte.\
**Commits**: e58fce8, 452d466, 9f0f08a, 11f9786

Teamentscheidung vorab: Die Teilnehmerliste liegt im GroupDetailScreen, Teilnehmer werden über Dialoge verwaltet (kein eigener Screen), der Losen-Button erscheint in der Startseite und im Gruppendetail.

**Prompt 2**:
GroupDetailUiState (Laden, Fehler, Nicht gefunden, Inhalt, Dialog-Felder) und ParticipantErrors (Übersetzung der Domain-Fehler in deutsche Texte, mit Test).\
**Accepted**: As is.\
**Commits**: 0f69e77

**Prompt 3**:
GroupDetailViewModel mit Factory: combine aus Gruppe und Teilnehmern, lokaler Dialog-Zustand, isSaving-Schutz in allen Handlern, keine Namen als Schlüssel.\
**Accepted**: As is.\
**Commits**: 6d30248

**Prompt 4**:
Zustandsloser GroupDetailScreen nach Mockup (Kopfbereich, Statistik-Karten, Stammdaten-Karte, Teilnehmerliste mit Drei-Punkte-Menü) mit Previews für alle Zustände.\
**Accepted**: As is.\
**Commits**: dcdf671

**Prompt 5**:
ParticipantEditorDialog und ParticipantDeleteDialog (zustandslos, Previews), danach im Screen über dem Inhalt anzeigen, gesteuert nur über den UiState.\
**Accepted**: As is.\
**Commits**: 20d64a3, 3c69a78

### 2026-11-12 (Viktoria)

**Prompt 1**:
Implement a Participant Room entity.
id/pk String, groupId String, name String, photopath like in GiftGroupEntity.
groupdId as foreignkey towards GiftGroupEntity.id in gift_groups,
onDelete=CASCADE.
**Accepted**: ParticipantEntity, Participant Mapper classes. Changes to AppDatabase, AppContainer.
For now, accept fallback to destructive Migration in AppDatabase.
**Commits**: 15d5479

**Prompt 2**:
Implement ParticipantDao with following functions:
getParticipantsByGroupId(groupId: String): Flow<List<ParticipantEntity>>
fun getParticipantById(id: String): Flow<ParticipantEntity?>
suspend fun insertParticipant(participant: ParticipantEntity)
suspend fun updateParticipant(participant: ParticipantEntity): Int
suspend fun deleteParticipantById(id: String)
**Accepted**: DAO.
**Commits**: 15d5479

**Prompt 3**:
Now implement ParticipantRepository according to the interface,
in analogy to GiftGroupRepositoryImpl
**Accepted**: ParticipantRepositoryImpl; ParticipantValidator as preliminary versions.
**Rejected**: loadParticipantsByGroup; was not part of Interface, remove for now.
**Commits**: 15d5479

**Prompt 4**:
Please remove singleton logic from AppDatabase.
**Accepted**: as is.
**Commits**: 2ad5f55

**Prompt 4**:
Please move UUID creation from the repository layer to the domain classes.
**Accepted**: as is.
**Commits**: 2ad5f55

**Prompt 5**:
Check all places where the name param is being trimmed.
Evaluate if it makes sense to also trim it in the RepositoryImpl classes or not.
**Accepted**: Was quickly accepted until single source of trimmig not 100% identified.
**Commits**: 2ad5f55

**Prompt 6**:
Replace (repositoryimpl classes) the UPDATE NOtFound Error handling with pure existence
check
**Accepted**: accepted but later on decision changed.
**Commits**: 2ad5f55

### 2026-11-12 (Diana, Folz), Gemini in Android Studio, Claude.ai (pro)

**Prompt 1**:
Kleine UI-Anpassungen im Gruppendetail an Mockup und echte Daten.
**Accepted**: As is.
**Commits**: 07f55f8

**Prompt 2**:
GroupDetailRoute mit den Callbacks onBack, onEditGroup(groupId) und onOpenWishlist(participantId). Die Navigation baut Person A.
**Accepted**: As is
**Commits**: 5eab887


### 2026-11-13 (Viktoria)

**Prompt 1**:
Change the WishListItem domain class so that it has the signature of TemporaryWishlistPlaceholders.kt.
Also add interface WishListRepository with: observerWishlistItems; createWishListItem, updateWishListItem,
deleteWishListItem
**Accepted**: as is.
**Commits**: b94c1f0

**Prompt 2**:
Implement a WishListValidator class
in analogy to existing validator classes.
**Accepted**: as is.
**Commits**: b94c1f0

**Prompt 3**:
Implement WishListItemEntity als Table wishlist_items with id:String PK, participantId String FK
(ParticipantEntity id, ondelete cascade, index).
Also implement WishListItemdao with observerbyparticipantid sorted by sortOrder;
insert, update; delete.
**Accepted**: as is.
**Commits**: b94c1f0

**Prompt 4**:
Add getWishListItemById to Wishlist dao and repository.
Implement WishListRepository according to the interface.
Add validation and trimming in analogy to existing repository classes.
**Accepted**: as is.
**Commits**: b94c1f0

### 2026-11-13 (Diana, Folz), Gemini in Android Studio, Claude.ai (pro)

**Prompt 1**:
TemporaryWishlistItem durch das Domain-Modell WishListItem von Viktoria ersetzen.
**Accepted**: As is.
**Commits**: 9ca94ca

**Prompt 2**:
Fehlerübersetzung für Wunschtexte (WishListErrors) und Anzeige von Löschfehlern im Löschdialog.
**Accepted**: As is.
**Commits**: b785d28, 7d4bebd

**Prompt 3**:
WishlistViewModel und WishlistViewModelFactory nach dem Plan vom 10.11.
**Accepted**: As is.
**Commits**: 6948548


### 2026-11-14 (Melissa, Meyer), Copilot von Microsoft (kostenlos)
**Prompt 1**: Der Button mit der Aufschirft "Losen" soll auf dem Screen immer ganz unten angezeigt werden. Zudem soll Button soll höhe und die die Aufschrieft groeßer und dick sein. (Plus einer Datei mit dem aktuellem Code der Klasse GroupsScreen.\
**Angenommen:** modifier vom Losen-Button; .weight(1f) fuer die LazyColum
**Geaendert:**  modigier vom Gruppe-Hinzufuegen-Button
**Commits:**    ce61af3

### 2026-11-14 (Melissa, Meyer), Copilot von Microsoft (kostenlos)
**Prompt 1**:Ich möchte eine Carde erstellen, wo links ein Name steht und rechts 3 Button sind. Auf dem Button ganz rechts soll eine Stift (zeichen für Bearbeitung) abgebildet sein.\
**Promt 2**: Wie fuege ich die Imports fuer die Icons hinzu?
**Geaendert:**  Aufbau der Cards der LazyList fur die Gruppen & Bearbeiten-IconButton 
**Commits:**    47ae2d8

### 2026-11-14 (Diana, Folz), Gemini in Android Studio, Claude.ai (pro)

**Prompt 1**:
ParticipantWishlistRoute, die AppContainer, Factory und Screen verbindet und nur participantId bekommt.
**Accepted**: As is.
**Commits**: c05c5cc

**Prompt 2**:
ParticipantWishlistScreen an das Mockup (Prototyp 2) angleichen: Kopfkarte mit Teilnehmer, nummerierte Wunschkarten, Hinweis bei leerer Liste.
**Accepted**: Neues Layout.
**Rejected**: Der Agent wollte eigenmächtig `git checkout HEAD -- MainActivity.kt WishlistViewModel.kt` ausführen und damit ungespeicherte Änderungen verwerfen. Abgelehnt. Seitdem steht in jedem Prompt „Führe keine git-Befehle aus“.
**Checked**: Emulator mit sehr langen Wunschtexten (Umbruch statt Abschneiden).
**Commits**: 60326de

**Prompt 3** :
Test-Infrastruktur für die Wunschliste: `kotlinx-coroutines-test`, `testOptions { unitTests.isReturnDefaultValues = true }` (damit `Log.e` in Unit-Tests nicht abstürzt), MainDispatcherRule sowie FakeWishListRepository und FakeParticipantRepository mit Test-Schaltern (failFlows, nextResult, shouldThrow) und Zählern.
**Accepted**: As is.
**Changed**: Version von coroutines-test an die vorhandene Coroutines-Version angepasst (vorher mit `./gradlew :app:dependencies` geprüft). FakeParticipantRepository in einem zweiten Schritt vervollständigt.
**Commits**: a3875e0, dddfe42, 709b849, 802bf94

**Prompt 4**:
WishlistViewModelTest mit fünf Grundfällen: Teilnehmer lädt, unbekannter Teilnehmer, leerer Text setzt editorError ohne Repository-Aufruf, drei Wünsche bekommen sortOrder 0, 1, 2, Bearbeiten behält id und sortOrder.
**Accepted**: As is.
**Commits**: e4b302b

**Prompt 5**:
Lies zuerst AGENTS.md. Ergänze in app/src/test/java/com/example/wichtly/ui/wishlist/WishlistViewModelTest.kt sechs weitere Tests, ohne die vorhandenen zu ändern. Ändere keine anderen Dateien, keine Gradle-Dateien, keine git-Befehle. Nutze die Schalter der Fakes. Teste: (6) Ein Flow-Fehler (failFlows) ergibt errorMessage und participantName bleibt null, notFound gilt nicht. (7) Ein ValidationError vom Repository (nextResult) lässt den Editor offen und setzt editorError. (8) onConfirmDelete mit shouldThrow setzt deleteError und lässt den Dialog offen. (9) Wird der Teilnehmer entfernt, während der Editor offen ist, schließt der Editor. (10) Nach dem Löschen des mittleren von drei Wünschen bekommt ein neu angelegter das höchste sortOrder plus 1. (11) Zweimal direkt hintereinander onSaveClick aufgerufen ergibt genau einen Aufruf am Repository (createCalls ist 1). Jeder Test sammelt den State aktiv wie in den vorhandenen Tests. Kommentare auf Deutsch. Das Projekt muss bauen und alle Tests laufen. Fasse zusammen, welche laufen und welche scheitern.
**Accepted**: Alle sechs Tests.
**Checked**: 42 Unit-Tests im Projekt grün.
**Commits**: ef5acc2


### 2026-11-16 (Viktoria)

**Prompt 1**: Look at Feature 3 in AGENTS.md. I am starting to work
on the Room entities for both Current draws and historic/immutable draw snapshots.
Please give your opinion whether one or several draw snapshot entities make more sense
and justify your decision. /plan
**Accepted**: entities draw, draw_assignment with fields.
**Rejected**: Extra tables for photopath. Photos do not belong in historical snapshots. giver/receiver id as nullables (not sensible for snapshot?).
**Decided**: Three tables: draw, draw_assignment (fk draw), draw_wishlist (fk draw_assignment). Then prompted to implement altered plan.
**Commits**: 2e0f8d3

**Prompt 2**:
Now help me figure out which repository functions are required for all user flows
of the entire draw feature (see AGENTS.md).
There will be three Screens: DrawPreparation (before submitting),
DrawHistory (showing summaries of all draws), DrawDetail (showing any
draw detail, including assignments and wishes).
Make a suggestion for the repository interface and justify it. /plan
**Accepted**: Suggested domain models. Suggested repo functions (Drawprep: getlastestdraw, performdraw;
drawhistory: getdrawsummaries; drawdetail: getdrawbyid)
**Rejected**: No delete function for snapshots. Performdraw did not return result, I modified plan.
**Commits**: 2e0f8d3

**Prompt 3**:
Implement drawrepository interface and domain models, but change the following
things: no delete functions, savedraw returns (for now empty) drawresult.
Now implement a DrawRepositoryImpl and a DrawDao based on that.
**Accepted**: as is.
**Rejected**: No separate usecase for now for simplicity reasons.
**Commits**: 2e0f8d3

**Prompt 4**:
Create comments for UI people so they exactly know when to use which of my repository
functions. Create them in a separate file.
**Accepted**: as is.
**Commits**: 2e0f8d3

**Prompt 5**:
Now work on a pure domain-level draw algorithm. /plan
**Accepted**: Algorithm plan was corrected once. Pure ring shuffle was replaced with real "random" permutation plus fallback to ring.
**Rejected**: No separate usecase for now. Added Validation errors for draw. Added Validation errors/result class in current plan.
Removed duplicate repo fun saveDraw vs. performDraw. Removed arg drawtype from performdraw; business rule for both types identical.
Generic <T> signature was replaced with Strings (for participant IDs).
**Commits**: 2e0f8d3



### 2026-11-15 – 2026-11-20 (Merve, Öztürk), Claude.ai (Opus 5.5)
**Prompt 1**: "ich arbeite gerade an einem code, android studios, das ist ein gruppenprojekt" (Anhang: Arbeitsplan AP3.3, Projektfahrplan, Architekturmodell, Leitfäden, APK)\
**Accepted**: Überblick über den Projektstand.\
**Commits**: -

**Prompt 2**: Teamaufteilung A–D mit offenen Fragen: "Was ist der AppContainer? Warum GroupsUiState UND ViewModel? Wie funktioniert StateFlow / Datenfluss DB → UI? Wo kommt die Formularvalidierung hin? Ich muss die Navigation"\
**Accepted**: Erklärungen zu AppContainer, UiState/ViewModel, StateFlow und Validierung.\
**Commits**: -

**Prompt 3**: "So sieht der Code bisher aus"\
**Accepted**: Analyse des Code-Stands: Navigation fehlt, GroupsScreen ohne Callbacks, GroupsViewModel mit Testdaten.\
**Rejected**: Von Claude erstellte Komplettlösung (inkl. Änderungen an Dateien von Person C).\
**Commits**: -

**Prompt 4**: "du hast jetzt alles einfach gemacht, kannst du das bitte mit mir zusammen machen, sprich dass wir das schritt für schritt machen"\
**Accepted**: Schrittweises Vorgehen mit Erklärungen.\
**Commits**: -

**Prompt 5**: "wenn du mir zb sagst füge hier room 2.8.5 ein, dann schreib auch dazu warum ich das mache und was das auswirkt"\
**Accepted**: Code-Vorschlag für navigation-compose in libs.versions.toml und build.gradle.kts mit Erklärung jeder Zeile; von mir eingetragen.\
**Commits**: 0194ad8

**Prompt 6**: Screenshot mit Fehler "Unresolved reference 'navigation'" in build.gradle.kts\
**Accepted**: Ursache: Gradle Sync fehlte. Sync ausgeführt, Fehler behoben.\
**Commits**: 0194ad8

**Prompt 7**: "also meine aufgabe ist nur die navigation, ich möchte nicht iwas von den anderen ändern"\
**Accepted**: Plan nur mit meinen Dateien (MainActivity, AppNavHost).\
**Rejected**: Änderungen an GroupsScreen, GroupsViewModel, GroupsUiState (Person C).\
**Commits**: -

**Prompt 8**: "kannst du mir hier zeigen, was ich dort einfügen soll" (AppNavHost)\
**Accepted**: Code-Vorschlag für object Routes und AppNavHost (Routen Gruppenliste, Formular neu/bearbeiten, Detail, Wunschliste); von mir eingefügt.\
**Commits**: 0194ad8

**Prompt 9**: "kannst du mir sagen was ich wo einfügen und auskommentieren soll" (MainActivity)\
**Accepted**: Aufruf von AppNavHost in setContent.\
**Changed**: Alten Testcode auskommentiert statt gelöscht (eigene Entscheidung).\
**Commits**: 0194ad8

**Prompt 10**: "wie weit haben meine anderen kommilitonen programmiert, was ist ganz fertig was noch nicht, zwischen welchen Screens kann ich das Navigieren einbauen?"\
**Accepted**: Übersicht fertiger/offener Teile und möglicher Navigationswege.\
**Commits**: -

**Prompt 11**: "was ist eine dependency, was ist jetpack compose; was ist ein object routes, warum platzhalter; was sind hilfsfunktionen; was sind navhost, composable(), navargument und popbackstack; was sind GroupFormRoute, GroupDetailRoute und ParticipantWishlistRoute"\
**Accepted**: Erklärungen.\
**Commits**: -

**Prompt 12**: "kannst du mir einfach erklären warum was wie gemacht wurde" (Anhang: AppNavHost.kt mit object Routes und fünf composable-Blöcken)\
**Accepted**: Erklärung des AppNavHost Abschnitt für Abschnitt.\
**Commits**: -

**Prompt 13**: "import com.example.wichtly.ui.GroupsScreen – was binde ich damit ein? das ist ja keine bibliothek"\
**Accepted**: Erklärung: Import von eigenem Code über Package-Namen.\
**Commits**: -

**Prompt 14**: "@OptIn(ExperimentalMaterial3Api::class) @Composable – warum muss man das davor schreiben"\
**Accepted**: Erklärung von Annotationen.\
**Commits**: -

**Prompt 15**: "Den Teil hat einer meiner Gruppenmitglieder programmiert, kannsr du sagen, was wir hier machen" (Anhang: object Routes aus AppNavHost.kt)\
**Accepted**: Erklärung von Routen, Platzhaltern und Hilfsfunktionen.\
**Commits**: -

**Prompt 16**: "const val ARG_GROUP_ID = "groupId" – kannst du mir das erklären"; "warum als string"; "wo wird diese information bei mir im code gebraucht"\
**Accepted**: Erklärung: Konstante enthält nur den Namen des Platzhalters, nicht die ID.\
**Commits**: -

**Prompt 17**: "von wo kommt diese lücke, woher weiß ich was reinkommt?"; "warum habe ich nicht in den anderen screens eine variable, der diese nummer zugeordnet wird"\
**Accepted**: Erklärung, wie die ID über die Route weitergegeben wird.\
**Commits**: -

**Prompt 18**: "kannst du mal einen ablauf machen, wie das aussehen würde"\
**Accepted**: Ablaufdiagramm DB → Liste → Klick → AppNavHost → Detail.\
**Commits**: -

**Prompt 19**: "const val GROUP_EDIT = "group-form/{$ARG_GROUP_ID}" – was bedeutet der letzte teil, warum gibt es dort $"\
**Accepted**: Erklärung von String-Templates.\
**Commits**: -

**Prompt 20**: "was passiert hier, definiere ich hier eine funktion, was sind die usages, reagieren sie auf klicks?"
```kotlin
fun groupEdit(groupId: String) = "group-form/$groupId"
fun groupDetail(groupId: String) = "group-detail/$groupId"
fun wishlist(participantId: String) = "wishlist/$participantId"
```
**Accepted**: Erklärung Definition vs. Aufruf.\
**Commits**: -

**Prompt 21**: "was ist Routes.GROUPS", "was ist ein object", "wo werden die adressen zugeordnet", "unterschied zwischen hilfsfunktion und composable"\
**Accepted**: Erklärungen.\
**Commits**: -

**Prompt 22**: "wie kann ich das in meinem text committen, damit meine kommilitonen das später nachvollziehen können warum ich das so gemacht habe"\
**Accepted**: Vorschlag für KDoc-Kommentar über object Routes und Commit-Nachricht.\
**Commits**: -

**Prompt 23**: "der Screen in GroupsScreen muss um Callbacks erweitert werden, damit ich es hier verdrahten kann"\
**Accepted**: Nachricht an Person C mit benötigter Funktionssignatur.\
**Commits**: -

**Prompt 24**: "diana hat eine ganze codedatei eingefügt mit groupdetailroute, warum? ist das nötig"\
**Accepted**: Erklärung Route vs. Screen.\
**Commits**: -

**Prompt 25**: "was ist viewmodel, was ist screen, was ist uistate"; "was ist groupsScreen.kt, GroupsUiState, GroupsViewModel", "kannst du mir noch mal ausführliche ihre Funktionen erklären"\
**Accepted**: Erklärungen.\
**Commits**: -

**Prompt 26**: "was ist ein callback, was passiert dann, wie hängt das alles zusammen"\
**Accepted**: Erklärung mit Beispiel.\
**Commits**: -

**Prompt 27**: "fun KnopfScreen(onKnopfClick: () -> Unit) – was bedeutet das und was muss in die klammer rein", "kommen auch eigenschaften rein, zb metadaten"\
**Accepted**: Erklärung von Parametern und Funktionstypen.\
**Commits**: -

**Prompt 28**: "der teil von group fehlt wegen der navigation, wie kann ich das einbinden?"\
**Accepted**: Schrittplan für GroupsUiState, GroupsViewModel, Factory, GroupsRoute, Callbacks.\
**Commits**: -


### 2026-11-17 (Viktoria)

**Prompt 1**: Tell me what exactly would belong to data- and domain sections of our camera feature. /plan
**Accepted**:  Repository as main responsiblity from my side.
**Commits**: f106370

**Prompt 2**:  "As a first step, add every needed File, Class and method (including signatures; but NOT implementation yet)."
**Accepted**: as is.
**Commits**: f106370

**Prompt 3**:  Implement these functions now. Also create comments for UI people so they know when to use what from domain/data.
**Accepted**: as is. But see follow-up prompts.
**Commits**: f106370

**Prompt 4**: Also implement any dependency injection.
**Accepted**: as is.
**Commits**: f106370

**Prompt 5**: Change the filename prefix to extra enum (groups, participant) so that it is typed instead of plain string.
Change directory structure so there are both photos/groups as well as photos/participant directories.
**Accepted**: as is.
**Commits**: f106370

**Prompt 6**: "make dirname and filenameprefix identical so it is only one arg."
Change directory structure so there are both photos/groups as well as photos/participant directories.
**Accepted**: as is.
**Commits**: f106370

### 2026-11-17 (Diana, Folz), Gemini in Android Studio, Claude.ai (pro)

**Prompt 1** (an Claude, mit den Draw-Dateien von Viktoria): „dies sind die dateien für draw soweit“\
**Accepted**: Review der Draw-Datenschicht. Der DrawMapper benutzte Felder, die es nicht gibt (drawId, assignmentId), und kompilierte nicht. Claude lieferte Änderungs-Snippets für Viktoria: Mapper korrigiert, Zuordnungen nach Wichtel-Name sortiert (ohne Groß-/Kleinschreibung), neue Abfrage `observeGroupDrawOverview`, Ergebnis `DrawTypeRulesUndefined` statt erfundener Schrottwichtel-Regeln.\
**Decision**: Wir behalten das Snapshot-Modell für Auslosungen.\
**Commits**: (von Viktoria eingebaut)

**Prompt 2** aus einem früheren Gemini CHat; die Prompts wurden auf meinen Wunsch neu geschrieben „ohne die Integration der Route, die Route macht Person A vollständig“):
(a) Draw-UiStates mit temporären Platzhalter-Modellen (TemporaryDrawPlaceholders.kt). (b) Zustandsloser DrawHistoryScreen mit Previews.
**Accepted**: As is.
**Commits**: 16a0a3a, 005a158

**Prompt 3**:
Lies zuerst AGENTS.md. orientiere dich im Stil an ui/groups/detail/GroupDetailScreen.kt. Lege im Paket com.example.wichtly.ui.draws die Datei DrawDetailScreen.kt an. Kein ViewModel, kein Repository, keine Navigation, keine Dependencies, kein !!.
Zustandsloser Composable DrawDetailScreen(state: DrawDetailUiState, onBack: () -> Unit, onGiverClick: (giverId: String) -> Unit, modifier: Modifier = Modifier), keine Standardwerte außer modifier. TopAppBar “Auslosung ansehen” mit Zurück-Aktion. Geprüft in dieser Reihenfolge: Laden, Fehler, nicht gefunden (Text “Auslosung nicht gefunden” mit Zurück-Button), sonst Inhalt.
Der Inhalt ist eine LazyColumn: Kopfbereich mit Gruppenname, Datum, Teilnehmerzahl und Wichtelart, dazu der Hinweis “Gespeicherter Durchlauf, nur lesend”, dann je Zuordnung eine Karte mit key = giverId, die NUR den Namen der schenkenden Person und den Platzhalter “→ ••••” zeigt. Wichtig für die Geheimhaltung: Der Name der beschenkten Person darf in der Liste nirgends vorkommen, weder als Text noch in einer contentDescription noch in einem semantics-Text. Ein Tipp auf die Karte ruft onGiverClick(giverId) auf, die Karte beschreibt sich per contentDescription als “<Name>, Ergebnis anzeigen”.
Previews: mit sechs Zuordnungen, Laden, Fehler, nicht gefunden. Fake-Daten nur in den Previews. Kommentare auf Deutsch. Das Projekt muss bauen. Fasse zusammen.
**Accepted**: As is. Der Name der beschenkten Person kommt in der Liste nirgends vor, auch nicht in contentDescription oder semantics.
**Commits**: 9d6624d

**Prompt 4**:
Lies zuerst AGENTS.md und orientiere dich an ui/groups/detail/ParticipantDeleteDialog.kt. Lege im Paket com.example.wichtly.ui.draws die Dateien DrawRevealConfirmDialog.kt und DrawResultDialog.kt an. Kein ViewModel, kein Repository, keine Dependencies, kein !!.

DrawRevealConfirmDialog: zustandsloser AlertDialog mit giverName, onConfirm, onDismiss. Text: “<Name>: geloste Person anzeigen? Stelle sicher, dass niemand auf deinen Bildschirm schauen kann.” Buttons “Anzeigen” und “Abbrechen”.

DrawResultDialog: zustandsloser AlertDialog mit assignment: TemporaryDrawAssignment und onDismiss. Zeigt “<Schenkender> → <Beschenkter>“ und darunter die Wunschliste des Beschenkten als Aufzählung aus assignment.wishes. Ist die Liste leer, steht dort “Zum Zeitpunkt der Auslosung waren keine Wünsche eingetragen.” Ein Button “Schließen”. Previews für beide, bei dem Ergebnisdialog mit drei Wünschen, ohne Wünsche und mit sehr langem Wunschtext. Kommentare auf Deutsch. Das Projekt muss bauen. Fasse zusammen.
**Accepted**: As is.
**Commits**: 0a16ed8

**Prompt 5**:
Lies zuerst AGENTS.md. Ändere nur DrawDetailScreen.kt. Der Screen bleibt zustandslos. Ergänze die Parameter onConfirmReveal: () -> Unit, onDismissConfirm: () -> Unit und onDismissResult: () -> Unit, ohne Standardwerte, vor modifier. Zeige DrawRevealConfirmDialog, wenn state.confirmGiverId gesetzt ist und in den Zuordnungen des Durchlaufs ein Eintrag mit dieser giverId existiert (giverName von dort). Zeige DrawResultDialog, wenn state.revealedGiverId gesetzt ist und ein Eintrag existiert, und übergib nur diese eine Zuordnung. Existiert der Eintrag nicht, wird kein Dialog gezeigt. Die Dialoge liegen über dem Inhalt und nicht in der Verzweigung der Zustände Laden, Fehler und nicht gefunden. Passe alle Previews an und füge zwei hinzu: offene Sicherheitsabfrage und offenes Ergebnis. Kommentare auf Deutsch. Das Projekt muss bauen. Fasse zusammen.
**Accepted**: As is.
**Commits**: a43b17b

**Prompt 6**:
Aufgabe: Erstelle ViewModel und Factory für den Screen DrawDetailScreen. Die UI bleibt unverändert. Es wird KEINE Route erstellt (die macht eine andere Person).

STRIKTE REGELN
- Führe keine git-Befehle aus (kein checkout, stash, reset, commit).
- Ändere oder erstelle NUR diese Dateien in ui/draws/: DrawDetailViewModel.kt, DrawDetailViewModelFactory.kt, und passe DrawDetailScreen.kt NUR dort an, wo unten beschrieben.
- Erstelle keine DrawDetailRoute. Fasse MainActivity.kt, AppContainer.kt, Navigation und alle Data-/Domain-Dateien nicht an.
- Kein !!, keine stillen catch-Blöcke, CancellationException immer weiterwerfen, Log.e bei Fehlern.

KONTEXT
- Lies zuerst DrawDetailScreen.kt (mit TemporaryDraw, TemporaryDrawAssignment und dem UiState-Typ), ui/wishlist/WishlistViewModel.kt und WishlistViewModelFactory.kt. Übernimm genau deren Muster (StateFlow, stateIn WhileSubscribed(5_000), Factory).
- Echte Domain-Modelle (domain/model): DrawDetail(id, groupId, drawnAt, drawType, assignments), DrawAssignment(id, giverName, giverId, receiverName, receiverId, receiverWishlistItems: List<DrawWishListItemSnapshot>), DrawWishListItemSnapshot(id, text, sortOrder).
- Repository: DrawRepository.observeDrawDetailById(drawId: String): Flow<DrawDetail?>.

ANFORDERUNGEN
1. DrawDetailViewModel(drawId: String, drawRepository: DrawRepository):
   - UiState mit den vier Zuständen aus dem Screen: Laden, Inhalt, Nicht gefunden (Flow liefert null), Fehler (Flow wirft; per .catch abfangen, CancellationException weiterwerfen, Log.e, Nachricht "Fehler beim Laden der Auslosung.").
   - Die DrawDetail wird in die UI-Modelle gemappt (private Funktion im ViewModel). Zuordnungen nach giverName ohne Groß-/Kleinschreibung sortieren, dann nach giverId. Wünsche nach sortOrder, nur der Text.
   - Dialog-Zustand: confirmGiverId: String? und revealedGiverId: String? (nur IDs, keine Namen).
     onGiverClick(giverId) setzt confirmGiverId. onConfirmReveal setzt revealedGiverId = confirmGiverId und confirmGiverId = null. onDismissConfirm und onDismissResult setzen jeweils auf null. Existiert die giverId nicht in der aktuellen Liste, passiert nichts.
   - Öffentliche Funktion onAppStopped(): schließt beide Dialoge (wird später von der Route bei ON_STOP aufgerufen).
   - Dialog-Zustand liegt im ViewModel, damit er die Rotation übersteht.
2. DrawDetailViewModelFactory(drawId, drawRepository) wie bei Wishlist.
3. DrawDetailScreen: bleibt stateless; nur die Typen TemporaryDraw/TemporaryDrawAssignment durch die neuen UI-Modelle ersetzen, falls nötig. Der Hinweiskasten "draw-detail-snapshot-hint" bleibt. Previews müssen weiter kompilieren.

Gib am Ende eine Liste der geänderten Dateien aus. Keine Tests, keine Route.
**Accepted**: ViewModel und Factory.
**Changed**: Gruppenname im Kopf zunächst als Platzhalter „Wichtelgruppe“.
**Checked**: Review durch Claude vor dem Push auf den Branch `diana`.
**Commits**: b4f671b

### 2026-11-18 (Diana, Folz), Gemini in Android Studio, Claude.ai (pro)

Umbenennung von TemporaryDrawPlaceholders.kt in DrawDetailUiModels.kt (Klassen DrawDetailUiModel, DrawAssignmentUiModel) von Hand mit dem Refactoring in Android Studio, auf Empfehlung von Claude erst nach dem ViewModel. Kein KI-Prompt.
**Commits**: 5edf8bb

**Prompt 1**:
was haben diese 31 errors zu bedeuten
**Prompt 2**:
ja
**Accepted**: Erklärung von Gemini: Nach dem Umbenennen verwiesen noch Stellen auf die alten Klassennamen. Auf „ja“ hat Gemini die Verweise korrigiert.
**Checked**: Build wieder grün.
**Commits**: d8b345a

**Prompt 3**:
Aufgabe: Erstelle ViewModel und Factory für den DrawHistoryScreen und stelle UiState und Screen auf ein echtes UI-Modell um. Es wird KEINE Route erstellt (die macht eine andere Person).

STRIKTE REGELN
- Führe keine git-Befehle aus (kein checkout, stash, reset, commit).
- Ändere oder erstelle NUR diese Dateien in ui/draws/: DrawHistoryViewModel.kt, DrawHistoryViewModelFactory.kt, DrawHistoryUiState.kt, DrawHistoryScreen.kt. Das neue UI-Modell DrawHistoryItemUiModel kommt in DrawHistoryUiState.kt.
- Fasse MainActivity.kt, AppContainer.kt, Navigation, DrawDetail*-Dateien und alle Data-/Domain-Dateien nicht an.
- Kein !!, keine stillen catch-Blöcke, CancellationException immer weiterwerfen, Log.e bei Fehlern.

KONTEXT
- Lies zuerst DrawHistoryUiState.kt, DrawHistoryScreen.kt, ui/draws/DrawDetailViewModel.kt und DrawDetailViewModelFactory.kt. Übernimm exakt deren Muster (combine, stateIn WhileSubscribed(5_000), Factory).
- Repositories:
  - DrawRepository.observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>> (bereits von neu nach alt sortiert).
  - GiftGroupRepository.getGiftGroupById(id: String): Flow<GiftGroup?> (GiftGroup hat name).
- Domain-Modell DrawSummary(id, groupId, drawnAt: Instant, drawType: DrawType, assignmentCount: Int).

ANFORDERUNGEN
1. DrawHistoryUiState.kt: Neues UI-Modell
   data class DrawHistoryItemUiModel(val id: String, val drawnAt: LocalDate, val drawType: DrawType, val assignmentCount: Int)
   DrawHistoryUiState: List<TemporaryDraw> durch List<DrawHistoryItemUiModel> ersetzen. Die übrigen Felder (isLoading, groupName, groupNotFound, errorMessage) bleiben. KDoc anpassen. Wenn TemporaryDraw dort noch vorkommt, entfernen. Wenn Klassen aus der Detail-Datei umbenannt wurden, deren neue Namen verwenden.
2. DrawHistoryViewModel(groupId: String, drawRepository: DrawRepository, giftGroupRepository: GiftGroupRepository):
   - uiState per combine aus dem Gruppen-Flow und dem Summaries-Flow, stateIn(WhileSubscribed(5_000)), Anfangswert isLoading = true.
   - Gruppe null -> groupNotFound = true (kein Fehler). Leere Liste ist ein gültiger Zustand.
   - Mapping: drawnAt per ZoneId.systemDefault().toLocalDate(), Reihenfolge unverändert lassen.
   - Fehler in einem der Flows per .catch abfangen: CancellationException weiterwerfen, Log.e, errorMessage = "Fehler beim Laden der Historie." (keine Exception nach außen).
   - Keine Dialoge, keine weiteren Callbacks.
3. DrawHistoryViewModelFactory(groupId, drawRepository, giftGroupRepository) wie bei Detail.
4. DrawHistoryScreen: Signatur unverändert (state, onBack, onOpenDraw(drawId), modifier). Karten nutzen draw.drawnAt und draw.assignmentCount statt draw.createdAt und draw.assignments.size. Texte und Layout bleiben. Alle Previews auf DrawHistoryItemUiModel umstellen, sie müssen kompilieren.

Gib am Ende die Liste der geänderten Dateien aus. Keine Tests, keine Route.\
**Accepted**: As is.\
**Checked**: Review durch Claude: CancellationException wird weitergeworfen, Log.e vorhanden, keine Platzhalter mehr. Offener Hinweis: Zwei Auslosungen am selben Tag sehen in der Liste gleich aus (nur Datum, keine Uhrzeit).
**Commits**: b6924e2

Teamabsprache vorab (Nachricht von Viktoria): Die Foto-UI für Gruppen und Teilnehmer ist fast gleich und soll nur einmal gebaut werden, parametrisiert über den Bereich. Ich übernehme die gemeinsamen Bausteine.

**Prompt 4**:
Aufgabe: Erstelle gemeinsame, zustandslose (stateless) UI-Bausteine für die Foto-Funktion von Gruppen UND Teilnehmern. Es ist ein reiner UI-Schritt: keine Kamera, keine Berechtigungslogik, keine Dateizugriffe, kein ViewModel.

STRIKTE REGELN
- Führe keine git-Befehle aus (kein checkout, stash, reset, commit).
- Erstelle NUR neue Dateien im neuen Paket ui/components/photo/ (siehe unten). Ändere KEINE bestehende Datei, insbesondere nicht GroupFormScreen.kt, ParticipantEditorDialog.kt, MainActivity.kt, AndroidManifest.xml, build.gradle.kts und nichts in data/domain.
- Keine neuen Dependencies (kein Coil, kein Accompanist).
- Alle Bausteine sind zustandslos: Zustand und Callbacks kommen von außen (State Hoisting). Kein remember von fachlichem Zustand.
- Alle Texte auf Deutsch. Es gibt KEINE Galerie-Auswahl, nur die Kamera.
- Jeder Baustein bekommt ein KDoc, eine Preview in WichtlyTheme und einen `modifier: Modifier = Modifier`.
- Orientiere dich an Stil, Abständen und Typografie der vorhandenen Dialoge (ui/groups/detail/ParticipantDeleteDialog.kt, ParticipantEditorDialog.kt) und an Material 3.

KONTEXT
Das Foto wird sowohl im Gruppenformular als auch im Teilnehmer-Dialog verwendet. Die UI ist fast identisch, nur die Texte unterscheiden sich. Deshalb gibt es EINE Implementierung, parametrisiert über einen Bereich:

enum class PhotoArea { GROUP, PARTICIPANT }

Datei PhotoArea.kt mit dem enum und einer Texttabelle (z. B. interne Funktionen oder Properties am enum) für:
- fieldTitle: GROUP "Gruppenfoto (optional)" / PARTICIPANT "Teilnehmerfoto (optional)"
- emptyTitle: GROUP "Noch kein Gruppenfoto" / PARTICIPANT "Noch kein Foto"
- emptyHint: GROUP "Die App funktioniert auch ohne Foto." / PARTICIPANT "Die Wunschliste und Verwaltung funktionieren auch ohne Foto."
- dialogTitle: GROUP "Gruppenfoto" / PARTICIPANT "Teilnehmerfoto"

BAUSTEINE (je eine Datei in ui/components/photo/)
1. PhotoPickerField.kt: `PhotoPickerField(area: PhotoArea, hasPhoto: Boolean, onSelectClick: () -> Unit, modifier)`.
   Gestrichelt umrandete Karte wie im Mockup: links ein "+"-Symbol in einem runden Feld, in der Mitte emptyTitle und emptyHint (bei hasPhoto == true stattdessen "Foto gespeichert" und "Tippe auf Ändern, um ein neues Foto aufzunehmen."), rechts ein Button "Auswählen" (bei hasPhoto "Ändern"). Über der Karte der Feldtitel (fieldTitle). Es wird KEIN Bild geladen; eine Vorschaubild-Anzeige kommt in einem späteren Schritt.
2. PhotoSourceDialog.kt: `PhotoSourceDialog(area: PhotoArea, hasPhoto: Boolean, onTakePhoto: () -> Unit, onRemovePhoto: () -> Unit, onDismiss: () -> Unit, modifier)`.
   Dialog mit dialogTitle und dem Hinweis "Das Foto wird nur auf diesem Gerät gespeichert.". Eintrag "Foto aufnehmen" mit Untertitel "Die Kamera wird erst jetzt angefragt." Wenn hasPhoto == true zusätzlich der Eintrag "Foto entfernen". Unten "Abbrechen".
3. CameraPermissionDialog.kt: `CameraPermissionDialog(onAllow: () -> Unit, onDeny: () -> Unit, modifier)`.
   AlertDialog, Titel "Kamerazugriff", Text "Kamerazugriff wird nur für die Fotoaufnahme benötigt. Eine Ablehnung blockiert weder das Formular noch die App – du kannst auch ohne Foto weiterarbeiten." Buttons "Nicht erlauben" (onDeny) und "Erlauben" (onAllow). onDismissRequest ruft onDeny auf.
4. CameraPermissionDeniedHint.kt: `CameraPermissionDeniedHint(onOpenSettings: (() -> Unit)?, modifier)`.
   Kleine Hinweiskarte (Warn-/Container-Farbe aus MaterialTheme) mit "Kamerazugriff wurde abgelehnt. Du kannst die App weiter ohne Foto nutzen." und, nur wenn onOpenSettings != null, einem TextButton "Einstellungen öffnen".

PREVIEWS (privat, in WichtlyTheme)
- PhotoPickerField: GROUP leer, PARTICIPANT leer, PARTICIPANT mit Foto.
- PhotoSourceDialog: GROUP ohne Foto, PARTICIPANT mit Foto.
- CameraPermissionDialog und CameraPermissionDeniedHint (mit und ohne Einstellungen-Button).

Gib am Ende die Liste der neuen Dateien aus. Keine Tests, kein Einbau in bestehende Screens.
**Accepted**: Fünf Dateien (PhotoArea, PhotoPickerField, PhotoSourceDialog, CameraPermissionDialog, CameraPermissionDeniedHint).
**Changed**: Der Rahmen ist durchgezogen statt gestrichelt wie im Mockup (gestrichelt bräuchte ein Canvas, lohnt sich nicht).
**Commits**: 8e9244f

### 2026-11-19 (Viktoria)
**Prompt 1**:
** Please make following picture as our Android app icon [Image #4] and make it also appear on App launch. State exactly what you changed and where.**
**Accepted**: as is.
**Commits**: 46e4385

**Prompt 2**: The app icon itself is finde; BUT its display on the launcher fullscreen is way to low-quality. Please fix.
**Accepted**: as is.
**Commits**: 46e4385

### 2026-11-20 – 2026-11-21 (Merve, Öztürk), Claude.ai (Opus 5.5)
**Prompt 1**: "also ich möchte mir erst den aktuellsten stand aus der main holen, dh ich mache einen fetch und dann einen merge oder wie" (Anhang: Screenshot Merge-Dialog)\
**Accepted**: Anleitung Fetch → Merge 'origin/main' into 'merve'; Hinweis, im Dialog origin/main statt merve auszuwählen.\
**Commits**: -

**Prompt 2**: "kannst du mir erklären, was die einzelnen Dateien machen … was ist der Sinn … welche Komponenten muss ich für meinen Teil wissen, um die einzelnen Screens miteinander zu verbinden?"\
**Accepted**: Erklärung der Projektstruktur (data, domain, di, ui, navigation) und dass für die Navigation nur Name, ID und Callbacks der Route-Funktionen nötig sind.\
**Commits**: -

**Prompt 3**: "ich habe nicht viel Ahnung vom Programmieren und von Kotlin, wenn du mir die Screens der anderen erklärst, kannst du die Programmiersprache Kotlich auch erklären"\
**Accepted**: Kotlin-Spickzettel (val/var, Typen, null, fun, class, data class, object, Lambda, String-Templates, Annotationen, package/import).\
**Commits**: -

**Prompt 4**: Fragen zu `fun KnopfScreen(onKnopfClick: () -> Unit)`: "ist das kein Parameter?", "ist das eine Funktion?", "Unterschied Lambda und Funktion?", "wie kann eine Funktion keinen Namen haben?", "ist onKnopfClick vordefiniert?", "woher weiß ich, was die Funktion macht?"\
**Accepted**: Erklärung Parameter mit Funktionstyp, Lambda vs. benannte Funktion, Namen von Callbacks werden vom Entwickler der Funktion vergeben (onClick ist von Google vorgegeben).\
**Commits**: -

**Prompt 5**: "warum heißt es Button(onClick = onKnopfClick) und nicht onClick = true/false?"; "ist onKnopfClick nicht überflüssig?"\
**Accepted**: Unterschied Zustand (enabled = true/false) vs. Anweisung (onClick = Code); Callbacks als Brücke zwischen Screen-Datei und AppNavHost (navController nur dort bekannt).\
**Commits**: -

**Prompt 6**: "ist Button eine Funktion?", "wann kommt eine Anweisung in die Klammer und wann nicht?", "geht fun KnopfScreen(onKnopfClick: () -> Unit = kk)?"\
**Accepted**: Button als @Composable-Funktion; Faustregel Definition (name: Typ) vs. Aufruf (name = Wert); Standardwerte für Funktionsparameter.\
**Commits**: -

**Prompt 7**: "woher weiß ich, welche Parameter ich brauche?", "warum geht fun abc(a: Int) { a = a + 2 } nicht?", "wo muss ich b definieren?", "wann brauche ich return?", "wie steht im Code, dass Button onClick ausführt?"\
**Accepted**: Parameter = was sich pro Aufruf ändert; Parameter sind schreibgeschützt; lokale Variablen mit val; return nur bei Rückgabewert; Beispiel Modifier.clickable { onClick() }.\
**Commits**: -

**Prompt 8**: Fragen zum Beispiel `machWas(aufgabe: (String) -> Unit)`: "was gebe ich in die Funktion ein?", "aber ich habe doch Merve definiert", "müsste es nicht umgekehrt sein – im Body was gemacht wird?"\
**Accepted**: Erklärung mit Zeitablauf; zwei Funktionen mit eigenem Parameter und Body; Unit = gibt nichts zurück, tut aber etwas.\
**Commits**: -

**Prompt 9**: "wenn ich doch eine Navigation-Datei habe, warum brauche ich dann noch extra eine Route-Datei für alle Screens?"\
**Accepted**: Unterschied Route als Adresse (AppNavHost) vs. …Route.kt als Eingangstür eines Screens.\
**Commits**: -

**Prompt 10**: "GroupsRoute – was muss ich da speziell machen, und was in GroupsViewModelFactory?"; "was macht GroupsViewModelFactory?"; "was ist ein Repository?"; "was ist ein Interface?"\
**Accepted**: Erklärung Factory, Repository, Interface; Code-Vorschlag für GroupsViewModelFactory.\
**Commits**: -

**Prompt 11**: Fehlermeldungen "Expecting a top level declaration", "imports are only allowed in the beginning of file"\
**Accepted**: Ursache: Chat-Markierungen bzw. doppelter Inhalt mitkopiert; Datei geleert und Code neu eingefügt.\
**Commits**: 2703530

**Prompt 12**: "manche Dateien haben eine Fahne, andere zwei blaue Kreise – Unterschied?"; "was heißt genau, dass die Factory vorgibt, wie das ViewModel aufgebaut wird?"; "muss ich das in AppNavHost mit reinnehmen?"\
**Accepted**: Erklärung IDE-Symbole (Datei mit Funktionen vs. eine Klasse); Ablauf Factory → ViewModel; AppNavHost ruft nur die Route auf.\
**Commits**: -

**Prompt 13**: "Wie kann ich die Callbacks in GroupsScreen einbauen"\
**Accepted**: Schrittweise Anleitung für 5 Änderungen (Funktionskopf, Button "Gruppe hinzufügen", Card, Stift-Button, Vorschau).\
**Changed**: Änderungen selbst eingetippt; eigene Fehler (fehlender Doppelpunkt, Definitions-Schreibweise im Card-Aufruf, onClick = { onAddGroupClick } ohne ()) mit Hilfe korrigiert; erklärenden Kommentar in GroupsScreen ergänzt.\
**Commits**: 1dd38c7

**Prompt 14**: "ist das die Schachtel und der Inhalt das Lambda?", "muss ich die Schachtel in GroupsScreen importieren?", "muss ich in AppNavHost importieren?", "die Schachteln sind ja in den Funktionen definiert", "wie viele Buttons müssen verdrahtet werden?", "brauche ich nur -> Unit?", "stimmt onGroupClick: (groupId: String) -> Unit?"\
**Accepted**: Erklärungen.\
**Commits**: -

**Prompt 15**: Fehlermeldungen "No value passed for parameter 'onAddGroupClick' / 'onEditGroupClick' / 'onGroupClick'"\
**Accepted**: Ursache: neue Pflicht-Parameter beim Aufruf (Vorschau, AppNavHost) nicht gefüllt; Vorschau mit leeren Lambdas ergänzt.\
**Commits**: -

**Prompt 16**: "der Button für Gruppendetail fehlt"; "muss ich bei der Schachtel mit Parameter noch was definieren?"; "warum geht Card(..., onClick: () -> Unit) nicht?"; "muss ich es im Body nochmal aufführen?"\
**Accepted**: Ganze Card wird antippbar; Definition mit ":" vs. Aufruf mit "="; onClick nur in den runden Klammern der Card.\
**Commits**: -

**Prompt 17**: "passt das so?"; "was soll ich als Commit-Text schreiben?" (Anhang: GroupsScreen.kt mit den drei neuen Callbacks)\
**Accepted**: Code-Review ohne Fehler; Vorschlag Commit-Nachricht.\
**Commits**: -

**Prompt 18**: "war das, was ich gemacht habe, der Callback?"; "wie springt er zu AppNavHost?"; "das heißt, wir sind eigentlich nur auf AppNavHost?"\
**Accepted**: Erklärung: kein Sprung, Lambda wird vorher weitergereicht und nimmt navController mit (Closure); NavHost reagiert auf navController.\
**Commits**: -

**Prompt 19**: "GroupsScreen(…, onGroupClick = group) – group.id gibt eine Fehlermeldung"; "kann ich den Parameter setzen und im Body die Zuordnung machen?"
```kotlin
GroupsScreen(uiState = GroupsUiState(),onAddGroupClick={},onGroupClick=group)
```
**Accepted**: group existiert nur im GroupsScreen; im AppNavHost wird die ID über das Lambda empfangen (groupId -> …); Alternative: Lambda vorher in Variable legen.\
**Changed**: Verdrahtung der drei Callbacks im AppNavHost selbst geschrieben.\
**Commits**: 745f303

**Prompt 20**: "Jetzt sind die Gruppen in AppNavHost vernetzt, Losen fehlt noch – fehlt sonst noch etwas?"\
**Accepted**: Hinweis: Liste ist noch leer (GroupsUiState()), GroupsRoute und echte Daten im GroupsViewModel fehlen noch.\
**Commits**: -

#
### 2026-11-21 (Melissa, Meyer), GitHup Copilot Education
**Prompt 1**: Bitte passe den Code an: GroupScreen soll die Daten aus dem Repository verwenden und nicht, wie bisher, die hard reingecodeten Daten. Bitte mache leichter verständliche Änderungen und viele Kommentare. Ich, als Anfängerin, möchte die Änderungen nachvollziehen können..\
**Geaendert:**   Quelle der GiftGroups & Kommentare
**Commits:**     ab5e756..3f8a537

### 2026-11-21 (Melissa, Meyer), GitHup Copilot Education
**Prompt 1**:Bitte füge einen Screen namens PreparationScreen (mit ViewModel, UiState, Route und ViewModelFactory) hinzu.
Auf diesen Screen soll gewechselt werden, wenn man im GroupsScreen eine Card ausgewählt hat und anschließend auf „Losen“ klickt.
Der Screen dient zur Kontrolle, ob das Auslosen für die Gruppe der ausgewählten Card gestartet werden darf oder nicht.
Es soll geprüft werden, ob die Gruppe aus der ausgewählten Card korrekt in der Datenbank gespeichert wurde und mindestens drei Teilnehmer hat.
Unten soll es zwei Buttons geben:
Zurück-Button (zurück zum GroupsScreen)
Jetzt losen-Button (grün; nur sichtbar, wenn das Losen erlaubt ist)
Titel der Seite:„Vorbereitung zum Losen“
Einleitungstext:
„Bitte überprüfe, ob die Daten der Gruppe korrekt sind, bevor die Auslosung beginnt: Name der Gruppe, Anzahl der Teilnehmer, Übergabedatum, Budget, Art des Wichtelns.“
Bitte ziehe die Daten der ausgewählten Gruppe aus dem Repository.
Danach soll eine LazyColumn zum Scrollen kommen, in der die Namen aller Teilnehmer angezeigt werden. Bitte lade auch diese Daten aus dem Repository.
Schreibe den Code leicht verständlich und kommentiere ihn auf Deutsch.
**Angenommen:**            ganzen Vorschlag (in spaeteren Commits angepasst und nachkommientiert
**Commits:**               50ee8b3
 ### 2026-11-21 (Melissa, Meyer), Copilot (kostenlos)
**Prompt 1**:Was muss ich in den Code einfügen, sodass sich die Card in einem Scrollbaren Bereich befinden? Beispielsitualtion: Die Card-Elemente nehmen mehr Platz ein als die Seite groß ist. Dann soll man durch scrollen die unteren Elemente erreichen können. Die Button sollen aber ihre Positon beibehalten. + Anhang (aktuelle Datei: GroupsScreen)..\
**Geaendert**:          Column Modifier
**Commits**:            4984ef2

### 2026-11-21 (Merve, Öztürk), Claude.ai (Opus 5.5)
**Prompt 1**: "kannst du analog zum GroupDetailRoute.kt die Route für GroupsRoute erstellen"\
**Accepted**: Erklärung, wie GroupDetailRoute aufgebaut ist und was bei der GroupsRoute anders ist (keine groupId, kein key, kein collectAsStateWithLifecycle wegen mutableStateOf im GroupsViewModel).\
**Rejected**: Ausführlichere Version mit KDoc-Kommentar nach Vorbild der GroupDetailRoute; die einfachere Version aus dem Schritt davor beibehalten.\
**Commits**: 89786a5

**Prompt 2**: "das ist ja jetzt eine Funktion und keine Klasse. Die muss jetzt aber in AppNavHost GroupsScreen ersetzen oder"\
**Accepted**: Bestätigung: GroupsRoute ist eine @Composable-Funktion; Aufrufkette AppNavHost → GroupsRoute → GroupsScreen; Anleitung zum Ersetzen des Aufrufs im AppNavHost und zum Aufräumen der Imports.\
**Changed**: Aufruf im AppNavHost selbst ersetzt (GroupsScreen → GroupsRoute, Parameternamen onAddGroup/onOpenGroup/onEditGroup); ungenutzte Imports von GroupsScreen und GroupsUiState entfernt.\
**Commits**: 89786a5

**Prompt 3**: "aber dann habe ich vorhin ja die Verbindung in AppNavHost umsonst aufgebaut"\
**Accepted**: Erklärung: Navigations-Lambdas bleiben identisch, nur Parameternamen ändern sich und uiState fällt weg; Callbacks im GroupsScreen werden weiterhin gebraucht, die Route reicht sie nur durch.\
**Commits**: -

**Prompt 4**: "aber bevor ich alles mache, muss ich ja erst die route importieren oder"\
**Accepted**: Erklärung: Reihenfolge egal (Import zuerst oder nach dem Ersetzen per Alt + Enter); Import nötig, weil GroupsRoute in einem anderen Paket liegt; ungenutzte Imports von GroupsScreen und GroupsUiState entfernen.\
**Commits**: -

**Prompt 5**: "passt das so?" (Anhang: AppNavHost.kt mit GroupsRoute)\
**Accepted**: Code-Review: GroupsRoute korrekt eingebunden; Hinweis, den ungenutzten Import von GroupsUiState zu löschen und die schließende Klammer in eine eigene Zeile zu setzen (Strg + Alt + L zum Formatieren).\
**Commits**: -

**Prompt 6**: "in welcher zeile?"\
**Accepted**: Zeilenangaben: Zeile 12 (ungenutzter Import GroupsUiState), Zeile 47 (Klammer umbrechen bzw. mit Strg + Alt + L formatieren).\
**Commits**: -

**Prompt 7**: "ich soll aber allgemein Tests schreiben für die App, also diese Tests, die die ganze App automatisch testen"\
**Accepted**: Erklärung automatischer Tests (Unit-Tests in app/src/test vs. UI-/Instrumented Tests in app/src/androidTest); Bestandsaufnahme vorhandener Tests (GroupFormErrorsTest, GroupFormInputTest, ParticipantErrorsTest, WishListErrorsTest, WishlistViewModelTest, testutil); Vorschlag für fehlende Tests (Routes, Factories, ViewModels, Navigation, Screens) nach Schwierigkeit geordnet.\
**Commits**: -

**Prompt 8**: "ich verstehe nicht, was hier gemacht wurde, wir haben DrawDetailScreen, DrawHistoryScreen, DrawResultDialog und DrawRevealConfirmDialog – was sind das? Result und DrawDetailScreen sind doch derselbe Screen"\
**Accepted**: Erklärung: nur zwei Screens (DrawHistory, DrawDetail); DrawRevealConfirmDialog (Personen-Check) und DrawResultDialog (Ergebnis mit Wunschliste) sind Dialoge innerhalb des DrawDetailScreen, gesteuert vom DrawDetailViewModel; dafür sind keine Routen im AppNavHost nötig.\
**Commits**: -

**Prompt 9**: "ok d.h. ich muss jetzt wieder 2 neue Files für DrawDetailRoute und DrawHistoryRoute anlegen"\
**Accepted**: Bestätigung: zwei neue Dateien DrawHistoryRoute.kt und DrawDetailRoute.kt im Ordner ui/draws.\
**Commits**: -

**Prompt 10**: "was stimmt hier nicht?" (Anhang: eigener Entwurf von DrawDetailRoute.kt)\
**Accepted**: Code-Review mit korrigierter Version: falsche Imports (Groups-ViewModel/Factory, DrawDetailScreen im selben Paket), Route-Parameter nur für Navigation (drawId, onBack, modifier), Dialog-Aktionen per viewModel::onGiverClick usw. aus dem ViewModel, drawRepository + DrawDetailViewModelFactory(drawId, drawRepository), viewModel(key = drawId), state per collectAsStateWithLifecycle statt Typname, onBack = onBack statt Typdefinition, modifier statt Modifier.\
**Changed**: Eigenen Entwurf durch die korrigierte Version ersetzt.\
**Rejected**: Hinweis zu onAppStopped() (Ergebnis verdecken, wenn die App in den Hintergrund geht) vorerst nicht umgesetzt, erst mit Diana klären.\
**Commits**: a8f20ad

**Prompt 11**: "warum nehme ich sie vom ViewModel, bei Groups habe ich das ja auch nicht so gemacht"\
**Accepted**: Faustregel: Klick führt zu anderem Screen → Callback vom AppNavHost über Route-Parameter; Klick bleibt im Screen und ändert nur den Zustand (Dialoge im DrawDetail) → Funktion aus dem ViewModel (viewModel::…); Vergleich mit GroupDetailRoute.\
**Commits**: -

**Prompt 12**: "dann muss ich nur das Zurück in AppNavHost machen, die restliche Navigation wird in der Route gemacht"\
**Accepted**: Präzisierung: Dialog-Aktionen sind keine Navigation, sondern Zustand; in den AppNavHost kommen die Route draw-detail/{drawId} mit navArgument und onBack = popBackStack(), der Hinweg zum DrawDetail über onOpenDraw bei der DrawHistory; dazu ARG_DRAW_ID, DRAW_DETAIL und drawDetail(drawId) im object Routes.\
**Commits**: -

**Prompt 13**: "const val DrawDetail = "drawDetail" – warum bekomme ich hier eine Fehlermeldung?"\
**Accepted**: Mögliche Ursachen: const val innerhalb einer Funktion, Namenskonflikt mit der Domain-Klasse DrawDetail; Lösung: Konstanten im object Routes in Großbuchstaben (ARG_DRAW_ID, DRAW_DETAIL = "draw-detail/{$ARG_DRAW_ID}") plus Hilfsfunktion drawDetail(drawId).\
**Changed**: Konstante in DRAW_DETAIL umbenannt und Platzhalter {$ARG_DRAW_ID} ergänzt.\
**Commits**: -

**Prompt 14**: Fehlerliste eingefügt ("No value passed for parameter 'content'", "Argument type mismatch: actual type is 'Routes', but 'String' was expected", "Expecting an element", "Unused import directive", "Const property name 'DrawDetail' should not contain lowercase letters", Typo-Hinweise)\
**Accepted**: Erklärung der Meldungen: ganzes Routes-Objekt statt Routes.DRAW_DETAIL übergeben, fehlender content-Block { … } bei composable(), Syntaxfehler ("Routes." ohne Namen), Konstante in Großbuchstaben umbenennen, ungenutzten Import löschen, Typo-Hinweise ignorieren.\
**Commits**: -

**Prompt 15**: (Anhang: AppNavHost.kt mit angefangenem DrawDetail-Block)\
**Accepted**: Fehlerursachen: unvollständiger composable-Block, Konstante DrawDetail ohne Großschreibung und ohne ID-Platzhalter, falscher Auto-Import com.example.wichtly.domain.model.DrawDetail; Korrektur: ARG_DRAW_ID, DRAW_DETAIL, drawDetail(drawId), Block 6 analog zu Gruppendetail/Wunschliste mit DrawDetailRoute(drawId, onBack), Import von ui.draws.DrawDetailRoute.\
**Changed**: Block 6 (DrawDetail) im AppNavHost selbst nachgebaut, ARG_DRAW_ID und DRAW_DETAIL ergänzt.\
**Rejected**: Entfernen des ungenutzten Imports domain.model.DrawDetail (vorerst).\
**Commits**: 197ff53

**Prompt 16**: (Anhang: überarbeitete AppNavHost.kt mit DrawDetail-Block)\
**Accepted**: Code-Review: arguments-Zeile falsch (navArgument fehlt, ARG.DRAW_ID statt ARG_DRAW_ID, Klammern); ARG_DRAW_ID und drawDetail(drawId) im object Routes fehlen; schließende Klammer der Funktion AppNavHost fehlt; Formatieren mit Strg + Alt + L.\
**Changed**: Fehler selbst korrigiert (navArgument-Zeile, ARG_DRAW_ID, schließende Klammer).\
**Commits**: 197ff53

**Prompt 17**: (Anhang: erneut überarbeitete AppNavHost.kt)\
**Accepted**: Restfehler: Routes.ARG.DRAW_ID statt Routes.ARG_DRAW_ID, vertauschte Klammern am Ende von DrawDetailRoute(...); Hinweis auf noch fehlende Hilfsfunktion drawDetail(drawId) für den Hinweg aus der Historie.\
**Changed**: ARG_DRAW_ID-Schreibweise und Klammerreihenfolge selbst korrigiert.\
**Commits**: 197ff53

**Prompt 18**: "ich mache jetzt DrawHistoryRoute und möchte bei DrawHistoryScreen onOpenDraw = (drawId: String) -> Unit umformen – ich definiere hier ja keine Funktion, sondern mache die Zuordnung, wie muss ich das ausdrücken?"\
**Accepted**: onOpenDraw ist Navigation und kommt vom AppNavHost; Definition im Funktionskopf der Route (onOpenDraw: (drawId: String) -> Unit), Zuordnung beim Screen-Aufruf (onOpenDraw = onOpenDraw); im AppNavHost onOpenDraw = { drawId -> navController.navigate(Routes.drawDetail(drawId)) }.\
**Commits**: -

**Prompt 19**: "irgendwo habe ich einen Fehler" (Anhang: eigener Entwurf von DrawHistoryRoute.kt als Kopie der DrawDetailRoute)\
**Accepted**: Code-Review mit korrigierter Version: groupId statt drawId, Parameter onOpenDraw im Funktionskopf ergänzen, DrawHistoryViewModelFactory(groupId, drawRepository, giftGroupRepository), DrawHistoryViewModel, key = groupId; Vergleichstabelle DrawDetailRoute vs. DrawHistoryRoute.\
**Changed**: Eigenen Entwurf durch die korrigierte Version ersetzt.\
**Commits**: c5d166f

**Prompt 20**: "key stimmt doch so, warum muss ich das ändern?"; "wo ist der Fehler?" (Anhang: überarbeitete DrawHistoryRoute.kt)\
**Accepted**: key = drawId funktioniert technisch, aber die Variable enthält eine Gruppen-ID – Parameter überall in groupId umbenennen (Umschalt + F6); echte Fehler: onOpenDraw im Funktionskopf fehlt, giftGroupRepository fehlt beim Aufruf der DrawHistoryViewModelFactory.\
**Changed**: Parameter in groupId umbenannt, onOpenDraw und giftGroupRepository ergänzt.\
**Rejected**: Markierungskommentare (// umbenannt, // neu) entfernen bzw. Kommentare anpassen (vorerst).\
**Commits**: c5d166f

**Prompt 21**: (Anhang: AppNavHost.kt mit angefangenem DrawHistory-Block)\
**Accepted**: Code-Review: Route DRAW_HIST ohne Platzhalter → DRAW_HISTORY = "draw-history/{$ARG_GROUP_ID}"; navArgument braucht den Platzhalter-Namen, nicht die Route; Import DrawHistoryScreen → DrawHistoryRoute (AppNavHost ruft immer die Route auf); Hilfsfunktionen drawDetail(drawId) und drawHistory(groupId) ergänzen; Hinweis, dass der Historie-Button im Gruppendetail noch einen Callback von Diana braucht.\
**Changed**: Block 7 (DrawHistory) im AppNavHost selbst eingebaut, Import DrawHistoryRoute ergänzt.\
**Commits**: 41472f4

**Prompt 22**: "ich habe aber 5 Fehlermeldungen" (Anhang: überarbeitete AppNavHost.kt)\
**Accepted**: Ursachen: Hilfsfunktionen drawDetail/drawHistory außerhalb des object Routes, Konstante DRAW_HIST statt DRAW_HISTORY und ohne Platzhalter, fehlender Import DrawHistoryRoute, eine schließende Klammer zu viel am Dateiende.\
**Changed**: Hilfsfunktionen ins object Routes verschoben, Konstante in DRAW_HISTORY umbenannt.\
**Commits**: 41472f4

**Prompt 23**: (Anhang: erneut überarbeitete AppNavHost.kt)\
**Accepted**: Restpunkte: überzählige schließende Klammer, fehlender Import DrawHistoryRoute, fehlender Platzhalter in DRAW_HISTORY (sonst Absturz, weil "draw-history/abc" nicht zur angemeldeten Route passt); Merkregel: Konstante {…} und Hilfsfunktion $… gleich aufbauen.\
**Changed**: Platzhalter {$ARG_GROUP_ID} in DRAW_HISTORY ergänzt, überzählige Klammer entfernt.\
**Rejected**: Entfernen des ungenutzten Imports domain.model.DrawDetail.\
**Commits**: 41472f4

**Prompt 24**: "was ist noch offen von den Aufgaben, die ich erledigen muss, habe ich irgendwas übersehen?"\
**Accepted**: Statusübersicht: erledigt (GroupsViewModelFactory, Callbacks, GroupsRoute, DrawDetailRoute, DrawHistoryRoute, Blöcke 6/7 im AppNavHost); offen (Testen, Commit/Push, Pull Request, AI_USAGE); Abstimmungen mit Diana (onOpenHistory-Callback, onAppStopped) und Melissa (Testdaten im GroupsViewModel, PreparationScreen); Tests für SecretSantaDrawAlgorithm und Routes.\
**Commits**: -

 ### 2026-11-22 (Melissa, Meyer), Copilot (kostenlos)
**Prompt 1**:val uiState: StateFlow<PreparationUiState> = combine(
    //
    giftGroupRepository.getGiftGroupById(groupId)
        // Fehlerbehandung
        .catch { error ->
            if (error is CancellationException) throw error
            Log.e("PreparationViewModel", "Gruppe konnte nicht geladen werden", error)
            errorState.update { "Die Gruppendaten konnten nicht geladen werden." }
            // emit() sendet einen Wert (hier null) an alle die den Flow beobachten.
            emit(null)
        },
    participantRepository.getParticipantsByGroupId(groupId)
        .catch { error ->
            if (error is CancellationException) throw error
            Log.e("PreparationViewModel", "Teilnehmer konnten nicht geladen werden", error)
            errorState.update { "Die Teilnehmerliste konnte nicht geladen werden." }
            emit(emptyList())
        },
    startingState,
    errorState,
) { group, participants, isStarting, errorMessage ->
    // Wenn kein Fehler aufgetreten ist und die Gruppe null bleibt,
    // wurde keine Gruppe mit dieser ID in der Datenbank gefunden.
    val notFound = group == null && errorMessage == null
    PreparationUiState(
        isLoading = false,
        group = group,
        participants = participants,
        notFound = notFound,
        errorMessage = errorMessage,
        isStarting = isStarting,
        // Mindestens drei Teilnehmer sind laut Aufgabenstellung erforderlich.
        canStartDraw = group != null && participants.size >= 3,
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = PreparationUiState(),
) bitte zerlege mir die Funktion ein bischen mehr und erkläre mir was getan wird. Kann ich das auch einfacher schrieben?
**Prompt 2**: Kann ich auch val notFound = (group == null && errorMessage == null) schreiben? Erkläre mir genauer, warum ich von Flow in StateFlow umwandelen sollte.
**Prompt 3:**: Erklaere mir nun nocht die Parameter von .stateIn()
**Prompt 4**: private val startingState = MutableStateFlow(false)
private val errorState = MutableStateFlow<String?>(null) Wozu sist das im ViewModel da?
**Prompt 5**:try {
    // traegt dem Repository auf, dass es die Auslosunf durchfueheren soll
    when (val result = drawRepository.performDraw(groupId)) {
        // Wenn die Auslosung erfolgreich durch gefuehrt wurde, 
        // wird in result die Id der Auslosung gespeichert
        is DrawResult.Success -> onSuccess(result.draw.id)
        // Wenn die Auslosung nicht erfolgreich durchgefuehrt wurde,
        // dann wird im ViewModel eine error-Meldung gespeichert 
        is DrawResult.ValidationError -> {
            errorState.update { "Die Auslosung konnte nicht gestartet werden." }
        }
    }
} Welchen Wert nimmt result an, wenn die Ausloung nicht erfolgreich war?.\
**Angenommen**: mit dem neuen Wissen kommentiert  
**Commits:**   eb999fb 

### 2026-11-22 (Melissa, Meyer), GitHup Copilot Education
**Prompt 1**Bitte passe den Code an:
Es soll im GruouenScreen das Feature eingebaut werden fuer jede Gruppe ein Gruppenbild mit der Gerätekamera aufzunehmen, persistent zu speichern und angezeigt zu werden.
Die Stelle, wo das Bild eingefuegt werden soll, ist durch einen Kommentar markiert.
Bevor man ein Bild aufnehmen kann sollen Abfragen erscheinen: Willst du wirklich ein Foto aufnehmen? Und eine Abfrage, die den Kamerazugriff erlaubt (immer, nur dieses Mal oder gar nicht).
Versuche den Code und das Design aus des von dem Teilnehmerfoto wiederzuverwerten..\
**Angenommen**:    Aenderungen im GroupsBereich
**Commits:**       eaad749 

### 2026-11-22 (Melissa, Meyer), Copilot (kostenlos)
**Prompt 1**: val activePhotoGroupId = uiState.photoFlowGroupId
if (activePhotoGroupId != null) {
    val activeGroup = uiState.groups.firstOrNull { it.id == activePhotoGroupId }
    if (activeGroup != null) {
        PhotoCaptureFlow(
            area = PhotoArea.GROUP,
            hasPhoto = activeGroup.photoPath != null,
            isOpen = true,
            onClose = onPhotoFlowClose,
            onPhotoCaptured = { file ->
                onPhotoCaptured(activePhotoGroupId, file)
            },
            onRemovePhoto = { onRemovePhoto(activePhotoGroupId) },
            onCameraDenied = { onCameraDenied(activePhotoGroupId) },
        )
    }
}

uiState.cameraDeniedGroupId?.let {
    CameraPermissionDeniedHint(
        onOpenSettings = {},
        modifier = Modifier.padding(horizontal = 16.dp),
    )
} was wird hier getan? .\
**Angenommen**:            Kommentare  
**Commits:**               eaad749 

### 2026-11-23 (Melissa, Meyer), GitHup Copilot Education
**Prompt 1**:Ist der Parameter isLoading generel in Prepartion notwenig? im ViewModel wird er ausschielich auf false gesetzt.
**Prompt 2**: Ist es viel Aufwand die isLoading umzusetzen?
**Prompt 3**: Okay, dann nehme ich isLoafding in Prepatation raus. Macht eine isLoaeding thethode beim GroupsSceen Sinn. da schon eher, da mehr Daten geladen werden oder?
**Prompt 4**: Wie setze ich das in GroupsSceen um? Stetze ich per Parameter isLoading vom UiState auf true und am Ende des intit auf false?
**Prompt 5**: Bitte binde die Fehlerbehandlung im Code korrekt ein und intregriere isLoading.\
**Geaendert**:      Preperation: loeschen von isLoading
**Angenommen**:     Groups: Einbindung von isLoading und ErrorMessage
**Commits:**        94961c9

### 2026-11-23 (Melissa, Meyer), Copilot (kostenlos)
**Prompt 1**: was macht dieser Code: photoFiles = groups.associate { group ->
    group.id to (group.photoPath?.let { photoRepository.getFileForRelativePath(it) } ?: File(""))
}.filterValues { it.exists() }, .\
**Geaendert**:      Komentare
**Commits:**        94961c9

### 2026-11-23 (Diana, Folz), Gemini in Android Studio, Claude.ai (pro)

**Prompt 1**:
Aufgabe: Die Buttons „Auslosung durchführen →“ und „Ansehen“ (Historie) im Gruppendetail haben leere onClick-Blöcke. Gib dem Screen zwei Callbacks, reiche sie bis zu den Buttons durch und über die GroupDetailRoute nach außen. Die eigentliche Navigation baut Person A, nicht hier.

STRIKTE REGELN
- Führe keine git-Befehle aus (kein checkout, stash, reset, commit).
- Erlaubte Änderungen: GroupDetailScreen.kt, GroupDetailRoute.kt und in navigation/AppNavHost.kt ausschließlich der Aufruf von GroupDetailRoute. Nichts sonst, keine neuen Dependencies, Button-Texte unverändert lassen.
- Der Screen bleibt zustandslos und kennt keine IDs und keine Navigation.

1. GroupDetailScreen: neue Parameter `onPrepareDraw: () -> Unit` und `onOpenHistory: () -> Unit` ohne Standardwerte, durchgereicht über GroupDetailContentState bis DrawAndHistorySection und dort als onClick der beiden Buttons. KDoc ergänzen.
2. Alle 8 @Preview-Funktionen: `onPrepareDraw = {}` und `onOpenHistory = {}` ergänzen.
3. GroupDetailRoute: `onPrepareDraw: (groupId: String) -> Unit` und `onOpenHistory: (groupId: String) -> Unit`, Aufruf mit `{ onPrepareDraw(groupId) }` (gleiches Muster wie onEditGroupClick).
4. AppNavHost.kt: vorläufig zwei Platzhalter mit `TODO(A)`.
5. Weitere Aufrufer per Grep suchen und nennen.
**Accepted**: As is.
**Changed**: Vor dem Prompt hat Claude die echten Dateien gelesen. Die Buttons liegen in einem privaten Composable zwei Ebenen tief, der erste Prompt-Entwurf hätte das übersehen. Auf meine Frage, ob ich damit in A's AppNavHost etwas kaputt mache: nein, es sind nur zwei Platzhalter. A ersetzt sie durch die echten Ziele (`Routes.preparation(id)`, `Routes.drawHistory(id)`).
**Checked**: Review der drei Dateien durch Claude, Build, alle Tests grün.
**Commits**: a6c62cd


### 2026-11-23 (Merve, Öztürk), Claude.ai (Opus 5.5)
**Prompt 1**: "geh in meinen Code und schau mal, was alles gemacht wurde, und sage, was noch von wem offen ist"\
**Accepted**: Analyse des main-Stands nach Pull: Push vom 21.11. bestätigt; neu von Melissa: PreparationScreen/-Route/-ViewModel, echte Daten im GroupsViewModel, Losen-Button verdrahtet, Gruppenfoto inkl. Kamera-Verweigerung; offen: Diana – Callbacks für "Auslosung vorbereiten" und "Historie" im Gruppendetail, onAppStopped; Merve – Callbacks danach im AppNavHost verdrahten, Aufräumen, Tests, Ablauf testen; alle – README und KI-Reflexion.\
**Commits**: -

**Prompt 2**: "kannst du bitte in meine App schauen, also in den Code, was wurde gemacht?" / "was ist noch offen?"\
**Accepted**: Analyse nach Merge von origin/main (a6c62cd): Diana hat onPrepareDraw und onOpenHistory im GroupDetailScreen/GroupDetailRoute ergänzt und im AppNavHost zwei Platzhalter "TODO(A)" hinterlassen; Vorschlag: onPrepareDraw = { id -> navController.navigate(Routes.preparation(id)) }, onOpenHistory = { id -> navController.navigate(Routes.drawHistory(id)) }.\
**Commits**: -

**Prompt 3**: Nachricht von Diana eingefügt ("habe meine offenen Punkte auf main gepusht, musste zwei Zeilen im AppNavHost hinzufügen – onAppStopped() wird von der Route aufgerufen, die Draw-Route würdest du bauen")\
**Accepted**: Prüfung von Dianas zwei Zeilen; Anleitung für onAppStopped in der DrawDetailRoute per LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAppStopped() } inkl. Imports und Erklärung (Geheimhaltung beim App-Wechsel, Zustand statt Navigation); Testablauf und Commit-Vorschlag.\
**Changed**: TODO(A)-Zeilen im AppNavHost selbst gefüllt; LifecycleEventEffect für onAppStopped in DrawDetailRoute eingebaut.\
**Commits**: 49191ee, 4b149e9

**Prompt 4**: "ok, was sind noch die offenen Punkte?"\
**Accepted**: Aktuelle Liste: Aufräumen, Emulator-Cold-Boot und kompletten Ablauf testen, Commit/Push; danach Tests (SecretSantaDrawAlgorithm, Routes, leere GroupsViewModelTest.kt); gemeinsam README und KI-Reflexion; keine Abhängigkeiten mehr zu anderen.\
**Commits**: -

**Prompt 5**: "ich muss noch die tests machen, ich lege die Testdateien in app/src/test an, kannst du mir die Tests erstellen, damit ich sie einfügen kann"\
**Accepted**: Abgleich mit der ursprünglichen Aufgabenliste; Testplan: SecretSantaDrawAlgorithm (wichtigster), Routes, GroupsViewModelFactory, später Navigations-UI-Tests. Die Tests wurden anschließend mit Claude erstellt: Unit-Tests für SecretSantaDrawAlgorithm, Routes-Hilfsfunktionen, GiftGroup-, Participant- und WishListValidator, GroupsViewModel und PreparationViewModel.\
**Changed**: Am 27.11. die Funktionsnamen und Kommentare in den Tests selbst ins Englische übersetzt und einen Test angepasst.\
**Commits**: 7695680, bb49589, afe7a93, 9957e3f, 1d3fa57, 537400a, 6b7c8f2, 0dbf54c, 4baa8c0

### 2026-11-23 (Viktoria)

**Prompts for UI: picking mockups."**
**Commits**: 8dcfa0a
"The UI of the app looks very inconsistent and messy across all screens. I am assigned with the task to unify it, give it a "secret santa" like, modern and minimalist but expressive layout. How do I proceed with this task?"
"please tell me what you mean by "layouts stay unachanged". Because maybe I DO want to change layouts, but you need to specify"
"so: picker with more color combinations. NO light/dark mode only ONE. also MORE fonts to choose from. color and font are first step."
"Palette B · Evergreen Night; Font 6 · Young Serif + Instrument Sans"
"Groups: hero cards; Detail: one scroll; Reveal: gift tag; Details: no pattern, 24dp corners, pill buttons, gift-tag chips; BUT also make some shadow (black or other dark colour) on the title text. create another artifact with ALL screen previews first BEFORE implementing. start implementing only after MY approval. Prepare to make several rounds of re-adjustments to the artifact mockup."
"update mockup artifact based on my criticism from (path) criticism.txt"
--> "Implement now."
**Accepted**: as is.

**Prompt**: "Please restore the style and amount of comments which were present BEFORE my major UI refactor. Please restore ANYTHING which was there before my last commits. "
"please do adjust any comments that do not match the new code anymore; every comment should be correct and fit to current code."\
**Commits**: 6df30c2


### 2026-11-24 (Viktoria)

**Prompts for further UI redesign; several rounds**
**Commits**: f815f72, 7927c36
"Last session we were working on major UI refactoring. However, there are still some problematic issues with the UI. How do we proceed so as to minimize the amounts of cycles of implementations while correcting those issue? here are the issues:
UI REMARKS AFTER REFACTOR
#####
DESIGN: Startseite anpassen ausgehend von Mockup-Auswahl (Liste VS Startseite?). Startseite soll deutlicher als solche erkennbar und einladender sein.
ALLG: Glitzersterne passen nicht überall (überschrieben usw) -->Topbar überschreibt das glitzer..
GROUPFORM: Abbrechbutton Text passt nicht in Button rein; alternative Überschrift bei EDIT statt "Gruppenstammdaten"??
GROUPDETAIL: "Doppelte Namen sind erlaubt" entfernen, ist falsch.
Statuschip "regeln offen" muss weg bei Schrottwichteln; ist auch vorber. möglich.
Chip "vorbereitung möglich" ist zu lang, kürzen (weil Text daneben ist dann hässlich).
Button "+Teilnehmer" ist zu lang, passt nicht rein!!!! (dann aber auch andere analoge Buttons entsprechend angleichen).
Wunschliste öffnen nur auf EINEM weg (von Teilnehmer aus per tippen, nicht per Side menu). Text "neue auslosung ist abgeschnitten" wegen des langen Labels (auch sub-text zu lang). Auslosungshistorie-subtext entfernen.
WISHLIST SCREEN:
"hilftex nur einfache texte in dieser version. die reihenfolge deiner Einträge bleibt erhalten" weg.
DRAWDETAIL:
DRAWRESULTDIALOG:
Closed tag soll den Text von "nur für deine Augen" übernehmen; "nur für deine augen" dialog wird dadurch ersetzt und kommt weg (also entfernen von "DrawRevealConfirmDialog")
DRAWDETAILSCREEN: subtitle "schenkt ++++" eher Fragezeichen oÄ statt den punkten
DRAWHISTORY:
DATUM UND UHRZEIT uhrzeit anzheigen in history. Dafür die anderen infos weglassen im drawhistoryscreen.
gespeichert label so groß dass viel text nicht sichtbar (zB DrawHistoryScreen -> davon die ListItemCard)
"Start screen: A, aber pass gut auf, dass der Appname nicht abgeschnitten wird, das ist im Mockup ein Problem."
"das Meiste sieht gut aus; noch paar Kritikpunkte:
NEWER REMARKS:
group erstellen: "gruppe erstellen" text passt nicht in button.
GROUPDETAIL: "neue auslosung" ist immer noch nicht sichtbar. inconsistency: dass sowohl "neue auslosung" drin ist als auch der "losen button"... diese inconsistency muss beseitigt werden; hierfür bitte mockups mit verschiedenen varianten."
"Refactor both Grouplist and Groupdetail so that "Losen" becomes main button (similar to proposal D) and adding actions are somewhetere else."
"create possibility to close the ClosedTag component in case the person is not the one in question"\



### 2026-11-25 (Viktoria)

**Prompt 1**: "Überführe alle hardcodierten Stringliterale in die strings xml"
**Accepted**: as is.
**Commits**: e27e524

**Prompt 2**: Zentralisiere die color referenzen. Zurzeit sind sie in colors.xml als auch in color.kt.
**Accepted**: as is.
**Commits**: e81ca74

**Prompt 3**: Entferne Validierung aus UI und ViewModels und dem Data-Layer. Erstelle Usecases im domain layer, sodass die repositories nur noch reine persistenz sind.
**Accepted**: as is.
**Commits**: fd91a6d

**Prompt 4**: ergänze die delete funktion für die gruppen, auf allen layern.
**Accepted**: as is.
**Commits**: 5606889

**Prompt 5**:Zeige auf dem Draw Result Screen den echten Gruppennamen anstatt Platzhalter.
**Accepted**: as is.
**Commits**: a54b7e0

**Prompt 6**: Verhindere mögliche Doppeltipp Ereignisse in der Navigation.
**Accepted**: as is.
**Commits**: 18070e2

**Prompt 7**: Exportiere unser aktuelles DB Schema und stelle auf automigrate ab der aktuellen version. Destruktiv für downgrade und Vorgängerversionen.
**Accepted**: as is.
**Commits**: 448dc65

**Prompt 8**: Erstelle zentralisiertes Logging ohne dass immer Log importiert werden muss mit Stufen debug, info, warn, error. Keine Klarnamen in den Infologs (datenschutz).
**Accepted**: as is.
**Commits**: 64f2fab

**Prompt 9**: Erstelle zentralisiertes Logging-Muster ohne dass immer Log importiert werden muss mit Stufen debug, info, warn, error. Keine Klarnamen in den Infologs (datenschutz).
**Accepted**: as is.
**Commits**: 64f2fab

**Prompt 10**: unterteile die UI feat packages weiter: groups in list, form, detail. draws: preparation, history, detail. components in sinnvoll thematisch gegliederte unterpackages. domain/model in: grouop, participant, draw, photo. unterteile die screen+state packages in stateful und stateless subpackages.
**Accepted**: as is.
**Commits**: e5bc693

**Prompt 11**: Erweitere die Validierung: doppelte wunschnamen verboten; budget ab 999999 verboten, wichteltag ab +100Jahren verboten, bearbeiten nach wichteltag erlaubt aber datumsänderung nur in zukünftige Daten, losen nach wichteltag sperren, begrenze zeichenlänge für participants und wishlist in den stateless ui components.
**Accepted**: as is.
**Commits**: edc1741

**Prompt 12**: Benenne die Navigationsereignisse einheitlich, benenne die Zustandsfelder isloading und notfound einheitlich, stelle die gruppenliste auf combine plus stateIn um, benenne wishlist überall in "Wishlist" um, füge hardcodierte abstände alle in Spacing.kt über, benenne die repository methoden und error typen einheitlich, ladende dialoge einheltich, benenne UI callbacks einheitlich, entferne default test file
**Accepted**: as is.
**Commits**: edd97b2

**Prompt 13**: schütze form/editor eingaben mit SavedStateHandle; erstelle einen prozess der verwaise photos bei Appstart aufräumt; erstelle ein grundlegendes README gemäß dem derzeitigen Codestand; stelle die viewmodel creation um auf viewModelFactory creation, lade alle daten für die auslösung zusammen inerhalb Transaktion, navigiere nach group create direkt ins detail, adde fehler handling für vollen speicher inklusive string, entferne stale ladefehler aus dem stateflow, beschränke die fotopfade auf filesDir/photos verzeichnis, stelle savephotofile auf verschieben statt kopieren um, trenne ladefehler von aktionsfehler beim losen, handle photocaptureerror als text,
**Accepted**: as is.
**Commits**: 60e30c0


**Prompt 14**: erstelle eigenen DeleteParticipantUseCase (erst datenbank, dann foto), erstelle für alle usecases einheitliches deleteresult, kapsele den participant editor state im groupdetailuistate, passe backup/data extraction rules an no-backup an, vereinheitliche fehler-mapper in group, wishlist, participant; lösche dead code in mainactivity und ungeuste androidtest dependency
**Accepted**: as is.
**Commits**: 3075dd3

**Prompt 15**: übersetze readme auf englisch; stelle einmaliges lesen in repository auf suspend fun um, zentralisiere draw datum formatierung
**Accepted**: as is.
**Commits**: dd76d70


**Prompt 16**: suche nach fachlich inkorrekten kommentaren und ersetze sie. übersetze alle kommentare für teammitlieder temporär auf deutsch.
**Accepted**: as is.
**Commits**: 2328b4b

**Prompt 17**: entferne  drawType, assignmentCount aus DrawSummary
**Accepted**: as is.
**Commits**: 1098bbe

**Prompt 18**: ergänze load fail state in groupform; übersetze alle log messages nach Englisch; verschiebe alle ergebnistypen nach domain/result, ergänze für wunschlöschung den DeleteWishlistItemUseCase, suche nach ungenutzten importen und entferne sie, photo getFileForRelativePath: behandle exceptions als null bzw. wie fehlendes foto,
**Accepted**: as is.
**Commits**: 1ef3e99

**Prompt 19**: Überführe den drawalgorithm in kotlin code muster.
**Accepted**: as is.
**Commits**:ae0ea59

**Prompt 20**: ergänze den README abschnitt um development umgebung, android studio version, gradle, jdk, sdk, platzhalter für benutzte devices
**Accepted**: as is.
**Commits**: 56665aa

**Prompt 21**: fasse das photohandling in groupdetailvm und groupformvm zusammen, kapsele photo ui state wie zuvor den participanteditorstate, bündle die photocallbacks für die jeweiligen screens, erstelle als action class ein participanteditoractions,
**Accepted**: as is.
**Commits**: 48273d0


### 2026-11-26 (Viktoria)
**Prompt 1**:
Der photo capture prozess im screen und uistate ist verwirrend, da er durch viele klassen führt. Bitte vereinfache und kapsele.
**Prompt 2**  
außerdem ist die location der photo components im ordner ui nicht uniform zu den anderen ui sachen. auch das muss man lösen.
**Accepted**: Flow until capture delegated from VM to PhotoFlowState. Main screens only see PhotoField. New package ui/photo.
**Commits**: dc16279

### 2026-11-26 (Melissa, Meyer), Copilot (kostenlos)
**Prompt 1**: Wir scheiben eine Ardoid App in Kotiln mit Git. Uns ist aufgefallen, dass wir 2 .gitignore haben. Ein .gitignore liegt ganz oben bei der README und ein .gitignore liegt in der ersten Schicht des App-Ordners. Ich möchte die .gitignore zusammenfassen. Es ist doch sinvoller, das .gitignore ganz oben zu behalten, oder?
**Prompt 2**: Habe ich das so richtig eingebunden?.\
**Geaendert**:         loesche .gitignore in app und passe das bleibende .gitignore an
**Commits:**           dac99ca

### 2026-11-23 (Melissa, Meyer), GitHup Copilot Education
**Prompt 1**: Ist es richtig, das beim ButtonContent ein Icon und ein Text angezeigt werden kann? Bitte kommentire den controls Ordner weiter in einfachen Englisch aus.
**Geaendert**:       Komantare
**Commits:**         d66adc4 

### 2026-11-27 (Viktoria)
**Prompt 1**
Die Dateien GroupFormViewModel, GroupFormScreen, GroupDetailScreen, GroupDetailViewModel sind zu riesig. 
Bitte entzerre sie, ohne substanziell etwas am Code selbst zu ändern. Orientiere dich an der bestehenden Dateistrutkur.
**Accepted**: File restructuring. 
**Commits**: 195b9b6

**Prompt 2**
Comment our code in a professional manner, with KDocs; translate all comments into english. Also 
search for dead code and remove; put remaining relevant files into gitignore.
Also do update the README so that it is more professional;
note that the remarks on architecture are too detailed for a README, also add my current xiaomi device (see androidstudio devices)
to the README devices. Also do create a CLAUDE.md and update the AGENTS.md (for non-claude agents such as gemini in android studio and other) 
so both of them are professional and conform to our current standards.
**Prompt 3**
Set up ktlint and run it through our code.
**Accepted**: Improved comments, README, CLAUDE.md, AGENTS.md, ktlint.
**Commits**: 7a45ff0, 5c13055, 5636f68

**Prompt 3**
Please prevent the DrawDetail popup card from closing on app rotation. 
**Accepted**: Changes to state handling in route/vm.
**Commits*: 1133ec0

### 2026-11-27 (Merve, Öztürk), Claude.ai (Opus 5.5)
**Prompt 1**: "ich habe folgendes Problem: im NavHost gibt es einen Widerspruch – requireNotNull und ? im AppNavHost raus, das ?. macht keinen Sinn" (Anhang: Foto vom Pseudocode des Dozenten, umgesetzt am Beispiel DrawHistory)
```
backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID)?.let { groupId ->
    DrawHistoryRoute(groupId = groupId, ...)
} ?: run {
    // TODO: Log
    // TODO: show error
}
```
**Accepted**: Erklärung: ?. lässt null zu, requireNotNull verbietet null direkt danach (App-Absturz); ?. allein löschen geht nicht, da arguments nullable ist. Erklärung ?.let und Elvis-Operator ?: run; TODOs ausgefüllt: AppLog.e(...) und popBackStack() in einem LaunchedEffect (keine Navigation während der Composition); Muster an allen Stellen einheitlich verwenden.\
**Rejected**: Extension-Funktion requireArg in Routes.kt (hätte requireNotNull nur verschoben, die App wäre weiterhin abgestürzt).\
**Commits**: -

**Prompt 2**: "was macht die Hilfsfunktion, wie sieht sie aus und kannst du die Kommentare direkt auf englisch machen"\
**Accepted**: Zeilenweise Erklärung (Parameter, ?.let, ?: run, LaunchedEffect, AppLog, popBackStack); Begründung: Logik nur an einer Stelle. Code der Hilfsfunktion WithArgument mit englischen Kommentaren, Position in der Datei (unten, nach dropUnlessResumedWithArg) und benötigte Imports.\
**Commits**: -

**Prompt 3**: Screenshot AppNavHost.kt mit 6 Fehlern (NavBackStackEntry, arguments, destination rot)\
**Accepted**: Ursache: Import androidx.navigation.NavBackStackEntry fehlte (Alt + Enter).\
**Changed**: Import selbst ergänzt.\
**Commits**: 279924a

**Prompt 4**: Screenshot von Block 4 (Gruppendetail) mit "..." im Code
```
WithArgument(backStackEntry, Routes.ARG_GROUP_ID, navController) { groupId ->
    GroupDetailRoute(
        groupId = groupId,
        onBack = ...,
        ...
        onGroupDeleted = { navController.popBackStack() },
    )
}
```
**Accepted**: Hinweis: "..." aus dem Beispiel wörtlich übernommen, dadurch fehlten die echten Callbacks (onEditGroup, onOpenWishlist, onPrepareDraw, onOpenHistory); vollständiger Block 4 als Vorlage.\
**Changed**: Block 4 durch den vollständigen Block ersetzt.\
**Commits**: 279924a

**Prompt 5**: "aber ganz ehrlich, wenn ich das überall manuell hinschreibe, verbrauche ich weniger Zeilen als mit der Hilfsfunktion"\
**Accepted**: Zeilenvergleich (manuell ca. +36 Zeilen, Hilfsfunktion ca. +24 Zeilen) und Argumente für die Hilfsfunktion (DRY, eine Stelle für Änderungen, weniger Fehlerquellen); beide Varianten korrekt.\
**Decision**: Hilfsfunktion WithArgument beibehalten.\
**Commits**: 279924a

## Reflection

### Why this setup, and how it changed

**Merve**
- Only Claude.ai (Opus 5.5), used as a tutor for navigation and Kotlin, because I had little programming experience.
- At first Claude produced a complete solution, including changes to other people's files. I rejected it and asked to work step by step, with an explanation for every line.
- Shared real code, screenshots of errors and the git log so that Claude worked with the actual project state.

**Viktoria**
- Gemini in Android Studio directly in the IDE because it was free. Most work on data and domain was done here. 
- Afterwards used Claude for global refactoring and design (including mockup creation).
- Real device for debugging due to performance reasons.

**Diana**
- Two tools: Gemini (Android Studio) for code changes, Claude.ai for planning, writing the prompts and reviewing before each commit 
- Prompts got smaller and stricter over time: allowed files named, no new dependencies, project rules repeated
- "No git commands" added to every prompt after Gemini tried to discard uncommitted work
- Sent the real files to Claude before prompting additionally to describing them from memory, which would have caught wrong assumptions
- Tests as the main check

**Melissa**
- There was a focus on the lecture content (Android Studio with Grandel, Kotlin, Material 3, Emulator 
  (I worked with a virtual device: Pixel 8 with Android 17.0 and API 37.0 - as slides 01)
- The elements from Material 3 are great for creating a beautiful app design
- We separate the UI, logic and data (e.g.by MVVM, repositorys and DAO)

**Team**
- Some members wanted to begin with free assistants, that is why no common start setup was possible.
- MVVM as a paradigmatic architecture suitable even for beginners (no one had prior experience with larger 
self-made projects or Android). 


### What you learned to control

**Merve**
- Explanations first, then code: why a line is needed and what it affects.
- Review my own code with Claude and fix the errors myself instead of copying a corrected version.
- Basics of Kotlin and Jetpack Compose, especially how functions and callbacks work together and how to handle missing values safely.
- Navigation pattern: the screen only reports clicks via callbacks, AppNavHost decides where to go.

**Viktoria**
- Switch between plan-only and implementation mode. 
- Data model (DB, entities, fields) requires especially careful review.

**Diana**
- Scope: name the exact files Gemini may touch, everything else is off-limits
- Rules in every prompt: no !!, no silent catch, rethrow CancellationException, IDs instead of names, no new dependencies
- Git stays with me: Gemini never commits, resets or checks out
- Plan first: use /plan or "analyse only, change nothing" before larger changes
- Verify facts: let Gemini read the real code and not invent class or method names
- Review before every commit: build, previews, emulator, and Claude reading the changed files
- Tests as a counter-check: red tests are analysed first, not "fixed" by changing the test
- Ownership: no edits to other people's files (e.g. AppNavHost) beyond placeholders, and tell them about it

**Melissa**
- I learn to develop with Android Studio and Kotlin. 
  I now understand the meaning of elements such as @Composable, Flow, @Suspend, Emit(), and so on.
  I can control a virtual Device.
- I understand the data flow behind our MVVM models
- I learned about permissions (e.g. for camera access)
- I learnd tha you can save Date permanent via Database. In Android Studio a greate solution is Room.

**Team**

### What helped, what got in the way

**Merve**
- Helped:
  - Step-by-step explanations with examples and flow diagrams (e.g. the data flow from the database to the detail screen).
  - Code reviews of my drafts that explained the error instead of only fixing it.
  - Routes of team members (e.g. GroupDetailRoute) as a template.
- Got in the way:
  - The AI often offered a finished solution right away instead of explaining first.

**Viktoria**
- Helped:
  - Planning before implementing. Especially the base Architecture itself. 
  - AI design mockups as Claude artifacts.
  - Separating validation and writes into use cases simplified the repository layer and ViewModels.
- Got in the way:
  - Side effects of agent, such as the UI refactor altering comments of team members.
  - Relying on decisions such as upsert, on "no separate use case" due to lack of experience.


**Diana**
- Helped:
  - small prompts with clear scope and fixed rules
  - a review by Claude before each commit, which caught EXIF rotation and photo deletion timing
  - fakes and ViewModel tests as a safety net
- Got in the way: 
  - the AI forgot team decisions, e.g. kept suggesting group photos
  - plausible but wrong fixes, e.g. the test dispatcher fix
  - missing context about other people's code, e.g. missing GroupsScreen callbacks

**Melissa**
- *To be added.*

**Team**
- Helped:
  - Maintaining clear folder structure. 
  - Separation of concerns:
      - Viktoria: data and domain layer
      - Diana: distinct UI + corresponding ViewModel 
      - Melissa: or distinct parts of UI + corresponding Viewmodel 
      - Merve: navigation, tests
- Got in the way:
  - Not agreeing early enough on a design system plus base UI components due to lack of experience with design/UI issues. 
  Kind of a technical debt.


### What you would do differently

**Merve**
- Write tests earlier, in parallel to the navigation.
- Commit smaller steps right away instead of implementing a lot at once and committing afterwards.
- Upload the lecture slides to the AI at the beginning, so that its solutions follow the slides and match the lecture content.

**Viktoria**
- Decide linting and CI as early as possible.
- Maybe set up another agent as Code Reviewer next time; 
let agents criticise own decisions more thoroughly.
- Set up room schema, logging, string/color res from beginning on.
 

**Diana**
- Merge main into my branch regularly instead of rarely and in large batches
- Test each feature on the emulator right away, not only at the end
- Use one (Gemini) chat per feature so the history stays easy to follow

**Melissa**
- *To be added.*

**Team**
- Instruct AI to keep files small(er)
- Constantly update AGENTS/CLAUDE.md file
