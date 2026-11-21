# Logbook fur die KI-Nutzung
In diesem Dokument sollen alle KI-Promps festgehalten werden, 
welche wir für das Projekt "WichelApp" in Rahmen der Vorlesung "Mobile Applikationen" bei Jan Brodbaecker geschrieben haben.

## Musterbeitrag
## 1. Titel
Datum: 2026-00-00  Autor: Vorname, Nachmame  Tool: Claude Code

Prompt 1:   "Erstelle einen Raum Entity und ein DAO für das Lekture. 
            Folge den Regeln in CLAUDE.md."
Prompt 2:  "Das Repositotry ruft das Netzwerk bei jedem Lesen.
            Mache die Datenbank zur einzigen Quelle für wahre Werte."
Prompt 3:  "Füge einen Unit Test für das Mapping von Entity auf Lecture ein."

Angenommen:  Entity, DAO und test wie vorgeschlagen
Geaendert:   Repository, nach dem 2. Prompt
Verworfen:   Der zweite Cache im Arbeitsspeicher. Nicht erforderlich.
Cheched:     Aprplane mode, Neustart: die Liste wird angezeigt.
Commits:     3f2a91c, 8be07d4

## Log Entries

#### Default tool: Gemini in Android Studio. Used if not stated otherwise.

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

### 2026-11-10 (Viktoria)

**Prompt 1**:
Read AGENTS.md. Read our domain model GiftGroup. Propose a domain model for Participant (as in)
secret santa participant of a GiftGroup. Justify it.
**Accepted**: id, groupId, name, photoPath for now.
**Rejected**: wishlist, assignedRecipientId (for now).
Decision was to have WishList as a multi-item entity itself.
**Commits**:

**Prompt 2**:
Suggest a Repository interface for the Participant model suitable
for CRUD operations, including listing.
nicht alles akzeptiert..; siehe erst prompt 3
akzeptiert: participantvalidator analog zu giftgroup validator

**Prompt 3**:
Two questions: 1) do still need deleteParticipantsByGroupId
even if we have CASCADE deletion for groups,2)
why did you not add add groupId to the signature of createParticipant?
Antwort war: deletion bei cascade nicht nötig, deletebygroup entfernt;
groupId war mein Fehler
-->deletebygroupId entfernt

**Prompt 4**:
Now do execute these steps (domain model, repository)
for the WishList entity itself. Each WishList
is linked to a participant and contains one or several
WishListItem objects.
(zurückgestellt... muss noch überprüft werden stand vom
promptgen 11 10)

--> erkläre warum wir sowohl wishlistitem als auch
wishlist brauchen??
reichtwishlist item wirklich nicht aus?

### 2026-11-12 (Viktoria)
**Prompt 1**:
Implement a Participant Room entity.
id/pk String, groupId String, name String, photopath like in GiftGroupEntity.
groupdId as foreignkey towards GiftGroupEntity.id in gift_groups,
onDelete=CASCADE.
**Accepted**: ParticipantEntity, Participant Mapper classes.
For now, accept fallback to destructive Migration in AppDatabase.
**Commits**:

**Prompt 2**:
Implement ParticipantDao with following functions:
getParticipantsByGroupId(groupId: String): Flow<List<ParticipantEntity>>
fun getParticipantById(id: String): Flow<ParticipantEntity?>
suspend fun insertParticipant(participant: ParticipantEntity)
suspend fun updateParticipant(participant: ParticipantEntity): Int
suspend fun deleteParticipantById(id: String)
-->dao klass passt so


**Prompt 3**:
Now implement ParticipantRepository according to the interface,
in analogy to GiftGroupRepositoryImpl
-> hinweis hat auch ParticipantValidator
gemacht, was eigtl. ok ist. Akz.
->loadParticipantsByGroup war gar nicht im Interface, erstmal weg!
-->darum ersetzung:
val existingParticipants = loadParticipantsByGroup(participantWithId.groupId)
val existingParticipants = participantDao.getParticipantsByGroupId(participantWithId.groupId).first().toDomainModelList()
-->sonst wurde VORLÄUFIG akzeptiert damit Diana mit was arbeiten kann;
aber nur als vorläufige Vers.


-->!! WICHTIG MUSS geprüft werden!!

**Prompt 4**:
Please make AppContainer sole owner and manager of AppDatabase.
Remove the Singleton pattern from AppDatabase.
-->nicht akzeptiert; create method impl bleibt in AppDatabase. 
->follow up prompt: No, please do leave the create method impl itself in the AppDatabase.
->accepted 

