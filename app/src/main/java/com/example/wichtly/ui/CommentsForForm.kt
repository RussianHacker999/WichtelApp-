package com.example.wichtly.ui

/*
Benutze die Funktionen von GiftGroupRepository aus domain/repository/GiftGroupRepository.kt
Benutze für das Fehler-Handling die Klassen GiftGroupValidationError, GiftGroupResult,
(ggf. GiftGroupValidator).
1) Create Formular:
Repository: createGiftGroup(group)
2) Update Formular:
Repository: getGiftGroupById(id)
Repository: updateGiftGroup(group)

Beachte, dass du die Validierungsfehler der "domain" package sauber abfangen musst
und in userfreundlichen Text übersetzen musst.
Überlege, ob wir noch weitere Formularvalidierung von UI-Seite brauchen,
die architekturtechnisch NICHT in die "domain" package gehören würden,
sondern ins "ui".

Als GiftGroup Datenklasse nutze die aus domain/model/GiftGroup, NICHT die GiftGroupEntity aus "data"

Beachtet Typenumwandlungen, zB in Sachen Geld oder Datumsformat.

 */