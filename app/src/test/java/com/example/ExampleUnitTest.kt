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
}
