package com.example.data.model

enum class QuranReaderTheme(
    val id: String,
    val labelId: String,
    val labelEn: String
) {
    IVORY_GOLD("IVORY_GOLD", "Krem Emas", "Ivory Gold"),
    AMBER_SEPIA("AMBER_SEPIA", "Madu Klasik", "Warm Sepia"),
    NIGHT_GOLD("NIGHT_GOLD", "Malam Emas", "Night Gold");

    companion object {
        fun fromId(id: String?): QuranReaderTheme =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: IVORY_GOLD
    }
}

enum class QuranArabicFontChoice(
    val id: String,
    val labelId: String,
    val labelEn: String
) {
    SCHEHERAZADE("SCHEHERAZADE", "Scheherazade (Jelas)", "Scheherazade (Clear)"),
    AMIRI("AMIRI", "Amiri (Klasik)", "Amiri (Classic)"),
    NOTO_NASKH("NOTO_NASKH", "Noto Naskh (Modern)", "Noto Naskh (Modern)");

    companion object {
        fun fromId(id: String?): QuranArabicFontChoice =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SCHEHERAZADE
    }
}

data class QuranQariSpec(
    val id: String,
    val nameId: String,
    val nameEn: String,
    val equranCode: String,
    val everyAyahFolder: String
)

data class QuranSurahSpec(
    val number: Int,
    val nameArabic: String,
    val nameLatin: String,
    val meaningId: String,
    val meaningEn: String,
    val revelationType: String,
    val versesCount: Int,
    val juzRange: String
)

data class QuranAyahItem(
    val numberInSurah: Int,
    val arabicText: String,
    val latinText: String,
    val translationId: String,
    val translationEn: String,
    val audioUrlPrimary: String = "",
    val audioUrlFallback: String = ""
)

data class QuranBookmark(
    val id: String,
    val surahNumber: Int,
    val surahNameLatin: String,
    val surahNameArabic: String,
    val ayahNumber: Int,
    val arabicPreview: String,
    val translationId: String,
    val highlightTag: String = "GOLD",
    val createdAt: Long = System.currentTimeMillis()
)

data class QuranNote(
    val id: String,
    val surahNumber: Int,
    val surahNameLatin: String,
    val ayahNumber: Int,
    val title: String,
    val content: String,
    val categoryTag: String = "HAFALAN",
    val updatedAt: Long = System.currentTimeMillis()
)

data class QuranLastRead(
    val surahNumber: Int = 1,
    val surahNameLatin: String = "Al-Fātiḥah",
    val ayahNumber: Int = 1,
    val updatedAt: Long = 0L
)

data class QuranDuaItem(
    val id: String,
    val titleId: String,
    val titleEn: String,
    val occasionId: String,
    val occasionEn: String,
    val arabicText: String,
    val latinText: String,
    val translationId: String,
    val translationEn: String,
    val linkedSurah: Int = 1,
    val linkedAyah: Int = 1
)

object QuranCatalog {

    const val BISMILLAH_ARABIC = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ"
    const val BISMILLAH_LATIN = "Bismillāhir-raḥmānir-raḥīm"
    const val BISMILLAH_MEANING_ID = "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang."
    const val BISMILLAH_MEANING_EN = "In the name of Allah, the Entirely Merciful, the Especially Merciful."

    val QARIS: List<QuranQariSpec> = listOf(
        QuranQariSpec(
            id = "alafasy",
            nameId = "Syekh Misyari Al-Afasy",
            nameEn = "Mishary Rashid Alafasy",
            equranCode = "05",
            everyAyahFolder = "Alafasy_128kbps"
        ),
        QuranQariSpec(
            id = "sudais",
            nameId = "Syekh Abdurrahman As-Sudais",
            nameEn = "Abdurrahman As-Sudais",
            equranCode = "03",
            everyAyahFolder = "Abdurrahmaan_As-Sudais_192kbps"
        ),
        QuranQariSpec(
            id = "husary",
            nameId = "Syekh Mahmud Al-Husary",
            nameEn = "Mahmoud Khalil Al-Husary",
            equranCode = "04",
            everyAyahFolder = "Husary_128kbps"
        )
    )

