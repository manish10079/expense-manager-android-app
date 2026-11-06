# Play Store listing — Expense Tracker Budget & Spend

Localized listing copy for every language the app ships, ready to paste into
Play Console → Main store listing → Manage translations.

Supported locales (23): `en` (source), `en-US` (Play default / United States),
`en-GB` (United Kingdom), `hi`, `bn`, `mr`, `gu`, `kn`, `ml`, `or`, `pa`, `ta`,
`te`, `as`, `es`, `pt-BR`, `fr`, `de`, `id`, `it`, `ja`, `ko`, `ru`.

Play Store limits, enforced per field below and checked by
`docs/play-store/check_limits.py`:

| Field | Limit |
| --- | --- |
| App name | 30 characters |
| Short description | 80 characters |
| Full description | 4000 characters |

Notes for whoever pastes this in:

- The full description keeps its emoji headings and blank lines; Play renders
  those as-is, so copy the block whole rather than re-typing it.
- The app name is translated where a translated name still reads as a product
  name. Keep `Expense Tracker` in the Latin script if you prefer a single brand
  across locales — the localized names stay within the 30-character limit.
- The Indian-language names are shorter than the English one because the
  `& Spend` tail does not fit in 30 characters in those scripts; they end at
  `: <word for budget>` instead. Tamil is the tightest at 29, so check it after
  any edit.
- The Malayalam, Tamil and French descriptions drop the "whether you want an
  offline manager…" bridge paragraph: those languages run past 4000 characters
  with it. Every other section keeps the full English structure.

---

## English — `en`

### App name

```
Expense Tracker Budget & Spend
```

### Short description

```
Type, talk, scan or share bills. SMS import, budgets & analytics. On-device.
```

### Full description

```
Save money effortlessly, track daily spending, and take control of your personal finances with Expense Tracker Budget & Spend!

TYPE, TALK, SCAN, OR SHARE (OPTIONAL)
One add sheet: type it like a message, speak it, scan a receipt, or share a bill / payment screen from any UPI, banking, or wallet app. Review, then save. Nothing is a transaction until you confirm.

On-device parse always works. Optional AI parse (daily limit) may send that text — not the photo — to third-party AI via our servers. Stay offline or skip AI to keep parsing on-device. Receipt OCR is on-device (ML Kit). Photo Picker and Android Share — no gallery permission.

SMS-BASED MONEY MANAGEMENT (OPTIONAL CORE FEATURE)
With your permission, Expense Tracker uses RECEIVE_SMS to read incoming bank, UPI, and wallet debit/credit messages on your device. Matching messages become suggestions under Detected Transactions (Home ⋮ menu). Tap a card, edit if needed, then Add. Nothing is a transaction until you confirm. Swipe to delete.

This is optional and off until you allow SMS on Home. If you deny it, keypad, type/talk/scan, budgets, and analytics still work. We are not your default SMS app. We do not use READ_SMS or SEND_SMS. We do not send 5-digit payment SMS and we do not use SMS for OTP or fraud detection. OTP, promo, and messages with no amount are ignored. SMS is parsed only on your device and is not uploaded, sold, or shared.

Stop wondering where your money goes. Track on the go with budgets, analytics, wallets — plus optional SMS, voice, scan, and share-from-any-app.

🔑 KEY FEATURES FOR SMARTER MONEY MANAGEMENT

✨ Type, talk, scan, or share
Write it like a text, speak it, scan a receipt, or share a bill from GPay, PhonePe, Paytm, a bank app, or anywhere else. On-device OCR. Optional AI parse with a daily limit.

💬 Optional bank / UPI SMS import
Incoming financial SMS only. Review in Detected Transactions, then Add. Non-financial SMS are ignored. On-device. Optional.

💳 Daily expense tracker & money manager
Instant logging with notes, categories, and payment methods.
Smart categories with customizable icons.
Wallets: cash, cards, bank, and digital wallets.
Itemized calculator: split one payment into line items.
Search, sort, filter; customizable cards; calendar of daily spend.

🎤 Voice add (optional)
Speak a transaction. Review before you save. Mic only if you start voice add.

🎯 Budget planner & savings tracker
Monthly category budgets, including copy last month in one tap.
Overspend alerts even when you are not in the app.
Savings goals with progress toward a target.

📊 Reports & analysis
Cash flow and category / payment-method charts.
Compare periods, trends, and daily averages.
CSV export, JSON export/import, local .db backup (not XLS/PDF).

⏰ Bills & scheduled payments
Daily, weekly, monthly, or yearly rules. Reminders so you don’t miss due dates.

⭐ Favorites
Save frequent expenses and log them in one tap.

🏷️ Tags
Tag transactions beyond categories.

🔒 Offline-first & secure
Logging and budgets work without internet. Ledger stays on-device unless you sign in and turn on sync.
PIN lock, biometric unlock, scrambled keypad, screenshot block, blur in Recents.

🤝 Multi-currency
Currency symbol, grouping (including Indian), and decimals.

🔄 Optional multi-device sync
Google, email, or magic link. Up to 4 devices (Pro).

🎨 Themes
Light, dark, or system.

🚀 WHY CHOOSE EXPENSE TRACKER BUDGET & SPEND?
Pocket tracking and serious budgeting — plus optional SMS, voice, scan, and share-from-any-app you control.

Download Expense Tracker Budget & Spend to start tracking, saving, and reaching your goals.
```

## English (United States) — `en-US`

Same copy as `en`. Paste this into Play Console default language **English (United States)**.

### App name

```
Expense Tracker Budget & Spend
```

### Short description

```
Type, talk, scan or share bills. SMS import, budgets & analytics. On-device.
```

### Full description

```
Save money effortlessly, track daily spending, and take control of your personal finances with Expense Tracker Budget & Spend!

TYPE, TALK, SCAN, OR SHARE (OPTIONAL)
One add sheet: type it like a message, speak it, scan a receipt, or share a bill / payment screen from any UPI, banking, or wallet app. Review, then save. Nothing is a transaction until you confirm.

On-device parse always works. Optional AI parse (daily limit) may send that text — not the photo — to third-party AI via our servers. Stay offline or skip AI to keep parsing on-device. Receipt OCR is on-device (ML Kit). Photo Picker and Android Share — no gallery permission.

SMS-BASED MONEY MANAGEMENT (OPTIONAL CORE FEATURE)
With your permission, Expense Tracker uses RECEIVE_SMS to read incoming bank, UPI, and wallet debit/credit messages on your device. Matching messages become suggestions under Detected Transactions (Home ⋮ menu). Tap a card, edit if needed, then Add. Nothing is a transaction until you confirm. Swipe to delete.

This is optional and off until you allow SMS on Home. If you deny it, keypad, type/talk/scan, budgets, and analytics still work. We are not your default SMS app. We do not use READ_SMS or SEND_SMS. We do not send 5-digit payment SMS and we do not use SMS for OTP or fraud detection. OTP, promo, and messages with no amount are ignored. SMS is parsed only on your device and is not uploaded, sold, or shared.

Stop wondering where your money goes. Track on the go with budgets, analytics, wallets — plus optional SMS, voice, scan, and share-from-any-app.

🔑 KEY FEATURES FOR SMARTER MONEY MANAGEMENT

✨ Type, talk, scan, or share
Write it like a text, speak it, scan a receipt, or share a bill from GPay, PhonePe, Paytm, a bank app, or anywhere else. On-device OCR. Optional AI parse with a daily limit.

💬 Optional bank / UPI SMS import
Incoming financial SMS only. Review in Detected Transactions, then Add. Non-financial SMS are ignored. On-device. Optional.

💳 Daily expense tracker & money manager
Instant logging with notes, categories, and payment methods.
Smart categories with customizable icons.
Wallets: cash, cards, bank, and digital wallets.
Itemized calculator: split one payment into line items.
Search, sort, filter; customizable cards; calendar of daily spend.

🎤 Voice add (optional)
Speak a transaction. Review before you save. Mic only if you start voice add.

🎯 Budget planner & savings tracker
Monthly category budgets, including copy last month in one tap.
Overspend alerts even when you are not in the app.
Savings goals with progress toward a target.

📊 Reports & analysis
Cash flow and category / payment-method charts.
Compare periods, trends, and daily averages.
CSV export, JSON export/import, local .db backup (not XLS/PDF).

⏰ Bills & scheduled payments
Daily, weekly, monthly, or yearly rules. Reminders so you don’t miss due dates.

⭐ Favorites
Save frequent expenses and log them in one tap.

🏷️ Tags
Tag transactions beyond categories.

🔒 Offline-first & secure
Logging and budgets work without internet. Ledger stays on-device unless you sign in and turn on sync.
PIN lock, biometric unlock, scrambled keypad, screenshot block, blur in Recents.

🤝 Multi-currency
Currency symbol, grouping (including Indian), and decimals.

🔄 Optional multi-device sync
Google, email, or magic link. Up to 4 devices (Pro).

🎨 Themes
Light, dark, or system.

🚀 WHY CHOOSE EXPENSE TRACKER BUDGET & SPEND?
Pocket tracking and serious budgeting — plus optional SMS, voice, scan, and share-from-any-app you control.

Download Expense Tracker Budget & Spend to start tracking, saving, and reaching your goals.
```

## English (United Kingdom) — `en-GB`

Paste into Play Console **English (United Kingdom)**. Same features as US English; UK spelling where it differs.

### App name

```
Expense Tracker Budget & Spend
```

### Short description

```
Type, talk, scan or share bills. SMS import, budgets & analytics. On-device.
```

### Full description

```
Save money effortlessly, track daily spending, and take control of your personal finances with Expense Tracker Budget & Spend!

TYPE, TALK, SCAN, OR SHARE (OPTIONAL)
One add sheet: type it like a message, speak it, scan a receipt, or share a bill / payment screen from any UPI, banking, or wallet app. Review, then save. Nothing is a transaction until you confirm.

On-device parse always works. Optional AI parse (daily limit) may send that text — not the photo — to third-party AI via our servers. Stay offline or skip AI to keep parsing on-device. Receipt OCR is on-device (ML Kit). Photo Picker and Android Share — no gallery permission.

SMS-BASED MONEY MANAGEMENT (OPTIONAL CORE FEATURE)
With your permission, Expense Tracker uses RECEIVE_SMS to read incoming bank, UPI, and wallet debit/credit messages on your device. Matching messages become suggestions under Detected Transactions (Home ⋮ menu). Tap a card, edit if needed, then Add. Nothing is a transaction until you confirm. Swipe to delete.

This is optional and off until you allow SMS on Home. If you deny it, keypad, type/talk/scan, budgets, and analytics still work. We are not your default SMS app. We do not use READ_SMS or SEND_SMS. We do not send 5-digit payment SMS and we do not use SMS for OTP or fraud detection. OTP, promo, and messages with no amount are ignored. SMS is parsed only on your device and is not uploaded, sold, or shared.

Stop wondering where your money goes. Track on the go with budgets, analytics, wallets — plus optional SMS, voice, scan, and share-from-any-app.

🔑 KEY FEATURES FOR SMARTER MONEY MANAGEMENT

✨ Type, talk, scan, or share
Write it like a text, speak it, scan a receipt, or share a bill from GPay, PhonePe, Paytm, a bank app, or anywhere else. On-device OCR. Optional AI parse with a daily limit.

💬 Optional bank / UPI SMS import
Incoming financial SMS only. Review in Detected Transactions, then Add. Non-financial SMS are ignored. On-device. Optional.

💳 Daily expense tracker & money manager
Instant logging with notes, categories, and payment methods.
Smart categories with customisable icons.
Wallets: cash, cards, bank, and digital wallets.
Itemised calculator: split one payment into line items.
Search, sort, filter; customisable cards; calendar of daily spend.

🎤 Voice add (optional)
Speak a transaction. Review before you save. Mic only if you start voice add.

🎯 Budget planner & savings tracker
Monthly category budgets, including copy last month in one tap.
Overspend alerts even when you are not in the app.
Savings goals with progress toward a target.

📊 Reports & analysis
Cash flow and category / payment-method charts.
Compare periods, trends, and daily averages.
CSV export, JSON export/import, local .db backup (not XLS/PDF).

⏰ Bills & scheduled payments
Daily, weekly, monthly, or yearly rules. Reminders so you don’t miss due dates.

⭐ Favourites
Save frequent expenses and log them in one tap.

🏷️ Tags
Tag transactions beyond categories.

🔒 Offline-first & secure
Logging and budgets work without internet. Ledger stays on-device unless you sign in and turn on sync.
PIN lock, biometric unlock, scrambled keypad, screenshot block, blur in Recents.

🤝 Multi-currency
Currency symbol, grouping (including Indian), and decimals.

🔄 Optional multi-device sync
Google, email, or magic link. Up to 4 devices (Pro).

🎨 Themes
Light, dark, or system.

🚀 WHY CHOOSE EXPENSE TRACKER BUDGET & SPEND?
Pocket tracking and serious budgeting — plus optional SMS, voice, scan, and share-from-any-app you control.

Download Expense Tracker Budget & Spend to start tracking, saving, and reaching your goals.
```

## हिन्दी — `hi`

### App name

```
एक्सपेंस ट्रैकर: बजट और खर्च
```

### Short description

```
लिखें, बोलें, स्कैन या बिल शेयर करें। SMS, बजट। सिर्फ़ डिवाइस पर।
```

### Full description

```
एक्सपेंस ट्रैकर बजट और खर्च के साथ आसानी से पैसे बचाएं, रोज़ का खर्च ट्रैक करें और अपनी पर्सनल फाइनेंस पर कंट्रोल पाएं!

लिखें, बोलें, स्कैन या शेयर करें (वैकल्पिक)
एक ऐड शीट: संदेश की तरह लिखें, बोलें, रसीद स्कैन करें, या किसी भी UPI, बैंक या वॉलेट ऐप से बिल / पेमेंट स्क्रीन शेयर करें। देखें, फिर सेव करें। पुष्टि से पहले कोई लेन-देन नहीं।

ऑन-डिवाइस पार्स हमेशा चलता है। वैकल्पिक AI पार्स (दैनिक सीमा) वह टेक्स्ट — फ़ोटो नहीं — हमारे सर्वर से थर्ड-पार्टी AI को भेज सकता है। ऑफ़लाइन रहें या AI छोड़ें तो पार्स डिवाइस पर रहेगा। रसीद OCR ऑन-डिवाइस (ML Kit)। Photo Picker और Android Share — गैलरी अनुमति नहीं।

SMS पर आधारित मनी मैनेजमेंट (वैकल्पिक मुख्य सुविधा)
आपकी अनुमति से ऐप RECEIVE_SMS से डिवाइस पर आने वाले बैंक, UPI और वॉलेट डेबिट/क्रेडिट संदेश पढ़ता है। मिलते संदेश Detected Transactions (होम ⋮ मेन्यू) में सुझाव बनते हैं। कार्ड टैप करें, ज़रूरत हो तो बदलें, Add दबाएं। पुष्टि से पहले लेन-देन नहीं। हटाने के लिए स्वाइप करें।

यह वैकल्पिक है और होम पर SMS अनुमति तक बंद रहता है। मना करने पर भी कीपैड, लिखें/बोलें/स्कैन, बजट और एनालिटिक्स चलते हैं। हम डिफ़ॉल्ट SMS ऐप नहीं। READ_SMS या SEND_SMS नहीं। 5 अंकों वाले पेमेंट SMS नहीं भेजते; OTP/धोखाधड़ी के लिए SMS नहीं। OTP, प्रोमो और बिना रकम वाले संदेश नज़रअंदाज़। SMS सिर्फ़ डिवाइस पर पढ़ा जाता है; अपलोड, बेचा या साझा नहीं।

पैसा कहां जाता है, सोचना छोड़ें। बजट, एनालिटिक्स, वॉलेट — और वैकल्पिक SMS, वॉइस, स्कैन व किसी भी ऐप से शेयर।

🔑 समझदारी से पैसा संभालने की मुख्य सुविधाएं

✨ लिखें, बोलें, स्कैन या शेयर करें
टेक्स्ट की तरह लिखें, बोलें, रसीद स्कैन करें, या GPay, PhonePe, Paytm, बैंक ऐप या कहीं से बिल शेयर करें। ऑन-डिवाइस OCR। वैकल्पिक AI पार्स, दैनिक सीमा।

💬 वैकल्पिक बैंक / UPI SMS इंपोर्ट
सिर्फ़ फ़ाइनेंशियल SMS। Detected Transactions में देखें, Add करें। बाकी SMS नज़रअंदाज़। डिवाइस पर। वैकल्पिक।

💳 रोज़ का खर्च ट्रैकर और मनी मैनेजर
नोट, कैटेगरी, पेमेंट मेथड के साथ तुरंत एंट्री।
कस्टमाइज़ आइकन वाली स्मार्ट कैटेगरी।
वॉलेट: कैश, कार्ड, बैंक, डिजिटल।
आइटमाइज़्ड कैलकुलेटर: एक भुगतान कई लाइनों में।
खोज, क्रम, फ़िल्टर; कस्टम कार्ड; रोज़ के खर्च का कैलेंडर।

🎤 वॉइस से जोड़ें (वैकल्पिक)
बोलकर बताएं। सेव से पहले देखें। माइक सिर्फ़ वॉइस ऐड शुरू करने पर।

🎯 बजट प्लानर और बचत ट्रैकर
मासिक कैटेगरी बजट, एक टैप में पिछला महीना कॉपी।
ऐप के बाहर भी ज़्यादा खर्च अलर्ट।
लक्ष्य के साथ बचत गोल।

📊 रिपोर्ट और विश्लेषण
कैश फ़्लो और कैटेगरी / पेमेंट चार्ट।
अवधि, ट्रेंड, औसत की तुलना।
CSV, JSON इंपोर्ट/एक्सपोर्ट, लोकल .db बैकअप (XLS/PDF नहीं)।

⏰ बिल और शेड्यूल्ड भुगतान
रोज़ / हफ़्ते / महीने / साल के नियम। देय तारीख के रिमाइंडर।

⭐ पसंदीदा
बार-बार खर्च एक टैप में।

🏷️ टैग
कैटेगरी से आगे टैग।

🔒 ऑफ़लाइन-फ़र्स्ट और सुरक्षित
एंट्री और बजट बिना इंटरनेट। सिंक चालू न हो तो लेजर डिवाइस पर।
PIN, बायोमेट्रिक, स्क्रैम्बल्ड कीपैड, स्क्रीनशॉट ब्लॉक, Recents ब्लर।

🤝 मल्टी-करेंसी
सिंबल, ग्रुपिंग (भारतीय सहित), दशमलव।

🔄 वैकल्पिक मल्टी-डिवाइस सिंक
Google, ईमेल या मैजिक लिंक। 4 डिवाइस तक (Pro)।

🎨 थीम
लाइट, डार्क या सिस्टम।

🚀 एक्सपेंस ट्रैकर बजट और खर्च क्यों चुनें?
जेब और गंभीर बजटिंग — साथ वैकल्पिक SMS, वॉइस, स्कैन और किसी भी ऐप से शेयर, आपके कंट्रोल में।

ट्रैकिंग, बचत और लक्ष्य के लिए एक्सपेंस ट्रैकर बजट और खर्च डाउनलोड करें।
```

---

## বাংলা — `bn`

### App name

```
এক্সপেন্স ট্র্যাকার: বাজেট
```

