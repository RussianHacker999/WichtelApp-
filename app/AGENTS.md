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