**Prompt 5**:
Please move UUID creation from the repository layer to the domain classes.
-->all acct; including changes do viewmodel i did not take into account 


**Prompt 6**:
Check all places where the name param is being trimmed.
Evaluate if it makes sense to also trim it in the RepositoryImpl classes or not.
--> all accept 

**Prompt 7**: 
Replace (repositoryimpl classes) the UPDATE NOtFound Error handling with pure existence 
check 
["Evaluate whether changing update error handling in GiftGroupRepositoryImpl and ParticipantRepositoryImpl
from checking rowsUpdated == 0 (post-Room @Update) to an explicit existence check
(existingItems.any { it.id == item.id }) beforehand is safe and appropriate for our offline-only app scope."]
--> umgesetzt; passt

### 2026-11-13 (Viktoria)

**Prompt 1**:
Change the WishListItem domain class so that it has the signature of TemporaryWishlistPlaceholders.kt.
Also add interface WishListRepository with: observerWishlistItems; createWishListItem, updateWishListItem,
deleteWishListItem
--> all accepting 

**Prompt 2**:
Implement a WishListValidator class
in analogy to existing validator classes.
-->all acc 

**Prompt 3**:
Implement WishListItemEntity als Table wishlist_items with id:String PK, participantId String FK 
(ParticipantEntity id, ondelete cascade, index).
Also implement WishListItemdao with observerbyparticipantid sorted by sortOrder;
insert, update; delete. 
-->passt 

**Prompt 4**:
Add getWishListItemById to Wishlist dao and repository.
Implement WishListRepository according to the interface. 
Add validation and trimming in analogy to existing repository classes.


COMMIT MSG:
feat(wishlist): implement database entity/dao, domain+mapper, repository layer, 
validator, add to AppDatabase/AppContainer




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

### 2026-11-16 (Merve, Öztürk), Claude.ai (Opus 5.5)
**Prompt 1**: "Lies AGENTS.md. Ich bin Person A und für die Navigation zuständig (MainActivity, AppNavHost, AppContainer). Erkläre mir, welche Screens im Projekt schon eine fertige Route-Funktion mit Callbacks haben und zwischen welchen Screens ich Navigation einbauen kann."\
**Accepted**: Nichts, die KI hatte keinen Zugriff auf den Projektordner.\
**Changed**: -\
**Commits**: -

**Prompt 2**: "Welche Dependency brauche ich für Navigation in Jetpack Compose?"\
**Accepted**: Dependency navigation-compose.\
**Changed**: Version auf die von der IDE vorgeschlagene angepasst.\
**Commits**: 0194ad8

**Prompt 3**: "Ich möchte in navigation/AppNavHost.kt ein object Routes anlegen. Über Routen sollen nur IDs übergeben werden. Erkläre mir, wie ich Routen mit Platzhalter (z. B. groupId) definiere und warum Hilfsfunktionen wie groupDetail(id) sinnvoll sind."\
**Accepted**: Aufbau von object Routes mit ARG-Konstanten, Routen-Mustern mit Platzhaltern und Hilfsfunktionen.\
**Changed**: Routen-Namen selbst gewählt, getrennte Routen für Gruppe anlegen und Gruppe bearbeiten.\
**Commits**: 0194ad8

**Prompt 4**: "Ich habe in AppNavHost.kt ein object Routes mit Adressen. Wie ordne ich diese Adressen den Screens GroupFormRoute, GroupDetailRoute und ParticipantWishlistRoute zu? Erkläre mir dabei, was NavHost, composable(), navArgument und popBackStack() machen."\
**Accepted**: Aufbau der composable-Blöcke mit navArgument und Auslesen der IDs über backStackEntry.arguments.\
**Changed**: Callbacks an die echten Signaturen unserer Route-Funktionen angepasst.\
**Commits**: 0194ad8

**Prompt 5**: "Ich habe AppNavHost fertig. Was muss ich in MainActivity ändern, damit die App mit dem AppNavHost startet?"\
**Accepted**: Aufruf von AppNavHost in setContent innerhalb unseres Themes.\
**Changed**: -\
**Commits**: 0194ad8

**Prompt 6**: "Hier ist mein AppNavHost [...]. Prüfe, ob etwas fehlt oder falsch ist, und erkläre Fehler. Schreibe den Code nicht komplett neu."\
**Accepted**: Hinweise aus dem Review als Prüfliste (fehlende Callbacks in GroupsScreen, Import von GroupsScreen, Zugriff auf AppContainer, Signaturen der Callbacks).\
**Changed**: Noch nichts geändert, Anpassungen folgen in einem späteren Commit.\
**Commits**: 0194ad8

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