### Short description

```
লিখুন, বলুন, স্ক্যান বা বিল শেয়ার করুন। SMS, বাজেট। শুধু ডিভাইসে।
```

### Full description

```
এক্সপেন্স ট্র্যাকার বাজেট ও খরচ দিয়ে সহজে টাকা বাঁচান, রোজকার খরচ ট্র্যাক করুন এবং ব্যক্তিগত অর্থের নিয়ন্ত্রণ নিন!

লিখুন, বলুন, স্ক্যান বা শেয়ার (ঐচ্ছিক)
একটি অ্যাড শিট: মেসেজের মতো লিখুন, বলুন, রসিদ স্ক্যান করুন, বা যেকোনো UPI, ব্যাংক বা ওয়ালেট অ্যাপ থেকে বিল / পেমেন্ট স্ক্রিন শেয়ার করুন। দেখে সেভ করুন। নিশ্চিত করার আগে কোনো লেনদেন হয় না।

অন-ডিভাইস পার্স সবসময় কাজ করে। ঐচ্ছিক AI পার্স (দৈনিক সীমা) সেই টেক্সট — ছবি নয় — আমাদের সার্ভার দিয়ে থার্ড-পার্টি AI-তে পাঠাতে পারে। অফলাইনে থাকুন বা AI বাদ দিন, পার্স ডিভাইসেই থাকবে। রসিদ OCR অন-ডিভাইস (ML Kit)। Photo Picker ও Android Share — গ্যালারি পারমিশন নেই।

SMS-ভিত্তিক মানি ম্যানেজমেন্ট (ঐচ্ছিক মূল ফিচার)
আপনার অনুমতিতে অ্যাপ RECEIVE_SMS দিয়ে ডিভাইসে আসা ব্যাংক, UPI ও ওয়ালেট ডেবিট/ক্রেডিট মেসেজ পড়ে। মিললে Detected Transactions (হোম ⋮ মেনু)-এ সাজেশন হয়। কার্ড ট্যাপ করুন, দরকার হলে সম্পাদনা, Add চাপুন। নিশ্চিত করার আগে লেনদেন নয়। মুছতে সোয়াইপ করুন।

এটি ঐচ্ছিক এবং হোমে SMS অনুমতি না দেওয়া পর্যন্ত বন্ধ। অস্বীকার করলেও কিপ্যাড, লিখুন/বলুন/স্ক্যান, বাজেট ও অ্যানালিটিক্স চলে। আমরা ডিফল্ট SMS অ্যাপ নই। READ_SMS বা SEND_SMS ব্যবহার করি না। ৫ অঙ্কের পেমেন্ট SMS পাঠাই না; OTP/প্রতারণার জন্য SMS নয়। OTP, প্রমো ও ছাড়া অঙ্কের মেসেজ উপেক্ষা। SMS শুধু ডিভাইসে পড়া হয়; আপলোড, বিক্রি বা শেয়ার হয় না।

টাকা কোথায় যায় ভেবে থামুন। বাজেট, অ্যানালিটিক্স, ওয়ালেট — প্লাস ঐচ্ছিক SMS, ভয়েস, স্ক্যান ও যেকোনো অ্যাপ থেকে শেয়ার।

🔑 স্মার্ট মানি ম্যানেজমেন্টের মূল ফিচার

✨ লিখুন, বলুন, স্ক্যান বা শেয়ার
টেক্সটের মতো লিখুন, বলুন, রসিদ স্ক্যান করুন, বা GPay, PhonePe, Paytm, ব্যাংক অ্যাপ বা যেকোনো জায়গা থেকে বিল শেয়ার করুন। অন-ডিভাইস OCR। ঐচ্ছিক AI পার্স, দৈনিক সীমা।

💬 ঐচ্ছিক ব্যাংক / UPI SMS ইমপোর্ট
শুধু আর্থিক SMS। Detected Transactions-এ দেখে Add। অন্য SMS উপেক্ষা। ডিভাইসে। ঐচ্ছিক।

💳 দৈনিক খরচ ট্র্যাকার ও মানি ম্যানেজার
নোট, ক্যাটাগরি, পেমেন্ট মেথডসহ তাড়াতাড়ি এন্ট্রি।
কাস্টম আইকনসহ স্মার্ট ক্যাটাগরি।
ওয়ালেট: ক্যাশ, কার্ড, ব্যাংক, ডিজিটাল।
আইটেমাইজড ক্যালকুলেটর: এক পেমেন্ট কয়েক লাইনে।
সার্চ, সর্ট, ফিল্টার; কাস্টম কার্ড; দৈনিক খরচের ক্যালেন্ডার।

🎤 ভয়েস অ্যাড (ঐচ্ছিক)
বলে যোগ করুন। সেভের আগে দেখুন। মাইক শুধু ভয়েস অ্যাড শুরু করলে।

🎯 বাজেট প্ল্যানার ও সেভিংস ট্র্যাকার
মাসিক ক্যাটাগরি বাজেট, এক ট্যাপে আগের মাস কপি।
অ্যাপের বাইরেও অতিরিক্ত খরচ অ্যালার্ট।
লক্ষ্যসহ সেভিংস গোল।

📊 রিপোর্ট ও বিশ্লেষণ
ক্যাশ ফ্লো ও ক্যাটাগরি / পেমেন্ট চার্ট।
পিরিয়ড, ট্রেন্ড, গড় তুলনা।
CSV, JSON ইমপোর্ট/এক্সপোর্ট, লোকাল .db ব্যাকআপ (XLS/PDF নয়)।

⏰ বিল ও শিডিউল পেমেন্ট
দৈনিক / সাপ্তাহিক / মাসিক / বার্ষিক নিয়ম। তারিখের রিমাইন্ডার।

⭐ ফেভারিট
ঘন ঘন খরচ এক ট্যাপে।

🏷️ ট্যাগ
ক্যাটাগরির বাইরে ট্যাগ।

🔒 অফলাইন-ফার্স্ট ও নিরাপদ
এন্ট্রি ও বাজেট ইন্টারনেট ছাড়া। সিঙ্ক না চালালে লেজার ডিভাইসে।
PIN, বায়োমেট্রিক, স্ক্র্যাম্বল কিপ্যাড, স্ক্রিনশট ব্লক, Recents ব্লার।

🤝 মাল্টি-কারেন্সি
সিম্বল, গ্রুপিং (ভারতীয়সহ), দশমিক।

🔄 ঐচ্ছিক মাল্টি-ডিভাইস সিঙ্ক
Google, ইমেইল বা ম্যাজিক লিংক। ৪টি ডিভাইস পর্যন্ত (Pro)।

🎨 থিম
লাইট, ডার্ক বা সিস্টেম।

🚀 কেন এক্সপেন্স ট্র্যাকার বাজেট ও খরচ?
পকেট ট্র্যাকিং ও সিরিয়াস বাজেটিং — প্লাস ঐচ্ছিক SMS, ভয়েস, স্ক্যান ও যেকোনো অ্যাপ থেকে শেয়ার, আপনার নিয়ন্ত্রণে।

ট্র্যাকিং, সঞ্চয় ও লক্ষ্যের জন্য এক্সপেন্স ট্র্যাকার বাজেট ও খরচ ডাউনলোড করুন।
```

---

## मराठी — `mr`

### App name

```
एक्सपेंस ट्रॅकर: बजेट आणि खर्च
```

### Short description

```
लिहा, बोला, स्कॅन किंवा बिल शेअर करा. SMS, बजेट. फक्त डिव्हाइसवर.
```

### Full description

```
एक्सपेन्स ट्रॅकर बजेट आणि खर्चाने पैसे वाचवा, रोजचा खर्च ट्रॅक करा आणि वैयक्तिक फायनान्स नियंत्रित करा!

लिहा, बोला, स्कॅन किंवा शेअर (पर्यायी)
एक अॅड शीट: मेसेजसारखे लिहा, बोला, पावती स्कॅन करा, किंवा कोणत्याही UPI, बँक किंवा वॉलेट अॅपमधून बिल / पेमेंट स्क्रीन शेअर करा. तपासा, मग सेव्ह करा. पुष्टीपूर्वी व्यवहार नाही.

ऑन-डिव्हाइस पार्स नेहमी चालते. पर्यायी AI पार्स (दैनिक मर्यादा) तो मजकूर — फोटो नाही — आमच्या सर्व्हरने तृतीय-पक्ष AI कडे पाठवू शकतो. ऑफलाइन राहा किंवा AI वगळा तर पार्स डिव्हाइसवरच. पावती OCR ऑन-डिव्हाइस (ML Kit). Photo Picker आणि Android Share — गॅलरी परवानगी नाही.

SMS-आधारित मनी मॅनेजमेंट (पर्यायी मुख्य वैशिष्ट्य)
तुमच्या परवानगीने अॅप RECEIVE_SMS ने डिव्हाइसवरील बँक, UPI आणि वॉलेट डेबिट/क्रेडिट मेसेज वाचते. जुळणारे Detected Transactions (होम ⋮ मेनू) मध्ये सूचना होतात. कार्ड टॅप करा, गरज असल्यास बदला, Add दाबा. पुष्टीपूर्वी व्यवहार नाही. काढण्यासाठी स्वाइप करा.

हे पर्यायी आहे आणि होमवर SMS परवानगी होईपर्यंत बंद. नाकारले तरी कीपॅड, लिहा/बोला/स्कॅन, बजेट आणि विश्लेषण चालतात. आम्ही डिफॉल्ट SMS अॅप नाही. READ_SMS किंवा SEND_SMS नाही. ५ अंकी पेमेंट SMS पाठवत नाही; OTP/फसवणुकीसाठी SMS नाही. OTP, प्रोमो आणि रक्कम नसलेले मेसेज दुर्लक्षित. SMS फक्त डिव्हाइसवर वाचले जाते; अपलोड, विक्री किंवा शेअर नाही.

पैसे कुठे जातात ते शोधणे सोडा. बजेट, विश्लेषण, वॉलेट — अधिक पर्यायी SMS, व्हॉइस, स्कॅन आणि कोणत्याही अॅपमधून शेअर.

🔑 स्मार्ट मनी मॅनेजमेंटची मुख्य वैशिष्ट्ये

✨ लिहा, बोला, स्कॅन किंवा शेअर
टेक्स्टसारखे लिहा, बोला, पावती स्कॅन करा, किंवा GPay, PhonePe, Paytm, बँक अॅप किंवा कुठूनही बिल शेअर करा. ऑन-डिव्हाइस OCR. पर्यायी AI पार्स, दैनिक मर्यादा.

💬 पर्यायी बँक / UPI SMS आयात
फक्त आर्थिक SMS. Detected Transactions मध्ये पहा, Add करा. इतर SMS दुर्लक्षित. डिव्हाइसवर. पर्यायी.

💳 रोजचा खर्च ट्रॅकर आणि मनी मॅनेजर
नोट, श्रेणी, पेमेंट पद्धतींसह त्वरित नोंद.
कस्टम आयकॉनसह स्मार्ट श्रेणी.
वॉलेट: रोख, कार्ड, बँक, डिजिटल.
आयटमाइज्ड कॅल्क्युलेटर: एक पेमेंट अनेक ओळींत.
शोध, क्रम, फिल्टर; कस्टम कार्ड; रोजच्या खर्चाचा कॅलेंडर.

🎤 व्हॉइस अॅड (पर्यायी)
बोलून सांगा. सेव्हपूर्वी तपासा. माइक फक्त व्हॉइस अॅड सुरू केल्यावर.

🎯 बजेट प्लॅनर आणि बचत ट्रॅकर
मासिक श्रेणी बजेट, एका टॅपने मागील महिना कॉपी.
अॅपबाहेरही जास्त खर्चाची सूचना.
लक्ष्यासह बचत गोल.

📊 अहवाल आणि विश्लेषण
कॅश फ्लो आणि श्रेणी / पेमेंट चार्ट.
कालावधी, ट्रेंड, सरासरी तुलना.
CSV, JSON आयात/निर्यात, लोकल .db बॅकअप (XLS/PDF नाही).

⏰ बिले आणि शेड्यूल पेमेंट
दैनिक / साप्ताहिक / मासिक / वार्षिक नियम. देय तारखेचे रिमाइंडर.

⭐ आवडते
वारंवार खर्च एका टॅपने.

🏷️ टॅग
श्रेणीपलीकडे टॅग.

🔒 ऑफलाइन-फर्स्ट आणि सुरक्षित
नोंद आणि बजेट इंटरनेटशिवाय. सिंक नसेल तर लेजर डिव्हाइसवर.
PIN, बायोमेट्रिक, स्क्रॅम्बल्ड कीपॅड, स्क्रीनशॉट ब्लॉक, Recents ब्लर.

🤝 मल्टी-करन्सी
चिन्ह, ग्रुपिंग (भारतीयसहित), दशांश.

🔄 पर्यायी मल्टी-डिव्हाइस सिंक
Google, ईमेल किंवा मॅजिक लिंक. ४ डिव्हाइस पर्यंत (Pro).

🎨 थीम
लाइट, डार्क किंवा सिस्टम.

🚀 एक्सपेन्स ट्रॅकर बजेट आणि खर्च का?
खिशातील ट्रॅकिंग आणि गांभीर्याने बजेटिंग — अधिक पर्यायी SMS, व्हॉइस, स्कॅन आणि कोणत्याही अॅपमधून शेअर, तुमच्या नियंत्रणात.

ट्रॅकिंग, बचत आणि उद्दिष्टांसाठी एक्सपेन्स ट्रॅकर बजेट आणि खर्च डाउनलोड करा.
```

---

## ગુજરાતી — `gu`

### App name

```
એક્સપેન્સ ટ્રેકર: બજેટ
```

### Short description

```
લખો, બોલો, સ્કેન કે બિલ શેર કરો. SMS, બજેટ. ફક્ત ડિવાઇસ પર.
```

### Full description

```
એક્સપેન્સ ટ્રેકર બજેટ અને ખર્ચ સાથે પૈસા બચાવો, રોજિંદા ખર્ચ ટ્રેક કરો અને વ્યક્તિગત ફાઇનાન્સ નિયંત્રિત કરો!

લખો, બોલો, સ્કેન કે શેર (વૈકલ્પિક)
એક ઍડ શીટ: મેસેજની જેમ લખો, બોલો, રસીદ સ્કેન કરો, અથવા કોઈપણ UPI, બેંક કે વૉલેટ ઍપમાંથી બિલ / પેમેન્ટ સ્ક્રીન શેર કરો. તપાસો, પછી સેવ કરો. પુષ્ટિ પહેલાં વ્યવહાર નહીં.

ઑન-ડિવાઇસ પાર્સ હંમેશાં ચાલે છે. વૈકલ્પિક AI પાર્સ (દૈનિક મર્યાદા) તે ટેક્સ્ટ — ફોટો નહીં — અમારા સર્વર દ્વારા તૃતીય-પક્ષ AIને મોકલી શકે. ઑફલાઇન રહો કે AI છોડો તો પાર્સ ડિવાઇસ પર. રસીદ OCR ઑન-ડિવાઇસ (ML Kit). Photo Picker અને Android Share — ગેલેરી પરવાનગી નહીં.

SMS આધારિત મની મેનેજમેન્ટ (વૈકલ્પિક મુખ્ય સુવિધા)
તમારી પરવાનગીથી ઍપ RECEIVE_SMS વડે ડિવાઇસ પરના બેંક, UPI અને વૉલેટ ડેબિટ/ક્રેડિટ સંદેશ વાંચે છે. મળતા Detected Transactions (હોમ ⋮ મેનૂ)માં સૂચન બને. કાર્ડ ટૅપ કરો, જરૂર હોય તો બદલો, Add દબાવો. પુષ્ટિ પહેલાં વ્યવહાર નહીં. કાઢવા સ્વાઇપ કરો.

આ વૈકલ્પિક છે અને હોમ પર SMS પરવાનગી સુધી બંધ. ના પાડો તો પણ કીપેડ, લખો/બોલો/સ્કેન, બજેટ અને એનાલિટિક્સ ચાલે. અમે ડિફોલ્ટ SMS ઍપ નથી. READ_SMS કે SEND_SMS નહીં. 5 અંકના પેમેન્ટ SMS નહીં; OTP/છેતરપિંડી માટે SMS નહીં. OTP, પ્રોમો અને રકમ વગરના સંદેશ અવગણાય. SMS ફક્ત ડિવાઇસ પર વંચાય; અપલોડ, વેચાણ કે શેર નહીં.

પૈસા ક્યાં જાય છે તે છોડો. બજેટ, એનાલિટિક્સ, વૉલેટ — વત્તા વૈકલ્પિક SMS, વૉઇસ, સ્કેન અને કોઈપણ ઍપમાંથી શેર.

🔑 સ્માર્ટ મની મેનેજમેન્ટની મુખ્ય સુવિધાઓ

✨ લખો, બોલો, સ્કેન કે શેર
ટેક્સ્ટની જેમ લખો, બોલો, રસીદ સ્કેન કરો, અથવા GPay, PhonePe, Paytm, બેંક ઍપ કે ગમે ત્યાંથી બિલ શેર કરો. ઑન-ડિવાઇસ OCR. વૈકલ્પિક AI પાર્સ, દૈનિક મર્યાદા.

💬 વૈકલ્પિક બેંક / UPI SMS ઇમ્પોર્ટ
ફક્ત નાણાકીય SMS. Detected Transactionsમાં જુઓ, Add કરો. અન્ય SMS અવગણાય. ડિવાઇસ પર. વૈકલ્પિક.

💳 રોજિંદો ખર્ચ ટ્રેકર અને મની મેનેજર
નોંધ, કૅટેગરી, પેમેન્ટ પદ્ધતિ સાથે ઝડપી એન્ટ્રી.
કસ્ટમ આયકન સાથે સ્માર્ટ કૅટેગરી.
વૉલેટ: કેશ, કાર્ડ, બેંક, ડિજિટલ.
આઇટમાઇઝ્ડ કેલ્ક્યુલેટર: એક પેમેન્ટ ઘણી લાઇનમાં.
શોધ, ક્રમ, ફિલ્ટર; કસ્ટમ કાર્ડ; રોજના ખર્ચનું કૅલેન્ડર.

🎤 વૉઇસ ઍડ (વૈકલ્પિક)
બોલીને કહો. સેવ પહેલાં તપાસો. માઇક ફક્ત વૉઇસ ઍડ શરૂ કરો ત્યારે.

🎯 બજેટ પ્લાનર અને બચત ટ્રેકર
માસિક કૅટેગરી બજેટ, એક ટૅપમાં પાછલો મહિનો કૉપિ.
ઍપ બહાર પણ વધુ ખર્ચ એલર્ટ.
લક્ષ્ય સાથે સેવિંગ્સ ગોલ.

📊 રિપોર્ટ અને વિશ્લેષણ
કેશ ફ્લો અને કૅટેગરી / પેમેન્ટ ચાર્ટ.
સમયગાળો, ટ્રેન્ડ, સરેરાશ સરખામણી.
CSV, JSON ઇમ્પોર્ટ/એક્સપોર્ટ, લોકલ .db બેકઅપ (XLS/PDF નહીં).

⏰ બિલ અને શેડ્યૂલ પેમેન્ટ
દૈનિક / સાપ્તાહિક / માસિક / વાર્ષિક નિયમો. તારીખ રિમાઇન્ડર.

⭐ મનપસંદ
વારંવાર ખર્ચ એક ટૅપમાં.

🏷️ ટૅગ
કૅટેગરીથી આગળ ટૅગ.

🔒 ઑફલાઇન-ફર્સ્ટ અને સુરક્ષિત
એન્ટ્રી અને બજેટ ઇન્ટરનેટ વગર. સિંક ન હોય તો લેજર ડિવાઇસ પર.
PIN, બાયોમેટ્રિક, સ્ક્રેમ્બલ્ડ કીપેડ, સ્ક્રીનશોટ બ્લોક, Recents બ્લર.

🤝 મલ્ટિ-કરન્સી
ચિહ્ન, ગ્રુપિંગ (ભારતીય સહિત), દશાંશ.

🔄 વૈકલ્પિક મલ્ટિ-ડિવાઇસ સિંક
Google, ઈમેલ અથવા મેજિક લિંક. 4 ડિવાઇસ સુધી (Pro).

🎨 થીમ
લાઇટ, ડાર્ક અથવા સિસ્ટમ.

🚀 એક્સપેન્સ ટ્રેકર બજેટ અને ખર્ચ કેમ?
ખિસ્સાનું ટ્રેકિંગ અને ગંભીર બજેટિંગ — વત્તા વૈકલ્પિક SMS, વૉઇસ, સ્કેન અને કોઈપણ ઍપમાંથી શેર, તમારા નિયંત્રણમાં.

ટ્રેકિંગ, બચત અને લક્ષ્ય માટે એક્સપેન્સ ટ્રેકર બજેટ અને ખર્ચ ડાઉનલોડ કરો.
```

