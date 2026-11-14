package com.example.util

import com.example.data.model.ChildStudent
import com.example.data.model.WaliAccount
import java.math.BigDecimal
import java.nio.charset.Charset
import java.util.Calendar

object StudentCsvParser {

    const val STANDARD_HEADER =
        "No,Nama,Kelas,Jenjang,Alamat,Jenis Kelamin,NIK,NO KK,TEMPAT LAHIR,TANGGAL LAHIR,NAMA IBU,NISN,KECAMATAN"

    val STANDARD_TEMPLATE_CSV: String = """
        $STANDARD_HEADER
        1,ADAM JULIAN SHANO,SD 1-2,SD,"Dsn. Manukan Rt 02 Rw 08 Ds. Jabon, Kec. Banyakan, Kab. Kediri",L,3508220907180002,3506220301180009,KEDIRI,09-07-2018,RUMIYATI,3185428544,Banyakan
        2,AZZURA FAUZIAH,SD 1-2,SD,"Karangtalun, Kec. Kras, Kab. Kediri",P,1771057004190001,3173010601096079,BENGKULU,30-04-2019,AULIA HANIFAH,3196328403,Kras
        3,AHMAD SYAIFUL ARSYAD,VII-1,SMP,"Pagung, Kec. Semen, Kab. Kediri",L,3506012001140002,3506013107120001,KEDIRI,20-01-2014,SITI MUNJIAH,0141204471,Semen
        4,ADELIA SUKMAWATI,XI-1 (IPA),SMA,"Dusun Gambiran, Desa Kedak, Kec. Semen, Kab. Kediri",P,3506015105090001,3506010412103889,KEDIRI,11-05-2009,ARI SATUJU,0099412567,Semen
    """.trimIndent()

    /**
     * Dekode byte CSV secara aman: prioritaskan UTF-8, lalu fallback Windows-1252 jika ditemukan
     * karakter rusak (\uFFFD) dari file ekspor Excel lama.
     */
    fun decodeCsvBytes(bytes: ByteArray): String {
        val utf8 = bytes.toString(Charsets.UTF_8)
        if (!utf8.contains('\uFFFD')) {
            return utf8
        }
        return try {
            bytes.toString(Charset.forName("windows-1252"))
        } catch (_: Exception) {
            bytes.toString(Charsets.ISO_8859_1)
        }
    }

    /**
     * Membersihkan BOM, karakter zero-width tersembunyi (\u200B-\u200F, \u2060),
     * karakter kontrol rusak (C1 \u0080-\u009F yang muncul sebagai ▯▯), mojibake UTF-8/Latin1,
     * serta spasi dan tanda petik luar.
     */
    fun cleanText(raw: String?): String {
        if (raw == null) return ""
        return raw
            .replace("\uFEFF", "")
            .replace("ï»¿", "")
            .replace(Regex("[\\u200B-\\u200F\\u202A-\\u202E\\u2060-\\u206F\\u00AD]"), "")
            .replace("\u00A0", " ")
            .replace(Regex("â[\\u0080-\\u009F€]+[^0-9A-Za-z\\s,;\"-]?"), "")
            .replace("â€Œ", "")
            .replace("â€‹", "")
            .replace("â€Ž", "")
            .replace("â€", "")
            .replace(Regex("[\\u0080-\\u009F]"), "")
            .trim()
            .removeSurrounding("\"")
            .trim()
    }

    /**
     * Membersihkan NIK & No. KK menjadi angka murni (0-9).
     * Mengonversi notasi ilmiah Excel (misal 3.5062E+15) terlebih dahulu bila ada,
     * lalu membuang seluruh karakter non-angka (mojibake â▯▯, petik, spasi, huruf, simbol)
     * tanpa mengubah digit awal asli (misal 350, 357, 177, 927, atau awalan daerah lain).
     */
    fun cleanDigitsOnly(raw: String?): String {
        val cleaned = cleanText(raw)
            .replace("'", "")
            .replace("`", "")
            .replace(" ", "")
        if (cleaned.isBlank()) return ""

        val sciCandidate = cleaned.replace(',', '.')
        if (sciCandidate.contains("E+", ignoreCase = true) || sciCandidate.contains("E-", ignoreCase = true)) {
            val numericSci = sciCandidate.replace(Regex("[^0-9eE+.-]"), "")
            try {
                return BigDecimal(numericSci).toBigInteger().toString().filter { it.isDigit() }
            } catch (_: Exception) {
                // fallback ambil angka murni
            }
        }
        return cleaned.filter { it.isDigit() }
    }

