package com.example.util

import com.example.data.model.QuranAyahItem

/**
 * Koleksi ayat terverifikasi siap baca Offline (Al-Fātiḥah & Juz 'Amma: Ad-Dhuha s.d. An-Nās)
 * agar anak asuh, Wali Asuh, dan Orang Tua tetap dapat mengaji walau tanpa koneksi internet.
 */
object QuranOfflineData {

    fun getOfflineSurah(surahNumber: Int): List<QuranAyahItem>? = OFFLINE_SURAHS[surahNumber]

    fun hasOfflineSurah(surahNumber: Int): Boolean = OFFLINE_SURAHS.containsKey(surahNumber)

    private val OFFLINE_SURAHS: Map<Int, List<QuranAyahItem>> = mapOf(
        1 to listOf(
            QuranAyahItem(1, "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ", "Bismillāhir-raḥmānir-raḥīm", "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang.", "In the name of Allah, the Entirely Merciful, the Especially Merciful."),
            QuranAyahItem(2, "اَلْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ", "Al-ḥamdu lillāhi rabbil-‘ālamīn", "Segala puji bagi Allah, Tuhan seluruh alam,", "All praise is due to Allah, Lord of the worlds,"),
            QuranAyahItem(3, "الرَّحْمٰنِ الرَّحِيْمِ", "Ar-raḥmānir-raḥīm", "Yang Maha Pengasih, Maha Penyayang,", "The Entirely Merciful, the Especially Merciful,"),
            QuranAyahItem(4, "مٰلِكِ يَوْمِ الدِّيْنِ", "Māliki yaumid-dīn", "Pemilik hari pembalasan.", "Sovereign of the Day of Recompense."),
            QuranAyahItem(5, "اِيَّاكَ نَعْبُدُ وَاِيَّاكَ نَسْتَعِيْنُ", "Iyyāka na‘budu wa iyyāka nasta‘īn", "Hanya kepada Engkaulah kami menyembah dan hanya kepada Engkaulah kami mohon pertolongan.", "It is You we worship and You we ask for help."),
            QuranAyahItem(6, "اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ", "Ihdinaṣ-ṣirāṭal-mustaqīm", "Tunjukilah kami jalan yang lurus,", "Guide us to the straight path,"),
            QuranAyahItem(7, "صِرَاطَ الَّذِيْنَ اَنْعَمْتَ عَلَيْهِمْ ەۙ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّاۤلِّيْنَ", "Ṣirāṭallażīna an‘amta ‘alaihim gairil-magḍūbi ‘alaihim wa laḍ-ḍāllīn", "(yaitu) jalan orang-orang yang telah Engkau beri nikmat kepadanya; bukan (jalan) mereka yang dimurkai, dan bukan (pula jalan) mereka yang sesat.", "The path of those upon whom You have bestowed favor, not of those who have evoked anger or of those who are astray.")
        ),
        93 to listOf(
            QuranAyahItem(1, "وَالضُّحٰىۙ", "Waḍ-ḍuḥā", "Demi waktu dhuha (ketika matahari naik sepenggalah),", "By the morning brightness,"),
            QuranAyahItem(2, "وَالَّيْلِ اِذَا سَجٰىۙ", "Wal-laili iżā sajā", "dan demi malam apabila telah sunyi,", "And by the night when it covers with darkness,"),
            QuranAyahItem(3, "مَا وَدَّعَكَ رَبُّكَ وَمَا قَلٰىۗ", "Mā wadda‘aka rabbuka wa mā qalā", "Tuhanmu tidak meninggalkan engkau (Muhammad) dan tidak (pula) membencimu,", "Your Lord has not taken leave of you, nor has He detested you."),
            QuranAyahItem(4, "وَلَلْاٰخِرَةُ خَيْرٌ لَّكَ مِنَ الْاُوْلٰىۗ", "Wa lal-ākhiratu khairul laka minal-ūlā", "dan sungguh, yang kemudian itu lebih baik bagimu daripada yang permulaan.", "And the Hereafter is better for you than the first life."),
            QuranAyahItem(5, "وَلَسَوْفَ يُعْطِيْكَ رَبُّكَ فَتَرْضٰىۗ", "Wa lasaufa yu‘ṭīka rabbuka fatarḍā", "Dan sungguh, kelak Tuhanmu pasti memberikan karunia-Nya kepadamu, sehingga engkau menjadi puas.", "And your Lord is going to give you, and you will be satisfied."),
            QuranAyahItem(6, "اَلَمْ يَجِدْكَ يَتِيْمًا فَاٰوٰىۖ", "Alam yajidka yatīman fa’āwā", "Bukankah Dia mendapatimu sebagai seorang yatim, lalu Dia melindungi(mu),", "Did He not find you an orphan and give you refuge?"),
            QuranAyahItem(7, "وَوَجَدَكَ ضَاۤلًّا فَهَدٰىۖ", "Wa wajadaka ḍāllan fahadā", "dan Dia mendapatimu sebagai seorang yang bingung, lalu Dia memberikan petunjuk,", "And He found you lost and guided you,"),
            QuranAyahItem(8, "وَوَجَدَكَ عَاۤىِٕلًا فَاَغْنٰىۗ", "Wa wajadaka ‘ā’ilan fa’agnā", "dan Dia mendapatimu sebagai seorang yang kekurangan, lalu Dia memberikan kecukupan.", "And He found you poor and made you self-sufficient."),
            QuranAyahItem(9, "فَاَمَّا الْيَتِيْمَ فَلَا تَقْهَرْۗ", "Fa’ammal-yatīma falā taqhar", "Maka terhadap anak yatim janganlah engkau berlaku sewenang-wenang.", "So as for the orphan, do not oppress him."),
            QuranAyahItem(10, "وَاَمَّا السَّاۤىِٕلَ فَلَا تَنْهَرْ", "Wa ammas-sā’ila falā tanhar", "Dan terhadap orang yang meminta-minta janganlah engkau menghardik(nya).", "And as for the petitioner, do not repel him."),
            QuranAyahItem(11, "وَاَمَّا بِنِعْمَةِ رَبِّكَ فَحَدِّثْ", "Wa ammā bini‘mati rabbika faḥaddiṡ", "Dan terhadap nikmat Tuhanmu hendaklah engkau nyatakan (dengan bersyukur).", "But as for the favor of your Lord, report it.")
        ),
        94 to listOf(
            QuranAyahItem(1, "اَلَمْ نَشْرَحْ لَكَ صَدْرَكَۙ", "Alam nasyraḥ laka ṣadrak", "Bukankah Kami telah melapangkan dadamu (Muhammad)?", "Did We not expand for you, your breast?"),
            QuranAyahItem(2, "وَوَضَعْنَا عَنْكَ وِزْرَكَۙ", "Wa waḍa‘nā ‘anka wizrak", "dan Kami pun telah menurunkan bebanmu darimu,", "And We removed from you your burden,"),
            QuranAyahItem(3, "الَّذِيْٓ اَنْقَضَ ظَهْرَكَۙ", "Allażī anqaḍa ẓahrak", "yang memberatkan punggungmu,", "Which had weighed upon your back,"),
            QuranAyahItem(4, "وَرَفَعْنَا لَكَ ذِكْرَكَۗ", "Wa rafa‘nā laka żikrak", "dan Kami tinggikan sebutan (nama)mu bagimu.", "And raised high for you your repute."),
            QuranAyahItem(5, "فَاِنَّ مَعَ الْعُسْرِ يُسْرًاۙ", "Fa’inna ma‘al-‘usri yusrā", "Maka sesungguhnya bersama kesulitan ada kemudahan,", "For indeed, with hardship will be ease."),
            QuranAyahItem(6, "اِنَّ مَعَ الْعُسْرِ يُسْرًاۗ", "Inna ma‘al-‘usri yusrā", "sesungguhnya bersama kesulitan ada kemudahan.", "Indeed, with hardship will be ease."),
            QuranAyahItem(7, "فَاِذَا فَرَغْتَ فَانْصَبْۙ", "Fa’iżā faragta fanṣab", "Maka apabila engkau telah selesai (dari sesuatu urusan), tetaplah bekerja keras (untuk urusan yang lain),", "So when you have finished your duties, then stand up for worship,"),
            QuranAyahItem(8, "وَاِلٰى رَبِّكَ فَارْغَبْ", "Wa ilā rabbika fargab", "dan hanya kepada Tuhanmulah engkau berharap.", "And to your Lord direct your longing.")
        ),
        95 to listOf(
            QuranAyahItem(1, "وَالتِّيْنِ وَالزَّيْتُوْنِۙ", "Wat-tīni waz-zaitūn", "Demi (buah) Tin dan (buah) Zaitun,", "By the fig and the olive,"),
            QuranAyahItem(2, "وَطُوْرِ سِيْنِيْنَۙ", "Wa ṭūri sīnīn", "demi gunung Sinai,", "And by Mount Sinai,"),
            QuranAyahItem(3, "وَهٰذَا الْبَلَدِ الْاَمِيْنِۙ", "Wa hāżal-baladil-amīn", "dan demi negeri (Mekah) yang aman ini.", "And by this secure city (Makkah),"),
            QuranAyahItem(4, "لَقَدْ خَلَقْنَا الْاِنْسَانَ فِيْٓ اَحْسَنِ تَقْوِيْمٍۖ", "Laqad khalaqnal-insāna fī aḥsani taqwīm", "Sungguh, Kami telah menciptakan manusia dalam bentuk yang sebaik-baiknya,", "We have certainly created man in the best of stature;"),
            QuranAyahItem(5, "ثُمَّ رَدَدْنٰهُ اَسْفَلَ سٰفِلِيْنَۙ", "Ṡumma radadnāhu asfala sāfilīn", "kemudian Kami kembalikan dia ke tempat yang serendah-rendahnya,", "Then We return him to the lowest of the low,"),
            QuranAyahItem(6, "اِلَّا الَّذِيْنَ اٰمَنُوْا وَعَمِلُوا الصّٰلِحٰتِ فَلَهُمْ اَجْرٌ غَيْرُ مَمْنُوْنٍۗ", "Illallażīna āmanū wa ‘amiluṣ-ṣāliḥāti falahum ajrun gairu mamnūn", "kecuali orang-orang yang beriman dan mengerjakan kebajikan; maka mereka akan mendapat pahala yang tidak ada putus-putusnya.", "Except for those who believe and do righteous deeds, for they will have a reward uninterrupted."),
            QuranAyahItem(7, "فَمَا يُكَذِّبُكَ بَعْدُ بِالدِّيْنِۗ", "Famā yukażżibuka ba‘du bid-dīn", "Maka apa yang menyebabkan (mereka) mendustakanmu (tentang) hari pembalasan setelah (adanya keterangan-keterangan) itu?", "So what yet causes you to deny the Recompense?"),
            QuranAyahItem(8, "اَلَيْسَ اللّٰهُ بِاَحْكَمِ الْحٰكِمِيْنَ", "Alaisallāhu bi’aḥkamil-ḥākimīn", "Bukankah Allah hakim yang paling adil?", "Is not Allah the most just of judges?")
        ),
        96 to listOf(
            QuranAyahItem(1, "اِقْرَأْ بِاسْمِ رَبِّكَ الَّذِيْ خَلَقَۚ", "Iqra’ bismi rabbikallażī khalaq", "Bacalah dengan (menyebut) nama Tuhanmu yang menciptakan,", "Recite in the name of your Lord who created,"),
            QuranAyahItem(2, "خَلَقَ الْاِنْسَانَ مِنْ عَلَقٍۚ", "Khalaqal-insāna min ‘alaq", "Dia telah menciptakan manusia dari segumpal darah.", "Created man from a clinging substance."),
            QuranAyahItem(3, "اِقْرَأْ وَرَبُّكَ الْاَكْرَمُۙ", "Iqra’ wa rabbukal-akram", "Bacalah, dan Tuhanmulah Yang Mahamulia,", "Recite, and your Lord is the most Generous,"),
            QuranAyahItem(4, "الَّذِيْ عَلَّمَ بِالْقَلَمِۙ", "Allażī ‘allama bil-qalam", "Yang mengajar (manusia) dengan pena.", "Who taught by the pen,"),
            QuranAyahItem(5, "عَلَّمَ الْاِنْسَانَ مَا لَمْ يَعْلَمْۗ", "‘Allamal-insāna mā lam ya‘lam", "Dia mengajarkan manusia apa yang tidak diketahuinya.", "Taught man that which he knew not."),
            QuranAyahItem(6, "كَلَّآ اِنَّ الْاِنْسَانَ لَيَطْغٰىٓۙ", "Kallā innal-insāna layaṭgā", "Sekali-kali tidak! Sungguh, manusia itu benar-benar melampaui batas,", "No! Indeed, man transgresses,"),
            QuranAyahItem(7, "اَنْ رَّاٰهُ اسْتَغْنٰىۗ", "Ar-ra’āhustagnā", "apabila melihat dirinya serba cukup.", "Because he sees himself self-sufficient."),
            QuranAyahItem(8, "اِنَّ اِلٰى رَبِّكَ الرُّجْعٰىۗ", "Inna ilā rabbikar-ruj‘ā", "Sungguh, hanya kepada Tuhanmulah tempat kembali(mu).", "Indeed, to your Lord is the return."),
            QuranAyahItem(9, "اَرَاَيْتَ الَّذِيْ يَنْهٰىۙ", "Ara’aitallażī yanhā", "Bagaimana pendapatmu tentang orang yang melarang", "Have you seen the one who forbids"),
            QuranAyahItem(10, "عَبْدًا اِذَا صَلّٰىۗ", "‘Abdan iżā ṣallā", "seorang hamba ketika dia melaksanakan sholat,", "A servant when he prays?"),
            QuranAyahItem(11, "اَرَاَيْتَ اِنْ كَانَ عَلَى الْهُدٰىٓۙ", "Ara’aita in kāna ‘alal-hudā", "bagaimana pendapatmu jika dia (yang dilarang sholat itu) berada di atas kebenaran (petunjuk),", "Have you seen if he is upon guidance"),
            QuranAyahItem(12, "اَوْ اَمَرَ بِالتَّقْوٰىۗ", "Au amara bit-taqwā", "atau dia menyuruh bertakwa (kepada Allah)?", "Or enjoins righteousness?"),
            QuranAyahItem(13, "اَرَاَيْتَ اِنْ كَذَّبَ وَتَوَلّٰىۗ", "Ara’aita in każżaba wa tawallā", "Bagaimana pendapatmu jika dia (yang melarang) itu mendustakan dan berpaling?", "Have you seen if he denies and turns away?"),
            QuranAyahItem(14, "اَلَمْ يَعْلَمْ بِاَنَّ اللّٰهَ يَرٰىۗ", "Alam ya‘lam bi’annallāha yarā", "Tidakkah dia mengetahui bahwa sesungguhnya Allah melihat (segala perbuatannya)?", "Does he not know that Allah sees?"),
            QuranAyahItem(15, "كَلَّا لَىِٕنْ لَّمْ يَنْتَهِ ەۙ لَنَسْفَعًاۢ بِالنَّاصِيَةِۙ", "Kallā la’il-lam yantahi lanasfa‘am bin-nāṣiyah", "Sekali-kali tidak! Sungguh, jika dia tidak berhenti (berbuat demikian) niscaya Kami tarik ubun-ubunnya,", "No! If he does not desist, We will surely drag him by the forelock,"),
            QuranAyahItem(16, "نَاصِيَةٍ كَاذِبَةٍ خَاطِئَةٍۚ", "Nāṣiyatin kāżibatin khāṭi’ah", "(yaitu) ubun-ubun orang yang mendustakan dan durhaka.", "A lying, sinning forelock."),
            QuranAyahItem(17, "فَلْيَدْعُ نَادِيَهٗۙ", "Falyad‘u nādiyah", "Maka biarlah dia memanggil golongannya (untuk menolongnya),", "Then let him call his associates;"),
            QuranAyahItem(18, "سَنَدْعُ الزَّبَانِيَةَۙ", "Sanad‘uz-zabāniyah", "kelak Kami akan memanggil Malaikat Zabaniyah,", "We will call the angels of Hell."),
            QuranAyahItem(19, "كَلَّاۗ لَا تُطِعْهُ وَاسْجُدْ وَاقْتَرِبْ", "Kallā lā tuṭi‘hu wasjud waqtarib", "Sekali-kali tidak! Janganlah kamu patuh kepadanya; dan sujudlah serta dekatkanlah (dirimu kepada Allah).", "No! Do not obey him. But prostrate and draw near to Allah.")
        ),
        97 to listOf(
            QuranAyahItem(1, "اِنَّآ اَنْزَلْنٰهُ فِيْ لَيْلَةِ الْقَدْرِ", "Innā anzalnāhu fī lailatil-qadr", "Sesungguhnya Kami telah menurunkannya (Al-Qur'an) pada malam kemuliaan (Lailatulqadar).", "Indeed, We sent it down during the Night of Decree."),
            QuranAyahItem(2, "وَمَآ اَدْرٰىكَ مَا لَيْلَةُ الْقَدْرِۗ", "Wa mā adrāka mā lailatul-qadr", "Dan tahukah kamu apakah malam kemuliaan itu?", "And what can make you know what is the Night of Decree?"),
            QuranAyahItem(3, "لَيْلَةُ الْقَدْرِ ەۙ خَيْرٌ مِّنْ اَلْفِ شَهْرٍۗ", "Lailatul-qadri khairum min alfi syahr", "Malam kemuliaan itu lebih baik daripada seribu bulan.", "The Night of Decree is better than a thousand months."),
            QuranAyahItem(4, "تَنَزَّلُ الْمَلٰۤىِٕكَةُ وَالرُّوْحُ فِيْهَا بِاِذْنِ رَبِّهِمْۚ مِنْ كُلِّ اَمْرٍۛ", "Tanazzalul-malā’ikatu war-rūḥu fīhā bi’iżni rabbihim min kulli amr", "Pada malam itu turun para malaikat dan Ruh (Jibril) dengan izin Tuhannya untuk mengatur semua urusan.", "The angels and the Spirit descend therein by permission of their Lord for every matter."),
            QuranAyahItem(5, "سَلٰمٌ ۛهِيَ حَتّٰى مَطْلَعِ الْفَجْرِ", "Salāmun hiya ḥattā maṭla‘il-fajr", "Sejahteralah (malam itu) sampai terbit fajar.", "Peace it is until the emergence of dawn.")
        ),
        98 to listOf(
            QuranAyahItem(1, "لَمْ يَكُنِ الَّذِيْنَ كَفَرُوْا مِنْ اَهْلِ الْكِتٰبِ وَالْمُشْرِكِيْنَ مُنْفَكِّيْنَ حَتّٰى تَأْتِيَهُمُ الْبَيِّنَةُۙ", "Lam yakunillażīna kafarū min ahlil-kitābi wal-musyrikīna munfakkīna ḥattā ta’tiyahumul-bayyinah", "Orang-orang yang kafir dari golongan Ahli Kitab dan orang-orang musyrik tidak akan meninggalkan (agama mereka) sampai datang kepada mereka bukti yang nyata,", "Those who disbelieved among the People of the Scripture and the polytheists were not to be parted until there came to them clear evidence:"),
            QuranAyahItem(2, "رَسُوْلٌ مِّنَ اللّٰهِ يَتْلُوْا صُحُفًا مُّطَهَّرَةًۙ", "Rasūlum minallāhi yatlū ṣuḥufam muṭahharah", "(yaitu) seorang Rasul dari Allah (Muhammad) yang membacakan lembaran-lembaran yang suci (Al-Qur'an),", "A Messenger from Allah, reciting purified scriptures"),
            QuranAyahItem(3, "فِيْهَا كُتُبٌ قَيِّمَةٌۗ", "Fīhā kutubun qayyimah", "di dalamnya terdapat (isi) kitab-kitab yang lurus (benar).", "Within which are correct writings."),
            QuranAyahItem(4, "وَمَا تَفَرَّقَ الَّذِيْنَ اُوْتُوا الْكِتٰبَ اِلَّا مِنْۢ بَعْدِ مَا جَاۤءَتْهُمُ الْبَيِّنَةُۗ", "Wa mā tafarraqallażīna ūtul-kitāba illā mim ba‘di mā jā’athumul-bayyinah", "Dan tidaklah terpecah-belah orang-orang Ahli Kitab melainkan setelah datang kepada mereka bukti yang nyata.", "Nor did those who were given the Scripture become divided until after there had come to them clear evidence."),
            QuranAyahItem(5, "وَمَآ اُمِرُوْٓا اِلَّا لِيَعْبُدُوا اللّٰهَ مُخْلِصِيْنَ لَهُ الدِّيْنَ ەۙ حُنَفَاۤءَ وَيُقِيْمُوا الصَّلٰوةَ وَيُؤْتُوا الزَّكٰوةَ وَذٰلِكَ دِيْنُ الْقَيِّمَةِۗ", "Wa mā umirū illā liya‘budullāha mukhliṣīna lahud-dīna ḥunafā’a wa yuqīmuṣ-ṣalāta wa yu’tuz-zakāta wa żālika dīnul-qayyimah", "Padahal mereka hanya diperintah menyembah Allah dengan ikhlas menaati-Nya semata-mata karena (menjalankan) agama, dan juga agar melaksanakan sholat dan menunaikan zakat; dan yang demikian itulah agama yang lurus (benar).", "And they were not commanded except to worship Allah, sincere to Him in religion, inclining to truth, and to establish prayer and to give zakah. And that is the correct religion."),
            QuranAyahItem(6, "اِنَّ الَّذِيْنَ كَفَرُوْا مِنْ اَهْلِ الْكِتٰبِ وَالْمُشْرِكِيْنَ فِيْ نَارِ جَهَنَّمَ خٰلِدِيْنَ فِيْهَاۗ اُولٰۤىِٕكَ هُمْ شَرُّ الْبَرِيَّةِۗ", "Innallażīna kafarū min ahlil-kitābi wal-musyrikīna fī nāri jahannama khālidīna fīhā, ulā’ika hum syarrul-bariyyah", "Sungguh, orang-orang yang kafir dari golongan Ahli Kitab dan orang-orang musyrik (akan masuk) ke neraka Jahanam; mereka kekal di dalamnya selama-lamanya. Mereka itu adalah sejahat-jahat makhluk.", "Indeed, they who disbelieved among the People of the Scripture and the polytheists will be in the fire of Hell, abiding eternally therein. Those are the worst of creatures."),
            QuranAyahItem(7, "اِنَّ الَّذِيْنَ اٰمَنُوْا وَعَمِلُوا الصّٰلِحٰتِ اُولٰۤىِٕكَ هُمْ خَيْرُ الْبَرِيَّةِۗ", "Innallażīna āmanū wa ‘amiluṣ-ṣāliḥāti ulā’ika hum khairul-bariyyah", "Sungguh, orang-orang yang beriman dan mengerjakan kebajikan, mereka itu adalah sebaik-baik makhluk.", "Indeed, they who have believed and done righteous deeds, those are the best of creatures."),
            QuranAyahItem(8, "جَزَاۤؤُهُمْ عِنْدَ رَبِّهِمْ جَنّٰتُ عَدْنٍ تَجْرِيْ مِنْ تَحْتِهَا الْاَنْهٰرُ خٰلِدِيْنَ فِيْهَآ اَبَدًاۗ رَضِيَ اللّٰهُ عَنْهُمْ وَرَضُوْا عَنْهُۗ ذٰلِكَ لِمَنْ خَشِيَ رَبَّهٗ", "Jazā’uhum ‘inda rabbihim jannātu ‘adnin tajrī min taḥtihal-anhāru khālidīna fīhā abadā, raḍiyallāhu ‘anhum wa raḍū ‘anh, żālika liman khasyiya rabbah", "Balasan mereka di sisi Tuhan mereka ialah surga 'Adn yang mengalir di bawahnya sungai-sungai; mereka kekal di dalamnya selama-lamanya. Allah rida terhadap mereka dan mereka pun rida kepada-Nya. Yang demikian itu adalah (balasan) bagi orang yang takut kepada Tuhannya.", "Their reward with Allah will be gardens of perpetual residence beneath which rivers flow, wherein they will abide forever, Allah being pleased with them and they with Him. That is for whoever has feared his Lord.")
        ),
        99 to listOf(
            QuranAyahItem(1, "اِذَا زُلْزِلَتِ الْاَرْضُ زِلْزَالَهَاۙ", "Iżā zulzilatil-arḍu zilzālahā", "Apabila bumi diguncangkan dengan guncangan yang dahsyat,", "When the earth is shaken with its final earthquake,"),
            QuranAyahItem(2, "وَاَخْرَجَتِ الْاَرْضُ اَثْقَالَهَاۙ", "Wa akhrajatil-arḍu aṡqālahā", "dan bumi telah mengeluarkan beban-beban berat (yang dikandung)nya,", "And the earth discharges its burdens,"),
            QuranAyahItem(3, "وَقَالَ الْاِنْسَانُ مَا لَهَاۚ", "Wa qālal-insānu mā lahā", "dan manusia bertanya, \"Apa yang terjadi pada bumi ini?\"", "And man says, \"What is wrong with it?\""),
            QuranAyahItem(4, "يَوْمَىِٕذٍ تُحَدِّثُ اَخْبَارَهَاۙ", "Yauma’iżin tuḥaddiṡu akhbārahā", "Pada hari itu bumi menyampaikan beritanya,", "That Day, it will report its news,"),
            QuranAyahItem(5, "بِاَنَّ رَبَّكَ اَوْحٰى لَهَاۗ", "Bi’anna rabbaka auḥā lahā", "karena sesungguhnya Tuhanmu telah memerintahkan (yang sedemikian itu) padanya.", "Because your Lord has inspired it."),
            QuranAyahItem(6, "يَوْمَىِٕذٍ يَّصْدُرُ النَّاسُ اَشْتَاتًا ەۙ لِّيُرَوْا اَعْمَالَهُمْۗ", "Yauma’iżiy yaṣdurun-nāsu asytātal liyurau a‘mālahum", "Pada hari itu manusia keluar dari kuburnya dalam keadaan berkelompok-kelompok, untuk diperlihatkan kepada mereka (balasan) semua perbuatannya.", "That Day, the people will depart separated into categories to be shown their deeds."),
            QuranAyahItem(7, "فَمَنْ يَّعْمَلْ مِثْقَالَ ذَرَّةٍ خَيْرًا يَّرَهٗۚ", "Famay ya‘mal miṡqāla żarratin khairay yarah", "Maka barangsiapa mengerjakan kebaikan seberat zarrah, niscaya dia akan melihat (balasan)nya,", "So whoever does an atom's weight of good will see it,"),
            QuranAyahItem(8, "وَمَنْ يَّعْمَلْ مِثْقَالَ ذَرَّةٍ شَرًّا يَّرَهٗ", "Wa may ya‘mal miṡqāla żarratin syarray yarah", "dan barangsiapa mengerjakan kejahatan seberat zarrah, niscaya dia akan melihat (balasan)nya.", "And whoever does an atom's weight of evil will see it.")
        ),
        100 to listOf(
            QuranAyahItem(1, "وَالْعٰدِيٰتِ ضَبْحًاۙ", "Wal-‘ādiyāti ḍabḥā", "Demi kuda perang yang berlari kencang terengah-engah,", "By the racers, panting,"),
            QuranAyahItem(2, "فَالْمُوْرِيٰتِ قَدْحًاۙ", "Fal-mūriyāti qadḥā", "dan kuda yang memercikkan bunga api (dengan pukulan kuku kakinya),", "And the producers of sparks when striking,"),
            QuranAyahItem(3, "فَالْمُغِيْرٰتِ صُبْحًاۙ", "Fal-mugīrāti ṣubḥā", "dan kuda yang menyerang (dengan tiba-tiba) pada waktu pagi,", "And the chargers at dawn,"),
            QuranAyahItem(4, "فَاَثَرْنَ بِهٖ نَقْعًاۙ", "Fa’aṡarna bihī naq‘ā", "sehingga menerbangkan debu,", "Stirring up thereby clouds of dust,"),
            QuranAyahItem(5, "فَوَسَطْنَ بِهٖ جَمْعًاۙ", "Fawasaṭna bihī jam‘ā", "lalu menyerbu ke tengah-tengah kumpulan musuh,", "Arriving thereby in the center collectively,"),
            QuranAyahItem(6, "اِنَّ الْاِنْسَانَ لِرَبِّهٖ لَكَنُوْدٌۚ", "Innal-insāna lirabbihī lakanūd", "sungguh, manusia itu sangat ingkar, (tidak bersyukur) kepada Tuhannya,", "Indeed mankind, to his Lord, is ungrateful."),
            QuranAyahItem(7, "وَاِنَّهٗ عَلٰى ذٰلِكَ لَشَهِيْدٌۚ", "Wa innahū ‘alā żālika lasyahīd", "dan sesungguhnya dia (manusia) menyaksikan (mengakui) keingkarannya,", "And indeed, he is to that a witness."),
            QuranAyahItem(8, "وَاِنَّهٗ لِحُبِّ الْخَيْرِ لَشَدِيْدٌۗ", "Wa innahū liḥubbil-khairi lasyadīd", "dan sesungguhnya cintanya kepada harta benar-benar berlebihan.", "And indeed he is, in love of wealth, intense."),
            QuranAyahItem(9, "اَفَلَا يَعْلَمُ اِذَا بُعْثِرَ مَا فِى الْقُبُوْرِۙ", "Afalā ya‘lamu iżā bu‘ṡira mā fil-qubūr", "Maka tidakkah dia mengetahui apabila apa yang di dalam kubur dikeluarkan,", "But does he not know that when the contents of the graves are scattered,"),
            QuranAyahItem(10, "وَحُصِّلَ مَا فِى الصُّدُوْرِۙ", "Wa ḥuṣṣila mā fiṣ-ṣudūr", "dan apa yang tersimpan di dalam dada dilahirkan?", "And that within the breasts is obtained,"),
            QuranAyahItem(11, "اِنَّ رَبَّهُمْ بِهِمْ يَوْمَىِٕذٍ لَّخَبِيْرٌ", "Inna rabbahum bihim yauma’iżil lakhabīr", "sungguh, Tuhan mereka pada hari itu Mahateliti terhadap keadaan mereka.", "Indeed, their Lord with them, that Day, is fully Acquainted.")
        ),
        101 to listOf(
            QuranAyahItem(1, "اَلْقَارِعَةُۙ", "Al-qāri‘ah", "Hari Kiamat,", "The Striking Calamity,"),
            QuranAyahItem(2, "مَا الْقَارِعَةُۚ", "Mal-qāri‘ah", "apakah hari Kiamat itu?", "What is the Striking Calamity?"),
            QuranAyahItem(3, "وَمَآ اَدْرٰىكَ مَا الْقَارِعَةُۗ", "Wa mā adrāka mal-qāri‘ah", "Dan tahukah kamu apakah hari Kiamat itu?", "And what can make you know what is the Striking Calamity?"),
            QuranAyahItem(4, "يَوْمَ يَكُوْنُ النَّاسُ كَالْفَرَاشِ الْمَبْثُوْثِۙ", "Yauma yakūnun-nāsu kal-farāsyil-mabṡūṡ", "Pada hari itu manusia seperti laron yang beterbangan,", "It is the Day when people will be like moths, dispersed,"),
            QuranAyahItem(5, "وَتَكُوْنُ الْجِبَالُ كَالْعِهْنِ الْمَنْفُوْشِۗ", "Wa takūnul-jibālu kal-‘ihnil-manfūsy", "dan gunung-gunung seperti bulu yang dihambur-hamburkan.", "And the mountains will be like wool, fluffed up."),
            QuranAyahItem(6, "فَاَمَّا مَنْ ثَقُلَتْ مَوَازِينُهٗۙ", "Fa’ammā man ṡaqulat mawāzīnuh", "Maka adapun orang yang berat timbangan (kebaikan)nya,", "Then as for one whose scales are heavy with good deeds,"),
            QuranAyahItem(7, "فَهُوَ فِيْ عِيْشَةٍ رَّاضِيَةٍۗ", "Fahuwa fī ‘īsyatir rāḍiyah", "maka dia berada dalam kehidupan yang memuaskan (senang).", "He will be in a pleasant life."),
            QuranAyahItem(8, "وَاَمَّا مَنْ خَفَّتْ مَوَازِيْنُهٗۙ", "Wa ammā man khaffat mawāzīnuh", "Dan adapun orang yang ringan timbangan (kebaikan)nya,", "But as for one whose scales are light,"),
            QuranAyahItem(9, "فَاُمُّهٗ هَاوِيَةٌۗ", "Fa’ummuhū hāwiyah", "maka tempat kembalinya adalah neraka Hawiyah.", "His refuge will be an abyss."),
            QuranAyahItem(10, "وَمَآ اَدْرٰىكَ مَا هِيَهْۗ", "Wa mā adrāka mā hiyah", "Dan tahukah kamu apakah neraka Hawiyah itu?", "And what can make you know what that is?"),
            QuranAyahItem(11, "نَارٌ حَامِيَةٌ", "Nārun ḥāmiyah", "(Yaitu) api yang sangat panas.", "It is a Fire, intensely hot.")
        ),
        102 to listOf(
            QuranAyahItem(1, "اَلْهٰىكُمُ التَّكَاثُرُۙ", "Alhākumut-takāṡur", "Bermegah-megahan telah melalaikan kamu,", "Competition in worldly increase diverts you,"),
            QuranAyahItem(2, "حَتّٰى زُرْتُمُ الْمَقَابِرَۗ", "Ḥattā zurtumul-maqābir", "sampai kamu masuk ke dalam kubur.", "Until you visit the graveyards."),
            QuranAyahItem(3, "كَلَّا سَوْفَ تَعْلَمُوْنَۙ", "Kallā saufa ta‘lamūn", "Sekali-kali tidak! Kelak kamu akan mengetahui (akibat perbuatanmu itu),", "No! You are going to know."),
            QuranAyahItem(4, "ثُمَّ كَلَّا سَوْفَ تَعْلَمُوْنَ", "Ṡumma kallā saufa ta‘lamūn", "kemudian sekali-kali tidak! Kelak kamu akan mengetahui.", "Then no! You are going to know."),
            QuranAyahItem(5, "كَلَّا لَوْ تَعْلَمُوْنَ عِلْمَ الْيَقِيْنِۗ", "Kallā lau ta‘lamūna ‘ilmal-yaqīn", "Sekali-kali tidak! Sekiranya kamu mengetahui dengan pasti,", "No! If you only knew with knowledge of certainty..."),
            QuranAyahItem(6, "لَتَرَوُنَّ الْجَحِيْمَۙ", "Latarawunnal-jaḥīm", "niscaya kamu benar-benar akan melihat neraka Jahim,", "You will surely see the Hellfire."),
            QuranAyahItem(7, "ثُمَّ لَتَرَوُنَّهَا عَيْنَ الْيَقِيْنِۙ", "Ṡumma latarawunnahā ‘ainal-yaqīn", "kemudian kamu benar-benar akan melihatnya dengan mata kepala sendiri,", "Then you will surely see it with the eye of certainty."),
            QuranAyahItem(8, "ثُمَّ لَتُسْـَٔلُنَّ يَوْمَىِٕذٍ عَنِ النَّعِيْمِ", "Ṡumma latus’alunna yauma’iżin ‘anin-na‘īm", "kemudian kamu benar-benar akan ditanya pada hari itu tentang kenikmatan (yang megah di dunia itu).", "Then you will surely be asked that Day about pleasure.")
        ),
        103 to listOf(
            QuranAyahItem(1, "وَالْعَصْرِۙ", "Wal-‘aṣr", "Demi masa,", "By time,"),
            QuranAyahItem(2, "اِنَّ الْاِنْسَانَ لَفِيْ خُسْرٍۙ", "Innal-insāna lafī khusr", "sungguh, manusia berada dalam kerugian,", "Indeed, mankind is in loss,"),
            QuranAyahItem(3, "اِلَّا الَّذِيْنَ اٰمَنُوْا وَعَمِلُوا الصّٰلِحٰتِ وَتَوَاصَوْا بِالْحَقِّ ەۙ وَتَوَاصَوْا بِالصَّبْرِ", "Illallażīna āmanū wa ‘amiluṣ-ṣāliḥāti wa tawāṣau bil-ḥaqqi wa tawāṣau biṣ-ṣabr", "kecuali orang-orang yang beriman dan mengerjakan kebajikan serta saling menasihati untuk kebenaran dan saling menasihati untuk kesabaran.", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.")
        ),
        104 to listOf(
            QuranAyahItem(1, "وَيْلٌ لِّكُلِّ هُمَزَةٍ لُّمَزَةٍۙ", "Wailul likulli humazatil-lumazah", "Celakalah bagi setiap pengumpat dan pencela,", "Woe to every scorner and mocker,"),
            QuranAyahItem(2, "الَّذِيْ جَمَعَ مَالًا وَّعَدَّدَهٗۙ", "Allażī jama‘a mālaw wa ‘addadah", "yang mengumpulkan harta dan menghitung-hitungnya,", "Who collects wealth and continuously counts it."),
            QuranAyahItem(3, "يَحْسَبُ اَنَّ مَالَهٗٓ اَخْلَدَهٗۚ", "Yaḥsabu anna mālahū akhladah", "dia (manusia) mengira bahwa hartanya itu dapat mengekalkannya.", "He thinks that his wealth will make him immortal."),
            QuranAyahItem(4, "كَلَّا لَيُنْۢبَذَنَّ فِى الْحُطَمَةِۖ", "Kallā layumbażanna fil-ḥuṭamah", "Sekali-kali tidak! Pasti dia akan dilemparkan ke dalam (neraka) Hutamah.", "No! He will surely be thrown into the Crusher."),
            QuranAyahItem(5, "وَمَآ اَدْرٰىكَ مَا الْحُطَمَةُۗ", "Wa mā adrāka mal-ḥuṭamah", "Dan tahukah kamu apakah (neraka) Hutamah itu?", "And what can make you know what is the Crusher?"),
            QuranAyahItem(6, "نَارُ اللّٰهِ الْمُوْقَدَةُۙ", "Nārullāhil-mūqadah", "(Yaitu) api (azab) Allah yang dinyalakan,", "It is the fire of Allah, eternally fueled,"),
            QuranAyahItem(7, "الَّتِيْ تَطَّلِعُ عَلَى الْاَفْـِٕدَةِۗ", "Allatī taṭṭali‘u ‘alal-af’idah", "yang (membakar) sampai ke hati.", "Which mounts directed at the hearts."),
            QuranAyahItem(8, "اِنَّهَا عَلَيْهِمْ مُّؤْصَدَةٌۙ", "Innahā ‘alaihim mu’ṣadah", "Sungguh, api itu ditutup rapat atas (diri) mereka,", "Indeed, Hellfire will be closed down upon them,"),
            QuranAyahItem(9, "فِيْ عَمَدٍ مُّمَدَّدَةٍ", "Fī ‘amadim mumaddadah", "(sedang mereka itu) diikat pada tiang-tiang yang panjang.", "In extended columns.")
        ),
        105 to listOf(
            QuranAyahItem(1, "اَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِاَصْحٰبِ الْفِيْلِۗ", "Alam tara kaifa fa‘ala rabbuka bi’aṣḥābil-fīl", "Tidakkah engkau (Muhammad) perhatikan bagaimana Tuhanmu telah bertindak terhadap pasukan bergajah?", "Have you not considered how your Lord dealt with the companions of the elephant?"),
            QuranAyahItem(2, "اَلَمْ يَجْعَلْ كَيْدَهُمْ فِيْ تَضْلِيْلٍۙ", "Alam yaj‘al kaidahum fī taḍlīl", "Bukankah Dia telah menjadikan tipu daya mereka itu sia-sia?", "Did He not make their plan into misguidance?"),
            QuranAyahItem(3, "وَّاَرْسَلَ عَلَيْهِمْ طَيْرًا اَبَابِيْلَۙ", "Wa arsala ‘alaihim ṭairan abābīl", "dan Dia mengirimkan kepada mereka burung yang berbondong-bondong,", "And He sent against them birds in flocks,"),
            QuranAyahItem(4, "تَرْمِيْهِمْ بِحِجَارَةٍ مِّنْ سِجِّيْلٍۙ", "Tarmīhim biḥijāratim min sijjīl", "yang melempari mereka dengan batu dari tanah liat yang dibakar,", "Striking them with stones of hard clay,"),
            QuranAyahItem(5, "فَجَعَلَهُمْ كَعَصْفٍ مَّأْكُوْلٍ", "Faja‘alahum ka‘aṣfim ma’kūl", "sehingga mereka dijadikan-Nya seperti daun-daun yang dimakan (ulat).", "And He made them like eaten straw.")
        ),
        106 to listOf(
            QuranAyahItem(1, "لِاِيْلٰفِ قُرَيْشٍۙ", "Li’īlāfi quraisy", "Karena kebiasaan orang-orang Quraisy,", "For the accustomed security of the Quraysh,"),
            QuranAyahItem(2, "اٖلٰفِهِمْ رِحْلَةَ الشِّتَاۤءِ وَالصَّيْفِۚ", "Īlāfihim riḥlatasy-syitā’i waṣ-ṣaif", "(yaitu) kebiasaan mereka bepergian pada musim dingin dan musim panas.", "Their accustomed security in the caravan of winter and summer,"),
            QuranAyahItem(3, "فَلْيَعْبُدُوْا رَبَّ هٰذَا الْبَيْتِۙ", "Falya‘budū rabba hāżal-bait", "Maka hendaklah mereka menyembah Tuhan (pemilik) rumah ini (Ka'bah),", "Let them worship the Lord of this House,"),
            QuranAyahItem(4, "الَّذِيْٓ اَطْعَمَهُمْ مِّنْ جُوْعٍ ەۙ وَّاٰمَنَهُمْ مِّنْ خَوْفٍ", "Allażī aṭ‘amahum min jū‘iw wa āmanahum min khauf", "yang telah memberi makanan kepada mereka untuk menghilangkan lapar dan mengamankan mereka dari rasa ketakutan.", "Who has fed them, saving them from hunger and made them safe from fear.")
        ),
        107 to listOf(
            QuranAyahItem(1, "اَرَاَيْتَ الَّذِيْ يُكَذِّبُ بِالدِّيْنِۗ", "Ara’aitallażī yukażżibu bid-dīn", "Tahukah kamu (orang) yang mendustakan agama?", "Have you seen the one who denies the Recompense?"),
            QuranAyahItem(2, "فَذٰلِكَ الَّذِيْ يَدُعُّ الْيَتِيْمَۙ", "Fażālikallażī yadu‘‘ul-yatīm", "Maka itulah orang yang menghardik anak yatim,", "For that is the one who drives away the orphan,"),
            QuranAyahItem(3, "وَلَا يَحُضُّ عَلٰى طَعَامِ الْمِسْكِيْنِۗ", "Wa lā yaḥuḍḍu ‘alā ṭa‘āmil-miskīn", "dan tidak mendorong memberi makan orang miskin.", "And does not encourage the feeding of the poor."),
            QuranAyahItem(4, "فَوَيْلٌ لِّلْمُصَلِّيْنَۙ", "Fawailul lil-muṣallīn", "Maka celakalah orang yang sholat,", "So woe to those who pray,"),
            QuranAyahItem(5, "الَّذِيْنَ هُمْ عَنْ صَلَاتِهِمْ سَاهُوْنَۙ", "Allażīna hum ‘an ṣalātihim sāhūn", "(yaitu) orang-orang yang lalai terhadap sholatnya,", "Who are heedless of their prayer,"),
            QuranAyahItem(6, "الَّذِيْنَ هُمْ يُرَاۤءُوْنَۙ", "Allażīna hum yurā’ūn", "yang berbuat riya,", "Those who make show of their deeds,"),
            QuranAyahItem(7, "وَيَمْنَعُوْنَ الْمَاعُوْنَ", "Wa yamna‘ūnal-mā‘ūn", "dan enggan (memberikan) bantuan.", "And withhold simple assistance.")
        ),
        108 to listOf(
            QuranAyahItem(1, "اِنَّآ اَعْطَيْنٰكَ الْكَوْثَرَۗ", "Innā a‘ṭainākal-kauṡar", "Sungguh, Kami telah memberimu (Muhammad) nikmat yang banyak.", "Indeed, We have granted you Al-Kawthar."),
            QuranAyahItem(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْۗ", "Faṣalli lirabbika wanḥar", "Maka laksanakanlah sholat karena Tuhanmu, dan berkurbanlah (sebagai ibadah dan mendekatkan diri kepada Allah).", "So pray to your Lord and sacrifice to Him alone."),
            QuranAyahItem(3, "اِنَّ شَانِئَكَ هُوَ الْاَبْتَرُ", "Inna syāni’aka huwal-abtar", "Sungguh, orang-orang yang membencimu dialah yang terputus (dari rahmat Allah).", "Indeed, your enemy is the one cut off.")
        ),
        109 to listOf(
            QuranAyahItem(1, "قُلْ يٰٓاَيُّهَا الْكٰفِرُوْنَۙ", "Qul yā ayyuhal-kāfirūn", "Katakanlah (Muhammad), \"Wahai orang-orang kafir!", "Say, \"O disbelievers,"),
            QuranAyahItem(2, "لَآ اَعْبُدُ مَا تَعْبُدُوْنَۙ", "Lā a‘budu mā ta‘budūn", "aku tidak akan menyembah apa yang kamu sembah,", "I do not worship what you worship."),
            QuranAyahItem(3, "وَلَآ اَنْتُمْ عٰبِدُوْنَ مَآ اَعْبُدُۚ", "Wa lā antum ‘ābidūna mā a‘bud", "dan kamu bukan penyembah apa yang aku sembah,", "Nor are you worshippers of what I worship."),
            QuranAyahItem(4, "وَلَآ اَنَا۠ عَابِدٌ مَّا عَبَدْتُّمْۙ", "Wa lā ana ‘ābidum mā ‘abattum", "dan aku tidak pernah menjadi penyembah apa yang kamu sembah,", "Nor will I be a worshipper of what you worship."),
            QuranAyahItem(5, "وَلَآ اَنْتُمْ عٰبِدُوْنَ مَآ اَعْبُدُۗ", "Wa lā antum ‘ābidūna mā a‘bud", "dan kamu tidak pernah (pula) menjadi penyembah apa yang aku sembah.", "Nor will you be worshippers of what I worship."),
            QuranAyahItem(6, "لَكُمْ دِيْنُكُمْ وَلِيَ دِيْنِ", "Lakum dīnukum wa liya dīn", "Untukmu agamamu, dan untukku agamaku.\"", "For you is your religion, and for me is my religion.\"")
        ),
        110 to listOf(
            QuranAyahItem(1, "اِذَا جَاۤءَ نَصْرُ اللّٰهِ وَالْفَتْحُۙ", "Iżā jā’a naṣrullāhi wal-fatḥ", "Apabila telah datang pertolongan Allah dan kemenangan,", "When the victory of Allah has come and the conquest,"),
            QuranAyahItem(2, "وَرَاَيْتَ النَّاسَ يَدْخُلُوْنَ فِيْ دِيْنِ اللّٰهِ اَفْوَاجًاۙ", "Wa ra’aitan-nāsa yadkhulūna fī dīnillāhi afwājā", "dan engkau melihat manusia berbondong-bondong masuk agama Allah,", "And you see the people entering into the religion of Allah in multitudes,"),
            QuranAyahItem(3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُۗ اِنَّهٗ كَانَ تَوَّابًا", "Fasabbiḥ biḥamdi rabbika wastagfirh, innahū kāna tawwābā", "maka bertasbihlah dengan memuji Tuhanmu dan mohonlah ampunan kepada-Nya. Sungguh, Dia Maha Penerima tobat.", "Then exalt Him with praise of your Lord and ask forgiveness of Him. Indeed, He is ever Accepting of repentance.")
        ),
        111 to listOf(
            QuranAyahItem(1, "تَبَّتْ يَدَآ اَبِيْ لَهَبٍ وَّتَبَّۗ", "Tabbat yadā abī lahabiw wa tabb", "Binasalah kedua tangan Abu Lahab dan benar-benar binasa dia!", "May the hands of Abu Lahab be ruined, and ruined is he."),
            QuranAyahItem(2, "مَآ اَغْنٰى عَنْهُ مَالُهٗ وَمَا كَسَبَۗ", "Mā agnā ‘anhu māluhū wa mā kasab", "Tidaklah berguna baginya hartanya dan apa yang dia usahakan.", "His wealth will not avail him or that which he gained."),
            QuranAyahItem(3, "سَيَصْلٰى نَارًا ذَاتَ لَهَبٍۙ", "Sayaṣlā nāran żāta lahab", "Kelak dia akan masuk ke dalam api yang bergejolak (neraka).", "He will enter to burn in a Fire of blazing flame,"),
            QuranAyahItem(4, "وَّامْرَاَتُهٗ ۗحَمَّالَةَ الْحَطَبِۚ", "Wamra’atuh, ḥammālatal-ḥaṭab", "Dan (begitu pula) istrinya, pembawa kayu bakar (penyebar fitnah).", "And his wife as well, the carrier of firewood,"),
            QuranAyahItem(5, "فِيْ جِيْدِهَا حَبْلٌ مِّنْ مَّسَدٍ", "Fī jīdihā ḥablum mim masad", "Di lehernya ada tali dari sabut yang dipintal.", "Around her neck is a rope of twisted fiber.")
        ),
        112 to listOf(
            QuranAyahItem(1, "قُلْ هُوَ اللّٰهُ اَحَدٌۚ", "Qul huwallāhu aḥad", "Katakanlah (Muhammad), \"Dialah Allah, Yang Maha Esa.", "Say, \"He is Allah, who is One,"),
            QuranAyahItem(2, "اَللّٰهُ الصَّمَدُۚ", "Allāhuṣ-ṣamad", "Allah tempat meminta segala sesuatu.", "Allah, the Eternal Refuge."),
            QuranAyahItem(3, "لَمْ يَلِدْ وَلَمْ يُوْلَدْۙ", "Lam yalid wa lam yūlad", "(Allah) tidak beranak dan tidak pula diperanakkan.", "He neither begets nor is born,"),
            QuranAyahItem(4, "وَلَمْ يَكُنْ لَّهٗ كُفُوًا اَحَدٌ", "Wa lam yakul lahū kufuwan aḥad", "Dan tidak ada sesuatu yang setara dengan Dia.\"", "Nor is there to Him any equivalent.\"")
        ),
        113 to listOf(
            QuranAyahItem(1, "قُلْ اَعُوْذُ بِرَبِّ الْفَلَقِۙ", "Qul a‘ūżu birabbil-falaq", "Katakanlah, \"Aku berlindung kepada Tuhan yang menguasai subuh (fajar),", "Say, \"I seek refuge in the Lord of daybreak"),
            QuranAyahItem(2, "مِنْ شَرِّ مَا خَلَقَۙ", "Min syarri mā khalaq", "dari kejahatan (makhluk yang) Dia ciptakan,", "From the evil of that which He created"),
            QuranAyahItem(3, "وَمِنْ شَرِّ غَاسِقٍ اِذَا وَقَبَۙ", "Wa min syarri gāsiqin iżā waqab", "dan dari kejahatan malam apabila telah gelap gulita,", "And from the evil of darkness when it settles"),
            QuranAyahItem(4, "وَمِنْ شَرِّ النَّفّٰثٰتِ فِى الْعُقَدِۙ", "Wa min syarrin-naffāṡāti fil-‘uqad", "dan dari kejahatan (perempuan-perempuan) penyihir yang meniup pada buhul-buhul (talinya),", "And from the evil of the blowers in knots"),
            QuranAyahItem(5, "وَمِنْ شَرِّ حَاسِدٍ اِذَا حَسَدَ", "Wa min syarri ḥāsidin iżā ḥasad", "dan dari kejahatan orang yang dengki apabila dia dengki.\"", "And from the evil of an envier when he envies.\"")
        ),
        114 to listOf(
            QuranAyahItem(1, "قُلْ اَعُوْذُ بِرَبِّ النَّاسِۙ", "Qul a‘ūżu birabbin-nās", "Katakanlah, \"Aku berlindung kepada Tuhannya manusia,", "Say, \"I seek refuge in the Lord of mankind,"),
            QuranAyahItem(2, "مَلِكِ النَّاسِۙ", "Malikin-nās", "Raja manusia,", "The Sovereign of mankind,"),
            QuranAyahItem(3, "اِلٰهِ النَّاسِۙ", "Ilāhin-nās", "sembahan manusia,", "The God of mankind,"),
            QuranAyahItem(4, "مِنْ شَرِّ الْوَسْوَاسِ ەۙ الْخَنَّاسِۙ", "Min syarril-waswāsil-khannās", "dari kejahatan (bisikan) setan yang bersembunyi,", "From the evil of the retreating whisperer,"),
            QuranAyahItem(5, "الَّذِيْ يُوَسْوِسُ فِيْ صُدُوْرِ النَّاسِۙ", "Allażī yuwaswisu fī ṣudūrin-nās", "yang membisikkan (kejahatan) ke dalam dada manusia,", "Who whispers evil into the breasts of mankind,"),
            QuranAyahItem(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "Minal-jinnati wan-nās", "dari (golongan) jin dan manusia.\"", "From among the jinn and mankind.\"")
        )
    )
}