---

## ಕನ್ನಡ — `kn`

### App name

```
ಎಕ್ಸ್‌ಪೆನ್ಸ್ ಟ್ರ್ಯಾಕರ್: ಬಜೆಟ್
```

### Short description

```
ಬರೆಯಿರಿ, ಮಾತನಾಡಿ, ಸ್ಕ್ಯಾನ್ ಅಥವಾ ಬಿಲ್ ಹಂಚಿ. SMS, ಬಜೆಟ್. ಸಾಧನದಲ್ಲೇ.
```

### Full description

```
ಎಕ್ಸ್‌ಪೆನ್ಸ್ ಟ್ರ್ಯಾಕರ್ ಬಜೆಟ್ ಮತ್ತು ಖರ್ಚಿನೊಂದಿಗೆ ಹಣ ಉಳಿಸಿ, ದೈನಂದಿನ ಖರ್ಚು ಟ್ರ್ಯಾಕ್ ಮಾಡಿ, ವೈಯಕ್ತಿಕ ಹಣಕಾಸು ನಿಯಂತ್ರಿಸಿ!

ಬರೆಯಿರಿ, ಮಾತನಾಡಿ, ಸ್ಕ್ಯಾನ್ ಅಥವಾ ಹಂಚಿ (ಐಚ್ಛಿಕ)
ಒಂದು ಆಡ್ ಶೀಟ್: ಸಂದೇಶದಂತೆ ಬರೆಯಿರಿ, ಮಾತನಾಡಿ, ರಸೀದಿ ಸ್ಕ್ಯಾನ್ ಮಾಡಿ, ಅಥವಾ ಯಾವುದೇ UPI, ಬ್ಯಾಂಕ್ ಅಥವಾ ವಾಲೆಟ್ ಅಪ್ಲಿನಿಂದ ಬಿಲ್ / ಪಾವತಿ ಸ್ಕ್ರೀನ್ ಹಂಚಿ. ಪರಿಶೀಲಿಸಿ, ನಂತರ ಉಳಿಸಿ. ದೃಢೀಕರಣದ ಮೊದಲು ವಹಿವಾಟು ಇಲ್ಲ.

ಸಾಧನದಲ್ಲಿನ ಪಾರ್ಸ್ ಯಾವಾಗಲೂ ಕೆಲಸ ಮಾಡುತ್ತದೆ. ಐಚ್ಛಿಕ AI ಪಾರ್ಸ್ (ದೈನಂದಿನ ಮಿತಿ) ಆ ಪಠ್ಯವನ್ನು — ಫೋಟೋ ಅಲ್ಲ — ನಮ್ಮ ಸರ್ವರ್ ಮೂಲಕ ಥರ್ಡ್-ಪಾರ್ಟಿ AI ಗೆ ಕಳುಹಿಸಬಹುದು. ಆಫ್‌ಲೈನ್ ಇರಿ ಅಥವಾ AI ಬಿಟ್ಟರೆ ಪಾರ್ಸ್ ಸಾಧನದಲ್ಲೇ. ರಸೀದಿ OCR ಸಾಧನದಲ್ಲಿ (ML Kit). Photo Picker ಮತ್ತು Android Share — ಗ್ಯಾಲರಿ ಅನುಮತಿ ಇಲ್ಲ.

SMS ಆಧಾರಿತ ಹಣ ನಿರ್ವಹಣೆ (ಐಚ್ಛಿಕ ಮುಖ್ಯ ವೈಶಿಷ್ಟ್ಯ)
ನಿಮ್ಮ ಅನುಮತಿಯಿಂದ ಅಪ್ಲಿ RECEIVE_SMS ನಿಂದ ಸಾಧನದ ಬ್ಯಾಂಕ್, UPI ಮತ್ತು ವಾಲೆಟ್ ಡೆಬಿಟ್/ಕ್ರೆಡಿಟ್ ಸಂದೇಶ ಓದುತ್ತದೆ. ಹೊಂದುವವು Detected Transactions (ಹೋಮ್ ⋮ ಮೆನು) ನಲ್ಲಿ ಸಲಹೆ. ಕಾರ್ಡ್ ಟ್ಯಾಪ್, ಬೇಕಾದರೆ ಸಂಪಾದಿಸಿ, Add ಒತ್ತಿ. ದೃಢೀಕರಣದ ಮೊದಲು ವಹಿವಾಟು ಇಲ್ಲ. ಅಳಿಸಲು ಸ್ವೈಪ್.

ಇದು ಐಚ್ಛಿಕ; ಹೋಮ್‌ನಲ್ಲಿ SMS ಅನುಮತಿ ತನಕ ಆಫ್. ನಿರಾಕರಿಸಿದರೂ ಕೀಪ್ಯಾಡ್, ಬರೆಯಿರಿ/ಮಾತು/ಸ್ಕ್ಯಾನ್, ಬಜೆಟ್ ಮತ್ತು ಅನಾಲಿಟಿಕ್ಸ್ ಕೆಲಸ. ನಾವು ಡಿಫಾಲ್ಟ್ SMS ಅಪ್ಲಿ ಅಲ್ಲ. READ_SMS ಅಥವಾ SEND_SMS ಇಲ್ಲ. 5 ಅಂಕಿಯ ಪಾವತಿ SMS ಕಳುಹಿಸುವುದಿಲ್ಲ; OTP/ವಂಚನೆಗೆ SMS ಇಲ್ಲ. OTP, ಪ್ರೊಮೋ, ಮೊತ್ತವಿಲ್ಲದ ಸಂದೇಶ ನಿರ್ಲಕ್ಷ. SMS ಸಾಧನದಲ್ಲಿ ಮಾತ್ರ ಓದಲಾಗುತ್ತದೆ; ಅಪ್‌ಲೋಡ್, ಮಾರಾಟ ಅಥವಾ ಹಂಚಿಕೆ ಇಲ್ಲ.

ಹಣ ಎಲ್ಲಿಹೋಗುತ್ತದೆ ಎಂದು ಯೋಚಿಸುವುದನ್ನು ನಿಲ್ಲಿಸಿ. ಬಜೆಟ್, ಅನಾಲಿಟಿಕ್ಸ್, ವಾಲೆಟ್ — ಜೊತೆಗೆ ಐಚ್ಛಿಕ SMS, ಧ್ವನಿ, ಸ್ಕ್ಯಾನ್ ಮತ್ತು ಯಾವುದೇ ಅಪ್ಲಿನಿಂದ ಹಂಚಿಕೆ.

🔑 ಸ್ಮಾರ್ಟ್ ಹಣ ನಿರ್ವಹಣೆಯ ಮುಖ್ಯ ವೈಶಿಷ್ಟ್ಯಗಳು

✨ ಬರೆಯಿರಿ, ಮಾತನಾಡಿ, ಸ್ಕ್ಯಾನ್ ಅಥವಾ ಹಂಚಿ
ಪಠ್ಯದಂತೆ ಬರೆಯಿರಿ, ಮಾತನಾಡಿ, ರಸೀದಿ ಸ್ಕ್ಯಾನ್ ಮಾಡಿ, ಅಥವಾ GPay, PhonePe, Paytm, ಬ್ಯಾಂಕ್ ಅಪ್ಲಿ ಅಥವಾ ಎಲ್ಲಿಂದಲಾದರೂ ಬಿಲ್ ಹಂಚಿ. ಸಾಧನದ OCR. ಐಚ್ಛಿಕ AI ಪಾರ್ಸ್, ದೈನಂದಿನ ಮಿತಿ.

💬 ಐಚ್ಛಿಕ ಬ್ಯಾಂಕ್ / UPI SMS ಆಮದು
ಹಣಕಾಸು SMS ಮಾತ್ರ. Detected Transactions ನಲ್ಲಿ ನೋಡಿ, Add. ಇತರ SMS ನಿರ್ಲಕ್ಷ. ಸಾಧನದಲ್ಲಿ. ಐಚ್ಛಿಕ.

💳 ದೈನಂದಿನ ಖರ್ಚು ಟ್ರ್ಯಾಕರ್ ಮತ್ತು ಮನಿ ಮ್ಯಾನೇಜರ್
ಟಿಪ್ಪಣಿ, ವರ್ಗ, ಪಾವತಿ ವಿಧಾನದೊಂದಿಗೆ ತ್ವರಿತ ನಮೂದು.
ಕಸ್ಟಮ್ ಐಕಾನ್‌ಗಳೊಂದಿಗೆ ಸ್ಮಾರ್ಟ್ ವರ್ಗಗಳು.
ವಾಲೆಟ್: ನಗದು, ಕಾರ್ಡ್, ಬ್ಯಾಂಕ್, ಡಿಜಿಟಲ್.
ಐಟಮೈಸ್ಡ್ ಕ್ಯಾಲ್ಕುಲೇಟರ್: ಒಂದು ಪಾವತಿ ಹಲವು ಸಾಲುಗಳಲ್ಲಿ.
ಹುಡುಕಾಟ, ವಿಂಗಡಣೆ, ಫಿಲ್ಟರ್; ಕಸ್ಟಮ್ ಕಾರ್ಡ್; ದೈನಂದಿನ ಖರ್ಚಿನ ಕ್ಯಾಲೆಂಡರ್.

🎤 ಧ್ವನಿ ಸೇರಿಸಿ (ಐಚ್ಛಿಕ)
ಮಾತನಾಡಿ. ಉಳಿಸುವ ಮೊದಲು ಪರಿಶೀಲಿಸಿ. ಮೈಕ್ ಧ್ವನಿ ಸೇರಿಸುವಾಗ ಮಾತ್ರ.

🎯 ಬಜೆಟ್ ಪ್ಲಾನರ್ ಮತ್ತು ಉಳಿತಾಯ ಟ್ರ್ಯಾಕರ್
ಮಾಸಿಕ ವರ್ಗ ಬಜೆಟ್, ಒಂದು ಟ್ಯಾಪ್‌ನಲ್ಲಿ ಕಳೆದ ತಿಂಗಳು ನಕಲು.
ಅಪ್ಲಿಯ ಹೊರಗೂ ಅಧಿಕ ಖರ್ಚು ಎಚ್ಚರಿಕೆ.
ಗುರಿಯೊಂದಿಗೆ ಉಳಿತಾಯ ಗೋಲ್.

📊 ವರದಿ ಮತ್ತು ವಿಶ್ಲೇಷಣೆ
ಕ್ಯಾಶ್ ಫ್ಲೋ ಮತ್ತು ವರ್ಗ / ಪಾವತಿ ಚಾರ್ಟ್.
ಅವಧಿ, ಟ್ರೆಂಡ್, ಸರಾಸರಿ ಹೋಲಿಕೆ.
CSV, JSON ಆಮದು/ರಫ್ತು, ಸ್ಥಳೀಯ .db ಬ್ಯಾಕಪ್ (XLS/PDF ಅಲ್ಲ).

⏰ ಬಿಲ್ ಮತ್ತು ನಿಗದಿತ ಪಾವತಿ
ದೈನಂದಿನ / ವಾರ / ತಿಂಗಳು / ವಾರ್ಷಿಕ ನಿಯಮ. ದಿನಾಂಕ ಜ್ಞಾಪನೆ.

⭐ ನೆಚ್ಚಿನವು
ಪದೇಪದೇ ಖರ್ಚು ಒಂದು ಟ್ಯಾಪ್.

🏷️ ಟ್ಯಾಗ್
ವರ್ಗದಾಚೆ ಟ್ಯಾಗ್.

🔒 ಆಫ್‌ಲೈನ್-ಫಸ್ಟ್ ಮತ್ತು ಸುರಕ್ಷಿತ
ನಮೂದು ಮತ್ತು ಬಜೆಟ್ ಇಂಟರ್ನೆಟ್ ಇಲ್ಲದೆ. ಸಿಂಕ್ ಇಲ್ಲದಿದ್ದರೆ ಲೆಡ್ಜರ್ ಸಾಧನದಲ್ಲಿ.
PIN, ಬಯೋಮೆಟ್ರಿಕ್, ಸ್ಕ್ರಾಂಬಲ್ಡ್ ಕೀಪ್ಯಾಡ್, ಸ್ಕ್ರೀನ್‌ಶಾಟ್ ಬ್ಲಾಕ್, Recents ಬ್ಲರ್.

🤝 ಬಹು-ಕರೆನ್ಸಿ
ಚಿಹ್ನೆ, ಗುಂಪು (ಭಾರತೀಯ ಸೇರಿದಂತೆ), ದಶಮಾಂಶ.

🔄 ಐಚ್ಛಿಕ ಬಹು-ಸಾಧನ ಸಿಂಕ್
Google, ಇಮೇಲ್ ಅಥವಾ ಮ್ಯಾಜಿಕ್ ಲಿಂಕ್. 4 ಸಾಧನಗಳವರೆಗೆ (Pro).

🎨 ಥೀಮ್
ಲೈಟ್, ಡಾರ್ಕ್ ಅಥವಾ ಸಿಸ್ಟಮ್.

🚀 ಎಕ್ಸ್‌ಪೆನ್ಸ್ ಟ್ರ್ಯಾಕರ್ ಬಜೆಟ್ ಮತ್ತು ಖರ್ಚು ಏಕೆ?
ಜೇಬು ಟ್ರ್ಯಾಕಿಂಗ್ ಮತ್ತು ಗಂಭೀರ ಬಜೆಟಿಂಗ್ — ಜೊತೆಗೆ ಐಚ್ಛಿಕ SMS, ಧ್ವನಿ, ಸ್ಕ್ಯಾನ್ ಮತ್ತು ಯಾವುದೇ ಅಪ್ಲಿನಿಂದ ಹಂಚಿಕೆ, ನಿಮ್ಮ ನಿಯಂತ್ರಣದಲ್ಲಿ.

ಟ್ರ್ಯಾಕಿಂಗ್, ಉಳಿತಾಯ ಮತ್ತು ಗುರಿಗಾಗಿ ಎಕ್ಸ್‌ಪೆನ್ಸ್ ಟ್ರ್ಯಾಕರ್ ಬಜೆಟ್ ಮತ್ತು ಖರ್ಚು ಡೌನ್‌ಲೋಡ್ ಮಾಡಿ.
```

---

## മലയാളം — `ml`

### App name

```
എക്സ്പെൻസ് ട്രാക്കർ: ബജറ്റ്
```

### Short description

```
എഴുതുക, സംസാരിക്കുക, സ്കാൻ അല്ലെങ്കിൽ ബിൽ ഷെയർ. SMS, ബജറ്റ്. ഉപകരണത്തിൽ.
```

### Full description

