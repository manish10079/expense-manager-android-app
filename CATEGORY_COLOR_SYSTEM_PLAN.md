# Category & payment colour system — implementation plan

Status: proposal, not started. No code has been changed.

Goal: give income/expense categories and payment methods their own colour, light and
dark specific, from the palette supplied in the design pass — and let users pick a
colour for the categories and payment methods they create themselves.

---

## 0. Decisions taken

1. **Custom items get a colour picker.** The picker offers the supplied palette as its
   swatch set rather than a free colour wheel, so a pick always stays inside the design
   system and is already known to be legible.
2. **Analytics uses identity colours**, not the positional `chartSeries` ramp. The
   legend's text label is what keeps repeated hues distinguishable; see §8.
3. **The supplied hexes are kept verbatim.** The amber and emerald entries stay exactly
   as given even though they measure just under 3:1 on their own tinted tile; that is a
   recorded exception, not an oversight. See §9.
4. **A picked colour adapts per theme.** One hex is stored, and the renderer adjusts it
   for a theme it does not clear. See §4.
5. **The picker is scoped to user-created items**, which is what keeps the initializer in
   §6 from ever fighting the user.

---

## 1. What the codebase has today

**Colour is not data on categories or payments.** `CategoryType` and `PaymentType` carry
`iconKey` and nothing else about appearance. Colour is decided at draw time, and today
that is almost always brand purple: `AppIconBox`'s default is `tint = accentInk`, and the
same role is repeated at call sites in `TransactionCard`, the choosers in
`AddTransactionScreen`, `SortFilterModal`, `CategoryManagementScreen` and
`AnalyticsScreen`.

**The supplied palette keys exactly onto the app's existing stable IDs.** Verified against
`categoryTypeData.kt` and `paymentTypeData.kt`:

| Supplied name | id | | Supplied name | id |
|---|---|---|---|---|
| Food, Travel, Shopping, Bills, Health, Entertainment, Rent, Groceries, Education, Subscriptions, Insurance, Gifts, Personal Care, Fuel, Maintenance, Taxes, Pets, Childcare, Donations | 1–19 in order | | Salary | 101 |
| Miscellaneous | 20 | | Business | 102 |
| Transport | 22 | | Investment | 103 |
| OtherExpense | 23 | | Freelance | 104 |
| *(id 21 does not exist)* | | | OtherIncome | 105 |
| Upi, Cash, Bank, Card, Other, Salary | 1–6 | | | |

22 expense + 5 income + 6 payment = 33 entries per theme, 66 literals total. Nothing is
missing and nothing is extra.

**Custom IDs cannot collide with seeded ones.** `CategoryRepository.createCustomCategory`
and `PaymentMethodRepository.createCustomPaymentMethod` both use `dao.getMaxId() + 1`, so
user categories start at 106+ and user payment methods at 7+.

**There is already a persisted user colour on another entity.** `Goal.colorHex` is a
nullable-in-spirit `String` on `GoalEntity`, mapped in `RoomMappers`, exported and
imported by `DataManagementRepository`, and synced by `SyncRepositoryImpl.GoalTask`. It is
never rendered and there is no picker for it, but it is the exact precedent for this
work — schema column, mapper, backup, cloud field, all four already demonstrated.

---

## 2. The shape of the answer

Four layers, added in this order. Each one ships on its own.

1. **A palette, keyed by id, in the theme package** — `id → Color`, once per theme.
2. **A stored override** — an optional `color_hex` on categories and payment methods,
   set by a picker the user drives.
3. **A resolver** — one function that turns `(id, colorHex?)` into a `Color` for the
   active theme.
4. **Call sites** — the glyph and its soft wash, rolled out in stages.

The key decision that keeps this cheap: **the palette is derived from the id, and the
stored override is `null` for every seeded row.** That means no backfill, no data
migration of values, and the built-in palette stays the single source of truth for the
27 seeded categories and 6 seeded payment methods — while the picker only ever writes a
value for the user's own creations.

### Why the palette does not go in `Color.kt`

`Color.kt`'s stated identity is "the mock's own values, diffable token for token", and
`TokenParityTest` exists to fail if that file drifts from `mock-design-system.html`. This
palette is app content colour, from a different design pass. It belongs in a new file so
the one file that mirrors the spec stays diffable.

