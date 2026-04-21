# ExpenseTracker Coding Guidelines

## Architecture Principles

### 1. Clean Architecture Layers
- **Data Layer**: Room database, DataStore, repositories (implementation)
- **Domain Layer**: Repository interfaces, use cases, business logic
- **UI Layer**: ViewModels, Composable screens, components

### 2. Dependency Injection
- **ALWAYS** use Hilt for dependency injection
- **NEVER** use `ExpenseTrackerRepositoryProvider` or manual instantiation
- ViewModels must use `@HiltViewModel` with constructor injection
- Repositories must be provided via Hilt modules

### 3. Repository Pattern
- Define repository interfaces in `domain/repository/` package
- Implement repositories in `data/repository/` package
- Repository interfaces should expose suspend functions and Flows
- Concrete repositories must implement domain interfaces

## Code Structure Rules

### 1. Package Organization
```
com.mkn0079.expensetracker/
├── data/                    # Data layer
│   ├── local/              # Local data sources
│   ├── repository/         # Repository implementations
│   └── constants/          # Constants and data classes
├── domain/                 # Domain layer
│   ├── repository/         # Repository interfaces
│   ├── usecase/           # Use cases
│   └── models/            # Domain models
├── ui/                     # UI layer
│   ├── screens/           # Composable screens
│   ├── viewmodels/        # ViewModels
│   ├── components/        # Reusable UI components
│   └── theme/             # Theme and styling
└── di/                    # Dependency injection modules
```

### 2. ViewModel Guidelines
```kotlin
// ✅ CORRECT
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {
    // State management with StateFlow
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
    
    // Use coroutine scope
    fun performAction() = viewModelScope.launch {
        // Business logic
    }
}

// ❌ WRONG - No constructor injection
class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExpenseTrackerRepositoryProvider.myRepository(application)
}
```

### 3. Repository Interface Pattern
```kotlin
// domain/repository/MyRepository.kt
interface MyRepository {
    suspend fun getData(): Result<Data>
    fun observeData(): Flow<List<Data>>
}

// data/repository/MyRepository.kt
class MyRepositoryImpl @Inject constructor(
    private val dao: MyDao,
    private val dataStore: DataStore<Preferences>
) : MyRepository {
    override suspend fun getData(): Result<Data> {
        // Implementation
    }
}
```

### 4. Hilt Module Setup
```kotlin
// di/MyModule.kt
@Module
@InstallIn(SingletonComponent::class)
object MyModule {
    @Provides
    @Singleton
    fun provideMyRepository(
        dao: MyDao,
        dataStore: DataStore<Preferences>
    ): MyRepository = MyRepositoryImpl(dao, dataStore)
}
```

## Testing Requirements

### 1. Unit Tests
- Test ViewModels with mocked dependencies
- Test use cases with mocked repositories
- Test utility functions

### 2. Integration Tests
- Test repository implementations with in-memory database
- Test DAO queries

### 3. UI Tests
- Test Composable screens with `@ComposeTestRule`
- Test navigation flows

## Code Quality Standards

### 1. Kotlin Conventions
- Use `val` for immutable variables
- Use data classes for state models
- Use sealed classes/interface for result types
- Prefer extension functions over utility classes

### 2. Coroutine Usage
- Use `viewModelScope` in ViewModels
- Use `Dispatchers.IO` for database operations
- Use `Dispatchers.Default` for CPU-intensive work
- Use `Dispatchers.Main` for UI updates

### 3. Error Handling
- Use `Result<T>` or sealed classes for operations
- Handle exceptions in repository layer
- Show user-friendly error messages in UI

### 4. State Management
- Use `StateFlow` for ViewModel state
- Use `MutableState` for Composable local state
- Keep UI state immutable

## Build Configuration

### 1. Dependencies
- Add new dependencies to `gradle/libs.versions.toml`
- Use version catalog references
- Group dependencies logically

### 2. Gradle Configuration
- Use KSP for Room and Hilt
- Configure proguard rules for release builds
- Enable compose compiler reports

## Navigation Guidelines

### 1. Navigation Structure
- Use single-activity architecture
- Define navigation graphs in `MainScreen.kt`
- Use type-safe navigation arguments

### 2. Screen Parameters
- Pass minimal data between screens
- Use ViewModel for screen-specific state
- Handle configuration changes properly

## UI/UX Standards

### 1. Composable Functions
- Use `@Composable` annotation
- Follow Material Design 3 guidelines
- Use theme colors and typography

### 2. Responsive Design
- Support different screen sizes
- Handle orientation changes
- Use adaptive layouts

### 3. Accessibility
- Add content descriptions
- Support screen readers
- Ensure proper contrast ratios

## Security Guidelines

### 1. Data Storage
- Use encrypted DataStore for sensitive data
- Use Room database with proper migrations
- Never store passwords in plain text

### 2. Network Security
- Use HTTPS for API calls
- Validate input data
- Sanitize user inputs

## Performance Guidelines

### 1. Database Optimization
- Use Room efficiently
- Implement proper indexing
- Use pagination for large datasets

### 2. UI Performance
- Use `LazyColumn` for lists
- Avoid recompositions
- Use `remember` for expensive calculations

### 3. Memory Management
- Close resources properly
- Use weak references where appropriate
- Monitor memory leaks

## Documentation Requirements

### 1. Code Documentation
- Document public APIs with KDoc
- Explain complex business logic
- Add TODO comments for future improvements

### 2. Architecture Documentation
- Update this guidelines document
- Document design decisions
- Maintain architecture diagrams

## Git Workflow

### 1. Commit Messages
- Use conventional commits
- Reference issue numbers
- Write descriptive messages

### 2. Code Review
- Follow these guidelines in reviews
- Request changes for violations
- Ensure tests are included

## When Adding New Features

### 1. Analysis Phase
- Analyze existing architecture
- Identify where the feature fits
- Plan dependencies and interfaces

### 2. Implementation Phase
1. Create domain interfaces
2. Implement data layer
3. Create Hilt modules
4. Implement ViewModel
5. Create UI components
6. Add tests

### 3. Testing Phase
1. Unit tests for business logic
2. Integration tests for data layer
3. UI tests for screens
4. Manual testing

## Common Pitfalls to Avoid

### ❌ DO NOT
- Create ViewModels without `@HiltViewModel`
- Use `ExpenseTrackerRepositoryProvider`
- Hardcode dependencies
- Mix business logic in UI layer
- Ignore error handling
- Skip tests

### ✅ DO
- Follow Clean Architecture
- Use dependency injection
- Write tests
- Handle errors gracefully
- Document your code
- Follow Kotlin conventions

## Template for New Feature Request

When requesting AI assistance for new features, include:

```
## Feature Request Template

### Feature Description
[Brief description of the feature]

### Architecture Impact
- Which layer(s) are affected?
- New dependencies needed?
- Changes to existing code?

### Implementation Steps
1. [Step 1]
2. [Step 2]
3. [Step 3]

### Testing Requirements
- [ ] Unit tests
- [ ] Integration tests
- [ ] UI tests

### Dependencies
- [ ] Add to libs.versions.toml
- [ ] Update build.gradle.kts

### Files to Create/Modify
- [ ] File 1
- [ ] File 2
- [ ] File 3
```

---