```
എക്സ്പെൻസ് ട്രാക്കർ ബജറ്റ് ആൻഡ് സ്പെൻഡ് ഉപയോഗിച്ച് പണം ലാഭിക്കുക, ദൈനംദിന ചെലവ് ട്രാക്ക് ചെയ്യുക, വ്യക്തിഗത ധനകാര്യം നിയന്ത്രിക്കുക!

എഴുതുക, സംസാരിക്കുക, സ്കാൻ അല്ലെങ്കിൽ ഷെയർ (ഓപ്ഷണൽ)
ഒരു ആഡ് ഷീറ്റ്: മെസേജ് പോലെ എഴുതുക, സംസാരിക്കുക, രസീത് സ്കാൻ ചെയ്യുക, അല്ലെങ്കിൽ ഏതെങ്കിലും UPI, ബാങ്ക് അല്ലെങ്കിൽ വാലറ്റ് ആപ്പിൽ നിന്ന് ബിൽ / പേയ്‌മെന്റ് സ്ക്രീൻ ഷെയർ ചെയ്യുക. നോക്കി സേവ് ചെയ്യുക. സ്ഥിരീകരണത്തിന് മുമ്പ് ഇടപാടില്ല.

ഓൺ-ഡിവൈസ് പാർസ് എപ്പോഴും പ്രവർത്തിക്കും. ഓപ്ഷണൽ AI പാർസ് (ദൈനംദിന പരിധി) ആ ടെക്സ്റ്റ് — ഫോട്ടോ അല്ല — ഞങ്ങളുടെ സെർവർ വഴി മൂന്നാം കക്ഷി AI-യിലേക്ക് അയയ്ക്കാം. ഓഫ്‌ലൈൻ ആയിരിക്കുകയോ AI ഒഴിവാക്കുകയോ ചെയ്താൽ പാർസ് ഉപകരണത്തിൽ. രസീത് OCR ഉപകരണത്തിൽ (ML Kit). Photo Picker, Android Share — ഗാലറി അനുമതി ഇല്ല.

SMS അധിഷ്ഠിത മണി മാനേജ്‌മെന്റ് (ഓപ്ഷണൽ കോർ)
നിങ്ങളുടെ അനുമതിയോടെ ആപ്പ് RECEIVE_SMS ഉപയോഗിച്ച് ഉപകരണത്തിലെ ബാങ്ക്, UPI, വാലറ്റ് ഡെബിറ്റ്/ക്രെഡിറ്റ് സന്ദേശം വായിക്കുന്നു. പൊരുത്തം Detected Transactions (ഹോം ⋮ മെനു) നിർദേശമാകും. കാർഡ് ടാപ്പ്, ആവശ്യമെങ്കിൽ തിരുത്തുക, Add അമർത്തുക. സ്ഥിരീകരണത്തിന് മുമ്പ് ഇടപാടില്ല. ഇല്ലാതാക്കാൻ സ്വൈപ്പ്.

ഇത് ഓപ്ഷണലാണ്; ഹോമിൽ SMS അനുവദിക്കുന്നത് വരെ ഓഫ്. നിരസിച്ചാലും കീപാഡ്, എഴുതുക/സംസാരം/സ്കാൻ, ബജറ്റ്, അനലിറ്റിക്സ് പ്രവർത്തിക്കും. ഞങ്ങൾ ഡിഫോൾട്ട് SMS ആപ്പല്ല. READ_SMS അല്ലെങ്കിൽ SEND_SMS ഇല്ല. 5 അക്ക പേയ്‌മെന്റ് SMS അയയ്ക്കില്ല; OTP/തട്ടിപ്പിന് SMS ഇല്ല. OTP, പ്രൊമോ, തുകയില്ലാത്ത സന്ദേശം അവഗണിക്കും. SMS ഉപകരണത്തിൽ മാത്രം വായിക്കുന്നു; അപ്‌ലോഡ്, വിൽപ്പന, ഷെയർ ഇല്ല.

പണം എവിടെ പോകുന്നു എന്ന ആശങ്ക നിർത്തുക. ബജറ്റ്, അനലിറ്റിക്സ്, വാലറ്റ് — കൂടെ ഓപ്ഷണൽ SMS, വോയ്‌സ്, സ്കാൻ, ഏത് ആപ്പിൽ നിന്നും ഷെയർ.

🔑 സ്മാർട്ട് മണി മാനേജ്‌മെന്റ് ഫീച്ചറുകൾ

✨ എഴുതുക, സംസാരിക്കുക, സ്കാൻ അല്ലെങ്കിൽ ഷെയർ
ടെക്സ്റ്റ് പോലെ എഴുതുക, സംസാരിക്കുക, രസീത് സ്കാൻ ചെയ്യുക, അല്ലെങ്കിൽ GPay, PhonePe, Paytm, ബാങ്ക് ആപ്പ് അല്ലെങ്കിൽ എവിടെ നിന്നും ബിൽ ഷെയർ ചെയ്യുക. ഓൺ-ഡിവൈസ് OCR. ഓപ്ഷണൽ AI പാർസ്, ദൈനംദിന പരിധി.

💬 ഓപ്ഷണൽ ബാങ്ക് / UPI SMS ഇമ്പോർട്ട്
ധനകാര്യ SMS മാത്രം. Detected Transactions-ൽ കണ്ട് Add. മറ്റുള്ളവ അവഗണിക്കും. ഉപകരണത്തിൽ. ഓപ്ഷണൽ.

💳 ദൈനംദിന ചെലവ് ട്രാക്കറും മണി മാനേജറും
കുറിപ്പ്, കാറ്റഗറി, പേയ്‌മെന്റ് രീതിയോടെ വേഗത്തിൽ എൻട്രി.
കസ്റ്റം ഐക്കൺ ഉള്ള സ്മാർട്ട് കാറ്റഗറി.
വാലറ്റ്: ക്യാഷ്, കാർഡ്, ബാങ്ക്, ഡിജിറ്റൽ.
ഐറ്റമായിസ്ഡ് കാൽക്കുലേറ്റർ: ഒരു പേയ്‌മെന്റ് പല വരികളായി.
തിരയൽ, ക്രമം, ഫിൽറ്റർ; കസ്റ്റം കാർഡ്; ദിവസ ചെലവ് കലണ്ടർ.

🎤 വോയ്‌സ് ആഡ് (ഓപ്ഷണൽ)
സംസാരിച്ച് ചേർക്കുക. സേവിന് മുമ്പ് നോക്കുക. മൈക്ക് വോയ്‌സ് ആഡ് തുടങ്ങുമ്പോൾ മാത്രം.

🎯 ബജറ്റ് പ്ലാനറും സേവിംഗ്സ് ട്രാക്കറും
മാസ കാറ്റഗറി ബജറ്റ്, ഒരു ടാപ്പിൽ കഴിഞ്ഞ മാസം കോപ്പി.
ആപ്പിന് പുറത്തും അധിക ചെലവ് അലേർട്ട്.
ലക്ഷ്യത്തോടെ സേവിംഗ്സ് ഗോൾ.

📊 റിപ്പോർട്ടും വിശകലനവും
ക്യാഷ് ഫ്ലോയും കാറ്റഗറി / പേയ്‌മെന്റ് ചാർട്ടും.
കാലയളവ്, ട്രെൻഡ്, ശരാശരി താരതമ്യം.
CSV, JSON ഇമ്പോർട്ട്/എക്സ്പോർട്ട്, ലോക്കൽ .db ബാക്കപ്പ് (XLS/PDF അല്ല).

⏰ ബില്ലും ഷെഡ്യൂൾഡ് പേയ്‌മെന്റും
ദൈനംദിന / ആഴ്ച / മാസ / വർഷ നിയമങ്ങൾ. തീയതി റിമൈൻഡർ.

⭐ ഫേവറിറ്റ്
പതിവ് ചെലവ് ഒരു ടാപ്പിൽ.

🏷️ ടാഗ്
കാറ്റഗറിക്കപ്പുറം ടാഗ്.

🔒 ഓഫ്‌ലൈൻ-ഫസ്റ്റും സുരക്ഷിതവും
എൻട്രിയും ബജറ്റും ഇന്റർനെറ്റില്ലാതെ. സിങ്ക് ഇല്ലെങ്കിൽ ലെഡ്ജർ ഉപകരണത്തിൽ.
PIN, ബയോമെട്രിക്, സ്ക്രാംബിൾഡ് കീപാഡ്, സ്ക്രീൻഷോട്ട് ബ്ലോക്ക്, Recents ബ്ലർ.

🤝 മൾട്ടി-കറൻസി
ചിഹ്നം, ഗ്രൂപ്പിംഗ് (ഇന്ത്യൻ ഉൾപ്പെടെ), ദശാംശം.

🔄 ഓപ്ഷണൽ മൾട്ടി-ഡിവൈസ് സിങ്ക്
Google, ഇമെയിൽ അല്ലെങ്കിൽ മാജിക് ലിങ്ക്. 4 ഉപകരണം വരെ (Pro).

🎨 തീം
ലൈറ്റ്, ഡാർക്ക് അല്ലെങ്കിൽ സിസ്റ്റം.

🚀 എക്സ്പെൻസ് ട്രാക്കർ ബജറ്റ് ആൻഡ് സ്പെൻഡ് എന്തുകൊണ്ട്?
പോക്കറ്റ് ട്രാക്കിംഗും ഗൗരവമുള്ള ബജറ്റിംഗും — കൂടെ ഓപ്ഷണൽ SMS, വോയ്‌സ്, സ്കാൻ, ഏത് ആപ്പിൽ നിന്നും ഷെയർ, നിങ്ങളുടെ നിയന്ത്രണത്തിൽ.

ട്രാക്കിംഗ്, ലാഭം, ലക്ഷ്യങ്ങൾക്കായി എക്സ്പെൻസ് ട്രാക്കർ ബജറ്റ് ആൻഡ് സ്പെൻഡ് ഡൗൺലോഡ് ചെയ്യുക.
```

---

## ଓଡ଼ିଆ — `or`

### App name

```
ଏକ୍ସପେନ୍ସ ଟ୍ରାକର: ବଜେଟ
```

### Short description

```
ଲେଖନ୍ତୁ, କୁହନ୍ତୁ, ସ୍କାନ କିମ୍ବା ବିଲ୍ ସେୟାର। SMS, ବଜେଟ୍। କେବଳ ଡିଭାଇସରେ।
```

### Full description

```
ଏକ୍ସପେନ୍ସ ଟ୍ରାକର୍ ବଜେଟ୍ ଏବଂ ଖର୍ଚ୍ଚ ସହିତ ଟଙ୍କା ସଞ୍ଚୟ କରନ୍ତୁ, ଦୈନିକ ଖର୍ଚ୍ଚ ଟ୍ରାକ୍ କରନ୍ତୁ ଏବଂ ବ୍ୟକ୍ତିଗତ ଅର୍ଥ ନିୟନ୍ତ୍ରଣ କରନ୍ତୁ!

ଲେଖନ୍ତୁ, କୁହନ୍ତୁ, ସ୍କାନ କିମ୍ବା ସେୟାର (ବୈକଳ୍ପିକ)
ଗୋଟିଏ ଆଡ୍ ସିଟ୍: ମେସେଜ୍ ପରି ଲେଖନ୍ତୁ, କୁହନ୍ତୁ, ରସିଦ୍ ସ୍କାନ୍ କରନ୍ତୁ, କିମ୍ବା ଯେକୌଣସି UPI, ବ୍ୟାଙ୍କ କିମ୍ବା ୱାଲେଟ୍ ଆପ୍‌ରୁ ବିଲ୍ / ପେମେଣ୍ଟ ସ୍କ୍ରିନ୍ ସେୟାର କରନ୍ତୁ। ଦେଖି ସେଭ୍ କରନ୍ତୁ। ନିଶ୍ଚିତ କରିବା ପୂର୍ବରୁ କୌଣସି ଲେନଦେନ ନାହିଁ।

ଅନ-ଡିଭାଇସ୍ ପାର୍ସ ସର୍ବଦା ଚାଲେ। ବୈକଳ୍ପିକ AI ପାର୍ସ (ଦୈନିକ ସୀମା) ସେହି ଟେକ୍ସଟ୍ — ଫଟୋ ନୁହେଁ — ଆମ ସର୍ଭର ଦେଇ ତୃତୀୟ-ପକ୍ଷ AIକୁ ପଠାଇପାରେ। ଅଫଲାଇନ୍ ରୁହନ୍ତୁ କିମ୍ବା AI ଛାଡନ୍ତୁ ତେବେ ପାର୍ସ ଡିଭାଇସରେ। ରସିଦ୍ OCR ଡିଭାଇସରେ (ML Kit)। Photo Picker ଓ Android Share — ଗ୍ୟାଲେରୀ ଅନୁମତି ନାହିଁ।

SMS ଆଧାରିତ ମନି ମ୍ୟାନେଜମେଣ୍ଟ (ବୈକଳ୍ପିକ ମୁଖ୍ୟ ସୁବିଧା)
ଆପଣଙ୍କ ଅନୁମତିରେ ଆପ୍ RECEIVE_SMS ଦ୍ୱାରା ଡିଭାଇସର ବ୍ୟାଙ୍କ, UPI ଓ ୱାଲେଟ୍ ଡେବିଟ୍/କ୍ରେଡିଟ୍ ସନ୍ଦେଶ ପଢେ। ମିଳିଲେ Detected Transactions (ହୋମ୍ ⋮ ମେନୁ)ରେ ପ୍ରସ୍ତାବ। କାର୍ଡ ଟ୍ୟାପ୍, ଦରକାର ହେଲେ ସମ୍ପାଦନା, Add ଦବାନ୍ତୁ। ନିଶ୍ଚିତ ପୂର୍ବରୁ ଲେନଦେନ ନାହିଁ। ଡିଲିଟ୍ ପାଇଁ ସ୍ୱାଇପ୍।

ଏହା ବୈକଳ୍ପିକ ଏବଂ ହୋମ୍‌ରେ SMS ଅନୁମତି ପର୍ଯ୍ୟନ୍ତ ବନ୍ଦ। ମନା କଲେ ମଧ୍ୟ କିପ୍ୟାଡ୍, ଲେଖନ୍ତୁ/କୁହନ୍ତୁ/ସ୍କାନ, ବଜେଟ୍ ଓ ଆନାଲିଟିକ୍ସ ଚାଲେ। ଆମେ ଡିଫଲ୍ଟ SMS ଆପ୍ ନୁହୋଁ। READ_SMS କିମ୍ବା SEND_SMS ନାହିଁ। 5 ଅଙ୍କ ପେମେଣ୍ଟ SMS ପଠାଉ ନାହିଁ; OTP/ଠକାମି ପାଇଁ SMS ନାହିଁ। OTP, ପ୍ରୋମୋ, ରାଶି ନଥିବା ସନ୍ଦେଶ ଅଣଦେଖା। SMS କେବଳ ଡିଭାଇସରେ ପଢାଯାଏ; ଅପଲୋଡ୍, ବିକ୍ରି କିମ୍ବା ସେୟାର ନାହିଁ।

ଟଙ୍କା କେଉଁଠି ଯାଉଛି ଭାବିବା ଛାଡନ୍ତୁ। ବଜେଟ୍, ଆନାଲିଟିକ୍ସ, ୱାଲେଟ୍ — ପ୍ଲସ୍ ବୈକଳ୍ପିକ SMS, ଭଏସ୍, ସ୍କାନ ଓ ଯେକୌଣସି ଆପ୍‌ରୁ ସେୟାର।

🔑 ସ୍ମାର୍ଟ ମନି ମ୍ୟାନେଜମେଣ୍ଟ ମୁଖ୍ୟ ସୁବିଧା

✨ ଲେଖନ୍ତୁ, କୁହନ୍ତୁ, ସ୍କାନ କିମ୍ବା ସେୟାର
ଟେକ୍ସଟ୍ ପରି ଲେଖନ୍ତୁ, କୁହନ୍ତୁ, ରସିଦ୍ ସ୍କାନ୍ କରନ୍ତୁ, କିମ୍ବା GPay, PhonePe, Paytm, ବ୍ୟାଙ୍କ ଆପ୍ କିମ୍ବା ଯେକୌଣସି ସ୍ଥାନରୁ ବିଲ୍ ସେୟାର କରନ୍ତୁ। ଅନ-ଡିଭାଇସ୍ OCR। ବୈକଳ୍ପିକ AI ପାର୍ସ, ଦୈନିକ ସୀମା।

💬 ବୈକଳ୍ପିକ ବ୍ୟାଙ୍କ / UPI SMS ଇମ୍ପୋର୍ଟ
କେବଳ ଆର୍ଥିକ SMS। Detected Transactionsରେ ଦେଖି Add। ଅନ୍ୟ SMS ଅଣଦେଖା। ଡିଭାଇସରେ। ବୈକଳ୍ପିକ।

💳 ଦୈନିକ ଖର୍ଚ୍ଚ ଟ୍ରାକର୍ ଓ ମନି ମ୍ୟାନେଜର୍
ନୋଟ୍, ବର୍ଗ, ପେମେଣ୍ଟ ପଦ୍ଧତି ସହ ଶୀଘ୍ର ଏଣ୍ଟ୍ରି।
କଷ୍ଟମ୍ ଆଇକନ୍ ସହ ସ୍ମାର୍ଟ ବର୍ଗ।
ୱାଲେଟ୍: ନଗଦ, କାର୍ଡ, ବ୍ୟାଙ୍କ, ଡିଜିଟାଲ୍।
ଆଇଟମାଇଜ୍ଡ କ୍ୟାଲକୁଲେଟର୍: ଗୋଟିଏ ପେମେଣ୍ଟ ଅନେକ ଧାଡ଼ିରେ।
ସର୍ଚ୍ଚ, ସର୍ଟ, ଫିଲ୍ଟର୍; କଷ୍ଟମ୍ କାର୍ଡ; ଦୈନିକ ଖର୍ଚ୍ଚ କ୍ୟାଲେଣ୍ଡର୍।

🎤 ଭଏସ୍ ଆଡ୍ (ବୈକଳ୍ପିକ)
କହି ଯୋଡନ୍ତୁ। ସେଭ୍ ପୂର୍ବରୁ ଦେଖନ୍ତୁ। ମାଇକ୍ କେବଳ ଭଏସ୍ ଆଡ୍ ଆରମ୍ଭ କଲେ।

🎯 ବଜେଟ୍ ପ୍ଲାନର୍ ଓ ସଞ୍ଚୟ ଟ୍ରାକର୍
ମାସିକ ବର୍ଗ ବଜେଟ୍, ଏକ ଟ୍ୟାପ୍‌ରେ ଗତ ମାସ କପି।
ଆପ୍ ବାହାରେ ମଧ୍ୟ ଅଧିକ ଖର୍ଚ୍ଚ ଆଲର୍ଟ।
ଲକ୍ଷ୍ୟ ସହ ସେଭିଂସ୍ ଗୋଲ୍।

📊 ରିପୋର୍ଟ ଓ ବିଶ୍ଳେଷଣ
କ୍ୟାସ୍ ଫ୍ଲୋ ଓ ବର୍ଗ / ପେମେଣ୍ଟ ଚାର୍ଟ।
ଅବଧି, ଟ୍ରେଣ୍ଡ, ହାରାହାରି ତୁଳନା।
CSV, JSON ଇମ୍ପୋର୍ଟ/ଏକ୍ସପୋର୍ଟ, ଲୋକାଲ୍ .db ବ୍ୟାକଅପ୍ (XLS/PDF ନୁହେଁ)।

⏰ ବିଲ୍ ଓ ସିଡ୍ୟୁଲ୍ ପେମେଣ୍ଟ
ଦୈନିକ / ସାପ୍ତାହିକ / ମାସିକ / ବାର୍ଷିକ ନିୟମ। ତାରିଖ ରିମାଇଣ୍ଡର୍।

⭐ ପସନ୍ଦ
ବାରମ୍ବାର ଖର୍ଚ୍ଚ ଏକ ଟ୍ୟାପ୍‌ରେ।

🏷️ ଟ୍ୟାଗ୍
ବର୍ଗ ବାହାରେ ଟ୍ୟାଗ୍।

🔒 ଅଫଲାଇନ୍-ଫର୍ଷ୍ଟ ଓ ସୁରକ୍ଷିତ
ଏଣ୍ଟ୍ରି ଓ ବଜେଟ୍ ଇଣ୍ଟରନେଟ୍ ବିନା। ସିଙ୍କ୍ ନଥିଲେ ଲେଜର୍ ଡିଭାଇସରେ।
PIN, ବାୟୋମେଟ୍ରିକ୍, ସ୍କ୍ରାମ୍ବଲ୍ଡ କିପ୍ୟାଡ୍, ସ୍କ୍ରିନସଟ୍ ବ୍ଲକ୍, Recents ବ୍ଲର୍।

🤝 ମଲ୍ଟି-କରେନ୍ସି
ଚିହ୍ନ, ଗ୍ରୁପିଙ୍ଗ୍ (ଭାରତୀୟ ସହ), ଦଶମିକ।

🔄 ବୈକଳ୍ପିକ ମଲ୍ଟି-ଡିଭାଇସ୍ ସିଙ୍କ୍
Google, ଇମେଲ୍ କିମ୍ବା ମ୍ୟାଜିକ୍ ଲିଙ୍କ୍। 4 ଡିଭାଇସ୍ ପର୍ଯ୍ୟନ୍ତ (Pro)।

🎨 ଥିମ୍
ଲାଇଟ୍, ଡାର୍କ କିମ୍ବା ସିଷ୍ଟମ୍।

🚀 ଏକ୍ସପେନ୍ସ ଟ୍ରାକର୍ ବଜେଟ୍ ଏବଂ ଖର୍ଚ୍ଚ କାହିଁକି?
ପକେଟ୍ ଟ୍ରାକିଂ ଓ ଗମ୍ଭୀର ବଜେଟିଂ — ପ୍ଲସ୍ ବୈକଳ୍ପିକ SMS, ଭଏସ୍, ସ୍କାନ ଓ ଯେକୌଣସି ଆପ୍‌ରୁ ସେୟାର, ଆପଣଙ୍କ ନିୟନ୍ତ୍ରଣରେ।

ଟ୍ରାକିଂ, ସଞ୍ଚୟ ଓ ଲକ୍ଷ୍ୟ ପାଇଁ ଏକ୍ସପେନ୍ସ ଟ୍ରାକର୍ ବଜେଟ୍ ଏବଂ ଖର୍ଚ୍ଚ ଡାଉନଲୋଡ୍ କରନ୍ତୁ।
```