### New file: `core/ui/theme/CategoryPalette.kt`

```kotlin
// 66 literals, all internal, named exactly as supplied (FoodLight, UpiDark, ...).
internal val CategoryAccentLight: Map<Int, Color>   // ids 1..23 + 101..105
internal val CategoryAccentDark:  Map<Int, Color>
internal val PaymentAccentLight:  Map<Int, Color>   // ids 1..6
internal val PaymentAccentDark:   Map<Int, Color>

val ColorScheme.categoryAccent: Map<Int, Color>
    get() = if (isDark) CategoryAccentDark else CategoryAccentLight
val ColorScheme.paymentAccent: Map<Int, Color>
    get() = if (isDark) PaymentAccentDark else PaymentAccentLight
val ColorScheme.categoryAccentFallback: Color   // the brand accentInk
```

Three reasons for this shape:

- **`isDark` and not `LocalConfiguration`.** `isDark` derives from the active scheme, so it
  follows the user's in-app theme toggle, not the system setting. Income, expense and
  hairline already behave this way in `Theme.kt`, and a category must not be the one
  colour that ignores the toggle.
- **Prebuilt maps, not a `when (id)`.** Resolved once at class load, constant-time lookup,
  zero per-frame work, and a test can iterate the entries instead of enumerating branches.
- **`Map<Int, Color>` cannot half-ship.** A test can assert the light and dark key sets are
  identical, which no `when` can be made to guarantee.

Naming: all 66 names were checked against `Color.kt` and `Gradient.kt` — none collide.

### The soft wash

The tile behind the glyph should be the glyph at 10% light / 14% dark, which is the split
`accentSoft` was already tuned to ("14% is a wash on black and a stain on white"). A
companion `ColorScheme.categorySoft(...)` mirroring `accentSoft` keeps that pairing in one
place.

---

## 3. Storing the user's choice

### Schema

`CategoryEntity` and `PaymentMethodEntity` each gain:

```kotlin
@ColumnInfo(name = "color_hex")
val colorHex: String? = null
```

Nullable with no default, so no backfill and no row rewrite. `ExpenseTrackerDatabase`
goes 16 → 17 with a `MIGRATION_16_17` that is two `ALTER TABLE ... ADD COLUMN`
statements — the same O(1), additive shape as `MIGRATION_14_15`, and it belongs in
`androidTest` alongside `Migration14To15Test`.

`exportSchema = true`, so `app/schemas/.../17.json` is generated and must be committed.

### The rest of the plumbing, per column

| Concern | File |
|---|---|
| Entity | `CategoryEntity.kt`, `PaymentMethodEntity.kt` |
| Mapper | `RoomMappers.kt` — `toDomain`/`toEntity` for both |
| Domain model | `CategoryType.kt`, `PaymentType.kt` — `colorHex: String? = null` |
| Cloud push | `SyncRepositoryImpl` — `CategoryTask.toCloudMap`, `PaymentMethodTask.toCloudMap` |
| Cloud pull | `SyncRepositoryImpl` — the category/payment read path |
| Backup | `DataManagementRepository` — export and import, exactly as `Goal.colorHex` does |

**Resolved during implementation — the assumption above was wrong.** This section first
guessed that categories deserialise through `doc.toObject(Entity::class.java)` and would
carry a new field automatically. Reading `pullCollection` showed otherwise: `CategoryEntity`
and `PaymentMethodEntity` each have a **hand-rolled branch** in the pull `when`, naming every
field explicitly. A new field is simply absent from a pulled row unless the branch is
taught to read it.

Had it not been checked, the failure would have been the worst kind to diagnose: the colour
would save, draw, and survive a restore, then vanish on whichever device synced — a bug
report that says "my colours reset sometimes" with nothing wrong in the database on either
side. Both branches now read `colorHex` explicitly, with a comment saying why the generic
path is not relied on.

---

## 4. The resolver

`colorHex` is a string, so something has to turn it into a `Color`. There is no helper for
this anywhere in the repo today — precisely because `Goal.colorHex` is stored but never
drawn.

Put the parse in the theme package as a pure, Compose-free function:

```kotlin
fun parseHexColorOrNull(hex: String?): Color?   // "#RRGGBB" / "#AARRGGBB", else null
```

