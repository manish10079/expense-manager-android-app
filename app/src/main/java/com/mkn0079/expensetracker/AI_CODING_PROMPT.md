# AI CODING PROMPT - ExpenseTracker Android App

## STRICT ARCHITECTURE RULES

### 1. **CLEAN ARCHITECTURE MANDATORY**
- **Data Layer**: Room, DataStore, repository implementations
- **Domain Layer**: Repository interfaces, use cases, business logic  
- **UI Layer**: ViewModels, Composable screens, components

### 2. **HILT DEPENDENCY INJECTION REQUIRED**
- **ALWAYS** use `@HiltViewModel` for ViewModels with constructor injection
- **NEVER** use `ExpenseTrackerRepositoryProvider` or manual instantiation
- **ALWAYS** create Hilt modules for new dependencies
- Repository interfaces in `domain/repository/`, implementations in `data/repository/`

### 3. **REPOSITORY PATTERN**
```kotlin
// domain/repository/MyRepository.kt (INTERFACE)
interface MyRepository {
    suspend fun getData(): Result<Data>
    fun observeData(): Flow<List<Data>>
}

// data/repository/MyRepository.kt (IMPLEMENTATION)  
class MyRepositoryImpl @Inject constructor(
    private val dao: MyDao
) : MyRepository { /* implementation */ }

// di/MyModule.kt (HILT MODULE)
@Module @InstallIn(SingletonComponent::class)
object MyModule {
    @Provides @Singleton
    fun provideMyRepository(dao: MyDao): MyRepository = MyRepositoryImpl(dao)
}
```

### 4. **VIEWMODEL TEMPLATE**
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
    
    fun performAction() = viewModelScope.launch {
        // Business logic
    }
}
```

## FILE ORGANIZATION
```
com.mkn0079.expensetracker/
├── data/                    # Data layer implementations
├── domain/                  # Domain interfaces and business logic  
├── ui/                      # UI layer (ViewModels, screens, components)
└── di/                      # Hilt dependency injection modules
```

## BUILD CONFIGURATION
- Add dependencies to `gradle/libs.versions.toml`
- Use KSP for Room and Hilt compilation
- Follow existing `app/build.gradle.kts` patterns

## TESTING REQUIREMENTS
- **MUST** include unit tests for ViewModels
- **MUST** include integration tests for repositories  
- **SHOULD** include UI tests for screens
- Use mocked dependencies for unit tests

## CODE QUALITY
- Use Kotlin conventions (val, data classes, sealed classes)
- Handle errors with `Result<T>` or sealed result types
- Use coroutines properly (`viewModelScope`, appropriate dispatchers)
- Follow Material Design 3 for UI components

## WHEN ADDING NEW FEATURES
1. **Analyze**: Where does this fit in Clean Architecture?
2. **Design**: Create domain interfaces first
3. **Implement**: Data layer → Hilt modules → ViewModel → UI
4. **Test**: Unit tests → Integration tests → UI tests
5. **Verify**: Build and run the app

## ABSOLUTE PROHIBITIONS
- ❌ NO `ExpenseTrackerRepositoryProvider` usage
- ❌ NO ViewModels without `@HiltViewModel`
- ❌ NO business logic in UI layer
- ❌ NO manual dependency instantiation
- ❌ NO skipping error handling
- ❌ NO untested code

## REFERENCE
Full guidelines: `CODING_GUIDELINES.md`
Current architecture: Hilt DI with Clean Architecture
Build status: Fixing kapt configuration (in progress)

---

**COPY THIS ENTIRE PROMPT WHEN REQUESTING AI ASSISTANCE FOR EXPENSETRACKER**