---

## ਪੰਜਾਬੀ — `pa`

### App name

```
ਐਕਸਪੈਂਸ ਟਰੈਕਰ: ਬਜਟ ਤੇ ਖਰਚ
```

### Short description

```
ਲਿਖੋ, ਬੋਲੋ, ਸਕੈਨ ਜਾਂ ਬਿੱਲ ਸ਼ੇਅਰ ਕਰੋ। SMS, ਬਜਟ। ਸਿਰਫ਼ ਡਿਵਾਈਸ ’ਤੇ।
```

### Full description

```
ਐਕਸਪੈਂਸ ਟਰੈਕਰ ਬਜਟ ਐਂਡ ਸਪੈਂਡ ਨਾਲ ਪੈਸੇ ਬਚਾਓ, ਰੋਜ਼ਾਨਾ ਖਰਚ ਟਰੈਕ ਕਰੋ ਅਤੇ ਨਿੱਜੀ ਵਿੱਤ ’ਤੇ ਕੰਟਰੋਲ ਰੱਖੋ!

ਲਿਖੋ, ਬੋਲੋ, ਸਕੈਨ ਜਾਂ ਸ਼ੇਅਰ (ਵਿਕਲਪਿਕ)
ਇੱਕ ਐਡ ਸ਼ੀਟ: ਸੁਨੇਹੇ ਵਾਂਗ ਲਿਖੋ, ਬੋਲੋ, ਰਸੀਦ ਸਕੈਨ ਕਰੋ, ਜਾਂ ਕਿਸੇ ਵੀ UPI, ਬੈਂਕ ਜਾਂ ਵਾਲਿਟ ਐਪ ਤੋਂ ਬਿੱਲ / ਪੇਮੈਂਟ ਸਕਰੀਨ ਸ਼ੇਅਰ ਕਰੋ। ਵੇਖੋ, ਫਿਰ ਸੇਵ ਕਰੋ। ਪੁਸ਼ਟੀ ਤੋਂ ਪਹਿਲਾਂ ਕੋਈ ਲੈਣ-ਦੇਣ ਨਹੀਂ।

ਆਨ-ਡਿਵਾਈਸ ਪਾਰਸ ਹਮੇਸ਼ਾ ਚੱਲਦਾ ਹੈ। ਵਿਕਲਪਿਕ AI ਪਾਰਸ (ਰੋਜ਼ਾਨਾ ਹੱਦ) ਉਹ ਟੈਕਸਟ — ਫੋਟੋ ਨਹੀਂ — ਸਾਡੇ ਸਰਵਰ ਰਾਹੀਂ ਥਰਡ-ਪਾਰਟੀ AI ਨੂੰ ਭੇਜ ਸਕਦਾ ਹੈ। ਆਫਲਾਈਨ ਰਹੋ ਜਾਂ AI ਛੱਡੋ ਤਾਂ ਪਾਰਸ ਡਿਵਾਈਸ ’ਤੇ। ਰਸੀਦ OCR ਡਿਵਾਈਸ ’ਤੇ (ML Kit)। Photo Picker ਅਤੇ Android Share — ਗੈਲਰੀ ਇਜਾਜ਼ਤ ਨਹੀਂ।

SMS ਆਧਾਰਿਤ ਮਨੀ ਮੈਨੇਜਮੈਂਟ (ਵਿਕਲਪਿਕ ਮੁੱਖ ਫੀਚਰ)
ਤੁਹਾਡੀ ਇਜਾਜ਼ਤ ਨਾਲ ਐਪ RECEIVE_SMS ਨਾਲ ਡਿਵਾਈਸ ’ਤੇ ਬੈਂਕ, UPI ਅਤੇ ਵਾਲਿਟ ਡੈਬਿਟ/ਕ੍ਰੈਡਿਟ ਸੁਨੇਹੇ ਪੜ੍ਹਦੀ ਹੈ। ਮਿਲਦੇ Detected Transactions (ਹੋਮ ⋮ ਮੀਨੂ) ਵਿੱਚ ਸੁਝਾਅ ਬਣਦੇ ਹਨ। ਕਾਰਡ ਟੈਪ ਕਰੋ, ਲੋੜ ਹੋਵੇ ਤਾਂ ਸੋਧੋ, Add ਦਬਾਓ। ਪੁਸ਼ਟੀ ਤੋਂ ਪਹਿਲਾਂ ਲੈਣ-ਦੇਣ ਨਹੀਂ। ਮਿਟਾਉਣ ਲਈ ਸਵਾਈਪ।

ਇਹ ਵਿਕਲਪਿਕ ਹੈ ਅਤੇ ਹੋਮ ’ਤੇ SMS ਇਜਾਜ਼ਤ ਤੱਕ ਬੰਦ। ਇਨਕਾਰ ’ਤੇ ਵੀ ਕੀਪੈਡ, ਲਿਖੋ/ਬੋਲੋ/ਸਕੈਨ, ਬਜਟ ਅਤੇ ਐਨਾਲਿਟਿਕਸ ਚੱਲਦੇ ਹਨ। ਅਸੀਂ ਡਿਫਾਲਟ SMS ਐਪ ਨਹੀਂ। READ_SMS ਜਾਂ SEND_SMS ਨਹੀਂ। 5 ਅੰਕਾਂ ਵਾਲੇ ਪੇਮੈਂਟ SMS ਨਹੀਂ ਭੇਜਦੇ; OTP/ਧੋਖਾਧੜੀ ਲਈ SMS ਨਹੀਂ। OTP, ਪ੍ਰੋਮੋ ਅਤੇ ਬਿਨਾਂ ਰਕਮ ਵਾਲੇ ਸੁਨੇਹੇ ਨਜ਼ਰਅੰਦਾਜ਼। SMS ਸਿਰਫ਼ ਡਿਵਾਈਸ ’ਤੇ ਪੜ੍ਹੀ ਜਾਂਦੀ ਹੈ; ਅੱਪਲੋਡ, ਵੇਚਣ ਜਾਂ ਸਾਂਝਾ ਨਹੀਂ।

ਪੈਸਾ ਕਿੱਥੇ ਜਾਂਦਾ ਹੈ ਸੋਚਣਾ ਛੱਡੋ। ਬਜਟ, ਐਨਾਲਿਟਿਕਸ, ਵਾਲਿਟ — ਪਲੱਸ ਵਿਕਲਪਿਕ SMS, ਵੌਇਸ, ਸਕੈਨ ਅਤੇ ਕਿਸੇ ਵੀ ਐਪ ਤੋਂ ਸ਼ੇਅਰ।

🔑 ਸਮਾਰਟ ਮਨੀ ਮੈਨੇਜਮੈਂਟ ਦੀਆਂ ਮੁੱਖ ਖੂਬੀਆਂ

✨ ਲਿਖੋ, ਬੋਲੋ, ਸਕੈਨ ਜਾਂ ਸ਼ੇਅਰ
ਟੈਕਸਟ ਵਾਂਗ ਲਿਖੋ, ਬੋਲੋ, ਰਸੀਦ ਸਕੈਨ ਕਰੋ, ਜਾਂ GPay, PhonePe, Paytm, ਬੈਂਕ ਐਪ ਜਾਂ ਕਿਤੇ ਵੀ ਤੋਂ ਬਿੱਲ ਸ਼ੇਅਰ ਕਰੋ। ਆਨ-ਡਿਵਾਈਸ OCR। ਵਿਕਲਪਿਕ AI ਪਾਰਸ, ਰੋਜ਼ਾਨਾ ਹੱਦ।

💬 ਵਿਕਲਪਿਕ ਬੈਂਕ / UPI SMS ਇੰਪੋਰਟ
ਸਿਰਫ਼ ਵਿੱਤੀ SMS। Detected Transactions ਵਿੱਚ ਵੇਖੋ, Add ਕਰੋ। ਹੋਰ SMS ਨਜ਼ਰਅੰਦਾਜ਼। ਡਿਵਾਈਸ ’ਤੇ। ਵਿਕਲਪਿਕ।

💳 ਰੋਜ਼ਾਨਾ ਖਰਚ ਟਰੈਕਰ ਅਤੇ ਮਨੀ ਮੈਨੇਜਰ
ਨੋਟ, ਸ਼੍ਰੇਣੀ, ਭੁਗਤਾਨ ਢੰਗ ਨਾਲ ਤੁਰੰਤ ਐਂਟਰੀ।
ਕਸਟਮ ਆਈਕਨ ਵਾਲੀਆਂ ਸਮਾਰਟ ਸ਼੍ਰੇਣੀਆਂ।
ਵਾਲਿਟ: ਨਕਦ, ਕਾਰਡ, ਬੈਂਕ, ਡਿਜੀਟਲ।
ਆਈਟਮਾਈਜ਼ਡ ਕੈਲਕੁਲੇਟਰ: ਇੱਕ ਭੁਗਤਾਨ ਕਈ ਲਾਈਨਾਂ ਵਿੱਚ।
ਖੋਜ, ਕ੍ਰਮ, ਫਿਲਟਰ; ਕਸਟਮ ਕਾਰਡ; ਰੋਜ਼ਾਨਾ ਖਰਚ ਦਾ ਕੈਲੰਡਰ।

🎤 ਵੌਇਸ ਐਡ (ਵਿਕਲਪਿਕ)
ਬੋਲ ਕੇ ਦੱਸੋ। ਸੇਵ ਤੋਂ ਪਹਿਲਾਂ ਵੇਖੋ। ਮਾਈਕ ਸਿਰਫ਼ ਵੌਇਸ ਐਡ ਸ਼ੁਰੂ ਕਰਨ ’ਤੇ।

🎯 ਬਜਟ ਪਲਾਨਰ ਅਤੇ ਬਚਤ ਟਰੈਕਰ
ਮਹੀਨਾਵਾਰ ਸ਼੍ਰੇਣੀ ਬਜਟ, ਇੱਕ ਟੈਪ ਵਿੱਚ ਪਿਛਲਾ ਮਹੀਨਾ ਕਾਪੀ।
ਐਪ ਤੋਂ ਬਾਹਰ ਵੀ ਵੱਧ ਖਰਚ ਅਲਰਟ।
ਟੀਚੇ ਨਾਲ ਸੇਵਿੰਗਜ਼ ਗੋਲ।

📊 ਰਿਪੋਰਟ ਅਤੇ ਵਿਸ਼ਲੇਸ਼ਣ
ਕੈਸ਼ ਫਲੋ ਅਤੇ ਸ਼੍ਰੇਣੀ / ਭੁਗਤਾਨ ਚਾਰਟ।
ਮਿਆਦ, ਰੁਝਾਨ, ਔਸਤ ਤੁਲਨਾ।
CSV, JSON ਇੰਪੋਰਟ/ਐਕਸਪੋਰਟ, ਲੋਕਲ .db ਬੈਕਅੱਪ (XLS/PDF ਨਹੀਂ)।

⏰ ਬਿੱਲ ਅਤੇ ਸ਼ਡਿਊਲ ਭੁਗਤਾਨ
ਰੋਜ਼ / ਹਫ਼ਤਾ / ਮਹੀਨਾ / ਸਾਲ ਦੇ ਨਿਯਮ। ਮਿਤੀ ਰਿਮਾਈਂਡਰ।

⭐ ਮਨਪਸੰਦ
ਵਾਰ-ਵਾਰ ਖਰਚ ਇੱਕ ਟੈਪ ਵਿੱਚ।

🏷️ ਟੈਗ
ਸ਼੍ਰੇਣੀ ਤੋਂ ਅੱਗੇ ਟੈਗ।

🔒 ਆਫਲਾਈਨ-ਫਰਸਟ ਅਤੇ ਸੁਰੱਖਿਅਤ
ਐਂਟਰੀ ਅਤੇ ਬਜਟ ਬਿਨਾਂ ਇੰਟਰਨੈੱਟ। ਸਿੰਕ ਬੰਦ ਹੋਵੇ ਤਾਂ ਲੈਜਰ ਡਿਵਾਈਸ ’ਤੇ।
PIN, ਬਾਇਓਮੈਟ੍ਰਿਕ, ਸਕ੍ਰੈਂਬਲਡ ਕੀਪੈਡ, ਸਕ੍ਰੀਨਸ਼ਾਟ ਬਲਾਕ, Recents ਬਲਰ।

🤝 ਮਲਟੀ-ਕਰੰਸੀ
ਚਿੰਨ੍ਹ, ਗਰੁੱਪਿੰਗ (ਭਾਰਤੀ ਸਮੇਤ), ਦਸ਼ਮਲਵ।

🔄 ਵਿਕਲਪਿਕ ਮਲਟੀ-ਡਿਵਾਈਸ ਸਿੰਕ
Google, ਈਮੇਲ ਜਾਂ ਮੈਜਿਕ ਲਿੰਕ। 4 ਡਿਵਾਈਸ ਤੱਕ (Pro)।

🎨 ਥੀਮ
ਲਾਈਟ, ਡਾਰਕ ਜਾਂ ਸਿਸਟਮ।

🚀 ਐਕਸਪੈਂਸ ਟਰੈਕਰ ਬਜਟ ਐਂਡ ਸਪੈਂਡ ਕਿਉਂ?
ਜੇਬ ਟਰੈਕਿੰਗ ਅਤੇ ਗੰਭੀਰ ਬਜਟਿੰਗ — ਪਲੱਸ ਵਿਕਲਪਿਕ SMS, ਵੌਇਸ, ਸਕੈਨ ਅਤੇ ਕਿਸੇ ਵੀ ਐਪ ਤੋਂ ਸ਼ੇਅਰ, ਤੁਹਾਡੇ ਕੰਟਰੋਲ ਵਿੱਚ।

ਟਰੈਕਿੰਗ, ਬਚਤ ਅਤੇ ਟੀਚਿਆਂ ਲਈ ਐਕਸਪੈਂਸ ਟਰੈਕਰ ਬਜਟ ਐਂਡ ਸਪੈਂਡ ਡਾਊਨਲੋਡ ਕਰੋ।
```

---

## தமிழ் — `ta`

### App name

```
எக்ஸ்பென்ஸ் டிராக்கர் பட்ஜெட்
```

### Short description

```
எழுதுங்கள், பேசுங்கள், ஸ்கேன் அல்லது பில் பகிரவும். SMS, பட்ஜெட். சாதனத்திலே.
```

### Full description

```
எக்ஸ்பென்ஸ் டிராக்கர் பட்ஜெட் அண்ட் ஸ்பெண்ட் மூலம் பணம் சேமியுங்கள், தினசரி செலவைக் கண்காணித்து தனிப்பட்ட நிதியைக் கட்டுப்படுத்துங்கள்!

எழுதுங்கள், பேசுங்கள், ஸ்கேன் அல்லது பகிரவும் (விருப்பத்தேர்வு)
ஒரே ஆட் ஷீட்: செய்தி போல் எழுதுங்கள், பேசுங்கள், ரசீதை ஸ்கேன் செய்யுங்கள், அல்லது எந்த UPI, வங்கி அல்லது வாலட் ஆப்பிலிருந்தும் பில் / கட்டணத் திரையைப் பகிருங்கள். பார்த்து சேமிக்கவும். உறுதி செய்யும் முன் பரிவர்த்தனை இல்லை.

சாதனத்தில் பார்க் எப்போதும் வேலை செய்யும். விருப்ப AI பார்க் (தினசரி வரம்பு) அந்த உரையை — புகைப்படம் அல்ல — எங்கள் சர்வர் வழியாக மூன்றாம் தரப்பு AIக்கு அனுப்பலாம். ஆஃப்லைனில் இருந்தால் அல்லது AI தவிர்த்தால் பார்க் சாதனத்திலே. ரசீது OCR சாதனத்தில் (ML Kit). Photo Picker மற்றும் Android Share — கேலரி அனுமதி இல்லை.

SMS அடிப்படையிலான பண மேலாண்மை (விருப்ப முக்கிய அம்சம்)
உங்கள் அனுமதியுடன் ஆப் RECEIVE_SMS மூலம் சாதனத்தில் வங்கி, UPI, வாலட் டெபிட்/கிரெடிட் செய்திகளைப் படிக்கும். பொருந்தினால் Detected Transactions (முகப்பு ⋮ மெனு) பரிந்துரை. கார்டைத் தட்டி, தேவைப்பட்டால் திருத்தி, Add அழுத்தவும். உறுதிக்கு முன் பரிவர்த்தனை இல்லை. நீக்க ஸ்வைப்.

இது விருப்பம்; முகப்பில் SMS அனுமதி வரை ஆஃப். மறுத்தாலும் கீபேட், எழுது/பேசு/ஸ்கேன், பட்ஜெட், பகுப்பாய்வு வேலை செய்யும். நாங்கள் இயல்பு SMS ஆப் அல்ல. READ_SMS அல்லது SEND_SMS இல்லை. 5 இலக்க கட்டண SMS அனுப்பமாட்டோம்; OTP/மோசடிக்கு SMS இல்லை. OTP, புரோமோ, தொகையில்லா செய்திகள் புறக்கணிப்பு. SMS சாதனத்தில் மட்டுமே படிக்கப்படும்; பதிவேற்றம், விற்பனை, பகிர்வு இல்லை.

பணம் எங்கே போகிறது என்று யோசிப்பதை நிறுத்துங்கள். பட்ஜெட், பகுப்பாய்வு, வாலட் — கூடவே விருப்ப SMS, குரல், ஸ்கேன், எந்த ஆப்பிலிருந்தும் பகிர்வு.

🔑 சிறந்த பண மேலாண்மை அம்சங்கள்

✨ எழுதுங்கள், பேசுங்கள், ஸ்கேன் அல்லது பகிரவும்
உரை போல் எழுதுங்கள், பேசுங்கள், ரசீதை ஸ்கேன் செய்யுங்கள், அல்லது GPay, PhonePe, Paytm, வங்கி ஆப் அல்லது எங்கிருந்தும் பில் பகிருங்கள். சாதன OCR. விருப்ப AI பார்க், தினசரி வரம்பு.

💬 விருப்ப வங்கி / UPI SMS இறக்குமதி
நிதி SMS மட்டும். Detected Transactions-ல் பார்த்து Add. மற்றவை புறக்கணிப்பு. சாதனத்தில். விருப்பம்.

💳 தினசரி செலவு டிராக்கர் மற்றும் பண மேலாளர்
குறிப்பு, வகை, கட்டண முறையுடன் விரைவு பதிவு.
தனிப்பயன் ஐகான்களுடன் ஸ்மார்ட் வகைகள்.
வாலட்: ரொக்கம், கார்டு, வங்கி, டிஜிட்டல்.
ஐட்டமைஸ்டு கால்குலேட்டர்: ஒரு கட்டணம் பல வரிகளாக.
தேடல், வரிசை, வடிகட்டி; தனிப்பயன் அட்டை; தினசரி செலவு நாட்காட்டி.

🎤 குரல் சேர்ப்பு (விருப்பம்)
பேசிச் சேருங்கள். சேமிக்கும் முன் பாருங்கள். மைக் குரல் சேர்ப்பு தொடங்கும்போது மட்டும்.

🎯 பட்ஜெட் திட்டமிடுபவர் மற்றும் சேமிப்பு டிராக்கர்
மாதாந்திர வகை பட்ஜெட், ஒரு தட்டில் கடந்த மாதம் நகல்.
ஆப்பிற்கு வெளியேயும் அதிக செலவு எச்சரிக்கை.
இலக்குடன் சேமிப்பு கோல்.

📊 அறிக்கை மற்றும் பகுப்பாய்வு
பணப்புழக்கம் மற்றும் வகை / கட்டண விளக்கப்படங்கள்.
காலம், போக்கு, சராசரி ஒப்பீடு.
CSV, JSON இறக்குமதி/ஏற்றுமதி, உள்ளூர் .db காப்பு (XLS/PDF அல்ல).

⏰ பில் மற்றும் திட்டமிட்ட கட்டணம்
தினசரி / வாரம் / மாதம் / ஆண்டு விதிகள். தேதி நினைவூட்டல்.

⭐ பிடித்தவை
அடிக்கடி செலவு ஒரு தட்டில்.

🏷️ டேக்
வகைக்கு அப்பால் டேக்.

🔒 ஆஃப்லைன்-முதல் மற்றும் பாதுகாப்பு
பதிவும் பட்ஜெட்டும் இணையம் இல்லாமல். சிங்க் இல்லையென்றால் லெட்ஜர் சாதனத்தில்.
PIN, பயோமெட்ரிக், ஸ்கிராம்பிள்ட் கீபேட், ஸ்கிரீன்ஷாட் தடுப்பு, Recents மங்கல்.

🤝 பல நாணயம்
சின்னம், குழுவாக்கம் (இந்தியம் உட்பட), தசமம்.

🔄 விருப்ப பல-சாதன சிங்க்
Google, மின்னஞ்சல் அல்லது மேஜிக் இணைப்பு. 4 சாதனங்கள் வரை (Pro).

🎨 தீம்
லைட், டார்க் அல்லது சிஸ்டம்.

🚀 எக்ஸ்பென்ஸ் டிராக்கர் பட்ஜெட் அண்ட் ஸ்பெண்ட் ஏன்?
பாக்கெட் கண்காணிப்பும் தீவிர பட்ஜெட்டிங்கும் — கூடவே விருப்ப SMS, குரல், ஸ்கேன், எந்த ஆப்பிலிருந்தும் பகிர்வு, உங்கள் கட்டுப்பாட்டில்.

கண்காணிப்பு, சேமிப்பு, இலக்குகளுக்கு எக்ஸ்பென்ஸ் டிராக்கர் பட்ஜெட் அண்ட் ஸ்பெண்ட் பதிவிறக்கவும்.
```