Then the resolver is a pure fallback chain, which makes it unit-testable with no
Compose rule:

1. `parseHexColorOrNull(storedHex)` — the user's pick, if it parses
2. `palette[id]` — the built-in, for seeded rows
3. `categoryAccentFallback` — the brand ink

Every step is tolerant. A malformed or blank value degrades to the palette rather than
drawing nothing, and a row the palette has never heard of still gets a colour.

### Adapting a picked colour to the theme it was not chosen for

A picked colour is one hex but the palette is two, so a pick made in light mode would
otherwise be drawn verbatim on the near-black field, where a deep tone loses most of its
contrast (a `#059669` pick measures around 2.5:1 on `#141418`). The renderer therefore
adapts a stored pick rather than trusting it blindly:

```kotlin
fun adaptForContrast(color: Color, background: Color, minRatio: Float): Color
```

Pure, Compose-free, and deterministic: if the colour already clears `minRatio` against
`background`, it is returned untouched; otherwise it is stepped toward that theme's
`onSurface` in fixed increments until it does, or until it reaches the ink, which is the
terminal case and always clears the ratio by construction. Because it takes the
background and the ratio as arguments rather than reading `MaterialTheme`, it works for
the card and for the tinted tile alike, and its monotonicity — contrast only ever rises
— is something a test can assert directly.

**This applies to picked colours only.** The supplied palette renders verbatim in both
themes, which is what decision 3 in §0 means. Running the palette through the adapter
would silently change the design that was signed off, so it must not be wired in there;
a comment at the palette should say so.

---

## 5. The picker

Searched for an existing colour picker: **there is none.** `Goal.colorHex` is only ever
initialised to `DEFAULT_GOAL_COLOR_HEX` or written by a test; nothing lets a user choose
one, and nothing draws one. So this is net-new UI, though it has an obvious template —
`AddCategoryScreen` already renders the icon picker as a grid over
`categoryIconOptions`.

Plan: a parallel `categoryColorOptions` list in `data/constants/`, a swatch grid beside
the icon grid, a `selectedColorHex` in `AddCategoryViewModel`, and the value threaded
through the existing `createCustomCategory` / `createCustomPaymentMethod` calls.

The swatch set should be **the supplied palette** rather than a free colour wheel. That
keeps the picker's output inside the design system, gives a curated set that is already
known to clear contrast, and avoids a user choosing `#FFFFFF`. A picked swatch stores one
hex; the renderer adapts it per theme as described in §4.

### Recolouring an existing category

There is no edit path today — categories support create and delete only
(`CategoryDao` has `softDelete` guarded by `is_system = 0`, and no update). `@Upsert`
means adding one is small, but see the landmine below before allowing it on seeded rows.

---

## 6. The landmine: the initializer rewrites seeded rows on every launch

`SplashViewModel` calls `ExpenseTrackerDatabaseInitializer.initialize(context)` **on every
app start**, and that does:

```kotlin
database.categoryDao().upsertAll(categoryMap.values.map { it.toEntity() })
database.paymentMethodDao().upsertAll(paymentTypeMap.values.map { it.toEntity() })
```

`@Upsert` is replace-on-primary-key, and `toEntity()` builds a fresh row from the
constant. Today this is harmless because the constants carry nothing user-editable. The
moment `colorHex` exists on those rows, **every app launch resets any colour the user put
on a seeded category or payment method.**

This drives a design choice, and it should be an explicit one:

- **Recommended: the picker is for user-created items only.** Seeded categories and
  payment methods always resolve from the palette, so the built-in palette is the single
  source of truth and the initializer can never fight the user. This also matches the
  screen's existing behaviour, where only custom rows can be deleted.
- **Alternative: allow recolouring built-ins too**, in which case the initializer must
  stop clobbering. The least invasive form is to seed only rows that are absent (or to
  merge the stored `color_hex` back over the constant before upserting), with a test that
  proves a recoloured built-in survives a restart.

### A second path with the same failure: create-after-delete reactivates the old row

Both `CategoryRepository.createCustomCategory` and
`PaymentMethodRepository.createCustomPaymentMethod` handle a name collision by
**reactivating the soft-deleted row** rather than inserting a new one:

