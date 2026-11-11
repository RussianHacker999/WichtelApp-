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
**Checked**: 12 Tests grün
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
**Changed**: Tests um Fehlermeldungen abzutasten: Vermeidung von doppelten Fehlertexten pro Feld bei mehreren Fehlern.
Korrekte Zuordnung der Fehlerklassen zu den jeweiligen Feldern.
**Checked**: 4 Tests grün + 2 Testsgrün (nachträglich 2 Test nachgetragen; insgesamt 6 Tests grün) 
**Commits**: ca5d2602, 58dbd7b0

**Prompt 3**:
Lies zuerst AGENTS.md. Lege im Paket com.example.wichtly.ui.groups.form die Dateien GroupFormViewModel.kt und GroupFormViewModelFactory.kt an. Ändere keine bestehenden Dateien. Kein Compose, keine neuen Dependencies. Nutze GroupFormUiState, GroupFormErrors.kt, GroupFormInput.kt, GiftGroupRepository, GiftGroupResult und GiftGroupValidationError genau wie im Projekt vorhanden, erfinde keine Klassen oder Felder.
GroupFormViewModel(groupId: String?, repository: GiftGroupRepository): groupId null bedeutet Erstellen, sonst Bearbeiten. Der State liegt in einem MutableStateFlow und wird als StateFlow bereitgestellt.
Laden: Bei groupId ungleich null setzt init isLoading und lädt die Gruppe genau einmal mit repository.getGiftGroupById(id).first(), nicht dauerhaft beobachten. Ist das Ergebnis null, wird notFound gesetzt. Sonst werden die Felder mit formatCentsForInput und formatDateForInput gefüllt, und die geladene Gruppe wird in einer privaten Variable gemerkt. Ein Fehler beim Laden setzt generalError mit einem deutschen Text. Im Erstellen-Modus entfällt das Laden.
Eingaben: onNameChange, onBudgetChange, onDrawDayChange, onDrawTypeChange. Jede setzt den Wert und löscht den Fehler des betroffenen Feldes.
onSaveClick: Läuft schon ein Speichervorgang, passiert nichts, diese Prüfung geschieht synchron vor dem Start der Coroutine. Zuerst Formatprüfung mit parseBudgetToCents und parseDate. Bei null wird der jeweilige Feldfehler mit den bereits vorhandenen Konstanten INVALID_BUDGET_FORMAT_ERROR bzw. INVALID_DATE_FORMAT_ERROR aus GroupFormErrors.kt gesetzt, lege keine neuen Texte an. ValidationError wird mit der vorhandenen Funktion applyDomainValidationErrors(state, errors) in den State übertragen., und das Repository wird nicht aufgerufen. Dann wird isSaving gesetzt und eine GiftGroup gebaut: Beim Erstellen mit leerer id (das Repository erzeugt die UUID) und photoPath null, beim Bearbeiten als copy() der geladenen Gruppe mit den neuen Werten, damit photoPath und id erhalten bleiben. Der Name wird mit trim() übergeben. Aufruf createGiftGroup bzw. updateGiftGroup. Success setzt isSaved und isSaving false. ValidationError wird mit der Übersetzungsfunktion aus GroupFormErrors.kt in die Feldfehler übertragen, isSaving wird false, die Eingaben bleiben erhalten. Andere Exceptions: CancellationException wird erneut geworfen, alles andere setzt generalError mit deutschem Text und isSaving false. Keine stillen catch-Blöcke.
GroupFormViewModelFactory(groupId: String?, repository: GiftGroupRepository) implementiert ViewModelProvider.Factory und erzeugt das ViewModel. Kommentare auf Deutsch, die das Warum erklären. Das Projekt muss bauen. Fasse am Ende zusammen, was du angelegt hast.
**Accepted**: GroupFormViewModel.kt, GroupFormViewModelFactory.kt, GroupFormViewModelTest.kt
**Checked**: 