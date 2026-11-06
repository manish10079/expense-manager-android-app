package com.mknlabs.expensetracker.data.constants

/**
 * Default multi-language Remote Config payloads for the in-app update dialog.
 * Live copy still comes from Firebase Remote Config; these strings are the
 * canonical map (and unit-test fixtures) for all app locales except `system`.
 */
object AppStringsPayload {
    val releaseNotesJson = """
      {
        "en": "What's New 291 (2.258.2)\n• Type, talk, or scan — one add sheet\n• Write it like a text; we parse it\n• System photo picker; shared receipts scan better\n• Tweak the amount, then a quick save animation\n• Notifications match your app language",
        "or": "ନୂଆ କଣ 291 (2.258.2)\n• ଟାଇପ୍, କୁହନ୍ତୁ କିମ୍ବା ସ୍କାନ୍ — ଗୋଟିଏ ଯୋଗ ସିଟ୍\n• ମେସେଜ୍ ପରି ଲେଖନ୍ତୁ; ଆମେ ପାର୍ସ କରୁ\n• ସିଷ୍ଟମ୍ ଫଟୋ ପିକର୍; ସେଆର୍ ରସିଦ ଭଲ ସ୍କାନ୍\n• ରାଶି ଠିକ୍ କରନ୍ତୁ, ତା’ପରେ ଦ୍ରୁତ ସେଭ୍ ଆନିମେସନ୍\n• ନୋଟିଫିକେସନ୍ ଆପ୍ ଭାଷା ଅନୁସାରେ",
        "hi": "नया क्या है 291 (2.258.2)\n• टाइप, बोलें या स्कैन — एक ही शीट\n• मैसेज की तरह लिखें; हम पार्स करते हैं\n• सिस्टम फ़ोटो पिकर; शेयर की रसीदें बेहतर स्कैन\n• राशि ठीक करें, फिर तेज़ सेव एनिमेशन\n• सूचनाएँ ऐप की भाषा में",
        "kn": "ಹೊಸತೇನು 291 (2.258.2)\n• ಟೈಪ್, ಮಾತು ಅಥವಾ ಸ್ಕ್ಯಾನ್ — ಒಂದೇ ಸೇರಿಸುವ ಶೀಟ್\n• ಸಂದೇಶದಂತೆ ಬರೆಯಿರಿ; ನಾವು ಪಾರ್ಸ್ ಮಾಡುತ್ತೇವೆ\n• ಸಿಸ್ಟಂ ಫೋಟೋ ಪಿಕರ್; ಹಂಚಿದ ರಸೀದಿಗಳು ಉತ್ತಮ ಸ್ಕ್ಯಾನ್\n• ಮೊತ್ತ ಸರಿಪಡಿಸಿ, ನಂತರ ತ್ವರಿತ ಸೇವ್ ಅನಿಮೇಷನ್\n• ಅಧಿಸೂಚನೆಗಳು ಅಪ್ಲಿಕೇಶನ್ ಭಾಷೆಗೆ ಹೊಂದುತ್ತವೆ",
        "ta": "புதியவை 291 (2.258.2)\n• தட்டச்சு, பேச்சு அல்லது ஸ்கேன் — ஒரே சேர்ப்புத் தாள்\n• செய்தி போல் எழுதுங்கள்; நாங்கள் பாகுபடுத்துவோம்\n• சிஸ்டம் புகைப்படத் தேர்வி; பகிர்ந்த ரசீதுகள் சிறப்பாக ஸ்கேன்\n• தொகையை சரிசெய்து, விரைவு சேமிப்பு அனிமேஷன்\n• அறிவிப்புகள் செயலியின் மொழியைப் பின்பற்றும்",
        "bn": "নতুন কী 291 (2.258.2)\n• টাইপ, কথা বা স্ক্যান — এক জায়গায় যোগ\n• মেসেজের মতো লিখুন; আমরা পার্স করি\n• সিস্টেম ফটো পিকার; শেয়ার করা রসিদ ভালো স্ক্যান\n• অঙ্ক ঠিক করুন, তারপর দ্রুত সেভ অ্যানিমেশন\n• নোটিফিকেশন অ্যাপের ভাষা অনুসারে",
        "mr": "नवीन काय 291 (2.258.2)\n• टाइप, बोला किंवा स्कॅन — एकच अॅड शीट\n• मेसेजसारखे लिहा; आम्ही पार्स करतो\n• सिस्टम फोटो पिकर; शेअर केलेल्या पावत्या चांगल्या स्कॅन\n• रक्कम दुरुस्त करा, मग झटपट सेव्ह अॅनिमेशन\n• सूचना अॅपच्या भाषेत",
        "ml": "പുതിയത് എന്തെല്ലാം 291 (2.258.2)\n• ടൈപ്പ്, സംസാരം അല്ലെങ്കിൽ സ്കാൻ — ഒരു ആഡ് ഷീറ്റ്\n• മെസേജ് പോലെ എഴുതൂ; ഞങ്ങൾ പാഴ്‌സ് ചെയ്യും\n• സിസ്റ്റം ഫോട്ടോ പിക്കർ; ഷെയർ ചെയ്ത രസീതുകൾ മെച്ചം\n• തുക ശരിയാക്കൂ, പിന്നെ വേഗത്തിലുള്ള സേവ് അനിമേഷൻ\n• അറിയിപ്പുകൾ ആപ്പ് ഭാഷ പിന്തുടരും",
        "te": "కొత్తది ఏమిటి 291 (2.258.2)\n• టైప్, మాట లేదా స్కాన్ — ఒకే యాడ్ షీట్\n• మెసేజ్‌లా రాయండి; మేము పార్స్ చేస్తాము\n• సిస్టమ్ ఫోటో పికర్; షేర్ చేసిన రసీదులు మెరుగ్గా స్కాన్\n• మొత్తం సరిచేసి, తర్వాత త్వరిత సేవ్ యానిమేషన్\n• నోటిఫికేషన్లు యాప్ భాషను అనుసరిస్తాయి",
        "pa": "ਨਵਾਂ ਕੀ ਹੈ 291 (2.258.2)\n• ਟਾਈਪ, ਗੱਲ ਜਾਂ ਸਕੈਨ — ਇੱਕ ਐਡ ਸ਼ੀਟ\n• ਮੈਸੇਜ ਵਾਂਗ ਲਿਖੋ; ਅਸੀਂ ਪਾਰਸ ਕਰਦੇ ਹਾਂ\n• ਸਿਸਟਮ ਫੋਟੋ ਪਿਕਰ; ਸਾਂਝੀਆਂ ਰਸੀਦਾਂ ਵਧੀਆ ਸਕੈਨ\n• ਰਕਮ ਠੀਕ ਕਰੋ, ਫਿਰ ਤੇਜ਼ ਸੇਵ ਐਨੀਮੇਸ਼ਨ\n• ਨੋਟੀਫਿਕੇਸ਼ਨ ਐਪ ਦੀ ਭਾਸ਼ਾ ਮੁਤਾਬਕ",
        "gu": "નવું શું છે 291 (2.258.2)\n• ટાઈપ, વાત અથવા સ્કેન — એક જ શીટ\n• મેસેજની જેમ લખો; અમે પાર્સ કરીએ\n• સિસ્ટમ ફોટો પિકર; શેર થયેલી રસીદ વધુ સારી સ્કેન\n• રકમ સુધારો, પછી ઝડપી સેવ એનિમેશન\n• નોટિફિકેશન એપની ભાષા અનુસાર",
        "as": "নতুন কি 291 (2.258.2)\n• টাইপ, কথা বা স্কেন — একেখন যোগ শ্বীট\n• মেচেজৰ দৰে লিখক; আমি পাৰ্চ কৰোঁ\n• চিষ্টেম ফটো পিকাৰ; শ্বেয়াৰ ৰচিদ ভাল স্কেন\n• পৰিমাণ ঠিক কৰক, তাৰ পিছত দ্ৰুত চেভ এনিমেচন\n• জাননী এপৰ ভাষা অনুসৰি",
        "es": "Novedades 291 (2.258.2)\n• Escribe, habla o escanea: un solo panel\n• Escríbelo como un mensaje; lo interpretamos\n• Selector de fotos del sistema; los tickets compartidos se leen mejor\n• Ajusta el importe y una animación de guardado rápida\n• Las notificaciones siguen el idioma de la app",
        "pt-BR": "Novidades 291 (2.258.2)\n• Digite, fale ou escaneie — uma só tela de adicionar\n• Escreva como mensagem; a gente interpreta\n• Seletor de fotos do sistema; recibos compartilhados leem melhor\n• Ajuste o valor e uma animação rápida de salvar\n• Notificações seguem o idioma do app",
        "fr": "Nouveautés 291 (2.258.2)\n• Tapez, parlez ou scannez — un seul panneau\n• Écrivez comme un texto ; on analyse\n• Sélecteur photo système ; reçus partagés mieux lus\n• Ajustez le montant, puis une courte animation d’enregistrement\n• Les notifications suivent la langue de l’app",
        "de": "Neuigkeiten 291 (2.258.2)\n• Tippen, sprechen oder scannen — ein Hinzufügen-Bereich\n• Schreib es wie eine Nachricht; wir werten aus\n• System-Fotowähler; geteilte Belege scannen besser\n• Betrag anpassen, dann kurze Speicheranimation\n• Mitteilungen folgen der App-Sprache",
        "id": "Yang baru 291 (2.258.2)\n• Ketik, bicara, atau pindai — satu lembar tambah\n• Tulis seperti chat; kami uraikan\n• Pemilih foto sistem; struk yang dibagikan lebih akurat\n• Sesuaikan jumlah, lalu animasi simpan singkat\n• Notifikasi mengikuti bahasa aplikasi",
        "ja": "新着情報 291 (2.258.2)\n• 入力・音声・スキャンはひとつの追加画面\n• メッセージのように書けば解析します\n• システムの写真ピッカー。共有レシートも読みやすく\n• 金額を直して、短い保存アニメ\n• 通知はアプリの言語に合わせます",
        "ko": "새로운 기능 291 (2.258.2)\n• 입력, 말하기, 스캔 — 추가 화면 하나\n• 문자처럼 쓰면 우리가 분석합니다\n• 시스템 사진 선택기. 공유 영수증도 더 잘 읽힘\n• 금액 수정 후 짧은 저장 애니메이션\n• 알림은 앱 언어를 따릅니다",
        "it": "Novità 291 (2.258.2)\n• Scrivi, parla o scansiona — un solo foglio\n• Scrivilo come un messaggio; lo interpretiamo\n• Selettore foto di sistema; scontrini condivisi letti meglio\n• Modifica l’importo, poi una breve animazione di salvataggio\n• Le notifiche seguono la lingua dell’app",
        "ru": "Что нового 291 (2.258.2)\n• Пишите, говорите или сканируйте — один экран добавления\n• Как в сообщении — мы разберём\n• Системный выбор фото; общие чеки читаются лучше\n• Поправьте сумму, затем короткая анимация сохранения\n• Уведомления на языке приложения"
      }
    """.trimIndent()

    val titlesJson = """
      {
        "en": "What's new",
        "or": "ନୂଆ କଣ",
        "hi": "नया क्या है",
        "kn": "ಹೊಸತೇನು",
        "ta": "புதியவை",
        "bn": "নতুন কী",
        "mr": "नवीन काय",
        "ml": "പുതിയത് എന്തെല്ലാം",
        "te": "కొత్తది ఏమిటి",
        "pa": "ਨਵਾਂ ਕੀ ਹੈ",
        "gu": "નવું શું છે",
        "as": "নতুন কি",
        "es": "Novedades",
        "pt-BR": "Novidades",
        "fr": "Nouveautés",
        "de": "Neuigkeiten",
        "id": "Yang baru",
        "ja": "新着情報",
        "ko": "새로운 기능",
        "it": "Novità",
        "ru": "Что нового"
      }
    """.trimIndent()
}
