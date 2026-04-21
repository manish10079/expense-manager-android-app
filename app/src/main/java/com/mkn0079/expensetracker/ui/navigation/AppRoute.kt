package com.mkn0079.expensetracker.ui.navigation

enum class AppRoute(
    val route: String,
    val showsFixedBottomBar: Boolean
) {
    Home("home", true),
    Analytics("analytics", true),
    Budget("budget", true),
    Calendar("calendar", true),
    Transactions("transactions", true),
    Settings("settings", false),
    Preferences("preferences", false),
    SecurityPrivacy("security_privacy", false),
    TransactionCardCustomize("transaction_card_customize", false),
    CategoryManagement("category_management", false),
    DataManagement("data_management", false),
    About("about", false),
    NotificationSettings("notification_settings", false),
    Profile("profile", false),
    AddTransaction("add_transaction", false),
    ItemizedCalculator("itemized_calculator", false);

    companion object {
        fun fromRoute(route: String?): AppRoute? {
            return entries.firstOrNull { it.route == route }
        }
    }
}