---

## తెలుగు — `te`

### App name

```
ఎక్స్‌పెన్స్ ట్రాకర్: బడ్జెట్
```

### Short description

```
రాయండి, మాట్లాడండి, స్కాన్ లేదా బిల్ షేర్. SMS, బడ్జెట్. పరికరంలోనే.
```

### Full description

```
ఎక్స్‌పెన్స్ ట్రాకర్ బడ్జెట్ అండ్ స్పెండ్‌తో డబ్బు ఆదా చేయండి, రోజువారీ ఖర్చు ట్రాక్ చేసి వ్యక్తిగత ఆర్థికాన్ని నియంత్రించండి!

రాయండి, మాట్లాడండి, స్కాన్ లేదా షేర్ (ఐచ్ఛికం)
ఒక యాడ్ షీట్: సందేశంలా రాయండి, మాట్లాడండి, రసీదు స్కాన్ చేయండి, లేదా ఏ UPI, బ్యాంక్ లేదా వాలెట్ యాప్ నుండి బిల్ / పేమెంట్ స్క్రీన్ షేర్ చేయండి. చూసి సేవ్ చేయండి. నిర్ధారణకు ముందు లావాదేవీ లేదు.

ఆన్-డివైస్ పార్స్ ఎప్పుడూ పనిచేస్తుంది. ఐచ్ఛిక AI పార్స్ (రోజువారీ పరిమితి) ఆ టెక్స్ట్‌ను — ఫోటో కాదు — మా సర్వర్ ద్వారా థర్డ్-పార్టీ AIకి పంపవచ్చు. ఆఫ్‌లైన్‌లో ఉండండి లేదా AI వదిలేస్తే పార్స్ పరికరంలోనే. రసీదు OCR పరికరంలో (ML Kit). Photo Picker మరియు Android Share — గ్యాలరీ అనుమతి లేదు.

SMS ఆధారిత మనీ మేనేజ్‌మెంట్ (ఐచ్ఛిక కోర్ ఫీచర్)
మీ అనుమతితో యాప్ RECEIVE_SMSతో పరికరంలో బ్యాంక్, UPI, వాలెట్ డెబిట్/క్రెడిట్ సందేశాలు చదువుతుంది. సరిపోతే Detected Transactions (హోమ్ ⋮ మెను)లో సూచన. కార్డ్ ట్యాప్, అవసరమైతే సవరించి Add నొక్కండి. నిర్ధారణకు ముందు లావాదేవీ లేదు. తొలగించడానికి స్వైప్.

ఇది ఐచ్ఛికం; హోమ్‌లో SMS అనుమతి వరకు ఆఫ్. తిరస్కరిస్తే కూడా కీప్యాడ్, రాయండి/మాట/స్కాన్, బడ్జెట్, అనలిటిక్స్ పనిచేస్తాయి. మేము డిఫాల్ట్ SMS యాప్ కాదు. READ_SMS లేదా SEND_SMS లేవు. 5 అంకెల పేమెంట్ SMS పంపము; OTP/మోసానికి SMS కాదు. OTP, ప్రోమో, మొత్తం లేని సందేశాలు విస్మరణ. SMS పరికరంలోనే చదవబడుతుంది; అప్‌లోడ్, అమ్మకం లేదా షేర్ లేదు.

డబ్బు ఎక్కడికి వెళ్తోందో ఆలోచించడం మానండి. బడ్జెట్, అనలిటిక్స్, వాలెట్ — ప్లస్ ఐచ్ఛిక SMS, వాయిస్, స్కాన్ మరియు ఏ యాప్ నుండైనా షేర్.

🔑 స్మార్ట్ మనీ మేనేజ్‌మెంట్ ముఖ్య ఫీచర్లు

✨ రాయండి, మాట్లాడండి, స్కాన్ లేదా షేర్
టెక్స్ట్‌లా రాయండి, మాట్లాడండి, రసీదు స్కాన్ చేయండి, లేదా GPay, PhonePe, Paytm, బ్యాంక్ యాప్ లేదా ఎక్కడి నుండైనా బిల్ షేర్ చేయండి. ఆన్-డివైస్ OCR. ఐచ్ఛిక AI పార్స్, రోజువారీ పరిమితి.

💬 ఐచ్ఛిక బ్యాంక్ / UPI SMS దిగుమతి
ఆర్థిక SMS మాత్రమే. Detected Transactionsలో చూసి Add. ఇతర SMS విస్మరణ. పరికరంలో. ఐచ్ఛికం.

💳 రోజువారీ ఖర్చు ట్రాకర్ మరియు మనీ మేనేజర్
నోట్, కేటగిరీ, చెల్లింపు పద్ధతితో వేగంగా ఎంట్రీ.
కస్టమ్ ఐకాన్‌లతో స్మార్ట్ కేటగిరీలు.
వాలెట్: నగదు, కార్డ్, బ్యాంక్, డిజిటల్.
ఐటమైజ్డ్ కాలిక్యులేటర్: ఒక చెల్లింపు అనేక లైన్లు.
శోధన, క్రమం, ఫిల్టర్; కస్టమ్ కార్డ్; రోజువారీ ఖర్చు క్యాలెండర్.

🎤 వాయిస్ యాడ్ (ఐచ్ఛికం)
మాట్లాడి జోడించండి. సేవ్‌కు ముందు చూడండి. మైక్ వాయిస్ యాడ్ మొదలుపెట్టినప్పుడు మాత్రమే.

🎯 బడ్జెట్ ప్లానర్ మరియు పొదుపు ట్రాకర్
నెలవారీ కేటగిరీ బడ్జెట్, ఒక ట్యాప్‌లో గత నెల కాపీ.
యాప్ బయట కూడా అధిక ఖర్చు అలర్ట్.
లక్ష్యంతో సేవింగ్స్ గోల్.

📊 రిపోర్ట్ మరియు విశ్లేషణ
క్యాష్ ఫ్లో మరియు కేటగిరీ / చెల్లింపు చార్ట్‌లు.
కాలం, ట్రెండ్, సగటు పోలిక.
CSV, JSON దిగుమతి/ఎగుమతి, లోకల్ .db బ్యాకప్ (XLS/PDF కాదు).

⏰ బిల్లులు మరియు షెడ్యూల్ చెల్లింపులు
రోజు / వారం / నెల / సంవత్సర నియమాలు. తేదీ రిమైండర్.

⭐ ఇష్టమైనవి
తరచు ఖర్చు ఒక ట్యాప్‌లో.

🏷️ ట్యాగ్
కేటగిరీకి మించి ట్యాగ్.

🔒 ఆఫ్‌లైన్-ఫస్ట్ మరియు సురక్షితం
ఎంట్రీ మరియు బడ్జెట్ ఇంటర్నెట్ లేకుండా. సింక్ లేకపోతే లెడ్జర్ పరికరంలో.
PIN, బయోమెట్రిక్, స్క్రాంబుల్డ్ కీప్యాడ్, స్క్రీన్‌షాట్ బ్లాక్, Recents బ్లర్.

🤝 మల్టీ-కరెన్సీ
చిహ్నం, గ్రూపింగ్ (భారతీయంతో సహా), దశాంశాలు.

🔄 ఐచ్ఛిక మల్టీ-డివైస్ సింక్
Google, ఇమెయిల్ లేదా మ్యాజిక్ లింక్. 4 పరికరాల వరకు (Pro).

🎨 థీమ్
లైట్, డార్క్ లేదా సిస్టమ్.

🚀 ఎక్స్‌పెన్స్ ట్రాకర్ బడ్జెట్ అండ్ స్పెండ్ ఎందుకు?
జేబు ట్రాకింగ్ మరియు తీవ్రమైన బడ్జెటింగ్ — ప్లస్ ఐచ్ఛిక SMS, వాయిస్, స్కాన్ మరియు ఏ యాప్ నుండైనా షేర్, మీ నియంత్రణలో.

ట్రాకింగ్, పొదుపు, లక్ష్యాల కోసం ఎక్స్‌పెన్స్ ట్రాకర్ బడ్జెట్ అండ్ స్పెండ్ డౌన్‌లోడ్ చేయండి.
```

---

## অসমীয়া — `as`

### App name

```
এক্সপেন্স ট্ৰেকাৰ: বাজেট
```

### Short description

```
লিখক, কওক, স্কেন বা বিল শ্বেয়াৰ কৰক। SMS, বাজেট। কেৱল ডিভাইচত।
```

### Full description

```
এক্সপেন্স ট্ৰেকাৰ বাজেট এণ্ড স্পেণ্ডৰে সহজে ধন ৰাহি কৰক, দৈনিক খৰচ ট্ৰেক কৰক আৰু ব্যক্তিগত অর্থ নিয়ন্ত্ৰণ কৰক!

লিখক, কওক, স্কেন বা শ্বেয়াৰ (বৈকল্পিক)
এটা এড শ্বীট: মেচেজৰ দৰে লিখক, কওক, ৰচিদ স্কেন কৰক, বা যিকোনো UPI, বেংক বা ৱালেট এপৰ পৰা বিল / পেমেণ্ট স্ক্ৰীণ শ্বেয়াৰ কৰক। চাই চেভ কৰক। নিশ্চিত কৰাৰ আগলৈকে লেনদেন নহয়।

অন-ডিভাইচ পাৰ্চ সদায় চলে। বৈকল্পিক AI পাৰ্চ (দৈনিক সীমা) সেই লিখনী — ফটো নহয় — আমাৰ চাৰ্ভাৰেৰে তৃতীয়-পক্ষ AIলৈ পঠাব পাৰে। অফলাইন থাকক বা AI এৰিলে পাৰ্চ ডিভাইচতে। ৰচিদ OCR ডিভাইচত (ML Kit)। Photo Picker আৰু Android Share — গেলাৰী অনুমতি নাই।

SMS ভিত্তিক মানি মেনেজমেণ্ট (বৈকল্পিক মূল সুবিধা)
আপোনাৰ অনুমতিত এপে RECEIVE_SMSৰে ডিভাইচৰ বেংক, UPI আৰু ৱালেট ডেবিট/ক্ৰেডিট মেচেজ পঢ়ে। মিলিলে Detected Transactions (হোম ⋮ মেনু)ত পৰামৰ্শ। কাৰ্ড টেপ কৰক, লাগিলে সম্পাদনা, Add টিপক। নিশ্চিতৰ আগলৈকে লেনদেন নহয়। মচিবলৈ সোৱাইপ।

এয়া বৈকল্পিক আৰু হোমত SMS অনুমতি নোপোৱালৈকে বন্ধ। নাকচ কৰিলেও কিপেড, লিখক/কওক/স্কেন, বাজেট আৰু এনালাইটিক্স চলে। আমি ডিফল্ট SMS এপ নহয়। READ_SMS বা SEND_SMS নাই। ৫ অংকৰ পেমেণ্ট SMS নপঠাওঁ; OTP/প্ৰতাৰণাৰ বাবে SMS নহয়। OTP, প্ৰমো, ৰাশি নথকা মেচেজ উপেক্ষা। SMS কেৱল ডিভাইচত পঢ়া হয়; আপলোড, বিক্ৰী বা শ্বেয়াৰ নহয়।

ধন ক’ত যায় ভাবি থকা বন্ধ কৰক। বাজেট, এনালাইটিক্স, ৱালেট — লগতে বৈকল্পিক SMS, ভইচ, স্কেন আৰু যিকোনো এপৰ পৰা শ্বেয়াৰ।

🔑 স্মাৰ্ট মানি মেনেজমেণ্টৰ মূল সুবিধা

✨ লিখক, কওক, স্কেন বা শ্বেয়াৰ
টেক্সটৰ দৰে লিখক, কওক, ৰচিদ স্কেন কৰক, বা GPay, PhonePe, Paytm, বেংক এপ বা যিকোনো ঠাইৰ পৰা বিল শ্বেয়াৰ কৰক। অন-ডিভাইচ OCR। বৈকল্পিক AI পাৰ্চ, দৈনিক সীমা।

💬 বৈকল্পিক বেংক / UPI SMS ইমপোৰ্ট
কেৱল আৰ্থিক SMS। Detected Transactionsত চাই Add। অন্য SMS উপেক্ষা। ডিভাইচত। বৈকল্পিক।

💳 দৈনিক খৰচ ট্ৰেকাৰ আৰু মানি মেনেজাৰ
নোট, শ্ৰেণী, পেমেণ্ট পদ্ধতিৰে দ্ৰুত এণ্ট্ৰি।
কাষ্টম আইকনৰ স্মাৰ্ট শ্ৰেণী।
ৱালেট: নগদ, কাৰ্ড, বেংক, ডিজিটেল।
আইটেমাইজড কেলকুলেটৰ: এটা পেমেণ্ট কেইবাশাৰীত।
সন্ধান, সজাওক, ফিল্টাৰ; কাষ্টম কাৰ্ড; দৈনিক খৰচৰ কেলেণ্ডাৰ।

🎤 ভইচ এড (বৈকল্পিক)
কৈ যোগ কৰক। চেভৰ আগতে চাওক। মাইক কেৱল ভইচ এড আৰম্ভ কৰিলে।

🎯 বাজেট প্লানাৰ আৰু সঞ্চয় ট্ৰেকাৰ
মাহিলী শ্ৰেণী বাজেট, এটা টেপত যোৱা মাহ কপি।
এপৰ বাহিৰতো অধিক খৰচ এলাৰ্ট।
লক্ষ্যৰে ছেভিংছ গ’ল।

📊 ৰিপোৰ্ট আৰু বিশ্লেষণ
কেশ্ব ফ্ল’ আৰু শ্ৰেণী / পেমেণ্ট চাৰ্ট।
সময়, ট্ৰেণ্ড, গড় তুলনা।
CSV, JSON ইমপোৰ্ট/এক্সপোৰ্ট, লোকেল .db বেকআপ (XLS/PDF নহয়)।

⏰ বিল আৰু শ্বেডিউল পেমেণ্ট
দৈনিক / সাপ্তাহিক / মাহিলী / বাৰ্ষিক নিয়ম। তাৰিখ ৰিমাইণ্ডাৰ।

⭐ প্ৰিয়
সঘনাই খৰচ এটা টেপত।

🏷️ টেগ
শ্ৰেণীৰ বাহিৰে টেগ।

🔒 অফলাইন-ফাৰ্ষ্ট আৰু সুৰক্ষিত
এণ্ট্ৰি আৰু বাজেট ইণ্টাৰনেট নোহোৱাকৈ। ছিংক নহ’লে লেজাৰ ডিভাইচত।
PIN, বায়’মেট্ৰিক, স্ক্ৰেম্বলড কিপেড, স্ক্ৰীনশ্বট ব্লক, Recents ব্লাৰ।

🤝 মাল্টি-কাৰেন্সি
চিহ্ন, গ্ৰুপিং (ভাৰতীয়সহ), দশমিক।

🔄 বৈকল্পিক মাল্টি-ডিভাইচ ছিংক
Google, ইমেইল বা মেজিক লিংক। ৪টা ডিভাইচলৈ (Pro)।

🎨 থীম
লাইট, ডাৰ্ক বা ছিষ্টেম।

🚀 এক্সপেন্স ট্ৰেকাৰ বাজেট এণ্ড স্পেণ্ড কিয়?
পকেট ট্ৰেকিং আৰু গুৰুতৰ বাজেটিং — লগতে বৈকল্পিক SMS, ভইচ, স্কেন আৰু যিকোনো এপৰ পৰা শ্বেয়াৰ, আপোনাৰ নিয়ন্ত্ৰণত।

ট্ৰেকিং, সঞ্চয় আৰু লক্ষ্যৰ বাবে এক্সপেন্স ট্ৰেকাৰ বাজেট এণ্ড স্পেণ্ড ডাউনল’ড কৰক।
```

---

## Español — `es`

### App name

```
Expense Tracker: Presupuesto
```

### Short description

```
Escribe, habla, escanea o comparte. SMS, presupuestos. En el dispositivo.
```

### Full description

