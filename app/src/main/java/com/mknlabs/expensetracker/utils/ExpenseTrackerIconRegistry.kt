package com.mknlabs.expensetracker.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The one table that turns a stored `iconKey` into a drawable icon.
 *
 * [com.mknlabs.expensetracker.models.CategoryType], `PaymentType` and `Goal` all resolve
 * through [iconForKey], and the icon picker no longer carries vectors of its own, so this is
 * the only place an icon is defined. A key missing from this map draws as a question mark,
 * which is why every key the picker offers must also live here.
 */
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
        "cake" to Icons.Filled.Cake,
        // Every key below was offered by the icon picker while having no entry here, so
        // choosing one stored a key that drew as a question mark in every list row. The picker
        // now reads its vectors from this map, and CategoryIconCatalogTest fails if a key the
        // picker offers ever goes missing from it again.
        "water_drop" to Icons.Filled.WaterDrop,
        "electric_bolt" to Icons.Filled.ElectricBolt,
        "gas_meter" to Icons.Filled.Whatshot,
        "wifi" to Icons.Filled.Wifi,
        "tv" to Icons.Filled.Tv,
        "local_shipping" to Icons.Filled.LocalShipping,
        "cleaning_services" to Icons.Filled.CleaningServices,
        "pest_control" to Icons.Filled.PestControl,
        "yard" to Icons.Filled.Yard,
        "plumbing" to Icons.Filled.Plumbing,
        "hvac" to Icons.Filled.Air,
        "garage" to Icons.Filled.HomeWork,
        "local_laundry_service" to Icons.Filled.LocalLaundryService,
        "checkroom" to Icons.Filled.Checkroom,
        "ice_skating" to Icons.Filled.AcUnit,
        "skiing" to Icons.Filled.AcUnit,
        "sports_tennis" to Icons.Filled.SportsTennis,
        "sports_golf" to Icons.Filled.SportsGolf,
        "kayaking" to Icons.Filled.Rowing,
        "surfing" to Icons.Filled.Waves,
        "directions_run" to Icons.AutoMirrored.Filled.DirectionsRun,
        "pedal_bike" to Icons.Filled.PedalBike,
        "sailing" to Icons.Filled.Sailing,
        "theater_comedy" to Icons.Filled.TheaterComedy,
        "casino" to Icons.Filled.Casino,
        "toys" to Icons.Filled.Toys,
        "piano" to Icons.Filled.Piano,
        "brush" to Icons.Filled.Brush,
        "palette" to Icons.Filled.Palette,
        "auto_stories" to Icons.Filled.AutoStories,
        "menu_book" to Icons.AutoMirrored.Filled.MenuBook,
        "computer" to Icons.Filled.Computer,
        "memory" to Icons.Filled.Memory,
        "mouse" to Icons.Filled.Mouse,
        "keyboard" to Icons.Filled.Keyboard,
        "headset" to Icons.Filled.Headset,
        "speaker" to Icons.Filled.Speaker,
        "router" to Icons.Filled.Router,
        "monitor" to Icons.Filled.Monitor,
        "print" to Icons.Filled.Print,
        "smart_home" to Icons.Filled.Hub,
        "videocam" to Icons.Filled.Videocam,
        "mic" to Icons.Filled.Mic,
        "radio" to Icons.Filled.Radio,
        "satellite" to Icons.Filled.Satellite,
        "explore" to Icons.Filled.Explore,
        "terrain" to Icons.Filled.Terrain,
        "landscape" to Icons.Filled.Landscape,
        "forest" to Icons.Filled.Forest
    )

    fun iconForKey(iconKey: String): ImageVector {
        return iconMap[iconKey] ?: Icons.Filled.QuestionMark
    }
}
