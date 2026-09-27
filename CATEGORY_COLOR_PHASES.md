# Category & payment colour system — phase-wise implementation plan

Derived from `CATEGORY_COLOR_SYSTEM_PLAN.md`. That document is the *what* and *why*; this
one is the *order of work*. Its five decisions (§0) are unchanged.

**Ordering principle:** invisible first, data second, pixels third. Each phase compiles,
ships and is independently revertible, so pausing after any phase leaves a coherent app
with nothing half-applied.

## At a glance

| Phase | Deliverable | User-visible | Build needed | Bump |
|---|---|---|---|---|
| 0 | Write this plan to `CATEGORY_COLOR_PHASES.md` | no | no | none |
| 1 | Palette + resolver + pure helpers | no | unit tests | patch |
| 2 | `color_hex` column, migration 17, sync, backup | no | compile + androidTest | patch |
| 3 | Colour on transaction rows | **yes** | compile + unit tests | minor |
| 4 | The colour picker | **yes** | compile | minor |
| 5 | Choosers, settings, filter modal | **yes** | none (UI-only) | minor |
| 6 | Analytics identity colours | **yes** | compile | minor |
| 7 | Recolour seeded items | **yes** | compile + androidTest | minor |

---

## Phase 0 — Land this plan

**Goal:** the phase plan exists in the repo next to its parent.

- Create `CATEGORY_COLOR_PHASES.md` at the repo root, beside `CATEGORY_COLOR_SYSTEM_PLAN.md`.
- No code, no version bump, no commit on its own — it rides with Phase 1.

---

## Phase 1 — Palette and resolver (invisible)

**Goal:** the 66 colours exist, keyed by id, resolvable for the active theme — with nothing
reading them yet. App behaviour is byte-for-byte identical.

**Parent plan:** §2, §4, §12.

**New file `core/ui/theme/CategoryPalette.kt`:**

- All 66 literals from §12, `internal`, named exactly as supplied.
- `CategoryAccentLight` / `CategoryAccentDark` (ids 1–23 + 101–105), `PaymentAccentLight` /
  `PaymentAccentDark` (ids 1–6).
- `val ColorScheme.categoryAccent` / `paymentAccent` — `get() = if (isDark) …Dark else …Light`.
- `val ColorScheme.categoryAccentFallback` = the brand ink.
- `val ColorScheme.categorySoft(…)` — the 10% light / 14% dark wash, mirroring `accentSoft`.
- A file comment stating that this palette renders **verbatim** and must not be run through
  the contrast adapter (§0 decision 3).

**New file(s) for the pure helpers** (Compose-free, so they unit-test without a rule):

- `parseHexColorOrNull(hex: String?): Color?` — `#RRGGBB` / `#AARRGGBB`.
- `adaptForContrast(color, background, minRatio): Color` — returns the colour untouched if
  it already clears the ratio, else steps it toward that theme's ink until it does. Wired
  to picked colours only.

**Tasks**

1. Author the 66 literals and the four maps.
2. Add the four `ColorScheme` roles.
3. Add the two pure helpers.
4. Tests: `CategoryPaletteTest` (every `categoryMap` and `paymentTypeMap` id present in both
   themes; no extra keys; light and dark key sets identical; hexes pinned), `HexColorTest`
   (valid, null, blank, missing `#`, non-hex, wrong length), `ContrastTest` (every entry
   ≥3:1 on its theme's card; the amber/emerald tile case recorded as a documented exception,
   not silently skipped; and the full swatch set swept through **both** backgrounds through
   the adapter and proven to clear the ratio).

**Exit criteria:** `:app:testDebugUnitTest` green; no call site changed; zero diff in any
rendered screen.

**Commit:** `refactor(ui): category and payment colour palette` + patch bump + README sync.

---

## Phase 2 — Storage (invisible)

**Goal:** a colour can be persisted, synced and restored, for rows that will never have one
yet.

**Parent plan:** §3, §6.

**Storage shape** (per §3): `colorHex: String? = null`, `@ColumnInfo(name = "color_hex")`,
on `CategoryEntity` and `PaymentMethodEntity`. Nullable with no default so no row is
rewritten. Canonical `"#RRGGBB"`, no alpha.

