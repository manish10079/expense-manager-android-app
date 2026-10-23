package com.mknlabs.expensetracker.data.constants

/**
 * Default multi-language Remote Config payloads for the in-app update dialog.
 * Live copy still comes from Firebase Remote Config; these strings are the
 * canonical map (and unit-test fixtures) for all app locales except `system`.
 */
object AppStringsPayload {
    val releaseNotesJson = """
      {
        "en": "What's New 277 (2.235.0)\n• Choose your language first in setup\n• Language applies immediately\n• Your language is remembered if setup isn’t finished\n• English, Odia, and Hindi are listed first\n• Fuller translations across languages\n• Install updates from inside the app\n• Dark transaction cards and sheets match Spent Today",
        "or": "ନୂଆ କଣ 277 (2.235.0)\n• ସେଟଅପରେ ପ୍ରଥମେ ଭାଷା ବାଛନ୍ତୁ\n• ଭାଷା ତୁରନ୍ତ ଲାଗୁ ହୁଏ\n• ସେଟଅପ ଅସମ୍ପୂର୍ଣ୍ଣ ରହିଲେ ମଧ୍ୟ ଭାଷା ମନେ ରହେ\n• ଇଂରାଜୀ, ଓଡ଼ିଆ ଓ ହିନ୍ଦୀ ତାଲିକାରେ ପ୍ରଥମେ\n• ଅଧିକ ଅନୁବାଦ\n• ଆପ୍ ଭିତରୁ ଅପଡେଟ୍ ଇନ୍‌ଷ୍ଟଲ୍\n• ଡାର୍କ କାର୍ଡ ଓ ସିଟ୍ Spent Today ସହିତ ମେଳ ଖାଏ",
        "hi": "नया क्या है 277 (2.235.0)\n• सेटअप में सबसे पहले भाषा चुनें\n• भाषा तुरंत लागू होती है\n• सेटअप अधूरा रहने पर भी भाषा याद रहती है\n• अंग्रेज़ी, ओड़िया और हिंदी सूची में सबसे ऊपर\n• बेहतर अनुवाद\n• ऐप के अंदर से अपडेट इंस्टॉल करें\n• डार्क कार्ड और शीट Spent Today से मैच करती हैं",
        "es": "Novedades 277 (2.235.0)\n• Elige el idioma al inicio de la configuración\n• El idioma se aplica al momento\n• Se recuerda si no terminas la configuración\n• Inglés, odia e hindi van primero\n• Traducciones más completas\n• Instala actualizaciones desde la app\n• Tarjetas y hojas oscuras coinciden con Spent Today",
        "pt-BR": "Novidades 277 (2.235.0)\n• Escolha o idioma no início da configuração\n• O idioma vale na hora\n• Fica salvo se você não terminar a configuração\n• Inglês, odia e hindi vêm primeiro\n• Traduções mais completas\n• Instale atualizações pelo app\n• Cards e sheets escuros combinam com Spent Today",
        "fr": "Nouveautés 277 (2.235.0)\n• Choisissez la langue dès le début de la configuration\n• La langue s’applique tout de suite\n• Elle est mémorisée si la configuration n’est pas terminée\n• Anglais, odia et hindi en tête de liste\n• Traductions plus complètes\n• Installez les mises à jour dans l’app\n• Cartes et feuilles sombres alignées sur Spent Today",
        "de": "Neuigkeiten 277 (2.235.0)\n• Sprache zuerst in der Einrichtung wählen\n• Sprache gilt sofort\n• Bleibt gespeichert, wenn die Einrichtung unvollständig ist\n• Englisch, Odia und Hindi stehen oben\n• Vollständigere Übersetzungen\n• Updates in der App installieren\n• Dunkle Karten und Sheets wie Spent Today",
        "id": "Yang baru 277 (2.235.0)\n• Pilih bahasa di awal penyiapan\n• Bahasa langsung diterapkan\n• Bahasa tetap tersimpan jika penyiapan belum selesai\n• Inggris, Odia, dan Hindi di urutan atas\n• Terjemahan lebih lengkap\n• Pasang pembaruan dari dalam aplikasi\n• Kartu dan sheet gelap mengikuti Spent Today",
        "ja": "新着情報 277 (2.235.0)\n• 初期設定で最初に言語を選べます\n• 言語はすぐに反映されます\n• 設定が途中でも言語は保存されます\n• 英語・オディア語・ヒンディー語が先頭\n• 翻訳をより充実\n• アプリ内から更新をインストール\n• ダークのカードとシートは Spent Today と同じ見た目",
        "ko": "새로운 기능 277 (2.235.0)\n• 설정 시작 시 언어를 먼저 선택\n• 언어가 바로 적용됨\n• 설정을 마치지 않아도 언어가 저장됨\n• 영어, 오디아어, 힌디어가 맨 위\n• 번역이 더 완전해졌습니다\n• 앱에서 바로 업데이트 설치\n• 다크 카드와 시트가 Spent Today와 동일",
        "it": "Novità 277 (2.235.0)\n• Scegli la lingua all’inizio della configurazione\n• La lingua si applica subito\n• Resta salvata se non completi la configurazione\n• Inglese, odia e hindi in cima\n• Traduzioni più complete\n• Installa gli aggiornamenti dall’app\n• Schede e sheet scuri come Spent Today",
        "ru": "Что нового 277 (2.235.0)\n• Сначала выберите язык при настройке\n• Язык применяется сразу\n• Сохраняется, если настройка не закончена\n• Английский, одия и хинди в начале списка\n• Более полные переводы\n• Установка обновлений в приложении\n• Тёмные карточки и шиты как у Spent Today"
      }
    """.trimIndent()

    val titlesJson = """
      {
        "en": "What's new",
        "or": "ନୂଆ କଣ",
        "hi": "नया क्या है",
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
