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
| 7 | *Deferred:* recolour seeded items | — | — | — |

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

**Commit:** `feat(ui): carry category colours through the choosers and settings` + minor bump +
README sync.

---

## Phase 6 — Analytics identity colours

**Goal:** the donut and legend use each category's own colour instead of the positional ramp.

**Parent plan:** §8.

- `categoryBreakdownColor` / `paymentBreakdownColor` become identity lookups.
- `seriesColor` survives only for the index-past-the-ramp case it already documents.
- A comment at the call site records the trade-off: the palette repeats hues deliberately
  (Travel/Insurance/Transport share `#0288D1`; Bills/Groceries/Donations share `#059669`), so
  two adjacent slices can now match — acceptable because every legend dot carries a text
  label, where `chartSeries`'s ordering existed to avoid exactly this.

**Exit criteria:** compile green; the light and dark donuts checked on a device before
committing.

**Commit:** `feat(ui): the analytics donut uses category identity colours` + minor bump +
README sync.

---

## Phase 7 — Deferred: recolouring seeded items

Deliberately **out of this plan.** Allowing it requires `ExpenseTrackerDatabaseInitializer` to
stop clobbering, since it runs on every launch and `upsertAll` is replace-on-primary-key. The
least invasive form is to seed only absent rows (or merge the stored `color_hex` back over the
constant), with a test proving a recoloured seeded row survives a restart. Do not start this
unless the picker being limited to custom items proves to be a problem in use.

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
