package com.example.wichtly.ui

/*
Anleitung für Entwickler der UI-Screens (DrawPreparation, DrawHistory, DrawDetail):

Verwende das DrawRepository aus domain/repository/DrawRepository.kt.
Verwende AUSSCHLIESSLICH die Domain-Modelle aus domain/model/ (DrawSummary, DrawDetail, DrawAssignment, DrawWishListItemSnapshot, DrawType)
und NICHT die Datenbank-Entitäten aus data/local/entity/.

--------------------------------------------------------------------------------
1) DrawPreparation Screen (Vor der Durchführung / Neue Auslosung starten):
--------------------------------------------------------------------------------
- Prüfen, ob bereits eine Auslosung existiert:
  Repository: observeLatestDrawDetail(groupId: String): Flow<DrawDetail?>
  -> Liefert die aktuellste Auslosung oder null, falls noch nie ausgelost wurde.

- Auslosung durchführen und unveränderlichen Snapshot automatisch in Room speichern:
  Repository: performDraw(groupId: String): suspend -> DrawResult
  -> Der drawType wird automatisch aus der Gruppe ausgelesen.
```
  -> Reagiere auf das Ergebnis:
     DrawResult.Success(draw) -> neuer Durchlauf ist gespeichert, draw.id für die Navigation zu DrawDetail nutzen.
     DrawResult.ValidationError(errors) -> mögliche Fehler: GroupNotFound, NotEnoughParticipants,
  -> Technische Speicherfehler kommen als Exception (try/catch im ViewModel, CancellationException nicht abfangen).
```


```
--------------------------------------------------------------------------------
1b) Teilnehmer-Vorschau für DrawPreparation (aktueller Stand, vor der Auslosung):
--------------------------------------------------------------------------------
  Repository: observeParticipantPreview(groupId: String): Flow<List<ParticipantPreview>>
  -> Name und Anzahl der aktuellen Wünsche je Teilnehmer, sortiert nach Name.

--------------------------------------------------------------------------------
1c) GroupsList (Quick-Start Auslosung und Gruppenkarten):
--------------------------------------------------------------------------------
  Repository: observeGroupDrawOverview(): Flow<List<GroupDrawOverview>>
  -> je Gruppe participantCount und drawCount, Zuordnung über groupId.
```


--------------------------------------------------------------------------------
2) DrawHistory Screen (Historie aller Auslosungen einer Gruppe):
--------------------------------------------------------------------------------
- Übersicht aller vergangenen Auslosungen anzeigen:
  Repository: observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>>
  -> Liefert eine schlanke Liste aller vergangenen Draws für die Gruppe (sortiert von neu nach alt).
  -> Enthält Datum (drawnAt), Wichtel-Typ (drawType) und Anzahl der Zuordnungen (assignmentCount).

--------------------------------------------------------------------------------
3) DrawDetail Screen (Detailansicht einer spezifischen Auslosung):
--------------------------------------------------------------------------------
- Details einer ausgewählten Auslosung laden:
  Repository: observeDrawDetailById(drawId: String): Flow<DrawDetail?>
  -> Liefert das vollständige DrawDetail mit allen Zuordnungen (giverName -> receiverName)
     und den zum Auslosungszeitpunkt eingefrorenen Wunschzettel-Einträgen (receiverWishlistItems).

*/