    /**
     * Membersihkan angka identitas (NIK, NO KK, NISN) dari karakter tak terlihat, tanda petik Excel ('),
     * mojibake, spasi di tengah angka, serta mengonversi notasi eksponensial Excel (misal 3.5062E+15).
     * Untuk NIS fallback bawaan sistem yang berformat kode sekolah (misal SRT1-KDR-1), formatnya tetap dijaga.
     */
    fun cleanNumericId(raw: String?): String {
        val cleaned = cleanText(raw)
            .replace("'", "")
            .replace("`", "")
            .replace(" ", "")
        if (cleaned.isBlank()) return ""

        val sciCandidate = cleaned.replace(',', '.')
        if (sciCandidate.contains("E+", ignoreCase = true) || sciCandidate.contains("E-", ignoreCase = true)) {
            val numericSci = sciCandidate.replace(Regex("[^0-9eE+.-]"), "")
            try {
                return BigDecimal(numericSci).toBigInteger().toString().filter { it.isDigit() }
            } catch (_: Exception) {
                // fallback ke logika di bawah
            }
        }

        if (cleaned.matches(Regex("^[A-Za-z][A-Za-z0-9]*-[A-Za-z0-9-]+$"))) {
            return cleaned
        }
        return cleaned.filter { it.isDigit() }
    }

    /**
     * Deteksi delimiter utama CSV (koma ',' atau titik koma ';' untuk locale Excel Indonesia).
     */
    fun detectDelimiter(sampleLines: List<String>): Char {
        var commaScore = 0
        var semicolonScore = 0
        sampleLines.take(15).forEach { line ->
            var inQuotes = false
            line.forEach { ch ->
                when (ch) {
                    '"' -> inQuotes = !inQuotes
                    ',' -> if (!inQuotes) commaScore++
                    ';' -> if (!inQuotes) semicolonScore++
                }
            }
        }
        return if (semicolonScore > commaScore) ';' else ','
    }