```kotlin
val deleted = dao.findDeletedByNameAndType(name, transactionTypeId)
if (deleted != null) {
    dao.upsert(
        deleted.copy(
            iconKey = iconKey,          // the new icon is taken
            isDeleted = false,
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )
    )
    return@withContext
}
```

`deleted.copy(...)` carries every field it does not name — so once `colorHex` exists on
the entity, a recreated category silently inherits **the deleted one's colour**, and the
colour the user just picked in the picker is discarded. The icon does not have this
problem, because `iconKey` is named in the copy; the colour will not be, unless it is
added there deliberately.

It has to be fixed in the same change that adds the parameter, not after: the symptom is
"the picker works, except sometimes", which is the hardest kind of report to act on. The
fix is one line in each repository (`colorHex = colorHex`), and the test is a create →
delete → recreate cycle asserting the second creation's colour wins.

---

## 7. Rollout to call sites

Glyph and wash only. One commit per stage.

1. **Primitives** — `AppIconBox` call sites and `TransactionCard`. This alone repaints
   every transaction row, and with it home, calendar and the SMS inbox.
2. **Choosers** — the category and payment chips in `AddTransactionScreen`, and
   `SortFilterModal`.
3. **Settings** — `CategoryManagementScreen` and `AddCategoryScreen` (alongside the new
   picker).
4. **Analytics** — separate, because it is a different problem; see below.

**Must not touch:** the amount figures, which stay on the semantic income/expense inks — a
category colour must never repaint the one number a row exists to show. Also the CTA
fills, and the `BRAND_AREA_BUDGET` accounting: scoping the new family to a ~40dp tile keeps
it near 2% of a screen.

---

## 8. Analytics: identity colours instead of the positional ramp

Decided: the donut and legend move from `seriesColor(index)` to each category's own
colour. That is a small change at `AnalyticsScreen.kt` — `categoryBreakdownColor` and
`paymentBreakdownColor` become identity lookups, and `seriesColor` survives only for the
index-past-the-end case it already documents.

One trade-off has to be recorded rather than discovered later. `chartSeries`'s KDoc says
the ramp is ordered so no two *adjacent* slices are confusable, and that a recycled hue
would leave the legend unable to say which slice is which. The supplied palette repeats
hues deliberately — Travel, Insurance and Transport are all `#0288D1` in light, Bills,
Groceries and Donations are all `#059669` — so two adjacent slices can now be identical.

This is acceptable because the legend carries a text label beside every dot, so the
identity of a slice is recoverable from the label even when two dots match. It should be
stated in a comment at the call site the way the rest of this codebase records its
compromises, and the "index past the ramp" neutral should stay.

---

## 9. Test plan

- **`CategoryPaletteTest`** — every id in `categoryMap` has an entry in both themes; every
  id in `paymentTypeMap` likewise; no extra keys; light and dark key sets are identical;
  each entry's hex pinned. The colour analogue of `CategoryIconCatalogTest`, which already
  pins that every key has a renderable icon.
- **Contrast** — WCAG contrast for each entry against its theme's card. Note the exception
  that has to be recorded: on the light theme the amber `#D97706` entries
  (Shopping/Gifts/Pets) measure **3.19:1** on white but **2.89:1** on their own 10% tile,
  and the `#059669` entries are close behind at 3.34:1. The amber trio is also the only
  thing in the palette that misses the floor on the light field, by a hair: 2.998:1 on
  `#F7F8FA`. Decision taken: keep the supplied hexes verbatim, so the card assertion is
  strict and the tile and field cases are pinned as a known exception rather than silently
  dropped or quietly "fixed".
- **`parseHexColorOrNull`** — pure unit test: valid `#RRGGBB`, valid `#AARRGGBB`, null,
  blank, missing `#`, non-hex characters, wrong length.
- **`adaptForContrast`** — a colour that already clears the ratio is returned unchanged; a
  colour that does not is raised until it clears it; the ink terminal case always clears
  it; and every value in the supplied palette, treated as if it had been picked, ends up
  clearing the ratio in **both** themes. That last one is the test that makes the picker
  safe by construction, because it sweeps the whole swatch set through both backgrounds.
- **Resolver chain** — override wins; malformed override falls through to palette;
  unknown id falls through to the fallback.
- **`Migration16To17Test`** — modelled on `Migration14To15Test`, asserting pre-existing
  rows survive with `color_hex` null.