| Concern | File |
|---|---|
| Schema | `CategoryEntity.kt`, `PaymentMethodEntity.kt` |
| Migration | `ExpenseTrackerDatabase.kt` — 16 → 17, two `ALTER TABLE ADD COLUMN`, added to `addMigrations` |
| Generated schema | commit `app/schemas/…/17.json` |
| Mapper | `RoomMappers.kt` — both directions, both entities |
| Domain | `CategoryType.kt`, `PaymentType.kt` |
| Cloud push | `SyncRepositoryImpl` — `CategoryTask` / `PaymentMethodTask` `toCloudMap()` |
| Cloud pull | `SyncRepositoryImpl` — **verify**, don't assume |
| Backup | `DataManagementRepository` — export and import, as `Goal.colorHex` does |
| Create path | `CategoryRepository`, `PaymentMethodRepository`, `MainViewModel` |

**Two traps to close in this phase, not later**

1. **Create-after-delete.** Both repositories reactivate a soft-deleted row with
   `deleted.copy(iconKey = iconKey, …)`. Everything unnamed is carried over, so a recreated
   category would silently inherit the deleted one's colour and discard the newly picked
   one. Add `colorHex = colorHex` to both copies. To make this atomic, Phase 2 also
   introduces the `colorHex: String? = null` parameter on `createCustomCategory` /
   `createCustomPaymentMethod`, threaded with a `null` default — so the parameter and its fix
   land in one commit and the picker merely starts passing a value in Phase 4.
2. **Confirm the cloud read path.** ✅ Confirmed, and the assumption was wrong: hopes that
   categories rode the generic `doc.toObject(Entity::class.java)` path were unfounded. Both
   `CategoryEntity` and `PaymentMethodEntity` have hand-rolled branches in the pull `when`
   that name every field, so both now read `colorHex` explicitly. Left unchecked this would
   have cost the colour on whichever device synced, with nothing wrong in either database.

**Tests:** `Migration17Test` modelled on `Migration14To15Test` — rows survive with
`color_hex` null and every other field intact. A create → delete → recreate cycle asserting
the **second** creation's colour wins.

**Exit criteria:** `:app:compileDebugKotlin` green; migration test green on the emulator; app
launches with an unchanged UI.

**Commit:** `feat(data): persist an optional category and payment colour` + patch bump +
README sync.

---

## Phase 3 — First visible change: transaction rows

**Goal:** every transaction row draws its category's own colour. With no picker yet, this is
purely the palette — a complete, shippable visual change on its own.

**Parent plan:** §7 stage 1.

**The key structural point:** `TransactionPresentationMapper.toTransactionCardItemUi` is the
only place that already holds both the category row and the transaction — it resolves
`category?.icon ?: categoryIcon`. The colour must ride that same path, so
`TransactionCardItemUi` gains the colour alongside its existing `icon` field, resolved from
`category?.colorHex`. Otherwise `TransactionCard` has the id but not the override.

**Tasks**

1. Add the colour to `TransactionCardItemUi`, resolved in `toTransactionCardItemUi`.
2. `TransactionCard` — glyph and wash from the resolver.
3. `AppIconBox` call sites for category glyphs.
4. Resolver call shape everywhere: `colorScheme.categoryAccent[id]` → override →
   `categoryAccentFallback`, never a bare map lookup.

**Exit criteria:** compile green; `TransactionPresentationMapper` unit tests still green;
identical behaviour in dark and light, and under the in-app theme toggle (not just the system
setting).

**Commit:** `feat(ui): transactions draw their category colour` + minor bump + README sync.

### A constraint this phase surfaced, for phases 5 and 6

Several surfaces resolve a category from `categoryMap` — the **compile-time constant** — rather
than from the rows in the database. A constant's `colorHex` is always null, so those surfaces can
never see a user's colour, however correct the storage is. Recorded here so phase 5 and phase 6
do not "discover" it twice.

| Surface | Reads | Consequence |
|---|---|---|
| `AnalyticsScreen` donut, legend, top transactions | the rows, via the snapshot | fine — see the correction below |
| `CategoryManagementViewModel` built-in lists | `categoryMap.values` | harmless — built-ins are never recoloured by design |
| `SortFilterModal` chip lists | `categoryMap.values` | chips will show palette colours only |
| `AddTransactionScreen` / `BudgetAndRecurringScreen` default parameters | `categoryMap.values` | harmless — previews only; the real call sites pass database rows |

