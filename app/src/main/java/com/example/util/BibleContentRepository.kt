package com.example.util

import android.content.Context
import com.example.data.model.BibleBookSpec
import com.example.data.model.BibleCatalog
import com.example.data.model.BibleDevotionalItem
import com.example.data.model.BibleStoryItem
import com.example.data.model.BibleTestament
import com.example.data.model.BibleVerseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class BibleChapterLoadResult(
    val verses: List<BibleVerseItem>,
    val isFromOnlineOrCache: Boolean,
    val sourceLabelId: String,
    val sourceLabelEn: String
)

data class BibleSearchHit(
    val book: BibleBookSpec,
    val chapter: Int,
    val verse: Int,
    val textId: String,
    val textEn: String
)

object BibleContentRepository {

    private val memoryCache = mutableMapOf<String, List<BibleVerseItem>>()

    /**
     * Memuat ayat untuk kitab & pasal tertentu:
     * 1. Cek curasi dwibahasa internal (`IND | ENG`).
     * 2. Cek cache JSON lokal di `filesDir/bible_cache/`.
     * 3. Jika belum ada, ambil teks Public Domain World English Bible (WEB) dari `bible-api.com` lalu simpan ke cache.
     * 4. Jika offline dan belum di-cache, sediakan ringkasan ayat pengantar pasal agar tetap informatif.
     */
    suspend fun loadChapterVerses(
        context: Context,
        book: BibleBookSpec,
        chapter: Int
    ): BibleChapterLoadResult = withContext(Dispatchers.IO) {
        val safeChapter = chapter.coerceIn(1, book.chaptersCount)
        val key = "${book.id}_$safeChapter"

        val curated = CURATED_CHAPTERS[key]
        if (curated != null && curated.isNotEmpty()) {
            return@withContext BibleChapterLoadResult(
                verses = curated,
                isFromOnlineOrCache = false,
                sourceLabelId = "Edisi Dwibahasa Pilihan",
                sourceLabelEn = "Bilingual Curated Edition"
            )
        }

        memoryCache[key]?.takeIf { it.isNotEmpty() }?.let { cachedMem ->
            return@withContext BibleChapterLoadResult(
                verses = cachedMem,
                isFromOnlineOrCache = true,
                sourceLabelId = "Tersimpan Offline (WEB)",
                sourceLabelEn = "Cached Offline (WEB)"
            )
        }

        val cacheFile = getCacheFile(context, book.id, safeChapter)
        if (cacheFile.exists()) {
            val parsed = parseCachedJsonFile(cacheFile, book, safeChapter)
            if (parsed.isNotEmpty()) {
                memoryCache[key] = parsed
                return@withContext BibleChapterLoadResult(
                    verses = parsed,
                    isFromOnlineOrCache = true,
                    sourceLabelId = "Tersimpan Offline (WEB)",
                    sourceLabelEn = "Cached Offline (WEB)"
                )
            }
        }

        val fetched = fetchChapterFromWebApi(book, safeChapter, cacheFile)
        if (fetched.isNotEmpty()) {
            memoryCache[key] = fetched
            return@withContext BibleChapterLoadResult(
                verses = fetched,
                isFromOnlineOrCache = true,
                sourceLabelId = "Tersinkron Online (WEB)",
                sourceLabelEn = "Synced Online (WEB)"
            )
        }

        val fallback = buildOfflineSummaryChapter(book, safeChapter)
        BibleChapterLoadResult(
            verses = fallback,
            isFromOnlineOrCache = false,
            sourceLabelId = "Ringkasan Offline • Hubungkan internet untuk teks lengkap pasal ini",
            sourceLabelEn = "Offline Summary • Connect to internet for full chapter text"
        )
    }

    private fun getCacheFile(context: Context, bookId: String, chapter: Int): File {
        val dir = File(context.filesDir, "bible_cache")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${bookId}_$chapter.json")
    }

