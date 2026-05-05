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
1. [fixed(pending verification)] while unlocking app with biometric doesnot work sometimes works sometimes not, analyze code related to it and fix it

### 🟡 Medium Priority - Functional Improvements


### 🟢 Low Priority - Visual Polish & Refactoring


---

### ✅ Completed & Verified
1. [verified] Unified background color for Home screen settings icon and Today's Spending icon
2. [verified] Upgrade TransactionCard with customization pills and settings toggles
3. [verified] in top spending in category and payment mode when i click view all some the category cards not visible , specifically from 6th card, make it lazycloumn just a suggestion , fix it
4. [verified] Fixed budget addition limit in Budget & Recurring screen
5. [verified] Implemented strict budget edit limits and historical locking
6. [verified] Fixed opacity of app lock overlay during startup
7. [verified] Aligned top padding across all screens
8. [verified] Unified gradient style across the entire app
9. [verified] Fixed app lock redirect (preserves navigation state)
10. [verified] Separated Income and Expense category sections in Filter Sheet
11. [verified] Locked app orientation to Portrait mode
12. [verified] Aligned Sort/Filter sheet color palette with brand design
13. [verified] Removed navbar from Transactions screen
14. [verified] Aligned Add Category buttons in a single row
15. [verified] Changed back button touch feedback to circular ripple
16. [verified] Add search icons in Add Category screen
17. [verified] Add calendar icon in Calendar screen near labels (Jump-to-date indicator)
18. [verified] Reduce top header padding to 10dp (Global layout polish)
19. [verified] Add a reset button to clear all fields in Add Transaction screen
20. [verified] Fix search and sort filter icon colors for dark/light mode
21. [verified] In sortfilter sheet: remove duplicate "bill/bills" categories
22. [verified] Fix label/arrow alignment consistency in Calendar screen between month and year view and color of arrows
23. [verified] Budget screen: Remove "Day" from custom month date picker (Show only Month/Year)
24. [verified] Add slide animations between tabs in Manage Categories screen
25. [verified] Remove "Personalize your vault" text from Add Category screen
26. [verified] Use official app icon in the About screen
27. [verified] Added gradient style to "Create Category" button
28. [verified] Apply unified gradient to all toggle switches
29. [verified] Made Add Category sheet scrollable and keyboard-aware
30. [verified] Disable app lock button is not working
31. [verified] Reset the add transaction form when add button clicked inside add transaction screen
32. [verified] Fix number input in profilescreen (Simplified input with placeholder)
33. [verified] Fix security question screen: white background on 'Your Answer' text in light mode
