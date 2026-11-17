# AGENTS.md
**Shared Guidelines for AI in Android Studio**

**Project:** Android Secret Santa App (working title is "Wichtly")   

## 1. App Scope 
- **Offline-Only Admin Application**
- **Core Features:**
    - Group Management (Name, Budget, Event Date, Group Photo).
    - Participant Management (Name, Photo, Wishlist).
    - Secret Santa Draw Logic (Initial draw, redraw, results display).
    - Immutable History Log (Past draws).

## 2. App Architecture 
- **Module Setup:** Single Gradle app module.
- **Language:** Kotlin only.
- **Architecture Principles**: MVVM pattern, Clean Architecture. 
- **Separation of Concerns:** Strictly separate Data-, State-, UI-, and Domain-Layers. 
- **UI Layer:**
    -  Jetpack Compose and Material 3.
    - each Composable component should include a @Preview decorator. 
- **State Layer:**
    - `ViewModel` components own screen state
- **Data Layer:**
    - Local database (Room/SQLite)
- **Domain Layer:**
    - Handles business logic. 

## 3. Current Feature Number 3: Draws and Draw Snapshots/History
### 3.1 Conducting the Draw
The organizer can:
- perform a random assignment of participants
- view all results in an overview
- select a participant
- display whom this participant is gifting to
- display the wishlist of the drawn participant
The draw algorithm must generate functionally valid assignments.
The following invariants are defined for the classic draw:
- Nobody draws themselves.
- Each participant is assigned exactly one recipient.
- Each participant is gifted to exactly once.
- There are no specific rules for enum values (DrawType) `NORMAL`or `JUNK`
## 3.4 Redrawing
The organizer can repeat a draw using the same unchanged list of participants.
The new draw creates a separate record and never overwrites previous runs.
## 3.5 Storage and History
The app permanently stores all completed draws.
The organizer can:
- view all previous draws
- select a saved draw
- view the assignments from that time
- view the wishlists that were valid at that time
Historical results must not be falsified by subsequent changes to current participants or wishlists.
A snapshot model is intended for this purpose.
Historical participant names and wishlist entries must be captured at the time of the draw.
Current participant data and historical draw data must be cleanly separated from one another.