**Correction, made while implementing phase 6.** The analytics rows were listed here as reading the
constant. They are not: `buildAnalyticsSnapshot` opens with
`val categoryMap = categories.associateBy { it.id }`, a **local** that shadows the imported constant
of the same name for the whole function body. Everything below that line — the breakdown labels,
the `isOther` flags, the top-spending categories — was therefore already resolving against the
loaded rows, and the only thing missing for colour was carrying the row's `colorHex` through to the
donut. The table above was written from the call sites without reading the shadowing declaration,
which is exactly how a name that means two things in one file misleads a reader; the same local
naming appears for `paymentTypeMap`.

Where the constant *is* genuinely read — the chip lists and the built-in management cards — it is
harmless for the reason given: those rows are never recoloured, because the picker is scoped to
user-created items. The general rule still holds: pass the loaded rows to anything that draws a
user's own data, and read the constant only for a seeded list.

---

## Phase 4 — The colour picker

**Goal:** a user can choose a colour for a category or payment method they create.

**Parent plan:** §5.

- `categoryColorOptions` in `data/constants/`, sourced from the palette so a pick always stays
  in the design system.
- A swatch grid in `AddCategoryScreen` beside the existing icon grid, following its
  `categoryIconOptions` pattern.
- `selectedColorHex` in `AddCategoryViewModel`, reset per tab as `selectedIconId` already is.
- Threaded into the existing create calls.
- **Scope guard:** user-created items only. Seeded rows keep resolving from the palette, which
  is what keeps the initializer in §6 from ever fighting the user.

**Exit criteria:** compile green; a created category carries its picked colour and renders it;
a seeded category is unaffected.

**Commit:** `feat(ui): colour picker for custom categories and payment methods` + minor bump +
README sync.

---

## Phase 5 — Remaining surfaces (UI-only)

**Goal:** the same colour everywhere a category or payment is shown.

- Category and payment choosers in `AddTransactionScreen`.
- `SortFilterModal` chips.
- `CategoryManagementScreen` rows and the `AddCategoryScreen` preview.

No new logic, no Gradle run under the standing instruction, since this is purely presentation.

**What was decided while doing it**

1. **The identity colour stops at the glyph on chips and picker rows.** A transaction row washes
   its whole tile, because the tile *is* a category swatch. A chip's fill is the chip, so tinting
   it would dissolve the button into the row; the glyph carries the colour and the fill stays the
   surface it was. Recorded at both call sites, since the asymmetry looks like an oversight.
2. **A selected chip or picker row keeps the brand ink** — the violet fill, the `SELECTED` label
   and the checked box are that state's whole signal, and a category colour under them reads as a
   second, competing emphasis rather than a selection.
3. **The budget screens were included**, though the plan listed only the transaction choosers: a
   budget's category picker is a category chooser, and leaving it grey would have made the same
   list look like two different systems depending on which tab it was opened from.
4. **`CategoryManagementItemUi` gained `isPaymentMethod`.** It had to: payment ids restart at 1,
   so id 1 is Food *and* UPI, and an unflagged lookup returns a colour — merely the wrong one,
   which no screenshot review catches. Asserted in the test rather than assumed.

**Commit:** `feat(ui): carry category colours through the choosers and settings` + minor bump +
README sync.

---

## Phase 6 — Analytics identity colours

**Goal:** the donut and legend use each category's own colour instead of the positional ramp.

**Parent plan:** §8.

- `categoryBreakdownColor` / `paymentBreakdownColor` become identity lookups.
- ~~`seriesColor` survives only for the index-past-the-ramp case it already documents.~~
  **Superseded — it was deleted; see note 1 below.**
- A comment at the call site records the trade-off: the palette repeats hues deliberately
  (Travel/Insurance/Transport share `#0288D1`; Bills/Groceries/Donations share `#059669`), so
  two adjacent slices can now match — acceptable because every legend dot carries a text
  label, where `chartSeries`'s ordering existed to avoid exactly this.

**Exit criteria:** compile green; the light and dark donuts checked on a device before
committing.

**What was decided while doing it**

