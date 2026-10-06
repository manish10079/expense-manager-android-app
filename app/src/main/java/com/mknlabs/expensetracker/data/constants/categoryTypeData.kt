package com.mknlabs.expensetracker.data.constants

import com.mknlabs.expensetracker.models.CategoryType

val categoryMap = mapOf(
    1 to CategoryType(1, "Food", "utensils", 2, sortOrder = 1),
    2 to CategoryType(2, "Travel", "plane", 2, sortOrder = 2),
    3 to CategoryType(3, "Shopping", "package", 2, sortOrder = 3),
    4 to CategoryType(4, "Bills", "receipt", 2, sortOrder = 4),
    5 to CategoryType(5, "Health", "heart-pulse", 2, sortOrder = 5),
    6 to CategoryType(6, "Entertainment", "film", 2, sortOrder = 6),
    7 to CategoryType(7, "Rent", "home", 2, sortOrder = 7),
    8 to CategoryType(8, "Groceries", "shopping-cart", 2, sortOrder = 8),
    9 to CategoryType(9, "Education", "graduation-cap", 2, sortOrder = 9),
    10 to CategoryType(10, "Subscriptions", "repeat", 2, sortOrder = 10),
    11 to CategoryType(11, "Insurance", "shield-check", 2, sortOrder = 11),
    12 to CategoryType(12, "Gifts", "gift", 2, sortOrder = 12),
    13 to CategoryType(13, "Personal Care", "sparkles", 2, sortOrder = 13),
    14 to CategoryType(14, "Fuel", "fuel", 2, sortOrder = 14),
    15 to CategoryType(15, "Maintenance", "wrench", 2, sortOrder = 15),
    16 to CategoryType(16, "Taxes", "file-text", 2, sortOrder = 16),
    17 to CategoryType(17, "Pets", "dog", 2, sortOrder = 17),
    18 to CategoryType(18, "Childcare", "baby", 2, sortOrder = 18),
    19 to CategoryType(19, "Donations", "heart", 2, sortOrder = 19),
    20 to CategoryType(20, "Miscellaneous", "hand-coins", 2, sortOrder = 20),
    22 to CategoryType(22, "Transport", "bus", 2, sortOrder = 22),
    23 to CategoryType(23, "Other", "more-horizontal", 2, sortOrder = 23),

    101 to CategoryType(101, "Salary", "wallet", 1, sortOrder = 101),
    102 to CategoryType(102, "Business", "briefcase", 1, sortOrder = 102),
    103 to CategoryType(103, "Investment", "trending-up", 1, sortOrder = 103),
    104 to CategoryType(104, "Freelance", "laptop", 1, sortOrder = 104),
    105 to CategoryType(105, "Other", "more-horizontal", 1, sortOrder = 105)
)
