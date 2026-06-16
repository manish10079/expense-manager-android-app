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
- **Kotlin Conventions:** Use `val` for immutable variables, data classes for state models, and sealed types for results.
- **Coroutines:** Use `viewModelScope` in ViewModels and `Dispatchers.IO` for database operations.
- **Theme Awareness:** All UI components MUST be strictly theme-aware. Use `MaterialTheme.colorScheme` for all colors and `MaterialTheme.typography` for all text styles. Components must respond seamlessly to Light/Dark mode transitions.

### 5. Internationalization (i18n)
- **Strings:** Never use hardcoded strings for user-facing UI text. All strings must be extracted to `app/src/main/res/values/strings.xml`.
- **Compose:** Use `stringResource(R.string.id)` for UI text in Composables.
- **ViewModels:** ViewModels should avoid resolving strings directly. Instead, expose the `@StringRes Int` resource ID or use a UI model that holds both a raw string (for user data) and a resource ID (for system labels/fallbacks).
- **Accessibility:** Always provide localized `contentDescription` for all icons and interactive elements.

### 6. Testing Requirements
- **ViewModels:** Unit tests with mocked repositories are mandatory.
- **Repositories:** Integration tests with in-memory Room database.
- **UI:** Compose tests for critical flows using `@ComposeTestRule`.

### 7. Performance & Security
- **Database:** Implement proper indexing and use pagination for large datasets.
- **UI:** Use `LazyColumn` for lists and `remember` for expensive calculations.
- **Security:** Use encrypted DataStore for sensitive data and never store credentials in plain text.

### 8. Versioning & Commits
- **Automated Updates:** Whenever a commit message is prepared or requested, the agent must update `app/build.gradle.kts`.
- **Version Code:** `versionCode` must always increment by **1**.
- **Version Name:** `versionName` must follow the format: `major.minor.bug.versioncode`.
- **Settings Version String:** Whenever the version is bumped in `app/build.gradle.kts`, the version string in `app/src/main/res/values/strings.xml` (specifically `label_version_100`) must be updated to match the new `versionName`.
- **Commit Mandatory Line:** Every commit message must explicitly include the phrase: `bumped version to <versionName>`.
- **Versioning Logic (SemVer):**
    - **Major:** Increment for breaking changes (e.g., `feat!`, `fix!`, or `BREAKING CHANGE` in footer). Reset minor and bug to 0.
    - **Minor:** Increment for new features (`feat`). Reset bug to 0.
    - **Bug:** Increment for bug fixes (`fix`).
    - **Other types (chore, refactor, docs, etc.):** Only increment `versionCode` and update the last segment of `versionName`.
- **Verification:** Ensure the updated version is reflected in the build file before finalizing the task.
- **Commit Message Generation:** Analyze changed and untracked files, and check previous commit messages to ensure the new message only covers fresh changes. Use bullet points (`-`) for descriptions; never use numbering.

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
- No hardcoded strings in UI or ViewModels (Mandatory use of `strings.xml`).
- No hardcoded colors in UI components (Mandatory use of `ui/theme/Color.kt` or `MaterialTheme.colorScheme`).
- No hardcoded hex values in Composables.
- No untested code or business logic in the UI layer.

---
**COPY THE ENTIRE CONTENT BELOW WHEN REQUESTING AI ASSISTANCE FOR EXPENSETRACKER**

# AI ASSISTANCE CONTEXT - ExpenseTracker
- **Architecture**: Hilt DI with Clean Architecture (Data, Domain, UI)
- **Primary Rules**: Mandatory i18n, `@HiltViewModel` injection, Repository pattern.
- **UI**: Jetpack Compose + Material 3.
- **Persistence**: Room (KSP) + DataStore.
- **Guidelines**: Follow `GEMINI.md` for full technical specifications.


  