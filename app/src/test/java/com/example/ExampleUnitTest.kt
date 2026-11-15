package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCsvLineTokenization_handlesQuotesAndCommas() {
    val sampleLine = """2,Ahmad Ali Nur Arifin,SD 1-2,PL_103,Laki-laki,3.50603E+15,KEDIRI,28-04-2019,NURIKAH,"Nyawangan, Kras, KAB. KEDIRI""""
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < sampleLine.length) {
      val c = sampleLine[i]
      if (c == '"') {
        if (inQuotes && i + 1 < sampleLine.length && sampleLine[i + 1] == '"') {
          sb.append('"')
          i++
        } else {
          inQuotes = !inQuotes
        }
      } else if (c == ',' && !inQuotes) {
        tokens.add(sb.toString().trim())
        sb.clear()
      } else {
        sb.append(c)
      }
      i++
    }
    tokens.add(sb.toString().trim())

    assertEquals(10, tokens.size)
    assertEquals("Ahmad Ali Nur Arifin", tokens[1])
    assertEquals("SD 1-2", tokens[2])
    assertEquals("PL_103", tokens[3])
    assertEquals("Laki-laki", tokens[4])
    assertEquals("Nyawangan, Kras, KAB. KEDIRI", tokens[9])
  }

  @Test
  fun testExportStudentsToCsv_roundTripsThroughParser() {
    val students = listOf(
      com.example.data.model.ChildStudent(
        id = 1L,
        name = "Aisyah Putri",
        nis = "3185428544",
        gender = "P",
        age = 13,
        dormitory = "Asrama Putri",
        classGrade = "VII-1",
        nik = "3506015105090001",
        noKk = "3506010412103889",
        birthPlace = "KEDIRI",
        birthDate = "11-05-2013",
        motherName = "Siti Aminah",
        address = "Dsn. Krajan, Kec. Semen, \"Kab. Kediri\"",
        district = "Semen",
        jenjang = "SMP",
        schoolId = "SRT1-KDR"
      )
    )
    val csv = com.example.util.StudentCsvParser.exportStudentsToCsv(students)
    val parsed = com.example.util.StudentCsvParser.parseCsv(csv, "SRT1-KDR")
    assertEquals(1, parsed.size)
    assertEquals("Aisyah Putri", parsed[0].name)
    assertEquals("VII-1", parsed[0].classGrade)
    assertEquals("SMP", parsed[0].jenjang)
    assertEquals("P", parsed[0].gender)
    assertEquals("3506015105090001", parsed[0].nik)
    assertEquals("3506010412103889", parsed[0].noKk)
    assertEquals("3185428544", parsed[0].nis)
    assertEquals("Semen", parsed[0].district)
    assertTrue(parsed[0].address.contains("Dsn. Krajan, Kec. Semen"))
  }

  @Test
  fun testParseLegacy12ColumnCsv_withoutDistrictDoesNotShiftColumns() {
    val legacyCsv = """
      No,Nama,Kelas,Jenjang,Alamat,Jenis Kelamin,NIK,NO KK,TEMPAT LAHIR,TANGGAL LAHIR,NAMA IBU,NISN
      1,BUDI SANTOSO,VIII-2,SMP,"Jl. Melati No. 5, Kec. Papar, Kab. Kediri",L,3506142512090002,3506141405120009,KEDIRI,25-12-2011,SITI FATIMAH,0119988776
    """.trimIndent()
    val parsed = com.example.util.StudentCsvParser.parseCsv(legacyCsv, "SRT1-KDR")
    assertEquals(1, parsed.size)
    assertEquals("BUDI SANTOSO", parsed[0].name)
    assertEquals("0119988776", parsed[0].nis)
    assertEquals("3506142512090002", parsed[0].nik)
    assertEquals("3506141405120009", parsed[0].noKk)
    assertEquals("", parsed[0].district)
    assertEquals("Papar", com.example.util.StudentCsvParser.extractDistrictFromAddress(parsed[0].address))
  }

  @Test
  fun testExportWaliToCsv_formatsExpectedColumns() {
    val accounts = listOf(
      com.example.data.model.WaliAccount(
        id = 1L,
        name = "Ustadz Ahmad",
        username = "ustahmad",
        password = "Pass#1234",
        dormitory = "Asrama Putra",
        phone = "081234567890",
        schoolId = "SRT1-KDR"
      ),
      com.example.data.model.WaliAccount(
        id = 2L,
        name = "Ustadzah Fatimah",
        username = "ustfatimah",
        password = "Ngaji#5678",
        dormitory = "Umum",
        phone = "081298765432",
        role = com.example.data.model.WaliAccount.ROLE_GURU_NGAJI,
        schoolId = "SRT1-KDR"
      )
    )
    val csv = com.example.util.StudentCsvParser.exportWaliToCsv(accounts)
    val lines = csv.lines()
    assertEquals("Nama Lengkap,Asrama,No WhatsApp,Username,Password,Peran", lines[0])
    assertEquals("Ustadz Ahmad,Asrama Putra,081234567890,ustahmad,Pass#1234,WALI", lines[1])
    assertEquals("Ustadzah Fatimah,Umum,081298765432,ustfatimah,Ngaji#5678,GURU_NGAJI", lines[2])
  }

  @Test
  fun testExportStudentsToCsv_preservesParentPhoneColumn() {
    val student = com.example.data.model.ChildStudent(
      id = 2L,
      name = "Farhan Hakim",
      nis = "3190011223",
      gender = "L",
      age = 11,
      dormitory = "Asrama Putra",
      classGrade = "V-A",
      nik = "3506011101120001",
      noKk = "3506011101120009",
      birthPlace = "KEDIRI",
      birthDate = "01-01-2015",
      motherName = "Aminah Zahra",
      address = "Jl. Merpati No. 12",
      district = "Pare",
      jenjang = "SD",
      parentPhone = "0812-3456-7890",
      schoolId = "SRT1-KDR"
    )
    val csv = com.example.util.StudentCsvParser.exportStudentsToCsv(listOf(student))
    val parsed = com.example.util.StudentCsvParser.parseCsv(csv, "SRT1-KDR")
    assertEquals(1, parsed.size)
    assertEquals("081234567890", parsed[0].parentPhone)
    assertEquals("6281234567890", com.example.util.StudentCsvParser.formatWhatsAppDigits(parsed[0].parentPhone))
  }
}
