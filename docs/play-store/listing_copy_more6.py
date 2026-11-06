# -*- coding: utf-8 -*-
def _b(s: str) -> str:
    return s.strip() + "\n"


MORE6 = {
    "es": {
        "short": "Escribe, habla, escanea o comparte. SMS, presupuestos. En el dispositivo.",
        "full": _b("""
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
"""),
    },
    "pt-BR": {
        "short": "Digite, fale, escaneie ou compartilhe contas. SMS, orçamentos. No dispositivo.",
        "full": _b("""
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
"""),
    },
    "fr": {
        "short": "Tapez, parlez, scannez ou partagez des factures. SMS, budgets. Sur l’appareil.",
        "full": _b("""
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
"""),
    },
    "de": {
        "short": "Tippen, sprechen, scannen oder Rechnungen teilen. SMS, Budgets. Auf dem Gerät.",
        "full": _b("""
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
"""),
    },
    "id": {
        "short": "Ketik, bicara, pindai, atau bagikan tagihan. SMS, anggaran. Di perangkat.",
        "full": _b("""
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
"""),
    },
    "it": {
        "short": "Scrivi, parla, scansiona o condividi bollette. SMS, budget. Sul dispositivo.",
        "full": _b("""
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
"""),
    },
    "ja": {
        "short": "入力・音声・スキャン・請求書共有。SMS、予算。端末内。",
        "full": _b("""
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
"""),
    },
    "ko": {
        "short": "입력, 말하기, 스캔 또는 청구서 공유. SMS, 예산. 기기에서.",
        "full": _b("""
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
"""),
    },
    "ru": {
        "short": "Пишите, говорите, сканируйте или делитесь счетами. SMS, бюджет. На устройстве.",
        "full": _b("""
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
"""),
    },
}