- If built-ins become recolourable, a test that a recoloured seeded row survives
  `ExpenseTrackerDatabaseInitializer.initialize`.

---

## 10. Out of scope

- `functions/parseVoiceTransaction.js` mirrors `categoryMap` and is unaffected, because the
  palette is keyed by id rather than stored per category.
- The goal colour picker. `Goal.colorHex` already exists and could reuse the same grid, but
  it is a separate screen and a separate change.
- Any change to the semantic income/expense/transfer/debt/savings/invest roles.

## 11. Commit discipline

Per `GEMINI.md`: every commit bumps `versionName` (patch segment for a chore/fix,
minor for a feature), leaves `versionCode` alone, includes the literal
`Bumped version to <versionName>` line, and syncs the `README.md` badge and version row.
No AI-attribution lines.

---

## 12. Appendix — the palette, annotated with ids

The palette exactly as supplied, with each entry's stable id added as a trailing comment
so the mapping cannot be guessed wrong during implementation. This is the literal content
of the four maps in `CategoryPalette.kt`; the declarations become `internal` there rather
than the `val` shown below, and the `Color` import stays because Kotlin imports are
per-file.

**One name was changed in the implementation.** `CardLight` / `CardDark` ship as
`CardPaymentLight` / `CardPaymentDark`, because `Color.kt` already declares `CardLight` as
the surface behind chips, segmented controls and search bars. It was the only collision in
the codebase — the other 64 names are clean — but it is a real one, and the suffix states
which of the two a call site means instead of leaving a reader to check the import.

### Expense categories — ids 1–23

```kotlin
import androidx.compose.ui.graphics.Color

// Light Mode
val FoodLight            = Color(0xFF5B2EED) // 1
val TravelLight          = Color(0xFF0288D1) // 2
val ShoppingLight        = Color(0xFFD97706) // 3
val BillsLight           = Color(0xFF059669) // 4
val HealthLight          = Color(0xFFDC2626) // 5
val EntertainmentLight   = Color(0xFF9333EA) // 6
val RentLight            = Color(0xFFEA580C) // 7
val GroceriesLight       = Color(0xFF059669) // 8
val EducationLight       = Color(0xFF2563EB) // 9
val SubscriptionsLight   = Color(0xFF7C3AED) // 10
val InsuranceLight       = Color(0xFF0288D1) // 11
val GiftsLight           = Color(0xFFD97706) // 12
val PersonalCareLight    = Color(0xFF9333EA) // 13
val FuelLight            = Color(0xFFEA580C) // 14
val MaintenanceLight     = Color(0xFF2563EB) // 15
val TaxesLight           = Color(0xFFDC2626) // 16
val PetsLight            = Color(0xFFD97706) // 17
val ChildcareLight       = Color(0xFF9333EA) // 18
val DonationsLight       = Color(0xFF059669) // 19
val MiscellaneousLight   = Color(0xFF4B5563) // 20
val TransportLight       = Color(0xFF0288D1) // 22  (id 21 does not exist)
val OtherExpenseLight    = Color(0xFF4B5563) // 23

// Dark Mode
val FoodDark             = Color(0xFF7A52FF) // 1
val TravelDark           = Color(0xFF4FC3F7) // 2
val ShoppingDark         = Color(0xFFF5C542) // 3
val BillsDark            = Color(0xFF3DDC97) // 4
val HealthDark           = Color(0xFFFF5C5C) // 5
val EntertainmentDark    = Color(0xFFC77DFF) // 6
val RentDark             = Color(0xFFFF9F45) // 7
val GroceriesDark        = Color(0xFF3DDC97) // 8
val EducationDark        = Color(0xFF6BA6FF) // 9
val SubscriptionsDark    = Color(0xFFA78BFA) // 10
val InsuranceDark        = Color(0xFF4FC3F7) // 11
val GiftsDark            = Color(0xFFF5C542) // 12
val PersonalCareDark     = Color(0xFFC77DFF) // 13
val FuelDark             = Color(0xFFFF9F45) // 14
val MaintenanceDark      = Color(0xFF6BA6FF) // 15
val TaxesDark            = Color(0xFFFF6B6B) // 16
val PetsDark             = Color(0xFFF5C542) // 17
val ChildcareDark        = Color(0xFFC77DFF) // 18
val DonationsDark        = Color(0xFF3DDC97) // 19
val MiscellaneousDark    = Color(0xFFA5A1B8) // 20
val TransportDark        = Color(0xFF4FC3F7) // 22
val OtherExpenseDark     = Color(0xFFA5A1B8) // 23
```

