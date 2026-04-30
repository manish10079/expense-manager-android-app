# ExpenseTracker Android App - Project Instructions

This project is a feature-rich personal finance management application built with modern Android development practices.

## 🏗️ Architecture & Frameworks

- **Architecture:** Clean Architecture with clearly defined layers:
  - **Data Layer:** Room (DB), DataStore (Preferences), and Repository implementations.
  - **Domain Layer:** Repository interfaces, Use Cases, and Business Logic.
  - **UI Layer:** ViewModels (State management) and Jetpack Compose (UI).
- **Dependency Injection:** Hilt (Dagger) is mandatory for all dependency management.
- **UI Framework:** Jetpack Compose with Material Design 3.
- **Database:** Room with KSP for schema generation and type safety.
- **Concurrency:** Kotlin Coroutines and Flow for reactive data streams.

## 📂 Project Structure

```
com.mkn0079.expensetracker/
├── data/                    # Data layer
│   ├── local/               # Room DB, DAOs, DataStore
│   ├── repository/          # Repository implementations
│   └── constants/           # Constants and shared data classes
├── domain/                  # Domain layer
│   ├── repository/          # Repository interfaces
│   ├── usecase/             # Use cases (optional but recommended for complex logic)
│   └── models/              # Domain/Business models
├── ui/                      # UI layer
│   ├── screens/             # Composable screens
│   ├── viewmodels/          # Hilt-injected ViewModels
│   ├── components/          # Reusable UI components
│   ├── theme/               # Material 3 Theme & Styling
│   └── navigation/          # Navigation graphs and state
├── di/                      # Hilt Modules
├── monetization/            # Ad-free access and feature registry logic
├── notifications/           # WorkManager and Notification management
└── utils/                   # Helper functions (Currency, Date, Money, etc.)
```

## 🛠️ Development Guidelines

### 1. Dependency Injection (Hilt)
- **ViewModels:** Must use `@HiltViewModel` and constructor injection.
- **Repositories:** Define interfaces in `domain/repository` and implementations in `data/repository`. Provide them via `@Provides` in `di/RepositoryModule.kt`.
- **DAOs/DataStores:** Provided via `di/DatabaseModule.kt` and `di/DataStoreModule.kt`.

### 2. UI & State Management
- Use `StateFlow` in ViewModels to expose UI state.
- Keep UI state immutable (use `data class` with `copy()`).
- Use `collectAsStateWithLifecycle()` in Composables to observe state safely.
- Follow the single-activity architecture; navigation is managed in `MainScreen.kt`.

### 3. Data & Persistence
- Use `Room` for structured data (Transactions, Categories, Budgets).
- Use `DataStore` for user preferences and app settings.
- Handle database operations on `Dispatchers.IO`.

### 4. Code Quality
- **Error Handling:** Use `Result<T>` or sealed classes for operations. Never skip error handling in repositories.
- **Testing:**
  - ViewModels: Unit tests with mocked repositories.
  - Repositories: Integration tests with in-memory Room database.
  - UI: Compose tests for critical flows.

## 🚀 Workflow for New Features

1. **Analyze:** Identify the impact across Data, Domain, and UI layers.
2. **Domain:** Define the repository interface and any necessary business models.
3. **Data:** Implement the DAO (if needed) and the repository. Register in Hilt modules.
4. **ViewModel:** Create a Hilt ViewModel to handle state and interact with the repository.
5. **UI:** Build the Composable screen and components. Integrate into `MainScreen.kt` navigation.
6. **Test:** Add unit and integration tests to verify the new functionality.

## ❌ Prohibitions
- No manual instantiation of repositories or ViewModels.
- No business logic or database calls directly in Composables.
- No usage of `ExpenseTrackerRepositoryProvider` (deprecated/forbidden).
- No skipping of `Result` wrapping for critical data operations.

---
*Refer to `CODING_GUIDELINES.md` and `AI_CODING_PROMPT.md` for more detailed technical specifications.*