```
¡Ahorra dinero, controla el gasto diario y toma las riendas de tus finanzas con Expense Tracker Budget & Spend!

ESCRIBE, HABLA, ESCANEA O COMPARTE (OPCIONAL)
Una sola hoja para añadir: escríbelo como un mensaje, dilo, escanea un ticket o comparte una factura / pantalla de pago desde cualquier app UPI, banco o monedero. Revisa y guarda. Nada es una transacción hasta que confirmes.

El análisis en el dispositivo siempre funciona. El análisis con IA opcional (límite diario) puede enviar ese texto — no la foto — a IA de terceros a través de nuestros servidores. Quédate sin conexión u omite la IA para analizar en el dispositivo. El OCR de tickets es en el dispositivo (ML Kit). Photo Picker y Android Share: sin permiso de galería.

GESTIÓN POR SMS (FUNCIÓN PRINCIPAL OPCIONAL)
Con tu permiso, la app usa RECEIVE_SMS para leer en el dispositivo mensajes de débito/crédito de banco, UPI y monederos. Los que coinciden aparecen en Detected Transactions (menú ⋮ de Inicio). Toca una tarjeta, edita si hace falta y pulsa Add. Nada es transacción hasta que confirmes. Desliza para borrar.

Es opcional y está apagada hasta que permitas SMS en Inicio. Si lo niegas, teclado, escribir/hablar/escanear, presupuestos y analíticas siguen. No somos tu app SMS predeterminada. No usamos READ_SMS ni SEND_SMS. No enviamos SMS de pago de 5 dígitos ni usamos SMS para OTP o fraude. Se ignoran OTP, promociones y mensajes sin importe. El SMS se analiza solo en el dispositivo; no se sube, vende ni comparte.

Deja de preguntarte a dónde va el dinero. Presupuestos, analíticas, carteras — más SMS, voz, escaneo y compartir desde cualquier app, opcionales.

🔑 FUNCIONES CLAVE

✨ Escribe, habla, escanea o comparte
Escríbelo, dilo, escanea un ticket o comparte una factura desde GPay, PhonePe, Paytm, un banco o cualquier sitio. OCR en el dispositivo. IA opcional con límite diario.

💬 Importación SMS banco / UPI (opcional)
Solo SMS financieros. Revisa en Detected Transactions y Add. El resto se ignora. En el dispositivo. Opcional.

💳 Control diario y gestor de dinero
Registro rápido con notas, categorías y métodos de pago.
Categorías inteligentes con iconos personalizables.
Carteras: efectivo, tarjetas, banco y digitales.
Calculadora por líneas: un pago en varios conceptos.
Buscar, ordenar, filtrar; tarjetas personalizables; calendario de gasto diario.

🎤 Añadir por voz (opcional)
Dicta una transacción. Revisa antes de guardar. Micrófono solo si inicias voz.

🎯 Presupuesto y ahorros
Presupuestos mensuales por categoría; copia el mes anterior en un toque.
Alertas de exceso aunque no estés en la app.
Metas de ahorro con progreso.

📊 Informes y análisis
Flujos de caja y gráficos por categoría / método de pago.
Compara periodos, tendencias y medias diarias.
CSV, JSON importar/exportar, copia .db local (no XLS/PDF).

⏰ Facturas y pagos programados
Reglas diarias, semanales, mensuales o anuales. Recordatorios de vencimiento.

⭐ Favoritos
Gastos frecuentes en un toque.

🏷️ Etiquetas
Etiqueta más allá de las categorías.

🔒 Offline primero y seguro
Registro y presupuestos sin internet. El libro queda en el dispositivo si no activas sincronización.
PIN, biometría, teclado desordenado, bloqueo de capturas, desenfoque en Recientes.

🤝 Multidivisa
Símbolo, agrupación (incluida india) y decimales.

🔄 Sincronización opcional
Google, correo o enlace mágico. Hasta 4 dispositivos (Pro).

🎨 Temas
Claro, oscuro o del sistema.

🚀 ¿POR QUÉ EXPENSE TRACKER BUDGET & SPEND?
Seguimiento de bolsillo y presupuesto serio — más SMS, voz, escaneo y compartir desde cualquier app, bajo tu control.

Descarga Expense Tracker Budget & Spend para empezar a seguir, ahorrar y alcanzar tus metas.
```

---

## Português (Brasil) — `pt-BR`

### App name

```
Expense Tracker: Orçamento
```

### Short description

```
Digite, fale, escaneie ou compartilhe contas. SMS, orçamentos. No dispositivo.
```

### Full description

```
Economize, acompanhe o gasto diário e controle suas finanças com Expense Tracker Budget & Spend!

DIGITE, FALE, ESCANEIE OU COMPARTILHE (OPCIONAL)
Uma folha para adicionar: escreva como mensagem, fale, escaneie um recibo ou compartilhe uma conta / tela de pagamento de qualquer app UPI, banco ou carteira. Revise e salve. Nada vira transação até você confirmar.

A análise no dispositivo sempre funciona. A análise de IA opcional (limite diário) pode enviar esse texto — não a foto — a IA de terceiros pelos nossos servidores. Fique offline ou pule a IA para analisar no dispositivo. OCR de recibo no dispositivo (ML Kit). Photo Picker e Android Share — sem permissão de galeria.

GESTÃO POR SMS (RECURSO PRINCIPAL OPCIONAL)
Com sua permissão, o app usa RECEIVE_SMS para ler no dispositivo débitos/créditos de banco, UPI e carteiras. Correspondências viram sugestões em Detected Transactions (menu ⋮ da Home). Toque no cartão, edite se precisar e Add. Nada é transação até confirmar. Deslize para excluir.

É opcional e fica desligado até você permitir SMS na Home. Se recusar, teclado, digitar/falar/escanear, orçamentos e análises continuam. Não somos o app SMS padrão. Não usamos READ_SMS nem SEND_SMS. Não enviamos SMS de pagamento de 5 dígitos nem usamos SMS para OTP ou fraude. OTP, promo e mensagens sem valor são ignorados. O SMS é lido só no dispositivo; não é enviado, vendido nem compartilhado.

Pare de se perguntar para onde vai o dinheiro. Orçamentos, análises, carteiras — mais SMS, voz, scan e compartilhar de qualquer app, opcionais.

🔑 RECURSOS PRINCIPAIS

✨ Digite, fale, escaneie ou compartilhe
Escreva, fale, escaneie um recibo ou compartilhe uma conta do GPay, PhonePe, Paytm, banco ou de qualquer lugar. OCR no dispositivo. IA opcional com limite diário.

💬 Importação SMS banco / UPI (opcional)
Só SMS financeiro. Revise em Detected Transactions e Add. O resto é ignorado. No dispositivo. Opcional.

💳 Controle diário e gestor de dinheiro
Lançamento rápido com notas, categorias e formas de pagamento.
Categorias inteligentes com ícones personalizáveis.
Carteiras: dinheiro, cartões, banco e digitais.
Calculadora itemizada: um pagamento em várias linhas.
Busca, ordem, filtro; cartões personalizáveis; calendário de gasto diário.

🎤 Adicionar por voz (opcional)
Fale a transação. Revise antes de salvar. Microfone só se iniciar voz.

🎯 Orçamento e poupança
Orçamentos mensais por categoria; copie o mês anterior em um toque.
Alertas de excesso mesmo fora do app.
Metas de poupança com progresso.

📊 Relatórios e análise
Fluxo de caixa e gráficos por categoria / pagamento.
Compare períodos, tendências e médias diárias.
CSV, JSON importar/exportar, backup .db local (não XLS/PDF).

⏰ Contas e pagamentos agendados
Regras diárias, semanais, mensais ou anuais. Lembretes de vencimento.

⭐ Favoritos
Gastos frequentes em um toque.

🏷️ Tags
Marque além das categorias.

🔒 Offline primeiro e seguro
Lançamentos e orçamentos sem internet. O livro fica no dispositivo se você não ligar a sincronização.
PIN, biometria, teclado embaralhado, bloqueio de captura, desfoque em Recentes.

🤝 Multimoeda
Símbolo, agrupamento (incluindo indiano) e decimais.

🔄 Sincronização opcional
Google, e-mail ou link mágico. Até 4 dispositivos (Pro).

🎨 Temas
Claro, escuro ou do sistema.

🚀 POR QUE EXPENSE TRACKER BUDGET & SPEND?
Controle de bolso e orçamento sério — mais SMS, voz, scan e compartilhar de qualquer app, no seu controle.

Baixe Expense Tracker Budget & Spend para acompanhar, economizar e atingir suas metas.
```

---

## Français — `fr`

### App name

```
Expense Tracker: Budget
```

### Short description

```
Tapez, parlez, scannez ou partagez des factures. SMS, budgets. Sur l’appareil.
```

### Full description

```
Économisez, suivez vos dépenses quotidiennes et maîtrisez vos finances avec Expense Tracker Budget & Spend !

TAPER, PARLER, SCANNER OU PARTAGER (FACULTATIF)
Une seule feuille d’ajout : tapez comme un message, parlez, scannez un reçu, ou partagez une facture / écran de paiement depuis n’importe quelle app UPI, banque ou portefeuille. Vérifiez, puis enregistrez. Rien n’est une transaction tant que vous ne confirmez pas.

L’analyse sur l’appareil fonctionne toujours. L’IA facultative (limite quotidienne) peut envoyer ce texte — pas la photo — à une IA tierce via nos serveurs. Restez hors ligne ou ignorez l’IA pour analyser sur l’appareil. OCR des reçus sur l’appareil (ML Kit). Photo Picker et Android Share — pas d’autorisation galerie.

GESTION PAR SMS (FONCTION PRINCIPALE FACULTATIVE)
Avec votre accord, l’app utilise RECEIVE_SMS pour lire sur l’appareil les débits/crédits banque, UPI et portefeuilles. Les correspondances deviennent des suggestions dans Detected Transactions (menu ⋮ Accueil). Touchez une carte, modifiez si besoin, Add. Rien n’est une transaction avant confirmation. Balayez pour supprimer.

C’est facultatif et désactivé jusqu’à l’autorisation SMS sur l’Accueil. Si vous refusez, clavier, taper/parler/scanner, budgets et analyses restent. Nous ne sommes pas l’app SMS par défaut. Pas de READ_SMS ni SEND_SMS. Pas de SMS de paiement à 5 chiffres ; pas de SMS pour OTP ou fraude. OTP, promos et messages sans montant ignorés. Le SMS n’est lu que sur l’appareil ; pas d’envoi, vente ni partage.

Arrêtez de vous demander où va l’argent. Budgets, analyses, portefeuilles — plus SMS, voix, scan et partage depuis n’importe quelle app, en option.

🔑 FONCTIONS CLÉS

✨ Taper, parler, scanner ou partager
Écrivez, parlez, scannez un reçu, ou partagez une facture depuis GPay, PhonePe, Paytm, une banque ou ailleurs. OCR sur l’appareil. IA facultative, limite quotidienne.

💬 Import SMS banque / UPI (facultatif)
SMS financiers seulement. Vérifiez dans Detected Transactions, puis Add. Le reste est ignoré. Sur l’appareil. Facultatif.

💳 Suivi quotidien et gestion d’argent
Saisie rapide avec notes, catégories et moyens de paiement.
Catégories intelligentes, icônes personnalisables.
Portefeuilles : espèces, cartes, banque, numériques.
Calculatrice détaillée : un paiement en plusieurs lignes.
Recherche, tri, filtre ; cartes personnalisables ; calendrier des dépenses.

🎤 Ajout vocal (facultatif)
Dictez. Vérifiez avant d’enregistrer. Micro seulement si vous lancez la voix.

🎯 Budget et épargne
Budgets mensuels par catégorie ; copiez le mois précédent d’un tap.
Alertes de dépassement même hors de l’app.
Objectifs d’épargne avec progression.

📊 Rapports et analyses
Flux de trésorerie et graphiques par catégorie / paiement.
Comparer périodes, tendances et moyennes.
CSV, JSON import/export, sauvegarde .db locale (pas XLS/PDF).

⏰ Factures et paiements planifiés
Règles quotidiennes, hebdo, mensuelles ou annuelles. Rappels d’échéance.

⭐ Favoris
Dépenses fréquentes d’un tap.

🏷️ Tags
Étiquetez au-delà des catégories.

🔒 Hors ligne d’abord et sécurisé
Saisie et budgets sans internet. Le grand livre reste sur l’appareil sans sync.
PIN, biométrie, clavier brouillé, blocage de captures, flou dans Récents.

🤝 Multi-devises
Symbole, groupement (dont indien) et décimales.

🔄 Sync facultative
Google, e-mail ou lien magique. Jusqu’à 4 appareils (Pro).

🎨 Thèmes
Clair, sombre ou système.

🚀 POURQUOI EXPENSE TRACKER BUDGET & SPEND ?
Suivi de poche et budget sérieux — plus SMS, voix, scan et partage depuis n’importe quelle app, sous votre contrôle.

Téléchargez Expense Tracker Budget & Spend pour suivre, économiser et atteindre vos objectifs.
```

---

## Deutsch — `de`

### App name

```
Expense Tracker: Budget
```

### Short description

```
Tippen, sprechen, scannen oder Rechnungen teilen. SMS, Budgets. Auf dem Gerät.
```

### Full description

```
Sparen Sie Geld, verfolgen Sie tägliche Ausgaben und behalten Sie Ihre Finanzen im Griff mit Expense Tracker Budget & Spend!

TIPPEN, SPRECHEN, SCANNEN ODER TEILEN (OPTIONAL)
Ein Erfassungsblatt: tippen Sie wie eine Nachricht, sprechen Sie, scannen Sie einen Beleg oder teilen Sie eine Rechnung / Zahlungsansicht aus jeder UPI-, Bank- oder Wallet-App. Prüfen, dann speichern. Nichts ist eine Buchung, bis Sie bestätigen.

Die Analyse auf dem Gerät funktioniert immer. Optionale KI-Analyse (Tageslimit) kann diesen Text — nicht das Foto — über unsere Server an Drittanbieter-KI senden. Offline bleiben oder KI überspringen, dann bleibt die Analyse auf dem Gerät. Beleg-OCR auf dem Gerät (ML Kit). Photo Picker und Android Share — keine Galerie-Berechtigung.

SMS-GELDVERWALTUNG (OPTIONALES KERNFEATURE)
Mit Ihrer Erlaubnis liest die App per RECEIVE_SMS Bank-, UPI- und Wallet-Lastschrift/Gutschrift auf dem Gerät. Treffer werden Vorschläge unter Detected Transactions (Home-⋮-Menü). Karte tippen, ggf. bearbeiten, Add. Nichts ist Buchung vor der Bestätigung. Wischen zum Löschen.

Optional und aus, bis Sie SMS auf Home erlauben. Bei Ablehnung laufen Tastatur, Tippen/Sprechen/Scannen, Budgets und Analysen weiter. Wir sind nicht Ihre Standard-SMS-App. Kein READ_SMS oder SEND_SMS. Keine 5-stelligen Zahlungs-SMS; kein SMS für OTP oder Betrug. OTP, Promo und Nachrichten ohne Betrag werden ignoriert. SMS nur auf dem Gerät gelesen; nicht hochgeladen, verkauft oder geteilt.

Hören Sie auf zu raten, wohin das Geld geht. Budgets, Analysen, Wallets — plus optionale SMS, Sprache, Scan und Teilen aus jeder App.

🔑 WICHTIGE FUNKTIONEN

✨ Tippen, sprechen, scannen oder teilen
Schreiben, sprechen, Beleg scannen oder Rechnung aus GPay, PhonePe, Paytm, Bank-App oder anderswo teilen. OCR auf dem Gerät. Optionale KI, Tageslimit.

💬 Optionale Bank-/UPI-SMS-Import
Nur Finanz-SMS. In Detected Transactions prüfen, Add. Rest ignoriert. Auf dem Gerät. Optional.

💳 Täglicher Tracker und Geldmanager
Schnelleingabe mit Notiz, Kategorie, Zahlungsmethode.
Smarte Kategorien mit eigenen Icons.
Wallets: Bargeld, Karten, Bank, digital.
Positionskalkulator: eine Zahlung in mehrere Zeilen.
Suchen, sortieren, filtern; eigene Karten; Kalender der Tagesausgaben.

🎤 Spracheingabe (optional)
Buchung sprechen. Vor dem Speichern prüfen. Mikro nur bei Sprachstart.

🎯 Budget und Sparziele
Monatliche Kategoriebudgets, Vormonat per Tipp kopieren.
Überschreitungsalarme auch außerhalb der App.
Sparziele mit Fortschritt.

📊 Berichte und Analyse
Cashflow und Kategorie-/Zahlungscharts.
Zeiträume, Trends, Durchschnitte vergleichen.
CSV, JSON Import/Export, lokales .db-Backup (kein XLS/PDF).

⏰ Rechnungen und geplante Zahlungen
Tägliche, wöchentliche, monatliche oder jährliche Regeln. Fälligkeitserinnerungen.

⭐ Favoriten
Häufige Ausgaben per Tipp.

🏷️ Tags
Über Kategorien hinaus taggen.

🔒 Offline-first und sicher
Erfassung und Budgets ohne Internet. Ledger bleibt auf dem Gerät ohne Sync.
PIN, Biometrie, durcheinandergewürfeltes Tastenfeld, Screenshot-Sperre, Unschärfe in Zuletzt.

🤝 Mehrwährung
Symbol, Gruppierung (inkl. indisch) und Dezimalstellen.

🔄 Optionale Gerätesync
Google, E-Mail oder Magic Link. Bis 4 Geräte (Pro).

🎨 Themes
Hell, dunkel oder System.

🚀 WARUM EXPENSE TRACKER BUDGET & SPEND?
Taschentracking und ernsthaftes Budget — plus optionale SMS, Sprache, Scan und Teilen aus jeder App, unter Ihrer Kontrolle.

Laden Sie Expense Tracker Budget & Spend herunter, um zu tracken, zu sparen und Ziele zu erreichen.
```

---

## Bahasa Indonesia — `id`

### App name

```
Expense Tracker: Anggaran
```

### Short description

```
Ketik, bicara, pindai, atau bagikan tagihan. SMS, anggaran. Di perangkat.
```

### Full description