    fun findQari(id: String?): QuranQariSpec =
        QARIS.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: QARIS.first()

    fun toArabicIndicDigits(number: Int): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { ch ->
            if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
        }.joinToString("")
    }

    fun formatSurahBannerNumber(number: Int): String = "سُورَةُ "

    val FEATURED_SURAH_NUMBERS: Set<Int> = setOf(1, 18, 36, 55, 56, 67, 78, 87, 93, 94, 97, 103, 108, 109, 112, 113, 114)

    val ALL_SURAHS: List<QuranSurahSpec> = listOf(
        QuranSurahSpec(1, "الْفَاتِحَة", "Al-Fātiḥah", "Pembukaan", "The Opening", "Makkiyah", 7, "Juz 1"),
        QuranSurahSpec(2, "الْبَقَرَة", "Al-Baqarah", "Sapi Betina", "The Cow", "Madaniyah", 286, "Juz 1–3"),
        QuranSurahSpec(3, "آلِ عِمْرَان", "Āli ‘Imrān", "Keluarga Imran", "Family of Imran", "Madaniyah", 200, "Juz 3–4"),
        QuranSurahSpec(4, "النِّسَاء", "An-Nisā’", "Wanita", "The Women", "Madaniyah", 176, "Juz 4–6"),
        QuranSurahSpec(5, "الْمَائِدَة", "Al-Mā’idah", "Hidangan", "The Table Spread", "Madaniyah", 120, "Juz 6–7"),
        QuranSurahSpec(6, "الْأَنْعَام", "Al-An‘ām", "Binatang Ternak", "The Cattle", "Makkiyah", 165, "Juz 7–8"),
        QuranSurahSpec(7, "الْأَعْرَاف", "Al-A‘rāf", "Tempat Tertinggi", "The Heights", "Makkiyah", 206, "Juz 8–9"),
        QuranSurahSpec(8, "الْأَنْفَال", "Al-Anfāl", "Harta Rampasan Perang", "The Spoils of War", "Madaniyah", 75, "Juz 9–10"),
        QuranSurahSpec(9, "التَّوْبَة", "At-Taubah", "Pengampunan", "The Repentance", "Madaniyah", 129, "Juz 10–11"),
        QuranSurahSpec(10, "يُونُس", "Yūnus", "Nabi Yunus", "Jonah", "Makkiyah", 109, "Juz 11"),
        QuranSurahSpec(11, "هُود", "Hūd", "Nabi Hud", "Hud", "Makkiyah", 123, "Juz 11–12"),
        QuranSurahSpec(12, "يُوسُف", "Yūsuf", "Nabi Yusuf", "Joseph", "Makkiyah", 111, "Juz 12–13"),
        QuranSurahSpec(13, "الرَّعْد", "Ar-Ra‘d", "Guruh", "The Thunder", "Madaniyah", 43, "Juz 13"),
        QuranSurahSpec(14, "إِبْرَاهِيم", "Ibrāhīm", "Nabi Ibrahim", "Abraham", "Makkiyah", 52, "Juz 13"),
        QuranSurahSpec(15, "الْحِجْر", "Al-Ḥijr", "Bukit Batu", "The Rocky Tract", "Makkiyah", 99, "Juz 14"),
        QuranSurahSpec(16, "النَّحْل", "An-Naḥl", "Lebah", "The Bee", "Makkiyah", 128, "Juz 14"),
        QuranSurahSpec(17, "الْإِسْرَاء", "Al-Isrā’", "Perjalanan Malam", "The Night Journey", "Makkiyah", 111, "Juz 15"),
        QuranSurahSpec(18, "الْكَهْف", "Al-Kahf", "Gua", "The Cave", "Makkiyah", 110, "Juz 15–16"),
        QuranSurahSpec(19, "مَرْيَم", "Maryam", "Maryam", "Mary", "Makkiyah", 98, "Juz 16"),
        QuranSurahSpec(20, "طٰهٰ", "Ṭāhā", "Taha", "Ta-Ha", "Makkiyah", 135, "Juz 16"),
        QuranSurahSpec(21, "الْأَنْبِيَاء", "Al-Anbiyā’", "Para Nabi", "The Prophets", "Makkiyah", 112, "Juz 17"),
        QuranSurahSpec(22, "الْحَجّ", "Al-Ḥajj", "Haji", "The Pilgrimage", "Madaniyah", 78, "Juz 17"),
        QuranSurahSpec(23, "الْمُؤْمِنُون", "Al-Mu’minūn", "Orang-Orang Mukmin", "The Believers", "Makkiyah", 118, "Juz 18"),
        QuranSurahSpec(24, "النُّور", "An-Nūr", "Cahaya", "The Light", "Madaniyah", 64, "Juz 18"),
        QuranSurahSpec(25, "الْفُرْقَان", "Al-Furqān", "Pembeda", "The Criterion", "Makkiyah", 77, "Juz 18–19"),
        QuranSurahSpec(26, "الشُّعَرَاء", "Asy-Syu‘arā’", "Para Penyair", "The Poets", "Makkiyah", 227, "Juz 19"),
        QuranSurahSpec(27, "النَّمْل", "An-Naml", "Semut", "The Ant", "Makkiyah", 93, "Juz 19–20"),
        QuranSurahSpec(28, "الْقَصَص", "Al-Qaṣaṣ", "Kisah-Kisah", "The Stories", "Makkiyah", 88, "Juz 20"),
        QuranSurahSpec(29, "الْعَنْكَبُوت", "Al-‘Ankabūt", "Laba-Laba", "The Spider", "Makkiyah", 69, "Juz 20–21"),
        QuranSurahSpec(30, "الرُّوم", "Ar-Rūm", "Bangsa Romawi", "The Romans", "Makkiyah", 60, "Juz 21"),
        QuranSurahSpec(31, "لُقْمَان", "Luqmān", "Luqman", "Luqman", "Makkiyah", 34, "Juz 21"),
        QuranSurahSpec(32, "السَّجْدَة", "As-Sajdah", "Sujud", "The Prostration", "Makkiyah", 30, "Juz 21"),
        QuranSurahSpec(33, "الْأَحْزَاب", "Al-Aḥzāb", "Golongan Bersekutu", "The Combined Forces", "Madaniyah", 73, "Juz 21–22"),
        QuranSurahSpec(34, "سَبَأ", "Saba’", "Kaum Saba’", "Sheba", "Makkiyah", 54, "Juz 22"),
        QuranSurahSpec(35, "فَاطِر", "Fāṭir", "Pencipta", "Originator", "Makkiyah", 45, "Juz 22"),
        QuranSurahSpec(36, "يٰسٓ", "Yāsīn", "Yasin", "Ya Sin", "Makkiyah", 83, "Juz 22–23"),
        QuranSurahSpec(37, "الصَّافَّات", "Aṣ-Ṣāffāt", "Barisan-Barisan", "Those Ranged in Ranks", "Makkiyah", 182, "Juz 23"),
        QuranSurahSpec(38, "صٓ", "Ṣād", "Shad", "Saad", "Makkiyah", 88, "Juz 23"),
        QuranSurahSpec(39, "الزُّمَر", "Az-Zumar", "Rombongan-Rombongan", "The Troops", "Makkiyah", 75, "Juz 23–24"),
        QuranSurahSpec(40, "غَافِر", "Gāfir", "Yang Mengampuni", "The Forgiver", "Makkiyah", 85, "Juz 24"),
        QuranSurahSpec(41, "فُصِّلَت", "Fuṣṣilat", "Yang Dijelaskan", "Explained in Detail", "Makkiyah", 54, "Juz 24–25"),
        QuranSurahSpec(42, "الشُّورَىٰ", "Asy-Syūrā", "Musyawarah", "Consultation", "Makkiyah", 53, "Juz 25"),
        QuranSurahSpec(43, "الزُّخْرُف", "Az-Zukhruf", "Perhiasan", "Ornaments of Gold", "Makkiyah", 89, "Juz 25"),
        QuranSurahSpec(44, "الدُّخَان", "Ad-Dukhān", "Kabut", "The Smoke", "Makkiyah", 59, "Juz 25"),
        QuranSurahSpec(45, "الْجَاثِيَة", "Al-Jāṡiyah", "Yang Bertekuk Lutut", "Crouching", "Makkiyah", 37, "Juz 25"),
        QuranSurahSpec(46, "الْأَحْقَاف", "Al-Aḥqāf", "Bukit-Bukit Pasir", "Wind-Curved Sandhills", "Makkiyah", 35, "Juz 26"),
        QuranSurahSpec(47, "مُحَمَّد", "Muḥammad", "Nabi Muhammad", "Muhammad", "Madaniyah", 38, "Juz 26"),
        QuranSurahSpec(48, "الْفَتْح", "Al-Fatḥ", "Kemenangan", "The Victory", "Madaniyah", 29, "Juz 26"),
        QuranSurahSpec(49, "الْحُجُرَات", "Al-Ḥujurāt", "Kamar-Kamar", "The Rooms", "Madaniyah", 18, "Juz 26"),
        QuranSurahSpec(50, "قٓ", "Qāf", "Qaf", "Qaf", "Makkiyah", 45, "Juz 26"),
        QuranSurahSpec(51, "الذَّارِيَات", "Aż-Żāriyāt", "Angin Yang Menerbangkan", "Winnowing Winds", "Makkiyah", 60, "Juz 26–27"),
        QuranSurahSpec(52, "الطُّور", "Aṭ-Ṭūr", "Bukit Thur", "The Mount", "Makkiyah", 49, "Juz 27"),
        QuranSurahSpec(53, "النَّجْم", "An-Najm", "Bintang", "The Star", "Makkiyah", 62, "Juz 27"),
        QuranSurahSpec(54, "الْقَمَر", "Al-Qamar", "Bulan", "The Moon", "Makkiyah", 55, "Juz 27"),
        QuranSurahSpec(55, "الرَّحْمٰن", "Ar-Raḥmān", "Yang Maha Pemurah", "The Beneficent", "Madaniyah", 78, "Juz 27"),
        QuranSurahSpec(56, "الْوَاقِعَة", "Al-Wāqi‘ah", "Hari Kiamat", "The Inevitable", "Makkiyah", 96, "Juz 27"),
        QuranSurahSpec(57, "الْحَدِيد", "Al-Ḥadīd", "Besi", "The Iron", "Madaniyah", 29, "Juz 27"),
        QuranSurahSpec(58, "الْمُجَادَلَة", "Al-Mujādalah", "Wanita Yang Mengajukan Gugatan", "The Pleading Woman", "Madaniyah", 22, "Juz 28"),
        QuranSurahSpec(59, "الْحَشْر", "Al-Ḥasyr", "Pengusiran", "The Exile", "Madaniyah", 24, "Juz 28"),
        QuranSurahSpec(60, "الْمُمْتَحَنَة", "Al-Mumtaḥanah", "Wanita Yang Diuji", "She That Is to Be Examined", "Madaniyah", 13, "Juz 28"),
        QuranSurahSpec(61, "الصَّفّ", "Aṣ-Ṣaff", "Barisan", "The Ranks", "Madaniyah", 14, "Juz 28"),
        QuranSurahSpec(62, "الْجُمُعَة", "Al-Jumu‘ah", "Hari Jumat", "The Congregation", "Madaniyah", 11, "Juz 28"),
        QuranSurahSpec(63, "الْمُنَافِقُون", "Al-Munāfiqūn", "Orang-Orang Munafik", "The Hypocrites", "Madaniyah", 11, "Juz 28"),
        QuranSurahSpec(64, "التَّغَابُن", "At-Tagābun", "Hari Dinampakkan Kesalahan", "Mutual Disillusion", "Madaniyah", 18, "Juz 28"),
        QuranSurahSpec(65, "الطَّلَاق", "Aṭ-Ṭalāq", "Talak", "Divorce", "Madaniyah", 12, "Juz 28"),
        QuranSurahSpec(66, "التَّحْرِيم", "At-Taḥrīm", "Pengharaman", "The Prohibition", "Madaniyah", 12, "Juz 28"),
        QuranSurahSpec(67, "الْمُلْك", "Al-Mulk", "Kerajaan", "The Sovereignty", "Makkiyah", 30, "Juz 29"),
        QuranSurahSpec(68, "الْقَلَم", "Al-Qalam", "Pena", "The Pen", "Makkiyah", 52, "Juz 29"),
        QuranSurahSpec(69, "الْحَاقَّة", "Al-Ḥāqqah", "Hari Kiamat", "The Reality", "Makkiyah", 52, "Juz 29"),
        QuranSurahSpec(70, "الْمَعَارِج", "Al-Ma‘ārij", "Tempat Naik", "Ascending Stairways", "Makkiyah", 44, "Juz 29"),
        QuranSurahSpec(71, "نُوح", "Nūḥ", "Nabi Nuh", "Noah", "Makkiyah", 28, "Juz 29"),
        QuranSurahSpec(72, "الْجِنّ", "Al-Jinn", "Jin", "The Jinn", "Makkiyah", 28, "Juz 29"),
        QuranSurahSpec(73, "الْمُزَّمِّل", "Al-Muzzammil", "Orang Yang Berselimut", "The Enshrouded One", "Makkiyah", 20, "Juz 29"),
        QuranSurahSpec(74, "الْمُدَّثِّر", "Al-Muddaṡṡir", "Orang Yang Berkemul", "The Cloaked One", "Makkiyah", 56, "Juz 29"),
        QuranSurahSpec(75, "الْقِيَامَة", "Al-Qiyāmah", "Hari Kebangkitan", "The Resurrection", "Makkiyah", 40, "Juz 29"),
        QuranSurahSpec(76, "الْإِنْسَان", "Al-Insān", "Manusia", "Man", "Madaniyah", 31, "Juz 29"),
        QuranSurahSpec(77, "الْمُرْسَلَات", "Al-Mursalāt", "Malaikat Yang Diutus", "The Emissaries", "Makkiyah", 50, "Juz 29"),
        QuranSurahSpec(78, "النَّبَأ", "An-Naba’", "Berita Besar", "The Tidings", "Makkiyah", 40, "Juz 30"),
        QuranSurahSpec(79, "النَّازِعَات", "An-Nāzi‘āt", "Malaikat Pencabut", "Those Who Drag Forth", "Makkiyah", 46, "Juz 30"),
        QuranSurahSpec(80, "عَبَسَ", "‘Abasa", "Bermuka Masam", "He Frowned", "Makkiyah", 42, "Juz 30"),
        QuranSurahSpec(81, "التَّكْوِير", "At-Takwīr", "Menggulung", "The Overthrowing", "Makkiyah", 29, "Juz 30"),
        QuranSurahSpec(82, "الْإِنْفِطَار", "Al-Infiṭār", "Terbelah", "The Cleaving", "Makkiyah", 19, "Juz 30"),
        QuranSurahSpec(83, "الْمُطَفِّفِين", "Al-Muṭaffifīn", "Orang-Orang Curang", "Defrauding", "Makkiyah", 36, "Juz 30"),
        QuranSurahSpec(84, "الْإِنْشِقَاق", "Al-Insyiqāq", "Terbelah", "The Sundering", "Makkiyah", 25, "Juz 30"),
        QuranSurahSpec(85, "الْبُرُوج", "Al-Burūj", "Gugusan Bintang", "Mansions of the Stars", "Makkiyah", 22, "Juz 30"),
        QuranSurahSpec(86, "الطَّارِق", "Aṭ-Ṭāriq", "Yang Datang Di Malam Hari", "The Morning Star", "Makkiyah", 17, "Juz 30"),
        QuranSurahSpec(87, "الْأَعْلَىٰ", "Al-A‘lā", "Yang Paling Tinggi", "The Most High", "Makkiyah", 19, "Juz 30"),
        QuranSurahSpec(88, "الْغَاشِيَة", "Al-Gāsyiyah", "Hari Pembalasan", "The Overwhelming", "Makkiyah", 26, "Juz 30"),
        QuranSurahSpec(89, "الْفَجْر", "Al-Fajr", "Fajar", "The Dawn", "Makkiyah", 30, "Juz 30"),
        QuranSurahSpec(90, "الْبَلَد", "Al-Balad", "Negeri", "The City", "Makkiyah", 20, "Juz 30"),
        QuranSurahSpec(91, "الشَّمْس", "Asy-Syams", "Matahari", "The Sun", "Makkiyah", 15, "Juz 30"),
        QuranSurahSpec(92, "اللَّيْل", "Al-Lail", "Malam", "The Night", "Makkiyah", 21, "Juz 30"),
        QuranSurahSpec(93, "الضُّحَىٰ", "Aḍ-Ḍuḥā", "Waktu Dhuha", "The Morning Hours", "Makkiyah", 11, "Juz 30"),
        QuranSurahSpec(94, "الشَّرْح", "Asy-Syarḥ", "Kelapangan", "The Relief", "Makkiyah", 8, "Juz 30"),
        QuranSurahSpec(95, "التِّين", "At-Tīn", "Buah Tin", "The Fig", "Makkiyah", 8, "Juz 30"),
        QuranSurahSpec(96, "الْعَلَق", "Al-‘Alaq", "Segumpal Darah", "The Clot", "Makkiyah", 19, "Juz 30"),
        QuranSurahSpec(97, "الْقَدْر", "Al-Qadr", "Kemuliaan", "The Power", "Makkiyah", 5, "Juz 30"),
        QuranSurahSpec(98, "الْبَيِّنَة", "Al-Bayyinah", "Pembuktian", "The Clear Proof", "Madaniyah", 8, "Juz 30"),
        QuranSurahSpec(99, "الزَّلْزَلَة", "Az-Zalzalah", "Kegoncangan", "The Earthquake", "Madaniyah", 8, "Juz 30"),
        QuranSurahSpec(100, "الْعَادِيَات", "Al-‘Ādiyāt", "Kuda Perang Yang Berlari Kencang", "The Courser", "Makkiyah", 11, "Juz 30"),
        QuranSurahSpec(101, "الْقَارِعَة", "Al-Qāri‘ah", "Hari Kiamat", "The Calamity", "Makkiyah", 11, "Juz 30"),
        QuranSurahSpec(102, "التَّكَاثُر", "At-Takāṡur", "Bermegah-Megahan", "The Rivalry", "Makkiyah", 8, "Juz 30"),
        QuranSurahSpec(103, "الْعَصْر", "Al-‘Aṣr", "Masa", "The Declining Day", "Makkiyah", 3, "Juz 30"),
        QuranSurahSpec(104, "الْهُمَزَة", "Al-Humazah", "Pengumpat", "The Traducer", "Makkiyah", 9, "Juz 30"),
        QuranSurahSpec(105, "الْفِيل", "Al-Fīl", "Gajah", "The Elephant", "Makkiyah", 5, "Juz 30"),
        QuranSurahSpec(106, "قُرَيْش", "Quraisy", "Suku Quraisy", "Quraysh", "Makkiyah", 4, "Juz 30"),
        QuranSurahSpec(107, "الْمَاعُون", "Al-Mā‘ūn", "Barang-Barang Berguna", "Small Kindnesses", "Makkiyah", 7, "Juz 30"),
        QuranSurahSpec(108, "الْكَوْثَر", "Al-Kauṡar", "Nikmat Yang Banyak", "Abundance", "Makkiyah", 3, "Juz 30"),
        QuranSurahSpec(109, "الْكَافِرُون", "Al-Kāfirūn", "Orang-Orang Kafir", "The Disbelievers", "Makkiyah", 6, "Juz 30"),
        QuranSurahSpec(110, "النَّصْر", "An-Naṣr", "Pertolongan", "Divine Support", "Madaniyah", 3, "Juz 30"),
        QuranSurahSpec(111, "الْمَسَد", "Al-Lahab / Al-Masad", "Gejolak Api", "The Palm Fiber", "Makkiyah", 5, "Juz 30"),
        QuranSurahSpec(112, "الْإِخْلَاص", "Al-Ikhlāṣ", "Kemurnian Keesaan Allah", "The Sincerity", "Makkiyah", 4, "Juz 30"),
        QuranSurahSpec(113, "الْفَلَق", "Al-Falaq", "Waktu Subuh", "The Daybreak", "Makkiyah", 5, "Juz 30"),
        QuranSurahSpec(114, "النَّاس", "An-Nās", "Manusia", "Mankind", "Makkiyah", 6, "Juz 30")
    )

    fun findSurah(number: Int): QuranSurahSpec =
        ALL_SURAHS.firstOrNull { it.number == number } ?: ALL_SURAHS.first()

    val CURATED_DUAS: List<QuranDuaItem> = listOf(
        QuranDuaItem(
            id = "dua_before_quran",
            titleId = "Doa Sebelum Membaca Al-Qur'an",
            titleEn = "Prayer Before Reciting Al-Qur'an",
            occasionId = "Adab Membuka Mushaf & Mengaji",
            occasionEn = "Etiquette Before Recitation",
            arabicText = "اَللّٰهُمَّ افْتَحْ عَلَيْنَا حِكْمَتَكَ وَانْشُرْ عَلَيْنَا رَحْمَتَكَ مِنْ خَزَائِنِ رَحْمَتِكَ يَا أَرْحَمَ الرَّاحِمِيْنَ",
            latinText = "Allāhummaftaḥ ‘alainā ḥikmataka wansyur ‘alainā raḥmataka min khazā’ini raḥmatika yā arḥamar-rāḥimīn.",
            translationId = "Ya Allah, bukakanlah bagi kami hikmah-Mu dan taburkanlah kepada kami rahmat-Mu dari khazanah rahmat-Mu, wahai Dzat Yang Maha Pengasih di antara para pengasih.",
            translationEn = "O Allah, open for us Your wisdom and shower upon us Your mercy from the treasuries of Your grace, O Most Merciful of the merciful.",
            linkedSurah = 1,
            linkedAyah = 1
        ),
        QuranDuaItem(
            id = "dua_after_quran",
            titleId = "Doa Sesudah Membaca Al-Qur'an (Khatam)",
            titleEn = "Prayer After Reciting Al-Qur'an",
            occasionId = "Doa Memohon Cahaya & Keberkahan",
            occasionEn = "Closing Prayer for Light & Blessing",
            arabicText = "اَللّٰهُمَّ ارْحَمْنِي بِالْقُرْآنِ وَاجْعَلْهُ لِي إِمَامًا وَنُوْرًا وَهُدًى وَرَحْمَةً، اَللّٰهُمَّ ذَكِّرْنِي مِنْهُ مَا نَسِيْتُ وَعَلِّمْنِي مِنْهُ مَا جَهِلْتُ وَارْزُقْنِي تِلَاوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ",
            latinText = "Allāhummarḥamnī bil-Qur’ān waj‘alhu lī imāman wa nūran wa hudan wa raḥmah. Allāhumma żakkirnī minhu mā nasītu wa ‘allimnī minhu mā jahiltu warzuqnī tilāwatahu ānā’al-laili wa aṭrāfan-nahār.",
            translationId = "Ya Allah, rahmatilah aku dengan Al-Qur'an, jadikanlah ia bagiku pemimpin, cahaya, petunjuk, dan rahmat. Ingatkanlah aku apa yang aku lupa darinya, ajarkanlah apa yang belum aku ketahui, dan karuniakanlah aku membacanya di waktu malam dan siang hari.",
            translationEn = "O Allah, have mercy on me through the Qur'an and make it for me a guide, light, guidance, and mercy. Remind me of what I forgot from it, teach me what I do not know, and grant me its recitation throughout the night and day.",
            linkedSurah = 94,
            linkedAyah = 1
        ),
        QuranDuaItem(
            id = "dua_rabbana_ilmu",
            titleId = "Doa Mohon Ilmu Bermanfaat (QS. Ṭāhā: 114)",
            titleEn = "Prayer for Beneficial Knowledge (QS. Ta-Ha: 114)",
            occasionId = "Sebelum Belajar & Menghafal",
            occasionEn = "Before Studying & Memorizing",
            arabicText = "رَبِّ زِدْنِي عِلْمًا وَارْزُقْنِي فَهْمًا",
            latinText = "Rabbi zidnī ‘ilman warzuqnī fahmā.",
            translationId = "Ya Tuhanku, tambahkanlah kepadaku ilmu pengetahuan dan karuniakanlah kepadaku pemahaman yang baik.",
            translationEn = "My Lord, increase me in knowledge and grant me sound understanding.",
            linkedSurah = 20,
            linkedAyah = 114
        )
    )
}