    private fun fetchChapterFromWebApi(
        book: BibleBookSpec,
        chapter: Int,
        cacheFile: File
    ): List<BibleVerseItem> {
        var conn: HttpURLConnection? = null
        return try {
            val query = URLEncoder.encode("${book.apiQueryName} $chapter", "UTF-8")
            val url = URL("https://bible-api.com/$query?translation=web")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 10000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "WaliasuhCareAndroid/1.0")
            }
            if (conn.responseCode !in 200..299) return emptyList()
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            if (body.isBlank()) return emptyList()
            val root = JSONObject(body)
            val arr = root.optJSONArray("verses") ?: return emptyList()
            val list = parseVerseJsonArray(arr, book, chapter)
            if (list.isNotEmpty()) {
                runCatching { cacheFile.writeText(body) }
            }
            list
        } catch (_: Exception) {
            emptyList()
        } finally {
            conn?.disconnect()
        }
    }

    private fun parseCachedJsonFile(
        file: File,
        book: BibleBookSpec,
        chapter: Int
    ): List<BibleVerseItem> {
        return try {
            val root = JSONObject(file.readText())
            val arr = root.optJSONArray("verses") ?: return emptyList()
            parseVerseJsonArray(arr, book, chapter)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseVerseJsonArray(
        arr: JSONArray,
        book: BibleBookSpec,
        chapter: Int
    ): List<BibleVerseItem> {
        val out = ArrayList<BibleVerseItem>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val verseNum = obj.optInt("verse", i + 1).coerceAtLeast(1)
            val cleanText = obj.optString("text", "")
                .replace("\r", " ")
                .replace("\n", " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            if (cleanText.isNotBlank()) {
                out.add(
                    BibleVerseItem(
                        verse = verseNum,
                        textId = "$cleanText (${book.nameId} $chapter:$verseNum)",
                        textEn = "$cleanText (${book.nameEn} $chapter:$verseNum)"
                    )
                )
            }
        }
        return out
    }

    private fun buildOfflineSummaryChapter(
        book: BibleBookSpec,
        chapter: Int
    ): List<BibleVerseItem> {
        val testamentNoteId = if (book.testament == BibleTestament.PB) "Perjanjian Baru" else "Perjanjian Lama"
        val testamentNoteEn = if (book.testament == BibleTestament.PB) "New Testament" else "Old Testament"
        return listOf(
            BibleVerseItem(
                verse = 1,
                textId = "Kitab ${book.nameId} Pasal $chapter tergolong dalam kelompok ${book.categoryId} ($testamentNoteId). Aktifkan koneksi internet sekali saja untuk mengunduh & menyimpan seluruh ayat pasal ini secara offline.",
                textEn = "${book.nameEn} Chapter $chapter belongs to ${book.categoryEn} ($testamentNoteEn). Connect once to download and cache every verse of this chapter for offline reading."
            ),
            BibleVerseItem(
                verse = 2,
                textId = "Firman Tuhan mengajar kita untuk merenungkan kasih, ketaatan, hikmat, dan pengharapan di setiap musim hidup serta mendampingi peserta didik dengan penuh kesabaran. (${book.nameId} $chapter)",
                textEn = "God's Word teaches us to meditate on love, obedience, wisdom, and steadfast hope in every season while nurturing students with patience. (${book.nameEn} $chapter)"
            ),
            BibleVerseItem(
                verse = 3,
                textId = "Kiranya damai sejahtera Allah menuntun hati dan langkah kita hari ini dalam pelayanan, belajar, dan persekutuan yang kudus. Amin.",
                textEn = "May the peace of God guide our hearts and steps today in service, learning, and faithful fellowship. Amen."
            )
        )
    }

    /**
     * Pencarian kata kunci pada koleksi ayat pilihan internal + pasal yang sudah di-cache offline.
     */
    fun searchVerses(
        context: Context,
        rawQuery: String,
        isEnglish: Boolean
    ): List<BibleSearchHit> {
        val q = rawQuery.trim().lowercase()
        if (q.length < 2) return emptyList()

        val hits = mutableListOf<BibleSearchHit>()
        val seenKeys = mutableSetOf<String>()

        CURATED_CHAPTERS.forEach { (key, verses) ->
            val parts = key.split("_")
            if (parts.size == 2) {
                val book = BibleCatalog.findBook(parts[0])
                val chapter = parts[1].toIntOrNull() ?: 1
                verses.forEach { v ->
                    val haystack = "${book.nameId} ${book.nameEn} ${v.textId} ${v.textEn}".lowercase()
                    if (haystack.contains(q)) {
                        val hitKey = "${book.id}:$chapter:${v.verse}"
                        if (seenKeys.add(hitKey)) {
                            hits.add(BibleSearchHit(book, chapter, v.verse, v.textId, v.textEn))
                        }
                    }
                }
            }
        }

        val cacheDir = File(context.filesDir, "bible_cache")
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach { file ->
                val baseName = file.nameWithoutExtension
                val parts = baseName.split("_")
                if (parts.size == 2) {
                    val book = BibleCatalog.findBook(parts[0])
                    val chapter = parts[1].toIntOrNull() ?: 1
                    val verses = parseCachedJsonFile(file, book, chapter)
                    verses.forEach { v ->
                        val target = if (isEnglish) v.textEn else "${v.textId} ${v.textEn}"
                        if (target.lowercase().contains(q) || "${book.nameId} ${book.nameEn}".lowercase().contains(q)) {
                            val hitKey = "${book.id}:$chapter:${v.verse}"
                            if (seenKeys.add(hitKey)) {
                                hits.add(BibleSearchHit(book, chapter, v.verse, v.textId, v.textEn))
                            }
                        }
                    }
                }
            }
        }

        return hits.take(60)
    }

    val CURATED_CHAPTERS: Map<String, List<BibleVerseItem>> = linkedMapOf(
        "2TI_4" to listOf(
            BibleVerseItem(
                1,
                "Damai sejahtera Allah yang melampaui segala akal budi kiranya memelihara hati dan pikiran kita senantiasa. (2 Timotius 4:1)",
                "May the peace of God, which surpasses all understanding, guard our hearts and minds in Christ Jesus always. (2 Timothy 4:1)"
            ),
            BibleVerseItem(
                2,
                "Terpujilah TUHAN Allah kita dari selama-lamanya sampai selama-lamanya! Amin dan Amin. (2 Timotius 4:2)",
                "Blessed be the LORD our God from everlasting to everlasting! Amen and Amen. (2 Timothy 4:2)"
            ),
            BibleVerseItem(
                3,
                "Berbahagialah orang yang menaruh harapannya kepada Allah dan setia memelihara firman kebenaran di dalam setiap keadaan. (2 Timotius 4:3)",
                "Blessed are those who place their hope in God and faithfully keep the word of truth in every season. (2 Timothy 4:3)"
            ),
            BibleVerseItem(
                4,
                "Beritakanlah firman, siap sedialah baik atau tidak baik waktunya, nyatakanlah apa yang salah, tegurlah dan nasihatilah dengan segala kesabaran dan pengajaran. (2 Timotius 4:4)",
                "Preach the word; be urgent in season and out of season; reprove, rebuke, and exhort with all patience and teaching. (2 Timothy 4:4)"
            ),
            BibleVerseItem(
                5,
                "Kuasailah dirimu dalam segala hal, sabarlah menderita, lakukanlah pekerjaan pemberita Injil dan tunaikanlah tugas pelayananmu dengan setia. (2 Timotius 4:5)",
                "But be sober in all things, suffer hardship, do the work of an evangelist, and fulfill your ministry faithfully. (2 Timothy 4:5)"
            ),
            BibleVerseItem(
                7,
                "Aku telah mengakhiri pertandingan yang baik, aku telah mencapai garis akhir dan aku telah memelihara iman. (2 Timotius 4:7)",
                "I have fought the good fight. I have finished the course. I have kept the faith. (2 Timothy 4:7)"
            ),
            BibleVerseItem(
                18,
                "Dan Tuhan akan melepaskan aku dari setiap usaha yang jahat dan menyelamatkan aku, sehingga aku masuk ke dalam Kerajaan-Nya di sorga. Bagi-Nyalah kemuliaan sampai selama-lamanya! Amin. (2 Timotius 4:18)",
                "And the Lord will deliver me from every evil work, and will preserve me for his heavenly Kingdom. To him be the glory forever and ever. Amen. (2 Timothy 4:18)"
            )
        ),
        "JHN_3" to listOf(
            BibleVerseItem(
                1,
                "Adalah seorang Farisi yang bernama Nikodemus, seorang pemimpin agama Yahudi. (Yohanes 3:1)",
                "Now there was a man of the Pharisees named Nicodemus, a ruler of the Jews. (John 3:1)"
            ),
            BibleVerseItem(
                3,
                "Yesus menjawab, kata-Nya: \"Aku berkata kepadamu, sesungguhnya jika seorang tidak dilahirkan kembali, ia tidak dapat melihat Kerajaan Allah.\" (Yohanes 3:3)",
                "Jesus answered him, \"Most certainly, I tell you, unless one is born anew, he can't see God's Kingdom.\" (John 3:3)"
            ),
            BibleVerseItem(
                16,
                "Karena begitu besar kasih Allah akan dunia ini, sehingga Ia telah mengaruniakan Anak-Nya yang tunggal, supaya setiap orang yang percaya kepada-Nya tidak binasa, melainkan beroleh hidup yang kekal. (Yohanes 3:16)",
                "For God so loved the world, that he gave his one and only Son, that whoever believes in him should not perish, but have eternal life. (John 3:16)"
            ),
            BibleVerseItem(
                17,
                "Sebab Allah mengutus Anak-Nya ke dalam dunia bukan untuk menghakimi dunia, melainkan untuk menyelamatkannya oleh Dia. (Yohanes 3:17)",
                "For God didn't send his Son into the world to judge the world, but that the world should be saved through him. (John 3:17)"
            ),
            BibleVerseItem(
                30,
                "Ia harus makin besar, tetapi aku harus makin kecil. (Yohanes 3:30)",
                "He must increase, but I must decrease. (John 3:30)"
            )
        ),
        "JHN_1" to listOf(
            BibleVerseItem(
                1,
                "Pada mulanya adalah Firman; Firman itu bersama-sama dengan Allah dan Firman itu adalah Allah. (Yohanes 1:1)",
                "In the beginning was the Word, and the Word was with God, and the Word was God. (John 1:1)"
            ),
            BibleVerseItem(
                4,
                "Dalam Dia ada hidup dan hidup itu adalah terang manusia. (Yohanes 1:4)",
                "In him was life, and the life was the light of men. (John 1:4)"
            ),
            BibleVerseItem(
                5,
                "Terang itu bercahaya di dalam kegelapan dan kegelapan itu tidak menguasainya. (Yohanes 1:5)",
                "The light shines in the darkness, and the darkness hasn't overcome it. (John 1:5)"
            ),
            BibleVerseItem(
                12,
                "Tetapi semua orang yang menerima-Nya diberi-Nya kuasa supaya menjadi anak-anak Allah, yaitu mereka yang percaya dalam nama-Nya. (Yohanes 1:12)",
                "But as many as received him, to them he gave the right to become God's children, to those who believe in his name. (John 1:12)"
            ),
            BibleVerseItem(
                14,
                "Firman itu telah menjadi manusia, dan diam di antara kita, dan kita telah melihat kemuliaan-Nya, yaitu kemuliaan yang diberikan kepada-Nya sebagai Anak Tunggal Bapa, penuh kasih karunia dan kebenaran. (Yohanes 1:14)",
                "The Word became flesh, and lived among us. We saw his glory, such glory as of the one and only Son of the Father, full of grace and truth. (John 1:14)"
            )
        ),
        "JHN_14" to listOf(
            BibleVerseItem(
                1,
                "Janganlah gelisah hatimu; percayalah kepada Allah, percayalah juga kepada-Ku. (Yohanes 14:1)",
                "\"Don't let your heart be troubled. Believe in God. Believe also in me.\" (John 14:1)"
            ),
            BibleVerseItem(
                6,
                "Kata Yesus kepadanya: \"Akulah jalan dan kebenaran dan hidup. Tidak ada seorang pun yang datang kepada Bapa, kalau tidak melalui Aku.\" (Yohanes 14:6)",
                "Jesus said to him, \"I am the way, the truth, and the life. No one comes to the Father, except through me.\" (John 14:6)"
            ),
            BibleVerseItem(
                27,
                "Damai sejahtera Kutinggalkan bagimu. Damai sejahtera-Ku Kuberikan kepadamu, dan apa yang Kuberikan tidak seperti yang diberikan oleh dunia kepadamu. Janganlah gelisah dan gentar hatimu. (Yohanes 14:27)",
                "\"Peace I leave with you. My peace I give to you; not as the world gives, I give to you. Don't let your heart be troubled, neither let it be fearful.\" (John 14:27)"
            )
        ),
        "PSA_23" to listOf(
            BibleVerseItem(
                1,
                "TUHAN adalah gembalaku, takkan kekurangan aku. (Mazmur 23:1)",
                "The LORD is my shepherd: I shall lack nothing. (Psalm 23:1)"
            ),
            BibleVerseItem(
                2,
                "Ia membaringkan aku di padang yang berumput hijau, Ia membimbing aku ke air yang tenang; (Mazmur 23:2)",
                "He makes me lie down in green pastures. He leads me beside still waters. (Psalm 23:2)"
            ),
            BibleVerseItem(
                3,
                "Ia menyegarkan jiwaku. Ia menuntun aku di jalan yang benar oleh karena nama-Nya. (Mazmur 23:3)",
                "He restores my soul. He guides me in the paths of righteousness for his name's sake. (Psalm 23:3)"
            ),
            BibleVerseItem(
                4,
                "Sekalipun aku berjalan dalam lembah kekelaman, aku tidak takut bahaya, sebab Engkau besertaku; gada-Mu dan tongkat-Mu, itulah yang menghibur aku. (Mazmur 23:4)",
                "Even though I walk through the valley of the shadow of death, I will fear no evil, for you are with me. Your rod and your staff, they comfort me. (Psalm 23:4)"
            ),
            BibleVerseItem(
                5,
                "Engkau menyediakan hidangan bagiku, di hadapan lawanku; Engkau mengurapi kepalaku dengan minyak; pialaku penuh melimpah. (Mazmur 23:5)",
                "You prepare a table before me in the presence of my enemies. You anoint my head with oil. My cup runs over. (Psalm 23:5)"
            ),
            BibleVerseItem(
                6,
                "Kebajikan dan kemurahan belaka akan mengikuti aku, seumur hidupku; dan aku akan diam dalam rumah TUHAN sepanjang masa. (Mazmur 23:6)",
                "Surely goodness and loving kindness shall follow me all the days of my life, and I will dwell in the LORD's house forever. (Psalm 23:6)"
            )
        ),
        "PSA_91" to listOf(
            BibleVerseItem(
                1,
                "Orang yang duduk dalam lindungan Yang Mahatinggi dan bermalam dalam naungan Yang Mahakuasa (Mazmur 91:1)",
                "He who dwells in the secret place of the Most High will rest in the shadow of the Almighty. (Psalm 91:1)"
            ),
            BibleVerseItem(
                2,
                "akan berkata kepada TUHAN: \"Tempat perlindunganku dan kubu pertahananku, Allahku, yang kupercayai.\" (Mazmur 91:2)",
                "I will say of the LORD, \"He is my refuge and my fortress; my God, in whom I trust.\" (Psalm 91:2)"
            ),
            BibleVerseItem(
                11,
                "Sebab malaikat-malaikat-Nya akan diperintahkan-Nya kepadamu untuk menjaga engkau di segala jalanmu. (Mazmur 91:11)",
                "For he will put his angels in charge of you, to guard you in all your ways. (Psalm 91:11)"
            )
        ),
        "PRO_3" to listOf(
            BibleVerseItem(
                5,
                "Percayalah kepada TUHAN dengan segenap hatimu, dan janganlah bersandar kepada pengertianmu sendiri. (Amsal 3:5)",
                "Trust in the LORD with all your heart, and don't lean on your own understanding. (Proverbs 3:5)"
            ),
            BibleVerseItem(
                6,
                "Akuilah Dia dalam segala lakumu, maka Ia akan meluruskan jalanmu. (Amsal 3:6)",
                "In all your ways acknowledge him, and he will make your paths straight. (Proverbs 3:6)"
            )
        ),
        "GEN_1" to listOf(
            BibleVerseItem(
                1,
                "Pada mulanya Allah menciptakan langit dan bumi. (Kejadian 1:1)",
                "In the beginning, God created the heavens and the earth. (Genesis 1:1)"
            ),
            BibleVerseItem(
                3,
                "Berfirmanlah Allah: \"Jadilah terang.\" Lalu terang itu jadi. (Kejadian 1:3)",
                "God said, \"Let there be light,\" and there was light. (Genesis 1:3)"
            ),
            BibleVerseItem(
                27,
                "Maka Allah menciptakan manusia itu menurut gambar-Nya, menurut gambar Allah diciptakan-Nya dia; laki-laki dan perempuan diciptakan-Nya mereka. (Kejadian 1:27)",
                "God created man in his own image. In God's image he created him; male and female he created them. (Genesis 1:27)"
            ),
            BibleVerseItem(
                31,
                "Maka Allah melihat segala yang dijadikan-Nya itu, sungguh amat baik. Jadilah petang dan jadilah pagi, itulah hari keenam. (Kejadian 1:31)",
                "God saw everything that he had made, and, behold, it was very good. There was evening and there was morning, a sixth day. (Genesis 1:31)"
            )
        ),
        "MAT_5" to listOf(
            BibleVerseItem(
                3,
                "Berbahagialah orang yang miskin di hadapan Allah, karena merekalah yang empunya Kerajaan Sorga. (Matius 5:3)",
                "\"Blessed are the poor in spirit, for theirs is the Kingdom of Heaven.\" (Matthew 5:3)"
            ),
            BibleVerseItem(
                9,
                "Berbahagialah orang yang membawa damai, karena mereka akan disebut anak-anak Allah. (Matius 5:9)",
                "\"Blessed are the peacemakers, for they shall be called children of God.\" (Matthew 5:9)"
            ),
            BibleVerseItem(
                14,
                "Kamu adalah terang dunia. Kota yang terletak di atas gunung tidak mungkin tersembunyi. (Matius 5:14)",
                "\"You are the light of the world. A city located on a hill can't be hidden.\" (Matthew 5:14)"
            ),
            BibleVerseItem(
                16,
                "Demikianlah hendaknya terangmu bercahaya di depan orang, supaya mereka melihat perbuatanmu yang baik dan memuliakan Bapamu yang di sorga. (Matius 5:16)",
                "\"Even so, let your light shine before men, that they may see your good works and glorify your Father who is in heaven.\" (Matthew 5:16)"
            )
        ),
        "MAT_6" to listOf(
            BibleVerseItem(
                9,
                "Karena itu berdoalah demikian: Bapa kami yang di sorga, Dikuduskanlah nama-Mu, (Matius 6:9)",
                "\"Pray like this: 'Our Father in heaven, may your name be kept holy.'\" (Matthew 6:9)"
            ),
            BibleVerseItem(
                33,
                "Tetapi carilah dahulu Kerajaan Allah dan kebenarannya, maka semuanya itu akan ditambahkan kepadamu. (Matius 6:33)",
                "\"But seek first God's Kingdom and his righteousness; and all these things will be given to you as well.\" (Matthew 6:33)"
            )
        ),
        "ROM_8" to listOf(
            BibleVerseItem(
                1,
                "Demikianlah sekarang tidak ada penghukuman bagi mereka yang ada di dalam Kristus Yesus. (Roma 8:1)",
                "There is therefore now no condemnation to those who are in Christ Jesus. (Romans 8:1)"
            ),
            BibleVerseItem(
                28,
                "Kita tahu sekarang, bahwa Allah turut bekerja dalam segala sesuatu untuk mendatangkan kebaikan bagi mereka yang mengasihi Dia, yaitu bagi mereka yang terpanggil sesuai dengan rencana Allah. (Roma 8:28)",
                "We know that all things work together for good for those who love God, for those who are called according to his purpose. (Romans 8:28)"
            ),
            BibleVerseItem(
                31,
                "Sebab itu apakah yang akan kita katakan tentang semuanya itu? Jika Allah di pihak kita, siapakah yang akan melawan kita? (Roma 8:31)",
                "What then shall we say about these things? If God is for us, who can be against us? (Romans 8:31)"
            ),
            BibleVerseItem(
                38,
                "Sebab aku yakin, bahwa baik maut, maupun hidup, baik malaikat-malaikat, maupun pemerintah-pemerintah, baik yang ada sekarang, maupun yang akan datang, (Roma 8:38)",
                "For I am persuaded that neither death, nor life, nor angels, nor principalities, nor things present, nor things to come, (Romans 8:38)"
            ),
            BibleVerseItem(
                39,
                "atau kuasa-kuasa, baik yang di atas, maupun yang di bawah, ataupun sesuatu makhluk lain, tidak akan dapat memisahkan kita dari kasih Allah, yang ada dalam Kristus Yesus, Tuhan kita. (Roma 8:39)",
                "nor powers, nor height, nor depth, nor any other created thing will be able to separate us from God's love which is in Christ Jesus our Lord. (Romans 8:39)"
            )
        ),
        "1CO_13" to listOf(
            BibleVerseItem(
                4,
                "Kasih itu sabar; kasih itu murah hati; ia tidak cemburu. Ia tidak memegahkan diri dan tidak sombong. (1 Korintus 13:4)",
                "Love is patient and is kind. Love doesn't envy. Love doesn't brag, is not proud, (1 Corinthians 13:4)"
            ),
            BibleVerseItem(
                7,
                "Ia menutupi segala sesuatu, percaya segala sesuatu, mengharapkan segala sesuatu, sabar menanggung segala sesuatu. (1 Korintus 13:7)",
                "bears all things, believes all things, hopes all things, and endures all things. (1 Corinthians 13:7)"
            ),
            BibleVerseItem(
                13,
                "Demikianlah tinggal ketiga hal ini, yaitu iman, pengharapan dan kasih, dan yang paling besar di antaranya ialah kasih. (1 Korintus 13:13)",
                "But now faith, hope, and love remain—these three. The greatest of these is love. (1 Corinthians 13:13)"
            )
        ),
        "PHP_4" to listOf(
            BibleVerseItem(
                4,
                "Bersukacitalah senantiasa dalam Tuhan! Sekali lagi kukatakan: Bersukacitalah! (Filipi 4:4)",
                "Rejoice in the Lord always! Again I will say, \"Rejoice!\" (Philippians 4:4)"
            ),
            BibleVerseItem(
                6,
                "Janganlah hendaknya kamu kuatir tentang apa pun juga, tetapi nyatakanlah dalam segala hal keinginanmu kepada Allah dalam doa dan permohonan dengan ucapan syukur. (Filipi 4:6)",
                "In nothing be anxious, but in everything, by prayer and petition with thanksgiving, let your requests be made known to God. (Philippians 4:6)"
            ),
            BibleVerseItem(
                7,
                "Damai sejahtera Allah, yang melampaui segala akal, akan memelihara hati dan pikiranmu dalam Kristus Yesus. (Filipi 4:7)",
                "And the peace of God, which surpasses all understanding, will guard your hearts and your thoughts in Christ Jesus. (Philippians 4:7)"
            ),
            BibleVerseItem(
                13,
                "Segala perkara dapat kutanggung di dalam Dia yang memberi kekuatan kepadaku. (Filipi 4:13)",
                "I can do all things through Christ, who strengthens me. (Philippians 4:13)"
            )
        ),
        "ISA_41" to listOf(
            BibleVerseItem(
                10,
                "Janganlah takut, sebab Aku menyertai engkau, janganlah bimbang, sebab Aku ini Allahmu; Aku akan meneguhkan, bahkan akan menolong engkau; Aku akan memegang engkau dengan tangan kanan-Ku yang membawa kemenangan. (Yesaya 41:10)",
                "Don't you be afraid, for I am with you. Don't be dismayed, for I am your God. I will strengthen you. Yes, I will help you. Yes, I will uphold you with the right hand of my righteousness. (Isaiah 41:10)"
            )
        ),
        "GAL_5" to listOf(
            BibleVerseItem(
                22,
                "Tetapi buah Roh ialah: kasih, sukacita, damai sejahtera, kesabaran, kemurahan, kebaikan, kesetiaan, (Galatia 5:22)",
                "But the fruit of the Spirit is love, joy, peace, patience, kindness, goodness, faith, (Galatians 5:22)"
            ),
            BibleVerseItem(
                23,
                "kelemahlembutan, penguasaan diri. Tidak ada hukum yang menentang hal-hal itu. (Galatia 5:23)",
                "gentleness, and self-control. Against such things there is no law. (Galatians 5:23)"
            )
        ),
        "REV_21" to listOf(
            BibleVerseItem(
                4,
                "Dan Ia akan menghapus segala air mata dari mata mereka, dan maut tidak akan ada lagi; tidak akan ada lagi perkabungan, atau ratap tangis, atau dukacita, sebab segala sesuatu yang lama itu telah berlalu. (Wahyu 21:4)",
                "He will wipe away every tear from their eyes. Death will be no more; neither will there be mourning, nor crying, nor pain, any more. The first things have passed away. (Revelation 21:4)"
            )
        )
    )

    val DEVOTIONALS: List<BibleDevotionalItem> = listOf(
        BibleDevotionalItem(
            id = "dev_peace",
            titleId = "Damai Sejahtera yang Menjaga Hati",
            titleEn = "The Peace That Guards Our Hearts",
            themeId = "Pengharapan & Ketenangan",
            themeEn = "Hope & Serenity",
            bookId = "PHP",
            chapter = 4,
            verse = 7,
            keyVerseId = "Filipi 4:6–7 — \"Janganlah hendaknya kamu kuatir tentang apa pun juga... Damai sejahtera Allah akan memelihara hati dan pikiranmu.\"",
            keyVerseEn = "Philippians 4:6–7 — \"In nothing be anxious... And the peace of God will guard your hearts and minds.\"",
            reflectionId = "Dalam mendampingi anak-anak asrama setiap hari, kekhawatiran sering muncul saat menghadapi tugas mendadak atau anak yang sedang berjuang dengan kesehatan maupun emosinya. Firman Tuhan mengajak kita mengubah kekhawatiran menjadi doa dengan ucapan syukur. Damai sejahtera Allah bukan sekadar perasaan sesaat, melainkan penjaga yang menjaga hati tetap teduh.",
            reflectionEn = "While caring for boarding students day by day, anxiety easily rises around urgent tasks or students struggling with health and emotions. Scripture invites us to turn worry into prayerful thanksgiving. God's peace acts as a steady sentinel over our hearts and minds.",
            prayerId = "Tuhan Yesus, ajari kami menyerahkan segala beban pelayanan hari ini ke dalam tangan-Mu. Penuhi hati kami dengan damai-Mu agar kami dapat mengasihi anak-anak dengan sabar. Amin.",
            prayerEn = "Lord Jesus, teach us to place today's burdens in Your hands. Fill our hearts with Your peace so we may nurture students with patience and grace. Amen."
        ),
        BibleDevotionalItem(
            id = "dev_shepherd",
            titleId = "Tuhan Adalah Gembala yang Setia",
            titleEn = "The Lord Is My Faithful Shepherd",
            themeId = "Pemeliharaan Tuhan",
            themeEn = "God's Provision",
            bookId = "PSA",
            chapter = 23,
            verse = 1,
            keyVerseId = "Mazmur 23:1 — \"TUHAN adalah gembalaku, takkan kekurangan aku.\"",
            keyVerseEn = "Psalm 23:1 — \"The LORD is my shepherd: I shall lack nothing.\"",
            reflectionId = "Gembala yang baik mengenal setiap dombanya secara pribadi. Demikian pula Allah mengenal kebutuhan kita, kelelahan kita, dan doa-doa sunyi yang kita panjatkan untuk masa depan anak asuh. Ketika kita berjalan bersama Sang Gembala, langkah kita dituntun pada jalan kebenaran dan penyegaran jiwa.",
            reflectionEn = "A good shepherd knows each sheep personally. Likewise, God knows our fatigue, our needs, and the quiet prayers we lift up for every student. Walking with the Shepherd restores our soul.",
            prayerId = "Bapa di surga, terima kasih karena Engkau tidak pernah meninggalkan kami. Bimbing kami menjadi pengasuh yang mencerminkan kasih Gembala yang Agung. Amin.",
            prayerEn = "Heavenly Father, thank You for never leaving us alone. Guide us to reflect the tenderness of the Good Shepherd in our daily care. Amen."
        ),
        BibleDevotionalItem(
            id = "dev_strength",
            titleId = "Jangan Takut, Aku Menyertai Engkau",
            titleEn = "Fear Not, For I Am With You",
            themeId = "Keberanian & Kekuatan",
            themeEn = "Courage & Strength",
            bookId = "ISA",
            chapter = 41,
            verse = 10,
            keyVerseId = "Yesaya 41:10 — \"Janganlah takut, sebab Aku menyertai engkau... Aku akan meneguhkan, bahkan akan menolong engkau.\"",
            keyVerseEn = "Isaiah 41:10 — \"Don't you be afraid, for I am with you... I will strengthen you and help you.\"",
            reflectionId = "Keberanian sejati bukan berarti tidak pernah merasa lelah atau gentar, melainkan keyakinan bahwa tangan Tuhan sendiri yang menopang kita. Saat terasa berat, ingatlah janji penyertaan-Nya tidak pernah bergantung pada situasi, melainkan pada kesetiaan-Nya yang kekal.",
            reflectionEn = "True courage is not the absence of weariness, but the confidence that God's righteous hand upholds us. His promise does not change with circumstances; it rests on His unchanging faithfulness.",
            prayerId = "Tuhan Yang Mahakuasa, teguhkan hati kami saat menghadapi tantangan baru hari ini dan jadikan hidup kami kesaksian kekuatan-Mu. Amin.",
            prayerEn = "Almighty God, strengthen our hearts as we face today's responsibilities and make our lives a witness to Your faithfulness. Amen."
        ),
        BibleDevotionalItem(
            id = "dev_love",
            titleId = "Kasih yang Sabar dan Murah Hati",
            titleEn = "Love That Is Patient and Kind",
            themeId = "Karakter & Kasih",
            themeEn = "Character & Love",
            bookId = "1CO",
            chapter = 13,
            verse = 4,
            keyVerseId = "1 Korintus 13:4 — \"Kasih itu sabar; kasih itu murah hati; ia tidak cemburu.\"",
            keyVerseEn = "1 Corinthians 13:4 — \"Love is patient and is kind. Love doesn't envy.\"",
            reflectionId = "Mendidik generasi muda membutuhkan kasih yang melampaui kata-kata: kesabaran saat mengulang nasihat, kemurahan hati saat memaafkan kesalahan, dan ketulusan saat mendoakan pertumbuhan mereka. Kasih seperti ini lahir dari kasih Kristus yang lebih dahulu kita terima.",
            reflectionEn = "Mentoring young people calls for love beyond words: patience when repeating guidance, kindness when forgiving mistakes, and sincerity in prayer. Such love flows from Christ's love for us first.",
            prayerId = "Tuhan, alirkan kasih-Mu dalam perkataan dan tindakan kami agar setiap anak merasakan kehangatan keluarga di dalam Kristus. Amin.",
            prayerEn = "Lord, pour Your love into our words and deeds so every student feels cared for and valued in Christ. Amen."
        ),
        BibleDevotionalItem(
            id = "dev_timothy",
            titleId = "Setia Sampai Garis Akhir",
            titleEn = "Faithful to the Finish Line",
            themeId = "Ketekunan Pelayanan",
            themeEn = "Endurance in Service",
            bookId = "2TI",
            chapter = 4,
            verse = 7,
            keyVerseId = "2 Timotius 4:7 — \"Aku telah mengakhiri pertandingan yang baik, aku telah mencapai garis akhir dan aku telah memelihara iman.\"",
            keyVerseEn = "2 Timothy 4:7 — \"I have fought the good fight. I have finished the course. I have kept the faith.\"",
            reflectionId = "Kesetiaan dalam hal-hal kecil setiap hari adalah fondasi dari pelayanan yang berdampak besar. Paulus menulis kepada Timotius agar tetap teguh, menguasai diri, dan tidak mudah goyah. Setiap kebaikan kecil yang dilakukan dengan iman sangat berharga di hadapan Allah.",
            reflectionEn = "Faithfulness in small daily duties builds lasting fruit. Paul encouraged Timothy to remain steady, self-controlled, and unshaken. Every quiet act of care done in faith matters deeply to God.",
            prayerId = "Tuhan, beri kami ketekunan untuk setia menjalankan panggilan kami sampai akhir dengan penuh sukacita. Amin.",
            prayerEn = "Lord, grant us perseverance to fulfill our calling faithfully and joyfully to the end. Amen."
        ),
        BibleDevotionalItem(
            id = "dev_wisdom",
            titleId = "Bersandar Penuh kepada Tuhan",
            titleEn = "Trusting the Lord With All Your Heart",
            themeId = "Hikmat & Tuntunan",
            themeEn = "Wisdom & Guidance",
            bookId = "PRO",
            chapter = 3,
            verse = 5,
            keyVerseId = "Amsal 3:5–6 — \"Percayalah kepada TUHAN dengan segenap hatimu, dan janganlah bersandar kepada pengertianmu sendiri.\"",
            keyVerseEn = "Proverbs 3:5–6 — \"Trust in the LORD with all your heart, and don't lean on your own understanding.\"",
            reflectionId = "Ketika mengambil keputusan penting, kita sering mengandalkan hitungan manusia semata. Hikmat sejati dimulai saat kita mengundang Tuhan dalam setiap rencana. Saat kita mengakui Dia dalam segala laku, Ia berjanji meluruskan jalan di depan kita.",
            reflectionEn = "When facing tough decisions, we often rely solely on human calculation. True wisdom starts when we invite God into every plan and trust Him to make our paths straight.",
            prayerId = "Bapa Yang Bijaksana, tuntun setiap keputusan kami agar selaras dengan kehendak-Mu dan membawa kebaikan bagi banyak orang. Amin.",
            prayerEn = "Wise Father, guide our decisions so they align with Your will and bring blessing to those we serve. Amen."
        )
    )

    val STORIES: List<BibleStoryItem> = listOf(
        BibleStoryItem(
            id = "story_creation",
            titleId = "Penciptaan Langit, Bumi, dan Manusia",
            titleEn = "The Creation of Heaven, Earth, and Humanity",
            eraId = "Permulaan Dunia • Taurat",
            eraEn = "The Beginning • Torah",
            bookId = "GEN",
            chapter = 1,
            summaryId = "Pada mulanya Allah menciptakan langit dan bumi dalam enam hari penuh keteraturan dan keindahan: terang, cakrawala, daratan dan lautan, benda-benda penerang, makhluk laut dan burung, hingga manusia yang diciptakan segambar dan serupa dengan Allah sendiri.",
            summaryEn = "In the beginning God spoke light, sky, seas, dry land, stars, living creatures, and finally humanity into existence—forming people in His own image and declaring all creation very good.",
            lessonId = "Setiap siswa dan manusia diciptakan berharga menurut gambar Allah dengan tujuan dan martabat yang mulia.",
            lessonEn = "Every student and person is wonderfully crafted in God's image with purpose and dignity."
        ),
        BibleStoryItem(
            id = "story_shepherd_david",
            titleId = "Daud Gembala Muda dan Mazmur Kepercayaan",
            titleEn = "David the Shepherd and the Psalm of Trust",
            eraId = "Kerajaan Israel • Puisi & Hikmat",
            eraEn = "Kingdom of Israel • Poetry & Wisdom",
            bookId = "PSA",
            chapter = 23,
            summaryId = "Sejak muda Daud menggembalakan domba di padang belantara Yudea. Pengalaman menjaga domba dari bahaya dan menuntun mereka ke air yang tenang menginspirasi Mazmur 23, nyanyian iman bahwa TUHAN sendiri adalah Gembala hidupnya.",
            summaryEn = "From his youth tending sheep in the Judean hills, David learned firsthand what gentle protection looks like—inspiring Psalm 23, his timeless song of trust in God as Shepherd.",
            lessonId = "Pengalaman menjaga dan membimbing yang sederhana bisa menjadi pujian terindah di hadapan Tuhan.",
            lessonEn = "Faithful daily care in humble places becomes a lifelong song of praise to God."
        ),
        BibleStoryItem(
            id = "story_sermon_mount",
            titleId = "Khotbah di Bukit: Menjadi Garam dan Terang",
            titleEn = "Sermon on the Mount: Salt and Light",
            eraId = "Pelayanan Yesus • Injil",
            eraEn = "Ministry of Jesus • Gospels",
            bookId = "MAT",
            chapter = 5,
            summaryId = "Di atas sebuah bukit dekat Danau Galilea, Yesus mengajar orang banyak tentang Ucapan Bahagia—kerendahan hati, belas kasih, kemurnian hati, dan pembawa damai—serta memanggil pengikut-Nya menjadi terang dunia.",
            summaryEn = "On a hillside overlooking Galilee, Jesus taught the Beatitudes—honoring humility, mercy, purity of heart, and peacemaking—calling His followers to shine as the light of the world.",
            lessonId = "Perbuatan baik yang tulus dan membawa damai di lingkungan asrama memuliakan Bapa di sorga.",
            lessonEn = "Sincere kindness and peacemaking in daily life shine God's light to everyone around us."
        ),
        BibleStoryItem(
            id = "story_nicodemus",
            titleId = "Percakapan Malam Yesus dengan Nikodemus",
            titleEn = "Jesus' Night Conversation With Nicodemus",
            eraId = "Pelayanan Yesus • Injil",
            eraEn = "Ministry of Jesus • Gospels",
            bookId = "JHN",
            chapter = 3,
            summaryId = "Nikodemus, seorang pemuka agama, datang menemui Yesus pada waktu malam untuk bertanya tentang Kerajaan Allah. Dalam percakapan hangat itulah Yesus mengucapkan Yohanes 3:16 tentang kasih Allah yang menyelamatkan dunia.",
            summaryEn = "Nicodemus, a respected leader, visited Jesus at night seeking truth about God's Kingdom. In that quiet conversation, Jesus revealed John 3:16—God's boundless love for the world.",
            lessonId = "Pertanyaan yang jujur dan kerinduan mencari kebenaran selalu disambut Yesus dengan kasih.",
            lessonEn = "Honest questions and a sincere search for truth are always met by Jesus with grace."
        ),
        BibleStoryItem(
            id = "story_paulus_timothy",
            titleId = "Nasihat Terakhir Paulus kepada Timotius",
            titleEn = "Paul's Final Encouragement to Timothy",
            eraId = "Gereja Mula-mula • Surat Paulus",
            eraEn = "Early Church • Pauline Epistles",
            bookId = "2TI",
            chapter = 4,
            summaryId = "Menjelang akhir pelayanannya, Rasul Paulus menulis surat kedua kepada murid rohaninya Timotius. Ia mendorong pemimpin muda itu agar tidak takut, setia mengajar firman dengan kesabaran, dan menjaga iman sampai garis akhir.",
            summaryEn = "Near the close of his earthly ministry, the Apostle Paul wrote a deeply personal letter to his young mentee Timothy, urging him to remain steady, teach with patience, and finish the race faithfully.",
            lessonId = "Pendampingan antargenerasi yang penuh doa melahirkan pemimpin muda yang tangguh dalam iman.",
            lessonEn = "Prayerful mentorship across generations raises resilient, faithful young leaders."
        ),
        BibleStoryItem(
            id = "story_new_jerusalem",
            titleId = "Langit yang Baru dan Bumi yang Baru",
            titleEn = "A New Heaven and a New Earth",
            eraId = "Pengharapan Kekal • Nubuat",
            eraEn = "Eternal Hope • Prophecy",
            bookId = "REV",
            chapter = 21,
            summaryId = "Dalam penglihatan di Pulau Patmos, Rasul Yohanes menyaksikan janji pemulihan sempurna: Allah sendiri diam bersama umat-Nya, menghapus setiap air mata, dukacita, dan kesakitan untuk selama-lamanya.",
            summaryEn = "In his vision on Patmos, John beheld the promise of ultimate restoration: God dwelling directly with His people and wiping away every tear, sorrow, and pain forever.",
            lessonId = "Pengharapan akan masa depan kekal memberi kekuatan untuk terus berbuat kasih hari ini.",
            lessonEn = "Our eternal hope in Christ gives strength and joy for loving service today."
        )
    )
}
