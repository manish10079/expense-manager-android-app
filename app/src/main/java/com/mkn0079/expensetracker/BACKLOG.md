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
*Currently no active high-priority items.*

---

### 🟡 Medium Priority - Functional Improvements
13. [verified] Add search icons in Add Category screen (Ready for verification)
15. [verified] Reduce top header padding to 10dp (Global layout polish)
17. [verified] Fix search and sort filter icon colors for dark/light mode
18. [verified] In sortfilter sheet: remove duplicate "bill/bills" categories
16. [ ] Add a reset button to clear all fields in Add Transaction screen
14. [verified] Add calendar icon in Calendar screen near labels (Jump-to-date indicator)
19. [verified] Fix label/arrow alignment consistency in Calendar screen between month and year view  and color of arrows 
20. [ ] Budget screen: Remove "Day" from custom month date picker (Show only Month/Year)

---

### 🟢 Low Priority - Visual Polish & Refactoring
22. [ ] Remove "Personalize your vault" text from Add Category screen
23. [ ] Use official app icon in the About screen
27. [ ] Fix circular background color of Delete icon in Dark Mode
28. [ ] Itemized calculator: Make "Add Item" popup keyboard-aware (Auto-lift)
21. [ ] Add slide animations between tabs in Manage Categories screen
30. [ ] Unify components (AppIconBox/SettingsItemCard) in Manage Categories
31. [ ] Apply unified gradient to all toggle switches
32. [ ] StatsCard: Auto-hide balance/income/expense (Show for 10s on click)
24. [ ] Refactor Notification settings to use SettingsItemCard
26. [ ] Sync Calendar screen jump-to-date with WheelDateTimePicker
29. [ ] Standardize all card/toggle backgrounds with unified gradient

---

### ✅ Completed & Verified
1. [verified] Fixed budget addition limit in Budget & Recurring screen
2. [verified] Implemented strict budget edit limits and historical locking
3. [verified] Fixed opacity of app lock overlay during startup
4. [verified] Aligned top padding across all screens
5. [verified] Unified gradient style across the entire app
6. [verified] Fixed app lock redirect (preserves navigation state)
7. [verified] Separated Income and Expense category sections in Filter Sheet
8. [verified] Locked app orientation to Portrait mode
9. [verified] Aligned Sort/Filter sheet color palette with brand design
10. [verified] Removed navbar from Transactions screen
11. [verified] Aligned Add Category buttons in a single row
12. [verified] Changed back button touch feedback to circular ripple
13. [verified] Add search icons in Add Category screen
25. [verified] Added gradient style to "Create Category" button
33. [verified] Made Add Category sheet scrollable and keyboard-aware
