# Jetpack Compose Architecture Refactor Prompt

Analyze the entire Android Jetpack Compose project and refactor every
screen that mixes ViewModel logic with UI rendering.

## Goal

Adopt the recommended Jetpack Compose architecture by separating:

1.  Dependency injection (Hilt)
2.  State collection (ViewModels, Flows)
3.  Pure UI rendering

The final result should make every screen previewable without Hilt and
improve maintainability.

------------------------------------------------------------------------

## What to Look For

Search the entire project for composables that:

-   use `hiltViewModel()`
-   use `viewModel()`
-   call `collectAsState()`
-   call `collectAsStateWithLifecycle()`
-   observe `StateFlow` or `Flow`
-   own business logic and UI in the same composable
-   cannot be previewed because of ViewModel dependencies

Example:

``` kotlin
val viewModel: HomeViewModel = hiltViewModel()
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

These composables should be treated as **Route composables**.

------------------------------------------------------------------------

## Refactor Pattern

Convert every affected screen into two composables.

**Before**

``` text
HomeScreen()
```

**After**

``` text
HomeScreen()          // Route
HomeScreenContent()   // Pure UI
```

------------------------------------------------------------------------

## Route Composable

The Route composable should ONLY:

-   obtain ViewModels
-   collect StateFlows
-   call `updateInputs()`
-   perform `LaunchedEffect` for initialization
-   connect callbacks
-   pass state into the UI composable

Example:

``` kotlin
@Composable
fun HomeScreen() {

    val viewModel: HomeViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        ...
    )
}
```

The Route composable should **not** contain UI rendering except invoking
the Content composable.

------------------------------------------------------------------------

## Content Composable

Move **all UI rendering** into:

``` text
ScreenContent()
```

The Content composable must:

-   NOT call `hiltViewModel()`
-   NOT call `viewModel()`
-   NOT call `collectAsState()`
-   NOT call `collectAsStateWithLifecycle()`
-   NOT access repositories
-   NOT access ViewModels
-   NOT perform dependency injection

Everything must come through parameters.

`rememberCoroutineScope()` is acceptable only for purely UI-related work
(animations, bottom sheets, etc.).

------------------------------------------------------------------------

## Callbacks

Replace direct ViewModel calls with callbacks.

Instead of:

``` kotlin
viewModel.delete()
```

Use:

``` kotlin
onDelete = {}
```

Instead of:

``` kotlin
viewModel.updateSearchQuery()
```

Use:

``` kotlin
onSearchQueryChange = {}
```

Instead of:

``` kotlin
viewModel.navigate()
```

Use:

``` kotlin
onNavigate = {}
```

The UI should never know that a ViewModel exists.

------------------------------------------------------------------------

## Preview Support

Every Content composable should have Preview support.

If fake data is required, create before asking user:

``` text
PreviewData.kt
```

or

``` kotlin
object PreviewData
```

containing realistic fake UI state.

Example:

``` kotlin
object PreviewData {
    val homeUiState = ...
}
```

Then:

``` kotlin
@Preview
@Composable
private fun HomeScreenPreview() {

    AppTheme {

        HomeScreenContent(
            uiState = PreviewData.homeUiState,
            ...
        )
    }
}
```

The preview must compile **without Hilt**.

------------------------------------------------------------------------

## Do Not Change

Do **not** change:

-   Business logic
-   Repositories
-   Database
-   Navigation
-   Flow logic
-   Sorting logic
-   Filtering logic
-   Animations
-   Existing behavior

Only reorganize the architecture.

------------------------------------------------------------------------

## Verification Checklist

After refactoring, verify:

-   Every screen behaves exactly the same.
-   No UI regressions.
-   No broken callbacks.
-   No business logic moved into UI.
-   All previews compile.
-   No `hiltViewModel()` exists inside previewable UI composables.
-   The architecture consistently follows the Route + Content pattern.

------------------------------------------------------------------------

## Deliverables

For every modified screen provide:

1.  File name
2.  What changed
3.  Why it changed
4.  Any new files created
5.  Any callbacks introduced
6.  Any previews added
7.  Confirmation that behavior is unchanged

If a screen already follows this architecture, leave it unchanged and
mention that no refactoring was necessary.
