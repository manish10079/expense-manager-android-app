package com.mknlabs.expensetracker.data.constants

import com.mknlabs.expensetracker.models.CategoryType

val categoryMap = mapOf(
    1 to CategoryType(1, "Food", "flatware", 2, sortOrder = 1),
    2 to CategoryType(2, "Travel", "directions_car", 2, sortOrder = 2),
    3 to CategoryType(3, "Shopping", "shopping_bag", 2, sortOrder = 3),
    4 to CategoryType(4, "Bills", "receipt_long", 2, sortOrder = 4),
    5 to CategoryType(5, "Health", "favorite", 2, sortOrder = 5),
    6 to CategoryType(6, "Entertainment", "movie", 2, sortOrder = 6),
    7 to CategoryType(7, "Rent", "home", 2, sortOrder = 7),
    8 to CategoryType(8, "Groceries", "shopping_cart", 2, sortOrder = 8),
    9 to CategoryType(9, "Education", "school", 2, sortOrder = 9),
    10 to CategoryType(10, "Subscriptions", "subscriptions", 2, sortOrder = 10),
    11 to CategoryType(11, "Insurance", "security", 2, sortOrder = 11),
    12 to CategoryType(12, "Gifts", "card_giftcard", 2, sortOrder = 12),
    13 to CategoryType(13, "Personal Care", "face", 2, sortOrder = 13),
    14 to CategoryType(14, "Fuel", "local_gas_station", 2, sortOrder = 14),
    15 to CategoryType(15, "Maintenance", "build", 2, sortOrder = 15),
    16 to CategoryType(16, "Taxes", "attach_money", 2, sortOrder = 16),
    17 to CategoryType(17, "Pets", "pets", 2, sortOrder = 17),
    18 to CategoryType(18, "Childcare", "child_care", 2, sortOrder = 18),
    19 to CategoryType(19, "Donations", "volunteer_activism", 2, sortOrder = 19),
    20 to CategoryType(20, "Miscellaneous", "category", 2, sortOrder = 20),
    22 to CategoryType(22, "Transport", "directions_car", 2, sortOrder = 22),
    23 to CategoryType(23, "Other", "more_horiz", 2, sortOrder = 23),

    
    101 to CategoryType(101, "Salary", "account_balance_wallet", 1, sortOrder = 101),
    102 to CategoryType(102, "Business", "business", 1, sortOrder = 102),
    103 to CategoryType(103, "Investment", "trending_up", 1, sortOrder = 103),
    104 to CategoryType(104, "Freelance", "laptop_mac", 1, sortOrder = 104),
    105 to CategoryType(105, "Other", "more_horiz", 1, sortOrder = 105)
)
