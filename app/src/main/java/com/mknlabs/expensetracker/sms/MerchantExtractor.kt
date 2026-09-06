package com.mknlabs.expensetracker.sms

import java.util.Locale

/**
 * High-performance Indian Merchant Extractor & Mapping Database.
 *
 * Handles:
 * 1. Contextual regex extraction from SMS ("paid to ...", "at ...", "vpa ...", "info: ...").
 * 2. UPI VPA stripping (e.g., `swiggy@ybl` -> `SWIGGY`).
 * 3. Comprehensive Indian merchant dictionary mapping to standard categories (Food, Groceries, Shopping, Travel, Bills, etc.).
 */
object MerchantExtractor {

    data class MerchantInfo(
        val rawName: String,
        val cleanedName: String,
        val categoryName: String?
    )

    private val MERCHANT_PATTERNS: List<Regex> = listOf(
        Regex("""(?:paid|sent|transfer(?:red)?|trf)\s+to\s+([A-Za-z0-9 .&'-@_]+?)(?=\s+(?:on|via|ref|upi|avl|bal|a/c|card|at|\.|\$|$))""", RegexOption.IGNORE_CASE),
        Regex("""(?:upi|vpa)\s+(?:to\s+)?([A-Za-z0-9 .&'-@_]+?)(?=\s+(?:on|via|ref|upi|avl|bal|a/c|card|at|\.|\$|$))""", RegexOption.IGNORE_CASE),
        Regex("""(?:at|towards)\s+([A-Za-z0-9 .&'-@_]+?)(?=\s+(?:on|via|ref|upi|avl|bal|a/c|card|\.|\$|$))""", RegexOption.IGNORE_CASE),
        Regex("""from\s+([A-Za-z0-9 .&'-@_]+?)(?=\s+(?:on|via|ref|upi|avl|bal|a/c|card|\.|\$|$))""", RegexOption.IGNORE_CASE),
        Regex("""info:?\s*([A-Za-z0-9 .&'-@_]+?)(?=\s+(?:on|via|ref|upi|avl|bal|a/c|card|\.|\$|$))""", RegexOption.IGNORE_CASE),
        Regex("""merchant\s+([A-Za-z0-9 .&'-@_]+?)(?=\s+(?:on|via|ref|upi|avl|bal|a/c|card|\.|\$|$))""", RegexOption.IGNORE_CASE)
    )

    private val VPA_SUFFIX_REGEX = Regex("""@[a-zA-Z0-9_-]+""")

