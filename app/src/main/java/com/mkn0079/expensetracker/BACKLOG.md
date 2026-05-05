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
1. [verified] Fix navigation propagation: Clicking a transaction card on the home screen should correctly open the edit transaction screen.
2. [ ] Analytics: Clicking "View All" in Top Spending should display transaction cards.

### 🟡 Medium Priority - Functional Improvements
1. [ ] Add custom date filter to the Sort/Filter sheet.

### 🟢 Low Priority - Visual Polish & Refactoring
1. [ ] Calendar screen: Standardize date format under 'Transactions' label based on app preferences (No Day of week).

---

### 🟡 Medium Priority - Functional Improvements
1. [verified] Instead of bottomsheet make add category a screen
2. [verified] Shift manage category card to settings screen from app preference screen
3. [verified] Add edit profile card in settings
4. [verified] Fix in add button get chipped, reduce its size or use icon instead of text
5. [verified] In multi select mode, change select all icon, and fix select icon background
6. [verified] Increase fingerprint icon size in applock
7. [verified] Add 50 more most used icons for user dont add which are already exist
8. [verified] Refactor Notification settings to use SettingsItemCard
9. [verified] Sync Calendar screen jump-to-date with WheelDateTimePicker
10. [verified] Fix circular background color of Delete icon in Dark Mode
11. [verified] Itemized calculator: Make "Add Item" popup keyboard-aware (Auto-lift)

---

### 🟢 Low Priority - Visual Polish & Refactoring
12. [verified] Add space between select all and delete icon
13. [verified] Remove glow in disable app lock screen
14. [verified] Reduce scrollable top padding in profile screen
15. [verified] Make profile card unclickable
16. [verified] Reduce font size of text 'track every move with confidence' in homescreen
17. [verified] Use icons.round.settings icon in homescreen
18. [verified] Standardize all card/toggle backgrounds with unified gradient
19. [verified] StatsCard: Auto-hide balance/income/expense (Show for 10s on click) by default it should be hidden and on click of card it should appear


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
14. [verified] Add calendar icon in Calendar screen near labels (Jump-to-date indicator)
15. [verified] Reduce top header padding to 10dp (Global layout polish)
16. [verified] Add a reset button to clear all fields in Add Transaction screen
17. [verified] Fix search and sort filter icon colors for dark/light mode
18. [verified] In sortfilter sheet: remove duplicate "bill/bills" categories
19. [verified] Fix label/arrow alignment consistency in Calendar screen between month and year view and color of arrows
20. [verified] Budget screen: Remove "Day" from custom month date picker (Show only Month/Year)
21. [verified] Add slide animations between tabs in Manage Categories screen
22. [verified] Remove "Personalize your vault" text from Add Category screen
23. [verified] Use official app icon in the About screen
24. [verified] Added gradient style to "Create Category" button
25. [verified] Apply unified gradient to all toggle switches
26. [verified] Made Add Category sheet scrollable and keyboard-aware
27. [verified] Disable app lock button is not working
28. [verified] Reset the add transaction form when add button clicked inside add transaction screen
29. [verified] Fix number input in profilescreen (Simplified input with placeholder)
30. [verified] Fix security question screen: white background on 'Your Answer' text in light mode