### Income categories — ids 101–105

```kotlin
// Light Mode
val SalaryLight          = Color(0xFF059669) // 101
val BusinessLight        = Color(0xFF5B2EED) // 102
val InvestmentLight      = Color(0xFF7C3AED) // 103
val FreelanceLight       = Color(0xFF2563EB) // 104
val OtherIncomeLight     = Color(0xFF4B5563) // 105

// Dark Mode
val SalaryDark           = Color(0xFF3DDC97) // 101
val BusinessDark         = Color(0xFF7A52FF) // 102
val InvestmentDark       = Color(0xFFA78BFA) // 103
val FreelanceDark        = Color(0xFF6BA6FF) // 104
val OtherIncomeDark      = Color(0xFFA5A1B8) // 105
```

### Payment methods — ids 1–6

Note the supplied order is not id order: `SalaryDeposit` is id 6 and `OtherPayment` is
id 5, so they are adjacent in the list above but must not be adjacent in the map.

```kotlin
// Light Mode
val UpiLight             = Color(0xFF5B2EED) // 1
val CashLight            = Color(0xFF059669) // 2
val BankLight            = Color(0xFF2563EB) // 3
val CardLight            = Color(0xFF9333EA) // 4
val OtherPaymentLight    = Color(0xFF4B5563) // 5
val SalaryDepositLight   = Color(0xFF059669) // 6

// Dark Mode
val UpiDark              = Color(0xFF7A52FF) // 1
val CashDark             = Color(0xFF3DDC97) // 2
val BankDark             = Color(0xFF6BA6FF) // 3
val CardDark             = Color(0xFFC77DFF) // 4
val OtherPaymentDark     = Color(0xFFA5A1B8) // 5
val SalaryDepositDark    = Color(0xFF3DDC97) // 6
```

### Repeated hues, recorded deliberately

Several tones are shared across categories on purpose. This is what makes the analytics
hue collisions in §8 possible, so the groups are listed here rather than left to be
discovered on a rendered donut.

| Light hex | Categories |
|---|---|
| `#5B2EED` | Food, Business, UPI |
| `#9333EA` | Entertainment, Personal Care, Childcare, Card |
| `#7C3AED` | Subscriptions, Investment |
| `#2563EB` | Education, Maintenance, Freelance, Bank |
| `#0288D1` | Travel, Insurance, Transport |
| `#D97706` | Shopping, Gifts, Pets |
| `#059669` | Bills, Groceries, Donations, Salary, Cash, Salary Deposit |
| `#EA580C` | Rent, Fuel |
| `#DC2626` | Health, Taxes |
| `#4B5563` | Miscellaneous, Other Expense, Other Income, Other Payment |

| Dark hex | Categories |
|---|---|
| `#7A52FF` | Food, Business, UPI |
| `#C77DFF` | Entertainment, Personal Care, Childcare, Card |
| `#A78BFA` | Subscriptions, Investment |
| `#6BA6FF` | Education, Maintenance, Freelance, Bank |
| `#4FC3F7` | Travel, Insurance, Transport |
| `#F5C542` | Shopping, Gifts, Pets |
| `#3DDC97` | Bills, Groceries, Donations, Salary, Cash, Salary Deposit |
| `#FF9F45` | Rent, Fuel |
| `#FF5C5C` / `#FF6B6B` | Health / Taxes — the one light pair that is *not* shared |
| `#A5A1B8` | Miscellaneous, Other Expense, Other Income, Other Payment |

Two observations that fall out of the table, for whenever the palette is next revised:
light mode repeats its greens across six different meanings, which makes green the
weakest identity in that theme; and the light pair Health/Taxes shares `#DC2626` while the
dark pair deliberately does not (`#FF5C5C` against `#FF6B6B`), so the two themes disagree
about whether those two categories are the same colour. Neither is a blocker — the icon
and the label carry identity — but both are worth knowing before the picker offers this
set as swatches.