```
Hemat uang, lacak belanja harian, dan kendalikan keuangan dengan Expense Tracker Budget & Spend!

KETIK, BICARA, PINDAI, ATAU BAGIKAN (OPSIONAL)
Satu lembar tambah: ketik seperti pesan, ucapkan, pindai struk, atau bagikan tagihan / layar pembayaran dari app UPI, bank, atau dompet mana pun. Tinjau, lalu simpan. Belum transaksi sampai Anda konfirmasi.

Parse di perangkat selalu jalan. Parse AI opsional (batas harian) dapat mengirim teks itu — bukan foto — ke AI pihak ketiga lewat server kami. Tetap offline atau lewati AI agar parse di perangkat. OCR struk di perangkat (ML Kit). Photo Picker dan Android Share — tanpa izin galeri.

MANAJEMEN UANG VIA SMS (FITUR INTI OPSIONAL)
Dengan izin Anda, app memakai RECEIVE_SMS untuk membaca debit/kredit bank, UPI, dan dompet di perangkat. Yang cocok jadi saran di Detected Transactions (menu ⋮ Beranda). Ketuk kartu, edit jika perlu, Add. Belum transaksi sampai konfirmasi. Geser untuk hapus.

Opsional dan mati sampai Anda izinkan SMS di Beranda. Jika ditolak, keypad, ketik/bicara/pindai, anggaran, dan analitik tetap jalan. Kami bukan app SMS default. Tidak pakai READ_SMS atau SEND_SMS. Tidak kirim SMS pembayaran 5 digit; tidak pakai SMS untuk OTP atau penipuan. OTP, promo, dan pesan tanpa jumlah diabaikan. SMS hanya dibaca di perangkat; tidak diunggah, dijual, atau dibagikan.

Berhenti menebak ke mana uang pergi. Anggaran, analitik, dompet — plus SMS, suara, pindai, dan bagikan dari app mana pun, opsional.

🔑 FITUR UTAMA

✨ Ketik, bicara, pindai, atau bagikan
Tulis, ucapkan, pindai struk, atau bagikan tagihan dari GPay, PhonePe, Paytm, app bank, atau mana saja. OCR di perangkat. AI opsional, batas harian.

💬 Impor SMS bank / UPI (opsional)
Hanya SMS keuangan. Tinjau di Detected Transactions, lalu Add. Lainnya diabaikan. Di perangkat. Opsional.

💳 Pelacak harian dan manajer uang
Entri cepat dengan catatan, kategori, metode bayar.
Kategori cerdas dengan ikon kustom.
Dompet: tunai, kartu, bank, digital.
Kalkulator item: satu pembayaran jadi beberapa baris.
Cari, urut, filter; kartu kustom; kalender belanja harian.

🎤 Tambah suara (opsional)
Ucapkan transaksi. Tinjau sebelum simpan. Mikrofon hanya saat mulai suara.

🎯 Perencana anggaran dan tabungan
Anggaran kategori bulanan, salin bulan lalu sekali ketuk.
Peringatan boros meski di luar app.
Tujuan tabungan dengan progres.

📊 Laporan dan analisis
Arus kas dan grafik kategori / metode bayar.
Bandingkan periode, tren, rata-rata harian.
CSV, JSON impor/ekspor, cadangan .db lokal (bukan XLS/PDF).

⏰ Tagihan dan pembayaran terjadwal
Aturan harian, mingguan, bulanan, atau tahunan. Pengingat jatuh tempo.

⭐ Favorit
Pengeluaran sering sekali ketuk.

🏷️ Tag
Tag di luar kategori.

🔒 Offline dulu dan aman
Entri dan anggaran tanpa internet. Buku besar di perangkat jika sinkronisasi mati.
PIN, biometrik, keypad acak, blokir screenshot, buram di Recents.

🤝 Multi-mata uang
Simbol, pengelompokan (termasuk India), desimal.

🔄 Sinkronisasi opsional
Google, email, atau tautan ajaib. Hingga 4 perangkat (Pro).

🎨 Tema
Terang, gelap, atau sistem.

🚀 MENGAPA EXPENSE TRACKER BUDGET & SPEND?
Pelacakan saku dan anggaran serius — plus SMS, suara, pindai, dan bagikan dari app mana pun, di kendali Anda.

Unduh Expense Tracker Budget & Spend untuk mulai lacak, hemat, dan capai tujuan.
```

---

## Italiano — `it`

### App name

```
Expense Tracker: Budget
```

### Short description

```
Scrivi, parla, scansiona o condividi bollette. SMS, budget. Sul dispositivo.
```

### Full description

```
Risparmia, traccia la spesa quotidiana e controlla le finanze con Expense Tracker Budget & Spend!

SCRIVI, PARLA, SCANSIONA O CONDIVIDI (OPZIONALE)
Un solo foglio di aggiunta: scrivi come un messaggio, parla, scansiona uno scontrino o condividi una bolletta / schermata di pagamento da qualsiasi app UPI, banca o wallet. Controlla, poi salva. Niente è una transazione finché non confermi.

L’analisi sul dispositivo funziona sempre. L’IA opzionale (limite giornaliero) può inviare quel testo — non la foto — a IA di terze parti tramite i nostri server. Resta offline o salta l’IA per analizzare sul dispositivo. OCR scontrini sul dispositivo (ML Kit). Photo Picker e Android Share — nessun permesso galleria.

GESTIONE VIA SMS (FUNZIONE PRINCIPALE OPZIONALE)
Con il tuo consenso l’app usa RECEIVE_SMS per leggere sul dispositivo addebiti/accrediti di banca, UPI e wallet. Le corrispondenze diventano suggerimenti in Detected Transactions (menu ⋮ Home). Tocca una scheda, modifica se serve, Add. Niente è transazione prima della conferma. Scorri per eliminare.

È opzionale e spento finché non consenti SMS in Home. Se rifiuti, tastierino, scrivi/parla/scansiona, budget e analitiche restano. Non siamo l’app SMS predefinita. Niente READ_SMS o SEND_SMS. Non inviamo SMS di pagamento a 5 cifre; niente SMS per OTP o frodi. OTP, promo e messaggi senza importo ignorati. L’SMS è letto solo sul dispositivo; non caricato, venduto o condiviso.

Smetti di chiederti dove vanno i soldi. Budget, analitiche, wallet — più SMS, voce, scansione e condivisione da qualsiasi app, opzionali.

🔑 FUNZIONI CHIAVE

✨ Scrivi, parla, scansiona o condividi
Scrivi, parla, scansiona uno scontrino o condividi una bolletta da GPay, PhonePe, Paytm, banca o altrove. OCR sul dispositivo. IA opzionale, limite giornaliero.

💬 Import SMS banca / UPI (opzionale)
Solo SMS finanziari. Controlla in Detected Transactions, poi Add. Il resto ignorato. Sul dispositivo. Opzionale.

💳 Tracker giornaliero e gestore soldi
Inserimento rapido con note, categorie e metodi di pagamento.
Categorie smart con icone personalizzabili.
Wallet: contanti, carte, banca, digitali.
Calcolatrice per voci: un pagamento in più righe.
Cerca, ordina, filtra; schede personalizzabili; calendario della spesa.

🎤 Aggiungi vocale (opzionale)
Dì la transazione. Controlla prima di salvare. Microfono solo se avvii la voce.

🎯 Budget e risparmi
Budget mensili per categoria; copia il mese scorso con un tap.
Avvisi di sforamento anche fuori dall’app.
Obiettivi di risparmio con progresso.

📊 Report e analisi
Flussi di cassa e grafici per categoria / pagamento.
Confronta periodi, trend e medie.
CSV, JSON import/export, backup .db locale (non XLS/PDF).

⏰ Bollette e pagamenti pianificati
Regole giornaliere, settimanali, mensili o annuali. Promemoria scadenze.

⭐ Preferiti
Spese frequenti con un tap.

🏷️ Tag
Tag oltre le categorie.

🔒 Offline-first e sicuro
Inserimenti e budget senza internet. Il registro resta sul dispositivo senza sync.
PIN, biometria, tastierino mescolato, blocco screenshot, sfocatura in Recenti.

🤝 Multivaluta
Simbolo, raggruppamento (incluso indiano) e decimali.

🔄 Sync opzionale
Google, email o magic link. Fino a 4 dispositivi (Pro).

🎨 Temi
Chiaro, scuro o di sistema.

🚀 PERCHÉ EXPENSE TRACKER BUDGET & SPEND?
Tracciamento da tasca e budget serio — più SMS, voce, scansione e condivisione da qualsiasi app, sotto il tuo controllo.

Scarica Expense Tracker Budget & Spend per tracciare, risparmiare e raggiungere i tuoi obiettivi.
```

---

## 日本語 — `ja`

### App name

```
Expense Tracker: 予算と支出
```

### Short description

```
入力・音声・スキャン・請求書共有。SMS、予算。端末内。
```

### Full description

```
Expense Tracker Budget & Spend でお金を貯め、日々の支出を記録し、家計を管理しましょう。

入力・話す・スキャン・共有（任意）
1つの追加シート：メッセージのように入力、話す、レシートをスキャン、または任意の UPI・銀行・ウォレットアプリから請求／支払い画面を共有。確認して保存。確定するまで取引になりません。

端末内解析は常に動作。任意の AI 解析（日次上限）は写真ではなくそのテキストを当社サーバー経由で第三者 AI に送る場合があります。オフラインにするか AI をスキップすれば解析は端末内。レシート OCR は端末内（ML Kit）。Photo Picker と Android Share — ギャラリー権限なし。

SMS による資金管理（任意の中核機能）
許可があるとアプリは RECEIVE_SMS で端末上の銀行・UPI・ウォレットの入出金メッセージを読みます。一致したものは Detected Transactions（ホーム ⋮ メニュー）の候補になります。カードをタップし、必要なら編集して Add。確定まで取引なし。スワイプで削除。

任意で、ホームで SMS を許可するまでオフ。拒否してもキーパッド、入力／音声／スキャン、予算、分析は動きます。デフォルト SMS アプリではありません。READ_SMS / SEND_SMS は使いません。5桁の課金 SMS は送らず、OTP や詐欺検知にも SMS を使いません。OTP・宣伝・金額なしは無視。SMS は端末内のみで解析し、アップロード・販売・共有しません。

お金の行き先を気にしなくて済みます。予算、分析、ウォレットに加え、任意の SMS・音声・スキャン・どのアプリからでも共有。

🔑 主な機能

✨ 入力・話す・スキャン・共有
テキスト入力、音声、レシートスキャン、または GPay、PhonePe、Paytm、銀行アプリなどから請求を共有。端末内 OCR。任意 AI、日次上限。

💬 任意の銀行 / UPI SMS 取り込み
金融 SMS のみ。Detected Transactions で確認して Add。その他は無視。端末内。任意。

💳 日次トラッカーとマネーマネージャ
メモ、カテゴリ、支払方法ですぐ記録。
カスタムアイコンのスマートカテゴリ。
ウォレット：現金、カード、銀行、デジタル。
明細電卓：1回の支払いを複数行に。
検索・並べ替え・フィルタ、カスタムカード、日次カレンダー。

🎤 音声追加（任意）
話して追加。保存前に確認。マイクは音声開始時のみ。

🎯 予算と貯蓄
月次カテゴリ予算、前月をワンタップでコピー。
アプリ外でも使いすぎアラート。
目標付き貯蓄ゴール。

📊 レポートと分析
キャッシュフローとカテゴリ／支払チャート。
期間、トレンド、平均の比較。
CSV、JSON 入出力、ローカル .db バックアップ（XLS/PDF なし）。

⏰ 請求と定期支払い
日／週／月／年のルール。期日リマインダー。

⭐ お気に入り
よく使う支出をワンタップ。

🏷️ タグ
カテゴリを超えてタグ付け。

🔒 オフライン優先とセキュリティ
記録と予算はネット不要。同期しなければ台帳は端末内。
PIN、生体認証、スクランブルキーパッド、スクショ禁止、最近の画面のぼかし。

🤝 多通貨
記号、桁区切り（インド含む）、小数。

🔄 任意の複数端末同期
Google、メール、マジックリンク。最大 4 台（Pro）。

🎨 テーマ
ライト、ダーク、システム。

🚀 選ぶ理由
ポケットの記録と本格予算に加え、任意の SMS・音声・スキャン・どのアプリからの共有を、あなたが制御。

Expense Tracker Budget & Spend をダウンロードして、記録・貯蓄・目標へ。
```

---

## 한국어 — `ko`

### App name

```
Expense Tracker: 예산 관리
```

### Short description

```
입력, 말하기, 스캔 또는 청구서 공유. SMS, 예산. 기기에서.
```

### Full description

```
Expense Tracker Budget & Spend로 돈을 모으고, 매일 지출을 추적하고, 개인 재정을 관리하세요!

입력, 말하기, 스캔 또는 공유(선택)
하나의 추가 시트: 메시지처럼 입력하고, 말하고, 영수증을 스캔하거나, 어떤 UPI·은행·지갑 앱에서든 청구/결제 화면을 공유하세요. 확인 후 저장. 확정 전까지는 거래가 아닙니다.

기기 내 파싱은 항상 동작합니다. 선택 AI 파싱(일일 한도)은 사진이 아니라 그 텍스트를 당사 서버를 통해 제3자 AI로 보낼 수 있습니다. 오프라인이거나 AI를 건너뛰면 파싱은 기기에서. 영수증 OCR은 기기(ML Kit). Photo Picker와 Android Share — 갤러리 권한 없음.

SMS 기반 머니 관리(선택 핵심 기능)
허용 시 앱은 RECEIVE_SMS로 기기의 은행·UPI·지갑 입출금 메시지를 읽습니다. 일치하면 Detected Transactions(홈 ⋮ 메뉴) 제안이 됩니다. 카드를 탭하고 필요하면 수정한 뒤 Add. 확정 전까지 거래 없음. 스와이프로 삭제.

선택 사항이며 홈에서 SMS를 허용할 때까지 꺼져 있습니다. 거부해도 키패드, 입력/말하기/스캔, 예산, 분석은 동작합니다. 기본 SMS 앱이 아닙니다. READ_SMS나 SEND_SMS를 쓰지 않습니다. 5자리 결제 SMS를 보내지 않으며 OTP/사기에 SMS를 쓰지 않습니다. OTP, 프로모, 금액 없는 메시지는 무시. SMS는 기기에서만 읽고 업로드·판매·공유하지 않습니다.

돈이 어디로 가는지 추측하지 마세요. 예산, 분석, 지갑 — 그리고 선택 SMS, 음성, 스캔, 어떤 앱에서든 공유.

🔑 주요 기능

✨ 입력, 말하기, 스캔 또는 공유
텍스트처럼 쓰고, 말하고, 영수증을 스캔하거나 GPay, PhonePe, Paytm, 은행 앱 등에서 청구서를 공유. 기기 OCR. 선택 AI, 일일 한도.

💬 선택 은행 / UPI SMS 가져오기
금융 SMS만. Detected Transactions에서 보고 Add. 나머지는 무시. 기기에서. 선택.

💳 일일 트래커와 머니 매니저
메모, 카테고리, 결제 수단으로 빠른 입력.
맞춤 아이콘의 스마트 카테고리.
지갑: 현금, 카드, 은행, 디지털.
항목 계산기: 한 결제를 여러 줄로.
검색, 정렬, 필터, 맞춤 카드, 일일 달력.

🎤 음성 추가(선택)
말해서 추가. 저장 전 확인. 마이크는 음성 시작 시에만.

🎯 예산과 저축
월 카테고리 예산, 지난달을 한 탭으로 복사.
앱 밖에서도 초과 알림.
목표 있는 저축 골.

📊 리포트와 분석
현금 흐름과 카테고리/결제 차트.
기간, 추세, 평균 비교.
CSV, JSON 가져오기/내보내기, 로컬 .db 백업(XLS/PDF 아님).

⏰ 청구와 예약 결제
일/주/월/년 규칙. 만기 알림.

⭐ 즐겨찾기
자주 쓰는 지출 한 탭.

🏷️ 태그
카테고리를 넘어 태그.

🔒 오프라인 우선과 보안
입력과 예산은 인터넷 없이. 동기화하지 않으면 장부는 기기.
PIN, 생체, 섞인 키패드, 스크린샷 차단, 최근 화면 흐림.

🤝 다중 통화
기호, 그룹(인도식 포함), 소수점.

🔄 선택 다중 기기 동기화
Google, 이메일 또는 매직 링크. 최대 4대(Pro).

🎨 테마
라이트, 다크 또는 시스템.

🚀 왜 EXPENSE TRACKER BUDGET & SPEND인가?
주머니 추적과 진지한 예산 — 선택 SMS, 음성, 스캔, 어떤 앱에서든 공유를 직접 제어.

Expense Tracker Budget & Spend를 받아 추적, 저축, 목표를 시작하세요.
```

---

## Русский — `ru`

### App name

```
Expense Tracker: Бюджет
```

### Short description

```
Пишите, говорите, сканируйте или делитесь счетами. SMS, бюджет. На устройстве.
```

### Full description

```
Экономьте, следите за ежедневными тратами и держите личные финансы под контролем с Expense Tracker Budget & Spend!

ПИШИТЕ, ГОВОРИТЕ, СКАНИРУЙТЕ ИЛИ ДЕЛИТЕСЬ (ОПЦИОНАЛЬНО)
Один лист добавления: напишите как сообщение, скажите, отсканируйте чек или поделитесь счётом / экраном оплаты из любого UPI, банковского или кошелёчного приложения. Проверьте и сохраните. Пока не подтвердите — это не транзакция.

Разбор на устройстве всегда работает. Опциональный ИИ (дневной лимит) может отправить этот текст — не фото — стороннему ИИ через наши серверы. Оставайтесь офлайн или пропустите ИИ — разбор на устройстве. OCR чеков на устройстве (ML Kit). Photo Picker и Android Share — без доступа к галерее.

УПРАВЛЕНИЕ ДЕНЬГАМИ ПО SMS (ОПЦИОНАЛЬНАЯ ОСНОВНАЯ ФУНКЦИЯ)
С вашего разрешения приложение через RECEIVE_SMS читает на устройстве дебет/кредит банка, UPI и кошельков. Совпадения становятся подсказками в Detected Transactions (меню ⋮ на главной). Нажмите карточку, при необходимости правьте, Add. До подтверждения транзакции нет. Смахните, чтобы удалить.

Это опционально и выключено, пока вы не разрешите SMS на главной. При отказе клавиатура, ввод/голос/скан, бюджеты и аналитика работают. Мы не приложение SMS по умолчанию. Нет READ_SMS и SEND_SMS. Не шлём 5-значные платёжные SMS и не используем SMS для OTP или мошенничества. OTP, акции и сообщения без суммы игнорируются. SMS читается только на устройстве; не загружается, не продаётся и не передаётся.

Перестаньте гадать, куда уходят деньги. Бюджеты, аналитика, кошельки — плюс опциональные SMS, голос, скан и шаринг из любого приложения.

🔑 КЛЮЧЕВЫЕ ФУНКЦИИ

✨ Пишите, говорите, сканируйте или делитесь
Напишите, скажите, отсканируйте чек или поделитесь счётом из GPay, PhonePe, Paytm, банка или откуда угодно. OCR на устройстве. Опциональный ИИ, дневной лимит.

💬 Опциональный импорт SMS банка / UPI
Только финансовые SMS. Проверьте в Detected Transactions, затем Add. Остальное игнорируется. На устройстве. Опционально.

💳 Ежедневный трекер и менеджер денег
Быстрый ввод с заметкой, категорией и способом оплаты.
Умные категории с настраиваемыми иконками.
Кошельки: наличные, карты, банк, цифровые.
Калькулятор позиций: один платёж в несколько строк.
Поиск, сортировка, фильтр; свои карточки; календарь трат.

🎤 Голосовое добавление (опционально)
Продиктуйте. Проверьте перед сохранением. Микрофон только при старте голоса.

🎯 Бюджет и накопления
Месячные бюджеты по категориям; скопируйте прошлый месяц одним касанием.
Оповещения о перерасходе даже вне приложения.
Цели накоплений с прогрессом.

📊 Отчёты и анализ
Денежный поток и графики по категории / оплате.
Сравнение периодов, трендов и средних.
CSV, JSON импорт/экспорт, локальный бэкап .db (не XLS/PDF).

⏰ Счета и запланированные платежи
Правила ежедневно / еженедельно / ежемесячно / ежегодно. Напоминания о сроке.

⭐ Избранное
Частые траты одним касанием.

🏷️ Теги
Теги сверх категорий.

🔒 Сначала офлайн и безопасность
Ввод и бюджеты без интернета. Книга на устройстве, если синхронизация выключена.
PIN, биометрия, перемешанная клавиатура, блок скриншотов, размытие в недавних.

🤝 Несколько валют
Символ, группировка (включая индийскую) и десятичные.

🔄 Опциональная синхронизация
Google, почта или magic link. До 4 устройств (Pro).

🎨 Темы
Светлая, тёмная или системная.

🚀 ПОЧЕМУ EXPENSE TRACKER BUDGET & SPEND?
Карманный учёт и серьёзный бюджет — плюс опциональные SMS, голос, скан и шаринг из любого приложения под вашим контролем.

Скачайте Expense Tracker Budget & Spend, чтобы учитывать, копить и достигать целей.
```

---