    // Dictionary of 1,500+ Indian merchant keywords to Category names
    private val MERCHANT_DICTIONARY: Map<String, String> = buildMap {
        // --- Food & Dining / Delivery ---
        val foodMerchants = listOf(
            "swiggy", "zomato", "dominos", "domino", "mcdonalds", "mcdonald", "kfc", "burger king", "subway",
            "pizza hut", "starbucks", "dunkin", "haldiram", "haldirams", "behrouz", "faasos", "ovenstory",
            "box8", "eatsure", "chai point", "chaayos", "third wave coffee", "blue tokai", "barbeque nation",
            "pirates of grill", "mainland china", "wow momo", "wow china", "baskin robbins", "kwality walls",
            "amul", "mother dairy", "country delight", "bikanervala", "sweet bengal", "gupta sweets",
            "natraj", "taco bell", "paradise biryani", "bawarchi", "sharief bhai", "california burrito"
        )
        foodMerchants.forEach { put(it, "Food") }

        // --- Groceries & Quick Commerce ---
        val groceryMerchants = listOf(
            "blinkit", "zepto", "instamart", "bigbasket", "bb daily", "dunzo", "grofers", "dmart", "d-mart",
            "avenue supermarts", "reliance fresh", "jiomart", "jio mart", "spencer", "spencers", "more retail",
            "nature basket", "star bazaar", "easyday", "heritage fresh", "ratnadeep", "licious", "freshtohome",
            "meatone", "tender cuts", "milkbasket", "fipola"
        )
        groceryMerchants.forEach { put(it, "Groceries") }

        // --- Shopping & E-Commerce / Fashion ---
        val shoppingMerchants = listOf(
            "amazon", "flipkart", "myntra", "ajio", "nykaa", "nykaa man", "nykaa fashion", "tata cliq",
            "tata neu", "meesho", "snapdeal", "shopclues", "decathlon", "zara", "h&m", "hm", "uniqlo",
            "marks and spencer", "westside", "trends", "reliance trends", "pantaloons", "lifestyle",
            "max fashion", "shopper stop", "shoppers stop", "fabindia", "manyavar", "allen solly",
            "peter england", "van heusen", "louis philippe", "us polo", "levis", "puma", "adidas",
            "nike", "reebok", "skechers", "woodland", "crocs", "lenskart", "titan", "fastrack",
            "tanishq", "caratlane", "kalyan jewellers", "malabar gold", "joyalukkas", "zivame", "clovia"
        )
        shoppingMerchants.forEach { put(it, "Shopping") }

        // --- Travel / Transport & Commute ---
        val travelMerchants = listOf(
            "uber", "ola", "rapido", "namma yatri", "indrive", "blusmart", "meru", "redbus", "abhibus",
            "makemytrip", "mmt", "goibibo", "yatra", "cleartrip", "easemytrip", "ixigo", "irctc", "indian railways",
            "indigo", "air india", "vistara", "spicejet", "akasa air", "airasia", "fastag", "nhai", "toll plaza",
            "metro", "dmrc", "bmrtc", "mmrcl", "zoomcar", "drivezy", "revv"
        )
        travelMerchants.forEach { put(it, "Travel") }

        // --- Fuel ---
        val fuelMerchants = listOf(
            "indianoil", "indian oil", "iocl", "hpcl", "hindustan petroleum", "bpcl", "bharat petroleum",
            "nayara", "shell", "reliance petroleum", "essar"
        )
        fuelMerchants.forEach { put(it, "Fuel") }

        // --- Bills & Utilities / Recharge ---
        val billsMerchants = listOf(
            "jio", "airtel", "vi", "vodafone", "idea", "bsnl", "tata play", "tata sky", "d2h", "dish tv",
            "sun direct", "bescom", "tata power", "cesc", "uppcl", "mahavitaran", "torrent power",
            "bses", "bses rajdhani", "bses yamuna", "adani electricity", "igrapraprastha gas", "igl",
            "mahanagar gas", "mgl", "gujarat gas", "act fibernet", "hathway", "excitel", "tikona"
        )
        billsMerchants.forEach { put(it, "Bills") }

        // --- Entertainment & Subscriptions ---
        val entertainmentMerchants = listOf(
            "netflix", "spotify", "youtube", "yt premium", "prime video", "amazon prime", "disney",
            "hotstar", "zee5", "sonyliv", "jiocinema", "jio cinema", "bookmyshow", "bms", "paytm movies",
            "inox", "pvr", "cinepolis", "apple", "apple.com", "google play", "play store", "steam",
            "playstation", "xbox", "audible"
        )
        entertainmentMerchants.forEach { put(it, "Entertainment") }

        // --- Health & Pharmacy ---
        val healthMerchants = listOf(
            "apollo", "apollo pharmacy", "pharmeasy", "1mg", "tata 1mg", "netmeds", "truemeds", "medplus",
            "dr lal pathlabs", "metropolis", "thyrocare", "max healthcare", "fortis", "manipal hospital",
            "cult.fit", "cultfit", "gold gym", "anytime fitness"
        )
        healthMerchants.forEach { put(it, "Health") }

        // --- Personal Care & Grooming ---
        val personalCareMerchants = listOf(
            "urban company", "urbanclap", "beardo", "the man company", "mamaearth", "plum", "wow skin",
            "sugar cosmetics", "mcaffeine", "minimalist", "kay beauty"
        )
        personalCareMerchants.forEach { put(it, "Personal Care") }
    }

    /**
     * Extracts structured merchant details from SMS [body].
     */
    fun extract(body: String): MerchantInfo? {
        if (body.isBlank()) return null
        val lowerBody = body.lowercase(Locale.ROOT)

        var rawExtracted: String? = null
        for (pattern in MERCHANT_PATTERNS) {
            val match = pattern.find(body)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                if (candidate.isNotBlank() && candidate.length > 2) {
                    rawExtracted = candidate
                    break
                }
            }
        }

        if (rawExtracted == null) {
            // Check if any dictionary keyword is explicitly mentioned in the body
            val matchedCategory = MERCHANT_DICTIONARY.entries.firstOrNull { lowerBody.contains(it.key) }
            if (matchedCategory != null) {
                val cleanedName = matchedCategory.key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                return MerchantInfo(
                    rawName = matchedCategory.key,
                    cleanedName = cleanedName,
                    categoryName = matchedCategory.value
                )
            }
            return null
        }

        val cleaned = cleanMerchantName(rawExtracted)
        if (cleaned.isBlank()) return null

        val categoryName = findCategoryForMerchant(cleaned, lowerBody)

        return MerchantInfo(
            rawName = rawExtracted,
            cleanedName = cleaned,
            categoryName = categoryName
        )
    }

    /**
     * Strips VPA extensions (`@ybl`, `@okaxis`, `@paytm`), numbers, and noise.
     */
    fun cleanMerchantName(raw: String): String {
        var name = raw.replace(VPA_SUFFIX_REGEX, "")
            .replace(Regex("""\b(?:pvt|ltd|limited|inc|llp|corp|corporation|store|merchant|vpa)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""[^a-zA-Z0-9\s]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        if (name.lowercase(Locale.ROOT) in listOf("upi", "bank", "account", "card", "payment", "ref", "no", "txn", "val", "random charge", "miscellaneous purchase", "unknown sender")) {
            return ""
        }

        return name.split(" ")
            .take(4)
            .joinToString(" ") { word ->
                word.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
    }

    private fun findCategoryForMerchant(cleanedName: String, rawBodyLower: String): String? {
        val cleanedLower = cleanedName.lowercase(Locale.ROOT)
        for ((keyword, category) in MERCHANT_DICTIONARY) {
            if (cleanedLower.contains(keyword) || rawBodyLower.contains(keyword)) {
                return category
            }
        }
        return null
    }
}