    /**
     * Memecah satu baris CSV dengan aturan RFC-4180 (aman terhadap delimiter di dalam tanda petik).
     */
    fun parseCsvLine(line: String, delimiter: Char = ','): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(cleanText(sb.toString()))
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(cleanText(sb.toString()))
        return tokens
    }

    /**
     * Menghitung usia siswa secara akurat dari string tanggal lahir (dd-MM-yyyy, dd/MM/yyyy, yyyy-MM-dd).
     */
    fun estimateAge(birthDate: String): Int {
        val cleaned = cleanText(birthDate)
        if (cleaned.isBlank()) return 12

        val cal = Calendar.getInstance()
        val currentYear = maxOf(cal.get(Calendar.YEAR), 2026)
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)

        val parts = Regex("\\d+").findAll(cleaned).mapNotNull { it.value.toIntOrNull() }.toList()
        if (parts.size >= 3) {
            val (day, month, year) = when {
                parts[0] in 1900..2100 -> Triple(parts[2], parts[1], parts[0]) // yyyy-MM-dd
                parts[2] in 1900..2100 -> Triple(parts[0], parts[1], parts[2]) // dd-MM-yyyy
                else -> Triple(0, 0, 0)
            }
            if (year in 1995..2025 && month in 1..12 && day in 1..31) {
                var age = currentYear - year
                if (currentMonth < month || (currentMonth == month && currentDay < day)) {
                    age--
                }
                if (age in 5..25) return age
            }
        }

        val yearMatch = Regex("\\b(19\\d\\d|20\\d\\d)\\b").find(cleaned)
        val birthYear = yearMatch?.value?.toIntOrNull()
        if (birthYear != null) {
            val calculated = currentYear - birthYear
            if (calculated in 5..25) return calculated
        }
        return 12
    }

    fun deriveJenjang(classGrade: String): String {
        val upper = classGrade.uppercase()
        return when {
            upper.contains("SD") || upper.contains("MI") -> "SD"
            upper.contains("VII") || upper.contains("VIII") || upper.contains("IX") || upper.contains("SMP") || upper.contains("MTS") -> "SMP"
            upper.contains("X") || upper.contains("XI") || upper.contains("XII") || upper.contains("SMA") || upper.contains("SMK") || upper.contains("MA") -> "SMA"
            else -> "SMP"
        }
    }

    private fun scoreHeaderRow(tokens: List<String>): Int {
        var score = 0
        var hasStrongColumn = false
        tokens.forEach { raw ->
            val col = cleanText(raw).lowercase().replace("_", " ").replace("-", " ").trim()
            if (col.isBlank()) return@forEach
            val matched = when {
                col == "no" || col == "no." || col == "nomor" -> true
                col == "nama" || col.contains("nama lengkap") || col.contains("nama siswa") || col.contains("nama anak") -> {
                    hasStrongColumn = true
                    true
                }
                col == "kelas" || col.contains("kelas") || col.contains("rombel") -> true
                col.contains("jenjang") -> true
                col.contains("alamat") || col.contains("domisili") -> true
                col == "kecamatan" || col == "kec" || col == "kec." || col.contains("kecamatan") || col == "district" -> true
                col.contains("kelamin") || col.contains("gender") || col == "jk" || col == "l/p" -> true
                col == "nik" || col.contains("no nik") || col.contains("nomor nik") -> {
                    hasStrongColumn = true
                    true
                }
                col == "no kk" || col == "kk" || col.contains("no kk") || col.contains("no. kk") || col.contains("kartu keluarga") -> true
                col.contains("tempat lahir") || col.contains("tmp lahir") -> true
                col.contains("tanggal lahir") || col.contains("tgl lahir") -> true
                col.contains("nama ibu") || col == "ibu" -> true
                col == "nisn" || col == "nis" || col.contains("nisn") || col.contains("no induk") -> {
                    hasStrongColumn = true
                    true
                }
                col.contains("kamar") || col.contains("asrama") -> true
                else -> false
            }
            if (matched) score++
        }
        return if (hasStrongColumn && score >= 3) score else 0
    }

    /**
     * Mengekstrak nama kecamatan dari teks alamat (mis. "Kec. Banyakan, Kab. Kediri" -> "Banyakan")
     * untuk membantu pengisian otomatis bila diminta oleh Wali Asuh.
     */
    fun extractDistrictFromAddress(address: String): String {
        val cleaned = cleanText(address)
        if (cleaned.isBlank()) return ""
        val match = Regex("(?i)\\b(?:kecamatan|kec)\\.?\\s+([A-Za-z0-9\\s'-]{2,35}?)(?=,|\\b(?:kab|kabupaten|kota|prov|jawa|jatim)\\b|$)")
            .find(cleaned)
        return match?.groupValues?.getOrNull(1)?.trim().orEmpty()
    }

    /**
     * Mem-parsing teks CSV siswa dari format standar 12 kolom (termasuk banner judul di baris atas
     * maupun baris pemisah kosong seperti ekspor Excel SRT 1 Kediri), serta tetap kompatibel dengan
     * format 10 kolom lama atau urutan kolom kustom yang memiliki header.
     */
    fun parseCsv(text: String, currentSchoolId: String = "SRT1-KDR"): List<ChildStudent> {
        val normalized = text
            .replace("\r\n", "\n")
            .replace('\r', '\n')
        val rawLines = normalized.lines()
            .map { cleanText(it) }
            .filter { it.isNotBlank() }
        if (rawLines.isEmpty()) return emptyList()

        val delimiter = detectDelimiter(rawLines)

        // Filter baris yang seluruh kolomnya kosong (misal: ",,,,,,,,,,,")
        val tokenizedLines = rawLines
            .map { parseCsvLine(it, delimiter) }
            .filter { tokens -> tokens.any { it.isNotBlank() } }
        if (tokenizedLines.isEmpty()) return emptyList()

        // Cari baris header terbaik pada 15 baris non-kosong pertama (melewati baris judul/banner sekolah)
        var headerRowIndex = -1
        var bestScore = 0
        val searchLimit = minOf(tokenizedLines.size, 15)
        for (idx in 0 until searchLimit) {
            val s = scoreHeaderRow(tokenizedLines[idx])
            if (s > bestScore) {
                bestScore = s
                headerRowIndex = idx
            }
        }

        var idxNo = -1
        var idxName = -1
        var idxClass = -1
        var idxJenjang = -1
        var idxAddress = -1
        var idxDistrict = -1
        var idxGender = -1
        var idxNik = -1
        var idxNoKk = -1
        var idxBirthPlace = -1
        var idxBirthDate = -1
        var idxMother = -1
        var idxNis = -1
        var idxRoom = -1
        var idxDorm = -1
        var idxBlood = -1
        var idxAllergy = -1
        var idxContact = -1
        var idxNotes = -1

        if (headerRowIndex != -1) {
            val headerTokens = tokenizedLines[headerRowIndex]
            headerTokens.forEachIndexed { idx, rawToken ->
                val col = cleanText(rawToken).lowercase()
                    .replace("_", " ")
                    .replace("-", " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                when {
                    col == "no" || col == "no." || col == "nomor" || col == "urut" ->
                        if (idxNo == -1) idxNo = idx
                    col.contains("ibu") || col.contains("orang tua") ->
                        if (idxMother == -1) idxMother = idx
                    col.contains("nama lengkap") || col.contains("nama siswa") || col.contains("nama anak") || col == "nama" ->
                        if (idxName == -1) idxName = idx
                    col.contains("kelas") || col.contains("rombel") || col.contains("tingkat") ->
                        if (idxClass == -1) idxClass = idx
                    col.contains("jenjang") ->
                        if (idxJenjang == -1) idxJenjang = idx
                    col == "kecamatan" || col == "kec" || col == "kec." || col.contains("kecamatan") || col == "district" ->
                        if (idxDistrict == -1) idxDistrict = idx
                    col.contains("alamat") || col.contains("domisili") || col.contains("asal") ->
                        if (idxAddress == -1) idxAddress = idx
                    col.contains("kelamin") || col.contains("gender") || col == "jk" || col == "l/p" || col == "lp" || col == "sex" ->
                        if (idxGender == -1) idxGender = idx
                    col == "nik" || col.contains("no nik") || col.contains("no. nik") || col.contains("nomor nik") ->
                        if (idxNik == -1) idxNik = idx
                    col == "no kk" || col == "kk" || col.contains("no kk") || col.contains("no. kk") || col.contains("kartu keluarga") ->
                        if (idxNoKk == -1) idxNoKk = idx
                    col.contains("tempat lahir") || col.contains("tmp lahir") || col == "tempat" ->
                        if (idxBirthPlace == -1) idxBirthPlace = idx
                    col.contains("tanggal lahir") || col.contains("tgl lahir") || col == "tanggal" || col == "tgl" ->
                        if (idxBirthDate == -1) idxBirthDate = idx
                    col.contains("nisn") || col == "nis" || col.contains("no induk") || col.contains("nomor induk") ->
                        if (idxNis == -1) idxNis = idx
                    col.contains("kamar") || col.contains("ruang") ->
                        if (idxRoom == -1) idxRoom = idx
                    col.contains("asrama") || col.contains("dorm") ->
                        if (idxDorm == -1) idxDorm = idx
                    col.contains("darah") || col == "gol" ->
                        if (idxBlood == -1) idxBlood = idx
                    col.contains("alergi") ->
                        if (idxAllergy == -1) idxAllergy = idx
                    col.contains("kontak") || col.contains("telp") || col.contains("hp") || col.contains("wa") ->
                        if (idxContact == -1) idxContact = idx
                    col.contains("catatan") || col.contains("keterangan") ->
                        if (idxNotes == -1) idxNotes = idx
                }
            }
        } else {
            // Tidak ada baris header: tentukan posisi kolom berdasarkan struktur standar 12-13 kolom vs 10 kolom lama
            val sampleRow = tokenizedLines.firstOrNull { it.size >= 6 } ?: tokenizedLines.first()
            if (sampleRow.size >= 12) {
                // Format Standar Lengkap 12-13 Kolom:
                // No, Nama, Kelas, Jenjang, Alamat, JK, NIK, NO KK, Tempat Lahir, Tanggal Lahir, Nama Ibu, NISN, [KECAMATAN]
                idxNo = 0
                idxName = 1
                idxClass = 2
                idxJenjang = 3
                idxAddress = 4
                idxGender = 5
                idxNik = 6
                idxNoKk = 7
                idxBirthPlace = 8
                idxBirthDate = 9
                idxMother = 10
                idxNis = 11
                if (sampleRow.size >= 13) {
                    val col12 = cleanText(sampleRow.getOrNull(12))
                    val looksLikeRoomCode = col12.startsWith("PL", ignoreCase = true) ||
                        col12.startsWith("PP", ignoreCase = true) ||
                        col12.startsWith("AL", ignoreCase = true) ||
                        col12.startsWith("AP", ignoreCase = true)
                    if (looksLikeRoomCode) {
                        idxRoom = 12
                        if (sampleRow.size >= 14) idxDorm = 13
                    } else {
                        idxDistrict = 12
                        if (sampleRow.size >= 14) idxRoom = 13
                        if (sampleRow.size >= 15) idxDorm = 14
                    }
                }
            } else if (sampleRow.size in 9..11) {
                val col3Upper = sampleRow.getOrNull(3)?.uppercase().orEmpty()
                val looksLikeJenjang = col3Upper in setOf("SD", "SMP", "SMA", "SMK", "MI", "MTS", "MA") ||
                    col3Upper.startsWith("SMA") || col3Upper.startsWith("SMP") || col3Upper.startsWith("SD")
                if (looksLikeJenjang) {
                    idxNo = 0
                    idxName = 1
                    idxClass = 2
                    idxJenjang = 3
                    idxAddress = 4
                    idxGender = 5
                    idxNik = 6
                    idxNoKk = 7
                    idxBirthPlace = 8
                    idxBirthDate = if (sampleRow.size > 9) 9 else -1
                    idxMother = if (sampleRow.size > 10) 10 else -1
                } else {
                    // Format 10 kolom lama: No, Nama, Kelas, Kamar, JK, NIK, Tempat Lahir, Tgl Lahir, Ibu, Alamat
                    idxNo = 0
                    idxName = 1
                    idxClass = 2
                    idxRoom = 3
                    idxGender = 4
                    idxNik = 5
                    idxBirthPlace = 6
                    idxBirthDate = 7
                    idxMother = 8
                    idxAddress = 9
                }
            } else {
                idxName = if (sampleRow.size > 1) 1 else 0
                if (sampleRow.size > 2) idxClass = 2
            }
        }

        if (idxName == -1) {
            idxName = if ((tokenizedLines.getOrNull(maxOf(headerRowIndex, 0))?.size ?: 0) > 1) 1 else 0
        }

        val startIdx = if (headerRowIndex != -1) headerRowIndex + 1 else 0
        val result = mutableListOf<ChildStudent>()

        for (i in startIdx until tokenizedLines.size) {
            val tokens = tokenizedLines[i]
            if (tokens.size < 2) continue

            val name = cleanText(tokens.getOrNull(idxName))
            if (
                name.isBlank() ||
                name.equals("Nama", ignoreCase = true) ||
                name.equals("Nama Lengkap", ignoreCase = true) ||
                name.equals("Nama Siswa", ignoreCase = true)
            ) {
                continue
            }

            val rawClass = cleanText(tokens.getOrNull(idxClass))
            val classGrade = rawClass.ifBlank { "Kelas -" }

            val rawJenjang = cleanText(tokens.getOrNull(idxJenjang))
            val jenjangVal = rawJenjang.ifBlank { deriveJenjang(classGrade) }

            val rawRoom = cleanText(tokens.getOrNull(idxRoom))
            val rawDorm = cleanText(tokens.getOrNull(idxDorm))
            val rawGender = cleanText(tokens.getOrNull(idxGender))

            val gender = StudentClassifier.detectGenderCode(rawGender, rawRoom, rawDorm)
            val isFemale = gender == "P"

            val dormitory = when {
                rawDorm.isNotBlank() -> rawDorm
                rawRoom.startsWith("PP", ignoreCase = true) || rawRoom.startsWith("AP", ignoreCase = true) -> "Asrama Putri"
                rawRoom.startsWith("PL", ignoreCase = true) || rawRoom.startsWith("AL", ignoreCase = true) -> "Asrama Putra"
                isFemale -> "Asrama Putri"
                else -> "Asrama Putra"
            }

            val nik = cleanDigitsOnly(tokens.getOrNull(idxNik))
            val noKk = cleanDigitsOnly(tokens.getOrNull(idxNoKk))
            val birthPlace = cleanText(tokens.getOrNull(idxBirthPlace))
            val birthDate = cleanText(tokens.getOrNull(idxBirthDate))
            val motherName = cleanText(tokens.getOrNull(idxMother))
            val address = cleanText(tokens.getOrNull(idxAddress))
            val district = cleanText(tokens.getOrNull(idxDistrict))
            val rawNis = cleanNumericId(tokens.getOrNull(idxNis))
            val rawNo = cleanText(tokens.getOrNull(idxNo))

            val nis = when {
                rawNis.isNotBlank() -> rawNis
                rawNo.toIntOrNull() != null -> "${currentSchoolId}-$rawNo"
                else -> "${currentSchoolId}-${result.size + 1}"
            }

            val bloodType = cleanText(tokens.getOrNull(idxBlood)).ifBlank { "-" }
            val allergies = cleanText(tokens.getOrNull(idxAllergy)).ifBlank { "Tidak ada" }
            val contactCol = cleanText(tokens.getOrNull(idxContact))
            val emergency = when {
                contactCol.isNotBlank() -> contactCol
                motherName.isNotBlank() -> "Ibu: $motherName"
                else -> "Pengasuh $dormitory"
            }
            val specialNotes = cleanText(tokens.getOrNull(idxNotes))
            val age = estimateAge(birthDate)

            result.add(
                ChildStudent(
                    id = 0L,
                    name = name,
                    nis = nis,
                    gender = gender,
                    age = age,
                    dormitory = dormitory,
                    classGrade = classGrade,
                    allergies = allergies,
                    bloodType = bloodType,
                    emergencyContact = emergency,
                    specialNotes = specialNotes,
                    nik = nik,
                    noKk = noKk,
                    birthPlace = birthPlace,
                    birthDate = birthDate,
                    motherName = motherName,
                    address = address,
                    district = district,
                    roomNumber = rawRoom,
                    jenjang = jenjangVal,
                    schoolId = currentSchoolId
                )
            )
        }
        return result
    }

    fun escapeCsvField(raw: String?): String {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return ""
        val needsQuotes = text.contains(',') || text.contains(';') || text.contains('"') ||
            text.contains('\n') || text.contains('\r')
        return if (needsQuotes) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }

    fun exportStudentsToCsv(students: List<ChildStudent>): String {
        val sb = StringBuilder()
        sb.appendLine(STANDARD_HEADER)
        students.forEachIndexed { index, student ->
            val jenjang = student.jenjang.trim().ifBlank { deriveJenjang(student.classGrade) }
            val gender = student.gender.trim().ifBlank { "L" }
            val row = listOf(
                (index + 1).toString(),
                escapeCsvField(student.name),
                escapeCsvField(student.classGrade),
                escapeCsvField(jenjang),
                escapeCsvField(student.address),
                escapeCsvField(gender),
                escapeCsvField(cleanDigitsOnly(student.nik)),
                escapeCsvField(cleanDigitsOnly(student.noKk)),
                escapeCsvField(student.birthPlace),
                escapeCsvField(student.birthDate),
                escapeCsvField(student.motherName),
                escapeCsvField(student.nis),
                escapeCsvField(student.district)
            ).joinToString(",")
            sb.appendLine(row)
        }
        return sb.toString().trimEnd()
    }

    fun exportWaliToCsv(accounts: List<WaliAccount>): String {
        val sb = StringBuilder()
        sb.appendLine("Nama Lengkap,Asrama,No WhatsApp,Username,Password")
        accounts.forEach { acc ->
            val effUser = acc.username.trim().ifBlank {
                acc.name.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
            }
            val row = listOf(
                escapeCsvField(acc.name),
                escapeCsvField(acc.dormitory),
                escapeCsvField(acc.phone),
                escapeCsvField(effUser),
                escapeCsvField(acc.password)
            ).joinToString(",")
            sb.appendLine(row)
        }
        return sb.toString().trimEnd()
    }
}
