package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BibleTestament(val code: String, val labelId: String, val labelEn: String) {
    PB("PB", "Perjanjian Baru", "New Testament"),
    PL("PL", "Perjanjian Lama", "Old Testament")
}

data class BibleBookSpec(
    val id: String,
    val apiQueryName: String,
    val nameId: String,
    val nameEn: String,
    val testament: BibleTestament,
    val categoryId: String,
    val categoryEn: String,
    val chaptersCount: Int
)

data class BibleVerseItem(
    val verse: Int,
    val textId: String,
    val textEn: String
)

data class BibleDevotionalItem(
    val id: String,
    val titleId: String,
    val titleEn: String,
    val themeId: String,
    val themeEn: String,
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val keyVerseId: String,
    val keyVerseEn: String,
    val reflectionId: String,
    val reflectionEn: String,
    val prayerId: String,
    val prayerEn: String
)

data class BibleStoryItem(
    val id: String,
    val titleId: String,
    val titleEn: String,
    val eraId: String,
    val eraEn: String,
    val bookId: String,
    val chapter: Int,
    val summaryId: String,
    val summaryEn: String,
    val lessonId: String,
    val lessonEn: String
)

@Entity(tableName = "bible_bookmarks")
data class BibleBookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val schoolId: String = "SRT1-KDR",
    val ownerWaliUsername: String = "",
    val ownerWaliName: String = "Wali Asuh",
    val bookId: String = "JHN",
    val bookNameId: String = "Yohanes",
    val bookNameEn: String = "John",
    val chapter: Int = 3,
    val verse: Int = 16,
    val verseTextId: String = "",
    val verseTextEn: String = "",
    val highlightColor: String = "GOLD",
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bible_notes")
data class BibleNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val schoolId: String = "SRT1-KDR",
    val ownerWaliUsername: String = "",
    val ownerWaliName: String = "Wali Asuh",
    val bookId: String = "2TI",
    val bookNameId: String = "2 Timotius",
    val bookNameEn: String = "2 Timothy",
    val chapter: Int = 4,
    val verse: Int = 1,
    val title: String = "",
    val content: String = "",
    val categoryTag: String = "RENUNGAN",
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object BibleCatalog {
    /**
     * Katalog lengkap 66 Kitab Alkitab (27 Perjanjian Baru + 39 Perjanjian Lama).
     */
    val ALL_BOOKS: List<BibleBookSpec> = listOf(
        // ================= PERJANJIAN BARU (27) =================
        // Injil (4)
        BibleBookSpec("MAT", "Matthew", "Matius", "Matthew", BibleTestament.PB, "Injil", "Gospels", 28),
        BibleBookSpec("MRK", "Mark", "Markus", "Mark", BibleTestament.PB, "Injil", "Gospels", 16),
        BibleBookSpec("LUK", "Luke", "Lukas", "Luke", BibleTestament.PB, "Injil", "Gospels", 24),
        BibleBookSpec("JHN", "John", "Yohanes", "John", BibleTestament.PB, "Injil", "Gospels", 21),
        // Sejarah (1)
        BibleBookSpec("ACT", "Acts", "Kisah Para Rasul", "Acts", BibleTestament.PB, "Sejarah", "History", 28),
        // Surat Paulus (13)
        BibleBookSpec("ROM", "Romans", "Roma", "Romans", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 16),
        BibleBookSpec("1CO", "1 Corinthians", "1 Korintus", "1 Corinthians", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 16),
        BibleBookSpec("2CO", "2 Corinthians", "2 Korintus", "2 Corinthians", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 13),
        BibleBookSpec("GAL", "Galatians", "Galatia", "Galatians", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 6),
        BibleBookSpec("EPH", "Ephesians", "Efesus", "Ephesians", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 6),
        BibleBookSpec("PHP", "Philippians", "Filipi", "Philippians", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 4),
        BibleBookSpec("COL", "Colossians", "Kolose", "Colossians", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 4),
        BibleBookSpec("1TH", "1 Thessalonians", "1 Tesalonika", "1 Thessalonians", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 5),
        BibleBookSpec("2TH", "2 Thessalonians", "2 Tesalonika", "2 Thessalonians", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 3),
        BibleBookSpec("1TI", "1 Timothy", "1 Timotius", "1 Timothy", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 6),
        BibleBookSpec("2TI", "2 Timothy", "2 Timotius", "2 Timothy", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 4),
        BibleBookSpec("TIT", "Titus", "Titus", "Titus", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 3),
        BibleBookSpec("PHM", "Philemon", "Filemon", "Philemon", BibleTestament.PB, "Surat Paulus", "Pauline Epistles", 1),
        // Surat Umum (8)
        BibleBookSpec("HEB", "Hebrews", "Ibrani", "Hebrews", BibleTestament.PB, "Surat Umum", "General Epistles", 13),
        BibleBookSpec("JAS", "James", "Yakobus", "James", BibleTestament.PB, "Surat Umum", "General Epistles", 5),
        BibleBookSpec("1PE", "1 Peter", "1 Petrus", "1 Peter", BibleTestament.PB, "Surat Umum", "General Epistles", 5),
        BibleBookSpec("2PE", "2 Peter", "2 Petrus", "2 Peter", BibleTestament.PB, "Surat Umum", "General Epistles", 3),
        BibleBookSpec("1JN", "1 John", "1 Yohanes", "1 John", BibleTestament.PB, "Surat Umum", "General Epistles", 5),
        BibleBookSpec("2JN", "2 John", "2 Yohanes", "2 John", BibleTestament.PB, "Surat Umum", "General Epistles", 1),
        BibleBookSpec("3JN", "3 John", "3 Yohanes", "3 John", BibleTestament.PB, "Surat Umum", "General Epistles", 1),
        BibleBookSpec("JUD", "Jude", "Yudas", "Jude", BibleTestament.PB, "Surat Umum", "General Epistles", 1),
        // Nubuat (1)
        BibleBookSpec("REV", "Revelation", "Wahyu", "Revelation", BibleTestament.PB, "Nubuat", "Prophecy", 22),

        // ================= PERJANJIAN LAMA (39) =================
        // Taurat (5)
        BibleBookSpec("GEN", "Genesis", "Kejadian", "Genesis", BibleTestament.PL, "Taurat", "Law", 50),
        BibleBookSpec("EXO", "Exodus", "Keluaran", "Exodus", BibleTestament.PL, "Taurat", "Law", 40),
        BibleBookSpec("LEV", "Leviticus", "Imamat", "Leviticus", BibleTestament.PL, "Taurat", "Law", 27),
        BibleBookSpec("NUM", "Numbers", "Bilangan", "Numbers", BibleTestament.PL, "Taurat", "Law", 36),
        BibleBookSpec("DEU", "Deuteronomy", "Ulangan", "Deuteronomy", BibleTestament.PL, "Taurat", "Law", 34),
        // Sejarah (12)
        BibleBookSpec("JOS", "Joshua", "Yosua", "Joshua", BibleTestament.PL, "Sejarah", "History", 24),
        BibleBookSpec("JDG", "Judges", "Hakim-hakim", "Judges", BibleTestament.PL, "Sejarah", "History", 21),
        BibleBookSpec("RUT", "Ruth", "Rut", "Ruth", BibleTestament.PL, "Sejarah", "History", 4),
        BibleBookSpec("1SA", "1 Samuel", "1 Samuel", "1 Samuel", BibleTestament.PL, "Sejarah", "History", 31),
        BibleBookSpec("2SA", "2 Samuel", "2 Samuel", "2 Samuel", BibleTestament.PL, "Sejarah", "History", 24),
        BibleBookSpec("1KI", "1 Kings", "1 Raja-raja", "1 Kings", BibleTestament.PL, "Sejarah", "History", 22),
        BibleBookSpec("2KI", "2 Kings", "2 Raja-raja", "2 Kings", BibleTestament.PL, "Sejarah", "History", 25),
        BibleBookSpec("1CH", "1 Chronicles", "1 Tawarikh", "1 Chronicles", BibleTestament.PL, "Sejarah", "History", 29),
        BibleBookSpec("2CH", "2 Chronicles", "2 Tawarikh", "2 Chronicles", BibleTestament.PL, "Sejarah", "History", 36),
        BibleBookSpec("EZR", "Ezra", "Ezra", "Ezra", BibleTestament.PL, "Sejarah", "History", 10),
        BibleBookSpec("NEH", "Nehemiah", "Nehemia", "Nehemiah", BibleTestament.PL, "Sejarah", "History", 13),
        BibleBookSpec("EST", "Esther", "Ester", "Esther", BibleTestament.PL, "Sejarah", "History", 10),
        // Puisi & Hikmat (5)
        BibleBookSpec("JOB", "Job", "Ayub", "Job", BibleTestament.PL, "Puisi & Hikmat", "Poetry & Wisdom", 42),
        BibleBookSpec("PSA", "Psalms", "Mazmur", "Psalms", BibleTestament.PL, "Puisi & Hikmat", "Poetry & Wisdom", 150),
        BibleBookSpec("PRO", "Proverbs", "Amsal", "Proverbs", BibleTestament.PL, "Puisi & Hikmat", "Poetry & Wisdom", 31),
        BibleBookSpec("ECC", "Ecclesiastes", "Pengkhotbah", "Ecclesiastes", BibleTestament.PL, "Puisi & Hikmat", "Poetry & Wisdom", 12),
        BibleBookSpec("SNG", "Song of Solomon", "Kidung Agung", "Song of Solomon", BibleTestament.PL, "Puisi & Hikmat", "Poetry & Wisdom", 8),
        // Nabi Besar (5)
        BibleBookSpec("ISA", "Isaiah", "Yesaya", "Isaiah", BibleTestament.PL, "Nabi Besar", "Major Prophets", 66),
        BibleBookSpec("JER", "Jeremiah", "Yeremia", "Jeremiah", BibleTestament.PL, "Nabi Besar", "Major Prophets", 52),
        BibleBookSpec("LAM", "Lamentations", "Ratapan", "Lamentations", BibleTestament.PL, "Nabi Besar", "Major Prophets", 5),
        BibleBookSpec("EZK", "Ezekiel", "Yehezkiel", "Ezekiel", BibleTestament.PL, "Nabi Besar", "Major Prophets", 48),
        BibleBookSpec("DAN", "Daniel", "Daniel", "Daniel", BibleTestament.PL, "Nabi Besar", "Major Prophets", 12),
        // Nabi Kecil (12)
        BibleBookSpec("HOS", "Hosea", "Hosea", "Hosea", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 14),
        BibleBookSpec("JOL", "Joel", "Yoel", "Joel", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 3),
        BibleBookSpec("AMO", "Amos", "Amos", "Amos", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 9),
        BibleBookSpec("OBA", "Obadiah", "Obaja", "Obadiah", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 1),
        BibleBookSpec("JON", "Jonah", "Yunus", "Jonah", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 4),
        BibleBookSpec("MIC", "Micah", "Mikha", "Micah", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 7),
        BibleBookSpec("NAM", "Nahum", "Nahum", "Nahum", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 3),
        BibleBookSpec("HAB", "Habakkuk", "Habakuk", "Habakkuk", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 3),
        BibleBookSpec("ZEP", "Zephaniah", "Zefanya", "Zephaniah", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 3),
        BibleBookSpec("HAG", "Haggai", "Hagai", "Haggai", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 2),
        BibleBookSpec("ZEC", "Zechariah", "Zakharia", "Zechariah", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 14),
        BibleBookSpec("MAL", "Malachi", "Maleakhi", "Malachi", BibleTestament.PL, "Nabi Kecil", "Minor Prophets", 4)
    )

    private val byId = ALL_BOOKS.associateBy { it.id }

    fun findBook(bookId: String): BibleBookSpec {
        return byId[bookId.trim().uppercase()] ?: byId["JHN"]!!
    }

    fun booksByTestament(testament: BibleTestament): List<BibleBookSpec> {
        return ALL_BOOKS.filter { it.testament == testament }
    }

    fun formatReference(
        bookId: String,
        chapter: Int,
        verse: Int? = null,
        isEnglish: Boolean = false
    ): String {
        val book = findBook(bookId)
        val name = if (isEnglish) book.nameEn else book.nameId
        return if (verse != null && verse > 0) {
            "$name $chapter:$verse"
        } else {
            "$name $chapter"
        }
    }
}