1. **`seriesColor` was deleted, not kept.** The plan said it survives for the index-past-the-ramp
   case, but once both breakdown colours are identity lookups nothing calls it, and an identity
   lookup cannot run out: a category id is either in the palette or it is not, and the not-in case
   takes the brand fallback ink. Keeping the helper as dead code would have left a second, silent
   colour rule in the file for the next reader to find. `chartSeries` / `chartOther` stay in
   `Color.kt` as spec tokens the parity test pins, and `chartOther`'s KDoc now says the donut no
   longer reaches it.
2. **`colorIndex` was removed from both breakdown models.** It existed only to index the ramp, so
   leaving it would have been a field that means nothing.
3. **The device check is a test, not an eyeball.** `AnalyticsDonutColorRenderTest` renders the real
   donut through the real theme and reads the pixels back: Health's dark `#FF5C5C` for a seeded
   slice, a picked `#9333EA` for a stored one (against an id the palette has never heard of, so
   agreement with a palette entry cannot pass it), payment 5's `#4B5563` with category 5's
   `#DC2626` asserted *absent*, and one negative case that the stored colour does not also leave
   the slice in the palette tone. That last pair is what a presence-only test would have missed.
   `SpendingDonutChart` / `PaymentDonutChart` became `internal` to make that possible; the whole
   screen could not host it, because it composes a gated action that needs a Hilt container.

**Commit:** `feat(ui): the analytics donut uses category identity colours` + minor bump +
README sync.

---

## Phase 7 — Recolouring seeded items (implemented)

**Goal:** a colour the user puts on a built-in category or payment method stays there, across the
restart that used to wipe it.

**What was built**

1. **The seeder carries the colour over.** `initialize` reads `getStoredColors()` — a projection of
   `id, color_hex` — and copies the stored value onto each row before `upsertAll`. Chosen over
   "seed only rows that are absent" because rewriting seeded rows is what lets a release correct a
   name or add a category, and skipping existing rows would have traded that away for a bug fix it
   does not need. Null-preserving, so a row with no override keeps resolving from the palette.
2. **An update path for the colour alone:** `updateColorHex` on both DAOs (a targeted `UPDATE`,
   with **no** `is_system = 0` guard — that stays on `softDelete`, which is the distinction the
   phase turns on), the two repository methods, and `CategoryManagementViewModel.updateColor`.
3. **A recolour sheet**, opened by tapping any card, using the same `CategoryColorRow` the create
   screen uses.
4. **The grid now reads the rows for its built-in cards too.** This was the part the plan did not
   anticipate: the built-ins came from the `categoryMap` constant, which is keyed by id and can
   never carry a value, so a recoloured built-in would have drawn as uncoloured until the screen was
   reopened. Both lists now come from `observeActiveCategories()` / `observeActivePaymentMethods()`
   and split on `isSystem` — the same flag the delete guard uses, so the 'x' a card draws and the
   delete the database permits cannot disagree.

**The exit criterion is a test, and it was checked negatively.** `CategoryColorReseedTest` performs
seed → recolour → seed against one store, which is the sequence two launches perform; it also adds
one assertion. A recolour must mark the row `PENDING_UPLOAD` and put it in the unsynced set, because
seeded rows are written with that state on every launch: without the carry-over, a *second* device's
startup would push `color_hex = null` for every seeded row and erase the first device's choice from
the cloud. The restart test was also run with the carry-over removed, and it fails — so it is a
regression test rather than a restatement of the code.

**Deliberately still out of scope:** renaming or re-iconing a seeded row, and deleting one. The
colour is the only field of a built-in the user owns; the rest is the app's, which is what the
`is_system` guards on those paths still say.

**Commit:** `feat(ui): a built-in category can be recoloured and keeps it` + minor bump + README sync.

---

## Ground rules for every phase

- **`GEMINI.md`:** bump `versionName`, never `versionCode`, include the literal
  `Bumped version to <versionName>` line, sync the `README.md` badge and version row, no AI
  attribution.
- **Route/Content** stays intact — new state goes in the ViewModel, previews keep calling the
  pure Content composable.
- **No hardcoded colours in Composables** — every colour reaches a screen through the theme
  package or `MaterialTheme.colorScheme`.
- **No strings without `strings.xml`**, and font sizes stay token-derived.
- **Must not touch:** the amount figures (income/expense inks stay semantic), the CTA fills, or
  the `BRAND_AREA_BUDGET` accounting.
- **Commit hygiene:** one phase per commit, no broad staging, nothing committed with failing
  tests.
