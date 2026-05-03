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

5. If task cannot proceed due to dependency, bug, or missing requirement:
   → Change status to [blocked]

6. Do NOT mark [verified] without:
   → Real user testing  
   → UI validation  
   → Edge case checks  

7. If a fix introduces new issues:
   → Change status back to [in-progress] or [blocked]

8. After completing and updating any issue:
   → compile the code if build successuful if not analyze  the error and fix it  then go to next step
   → Ask the user whether to proceed with the next issue or work on a specific issue they choose  
   → Continue this loop until the user explicitly exits or stops  

9. Always update status immediately after work (no delayed updates)

---

### 🔴 High Priority - Critical Bugs & Core UX
1. [verified] Not able to add more than one budget in Budget and Recurring screen (Blocks core functionality)
2. [verified] Implement strict budget edit limits and historical month locking (Ensures data integrity)
3. [verified] App lock overlay while opening app: fix opacity (Affects privacy/security feel)
4. [verified] All screen top padding not aligned (Affects visual integrity of every screen)
5. [verified] Unify one gradient style across the app (Fixes inconsistent branding/design)
6. [verified] After app lock when unlock it redirect to home
7. [verified] Make separate section for category of income and expense (Right now it shows all as all categories)
8. [verified] Lock the app's orientation to portrait mode (Ensures layout stability across all devices)
9. [in-progress] Align Sort/Filter bottom sheet color palette with the rest of the app for both light and dark modes

### 🟡 Medium Priority - Screen-Specific UI & Navigation
10. [ ] Remove navbar from Transactions screen
11. [ ] Put Add Category and Cancel button in a single row in Add Category screen
12. [ ] Back button touch feedback: change from square style to standard ripple (Fixes "show taps" look)
13. [ ] Add search icons in Add Category screen
14. [ ] Add calendar icon in Calendar screen near year/month labels (Indicates jump-to-date functionality)
15. [ ] Reduce top header padding to 10dp
16. [ ] Add a reset button to reset all fields in add transaction screen
17. [ ] Fix search and sort filter icon for dark mode and light mode
18. [ ] In sortfilter sheet there bill and bills remove one
19. [ ] Make consistent arrow and label distance in calendar screen between month and year view
20. [ ] In budget and recurring screen in custom month toggle date picker shows day month year, remove day

### 🟢 Low Priority - Visual Polish & Refactoring
21. [ ] Add slide toggle animation between categories in Manage Categories (Expense, Income, Payment)
22. [ ] Remove "Personalize your vault" from Add Category screen
23. [ ] Use app's icon in About screen
24. [ ] Use same unified `SettingsItemCard` reusable component in Notification settings
25. [verified] Add the same gradient style to "Create Category" button used across the app
26. [ ] Use same wheeldatetimepicker in calendar screen to jump over specific period
27. [ ] Fix delete icon's circular background color in dark mode
28. [ ] Itemized calculator "Add Item" popup: Lift up or make keyboard aware to prevent being hidden
29. [ ] Standardize card and toggle backgrounds with unified gradient across the app
30. [ ] In Manage category screen  category card should use appiconbox component if not using and  use settingsItemCard component if not used only if possible
31. [] use the same gradient  background used on all toggle  switches in mange category toggle( income, expense, payment) 
not selected toggle 
32. [] in statscard by default total balance, income amount and expnse amount should hidden  when user clicks to unhide it will
unhide for 10 seconds and then hides it again
