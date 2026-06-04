package com.mknlabs.expensetracker.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AssuredWorkload
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object ExpenseTrackerIconRegistry {

    private val iconMap: Map<String, ImageVector> = mapOf(
        "flatware" to Icons.Filled.Flatware,
        "directions_car" to Icons.Filled.DirectionsCar,
        "shopping_bag" to Icons.Filled.ShoppingBag,
        "receipt_long" to Icons.AutoMirrored.Filled.ReceiptLong,
        "favorite" to Icons.Filled.Favorite,
        "movie" to Icons.Filled.Movie,
        "home" to Icons.Filled.Home,
        "shopping_cart" to Icons.Filled.ShoppingCart,
        "school" to Icons.Filled.School,
        "subscriptions" to Icons.Filled.Subscriptions,
        "security" to Icons.Filled.Security,
        "card_giftcard" to Icons.Filled.CardGiftcard,
        "face" to Icons.Filled.Face,
        "local_gas_station" to Icons.Filled.LocalGasStation,
        "build" to Icons.Filled.Build,
        "attach_money" to Icons.Filled.AttachMoney,
        "pets" to Icons.Filled.Pets,
        "child_care" to Icons.Filled.ChildCare,
        "volunteer_activism" to Icons.Filled.VolunteerActivism,
        "category" to Icons.Filled.Category,
        "account_balance_wallet" to Icons.Filled.AccountBalanceWallet,
        "business" to Icons.Filled.Business,
        "trending_up" to Icons.AutoMirrored.Filled.TrendingUp,
        "laptop_mac" to Icons.Filled.LaptopMac,
        "more_horiz" to Icons.Filled.MoreHoriz,
        "qr_code" to Icons.Filled.QrCode,
        "payments" to Icons.Filled.Payments,
        "assured_workload" to Icons.Filled.AssuredWorkload,
        "credit_card" to Icons.Filled.CreditCard,
        "restaurant" to Icons.Filled.Flatware,
        "directions_bus" to Icons.Filled.DirectionsBus,
        "flight" to Icons.Filled.Flight,
        "local_cafe" to Icons.Filled.LocalCafe,
        "fitness_center" to Icons.Filled.FitnessCenter,
        "spa" to Icons.Filled.Spa,
        "music_note" to Icons.Filled.MusicNote,
        "sports_esports" to Icons.Filled.SportsEsports,
        "work" to Icons.Filled.Work,
        "phone_android" to Icons.Filled.PhoneAndroid,
        "camera_alt" to Icons.Filled.CameraAlt,
        "celebration" to Icons.Filled.Celebration,
        "local_hospital" to Icons.Filled.LocalHospital,
        "medication" to Icons.Filled.Medication,
        "two_wheeler" to Icons.Filled.TwoWheeler,
        "train" to Icons.Filled.Train,
        "hotel" to Icons.Filled.Hotel,
        "beach_access" to Icons.Filled.BeachAccess,
        "park" to Icons.Filled.Park,
        "hiking" to Icons.Filled.Hiking,
        "sports_soccer" to Icons.Filled.SportsSoccer,
        "sports_basketball" to Icons.Filled.SportsBasketball,
        "pool" to Icons.Filled.Pool,
        "directions_boat" to Icons.Filled.DirectionsBoat,
        "account_balance" to Icons.Filled.AccountBalance,
        "savings" to Icons.Filled.Savings,
        "wallet" to Icons.Filled.AccountBalanceWallet,
        "currency_exchange" to Icons.Filled.CurrencyExchange,
        "storefront" to Icons.Filled.Storefront,
        "fastfood" to Icons.Filled.Fastfood,
        "cake" to Icons.Filled.Cake
    )

    fun iconForKey(iconKey: String): ImageVector {
        return iconMap[iconKey] ?: Icons.Filled.QuestionMark
    }
}
