package com.mknlabs.expensetracker.data.constants

import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.utils.getCategoryIcon

//Dummy data

val transactionList = listOf(
        Transaction(1L, "Salary Credit", 1769936400000, 35000.0, getCategoryIcon(101), 1, 3, 101), // Feb 1
        Transaction(2L, "Swiggy Dinner", 1770063300000, 450.0, getCategoryIcon(1), 2, 1, 1),
        Transaction(3L, "Uber Ride", 1770104400000, 220.0, getCategoryIcon(2), 2, 1, 2),
        Transaction(4L, "Amazon Shopping", 1770210300000, 2499.0, getCategoryIcon(3), 2, 4, 3),
        Transaction(5L, "Electricity Bill", 1770287400000, 1800.0, getCategoryIcon(4), 2, 3, 4),

        Transaction(6L, "Doctor Visit", 1770365400000, 700.0, getCategoryIcon(5), 2, 2, 5),
        Transaction(7L, "Movie Night", 1770501900000, 600.0, getCategoryIcon(6), 2, 4, 6),
        Transaction(8L, "House Rent", 1770533100000, 12000.0, getCategoryIcon(7), 2, 3, 7),
        Transaction(9L, "Big Bazaar", 1770657600000, 2200.0, getCategoryIcon(8), 2, 2, 8),
        Transaction(10L, "Course Purchase", 1770724500000, 999.0, getCategoryIcon(9), 2, 1, 9),

        Transaction(11L, "Netflix", 1770843600000, 649.0, getCategoryIcon(10), 2, 4, 10),
        Transaction(12L, "LIC Premium", 1770888300000, 2500.0, getCategoryIcon(11), 2, 3, 11),
        Transaction(13L, "Birthday Gift", 1770996900000, 1500.0, getCategoryIcon(12), 2, 2, 12),
        Transaction(14L, "Salon", 1771073400000, 800.0, getCategoryIcon(13), 2, 2, 13),
        Transaction(15L, "Petrol", 1771179000000, 3000.0, getCategoryIcon(14), 2, 1, 14),

        Transaction(16L, "Bike Repair", 1771252800000, 1200.0, getCategoryIcon(15), 2, 2, 15),
        Transaction(17L, "Tax Payment", 1771322400000, 5000.0, getCategoryIcon(16), 2, 3, 16),
        Transaction(18L, "Dog Food", 1771430700000, 900.0, getCategoryIcon(17), 2, 2, 17),
        Transaction(19L, "School Fees", 1771491300000, 4000.0, getCategoryIcon(18), 2, 3, 18),
        Transaction(20L, "Temple Donation", 1771615800000, 500.0, getCategoryIcon(19), 2, 2, 19),

        Transaction(21L, "Misc Expense", 1771658100000, 300.0, getCategoryIcon(20), 2, 5, 20),
        Transaction(22L, "Freelance Work", 1771767900000, 12000.0, getCategoryIcon(104), 1, 3, 104),
        Transaction(23L, "Stock Profit", 1771845600000, 8000.0, getCategoryIcon(103), 1, 3, 103),
        Transaction(24L, "Business Income", 1771926600000, 20000.0, getCategoryIcon(102), 1, 3, 102),
        Transaction(25L, "Gift Received", 1772043900000, 3500.0, getCategoryIcon(105), 1, 2, 105),

        Transaction(26L, "Zomato Lunch", 1772107800000, 350.0, getCategoryIcon(1), 2, 1, 1),
        Transaction(27L, "Train Ticket", 1772174100000, 1250.0, getCategoryIcon(2), 2, 1, 2),
        Transaction(28L, "Clothes Shopping", 1772301300000, 2999.0, getCategoryIcon(3), 2, 4, 3),
        Transaction(29L, "Water Bill", 1772353200000, 600.0, getCategoryIcon(4), 2, 3, 4),
        Transaction(30L, "Gym Fees", 1772478300000, 1500.0, getCategoryIcon(5), 2, 2, 5),

        Transaction(31L, "Office Lunch", 1774787400000, 320.0, getCategoryIcon(1), 2, 1, 1),
        Transaction(32L, "Freelance Payout", 1774809900000, 7800.0, getCategoryIcon(104), 1, 3, 104),

        Transaction(33L, "Grocery Shopping", 1774861800000, 1450.0, getCategoryIcon(8), 2, 2, 8),
        Transaction(34L, "Gift Received", 1774905300000, 900.0, getCategoryIcon(105), 1, 2, 105),

        Transaction(35L, "Coffee Meetup", 1774943400000, 260.0, getCategoryIcon(6), 2, 4, 6),
        Transaction(36L, "Bonus Credit", 1774998300000, 4200.0, getCategoryIcon(101), 1, 3, 101)
)
