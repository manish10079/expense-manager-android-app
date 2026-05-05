# 📝 Expense Tracker - Backlog

### 📌 Status Rules
[ ] = Not started  
[in-progress] = Currently working on it 
[fixed] = Implementation completed  
[verified] = Tested and confirmed working  
[blocked] = Cannot proceed due to dependency/issue  

Flow to follow:
[ ] → [in-progress] → [fixed] → [verified]

---

### ⚙️ Update Instructions (For AI Agent / Developer)

1. When starting any task:
   → Change [ ] to [in-progress]
2. When implementation/code is completed:
   → Change [in-progress] to [fixed]
3. After fixing any issue:
   → Ask user to test the feature in real usage
4. Based on user feedback:
   → If user confirms working correctly → Change [fixed] to [verified]  
   → If user reports issue or improvement → Change status back to [in-progress] and fix accordingly  
5. After completing and updating any issue:
   → **Compile the code**: Run build to ensure no syntax errors.
   → **Verify UI**: Ensure it matches brand guidelines (gradients, padding, ripples).
   → **Ask User**: Confirm if they want to proceed to the next item.

---

### 🔴 High Priority - Critical Bugs & UX
1. [ ] while unlocking app with biometric doesnot work sometimes works sometimes not, analyze code related to it and fix it

### 🟡 Medium Priority - Functional Improvements
1. [ ] Role

You are a senior Android UI engineer and design system builder. Your job is to create reusable, scalable, and responsive UI components with clean structure and consistency across the app.

---

Context

The app already has a reusable "TransactionCard" component used across multiple screens.

A StatsCard component is present in the "HomeScreen.kt" file, which shows:

- Income (green up arrow style)
- Expense (red down arrow style)

The TransactionCard must visually align with this StatsCard styling for consistency.

The goal is to make the TransactionCard more informative, reusable, and adaptive to user settings.

---

Task

Upgrade the existing reusable "TransactionCard" component to include additional labels (pills), consistent styling, responsiveness, and user-controlled visibility. Implement everything in one pass.

---

1. Add new UI elements

Inside the card, below the date/time row, add rounded labels (pills):

- Transaction Type → INCOME / EXPENSE
- Category → dynamic (e.g., FOOD, GROCERIES)
- Payment Method → dynamic (e.g., UPI, CASH, CARD)

These must be part of the reusable component so all screens automatically get this.

---

2. Layout rules

- Place pills below date/time, aligned left
- Keep them in a single horizontal row
- Maintain compact spacing:
  - Small gap between date and pills
  - Small spacing between pills
- If space is tight:
  - Allow wrapping to next line
  - No clipping or overflow

---

3. Styling rules (strict)

Transaction Type Pill (must match StatsCard in HomeScreen.kt)

- INCOME
  
  - Same style as StatsCard up arrow (HomeScreen.kt)
  - Same green text color
  - Same light green background

- EXPENSE
  
  - Same style as StatsCard down arrow (HomeScreen.kt)
  - Same red text color
  - Same light red/pink background

Must visually match StatsCard exactly.

---

Category Pill

- Light gray background
- Slightly darker gray text

Payment Method Pill

- Same as category style

---

4. Reusable pill component (important)

Create a flexible reusable pill UI component.

It should allow:

- Text
- Text color
- Background color
- Size (small/medium or padding-based)
- Shape (rounded/corner radius)

Make the pill fully customizable so it can be reused anywhere.

---

5. Responsiveness (critical)

Ensure full support for maximum Android system font size:

- No overlap or broken layout
- Text should wrap properly
- Pills should move to next line if needed
- Card height should expand naturally
- UI must remain clean and readable

---

6. Settings integration

In Card Settings screen, add toggles:

- Show Category (on/off)
- Show Payment Method (on/off)

Behavior:

- OFF → hide that pill
- ON → show it
- Transaction Type is always visible

---

7. Reusability requirement

- Everything handled inside TransactionCard
- Any screen using it automatically gets:
  - Pills
  - Styling
  - Responsiveness
  - Toggle behavior

No extra work needed anywhere else.

---

8. Do not change

- Title
- Amount position (right aligned)
- Icon
- Card structure

---

9. Completion criteria

- Matches StatsCard colors exactly
- Works on all screens
- Fully responsive at max font size
- Toggles work correctly
- Handles long text and small width

---

10. Final step

After implementation:

- Show result
- Be ready to refine based on feedback

---

Execute this in one go without unnecessary questions.

### 🟢 Low Priority - Visual Polish & Refactoring
1. [ ] use same background color for homescreen settings icon from todays spending icon

---

### ✅ Completed & Verified
1. [verified] in top spending in category and payment mode when i click view all some the category cards not visible , specifically from 6th card, make it lazycloumn just a suggestion , fix it
2. [verified] Fixed budget addition limit in Budget & Recurring screen
3. [verified] Implemented strict budget edit limits and historical locking
4. [verified] Fixed opacity of app lock overlay during startup
5. [verified] Aligned top padding across all screens
6. [verified] Unified gradient style across the entire app
7. [verified] Fixed app lock redirect (preserves navigation state)
8. [verified] Separated Income and Expense category sections in Filter Sheet
9. [verified] Locked app orientation to Portrait mode
10. [verified] Aligned Sort/Filter sheet color palette with brand design
11. [verified] Removed navbar from Transactions screen
12. [verified] Aligned Add Category buttons in a single row
13. [verified] Changed back button touch feedback to circular ripple
14. [verified] Add search icons in Add Category screen
15. [verified] Add calendar icon in Calendar screen near labels (Jump-to-date indicator)
16. [verified] Reduce top header padding to 10dp (Global layout polish)
17. [verified] Add a reset button to clear all fields in Add Transaction screen
18. [verified] Fix search and sort filter icon colors for dark/light mode
19. [verified] In sortfilter sheet: remove duplicate "bill/bills" categories
20. [verified] Fix label/arrow alignment consistency in Calendar screen between month and year view and color of arrows
21. [verified] Budget screen: Remove "Day" from custom month date picker (Show only Month/Year)
22. [verified] Add slide animations between tabs in Manage Categories screen
23. [verified] Remove "Personalize your vault" text from Add Category screen
24. [verified] Use official app icon in the About screen
25. [verified] Added gradient style to "Create Category" button
26. [verified] Apply unified gradient to all toggle switches
27. [verified] Made Add Category sheet scrollable and keyboard-aware
28. [verified] Disable app lock button is not working
29. [verified] Reset the add transaction form when add button clicked inside add transaction screen
30. [verified] Fix number input in profilescreen (Simplified input with placeholder)
31. [verified] Fix security question screen: white background on 'Your Answer' text in light mode
