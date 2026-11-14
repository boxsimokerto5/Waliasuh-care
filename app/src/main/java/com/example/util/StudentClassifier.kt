package com.example.util

import com.example.data.model.ChildStudent

object StudentClassifier {

    fun isFemaleGenderText(rawGender: String): Boolean {
        val g = rawGender.trim().lowercase()
        if (g.isBlank()) return false
        return g == "p" ||
            g == "pr" ||
            g == "w" ||
            g == "f" ||
            g.contains("perempuan") ||
            g.contains("putri") ||
            g.contains("wanita") ||
            g.contains("female") ||
            g.contains("cewek") ||
            g.contains("siswi") ||
            g.contains("santriwati")
    }

    fun isMaleGenderText(rawGender: String): Boolean {
        val g = rawGender.trim().lowercase()
        if (g.isBlank()) return false
        return g == "l" ||
            g == "lk" ||
            g == "m" ||
            g.contains("laki") ||
            g.contains("putra") ||
            g.contains("pria") ||
            g.contains("male") ||
            g.contains("cowok") ||
            g.contains("santriwan")
    }

    fun detectGenderCode(
        rawGender: String,
        rawRoom: String = "",
        rawDorm: String = ""
    ): String {
        if (isFemaleGenderText(rawGender)) return "P"
        if (isMaleGenderText(rawGender)) return "L"

        val r = rawRoom.trim().lowercase()
        val d = rawDorm.trim().lowercase()
        if (
            r.startsWith("pp") ||
            r.startsWith("ap") ||
            r.startsWith("pi") ||
            r.contains("putri") ||
            d.contains("putri") ||
            d.contains("perempuan") ||
            d.contains("banat") ||
            d.contains("nisa")
        ) {
            return "P"
        }
        return "L"
    }

    fun isFemaleStudent(student: ChildStudent): Boolean {
        val d = student.dormitory.trim().lowercase()
        val r = student.roomNumber.trim().lowercase()
        return isFemaleGenderText(student.gender) ||
            d.contains("putri") ||
            d.contains("perempuan") ||
            d.contains("banat") ||
            d.contains("nisa") ||
            r.startsWith("pp") ||
            r.startsWith("ap") ||
            r.startsWith("pi") ||
            r.contains("putri")
    }

    fun isMaleStudent(student: ChildStudent): Boolean = !isFemaleStudent(student)

    fun findMatchingFemaleStudent(
        femaleStudents: List<ChildStudent>,
        rawCode: String
    ): ChildStudent? = findMatchingStudent(femaleStudents, rawCode)

    fun findMatchingStudent(
        students: List<ChildStudent>,
        rawCode: String
    ): ChildStudent? {
        val q = rawCode.trim()
        if (q.isEmpty()) return null
        val qDigits = StudentCsvParser.cleanDigitsOnly(q)

        // Prioritaskan kecocokan eksak NIK / NIS / ID sebelum pencarian nama parsial
        return students.firstOrNull {
            val cleanNik = StudentCsvParser.cleanDigitsOnly(it.nik)
            (cleanNik.isNotBlank() && (cleanNik.equals(q, ignoreCase = true) || (qDigits.length >= 6 && cleanNik == qDigits))) ||
                (it.nis.isNotBlank() && it.nis.trim().equals(q, ignoreCase = true)) ||
                it.id.toString() == q
        } ?: students.firstOrNull {
            it.name.trim().contains(q, ignoreCase = true)
        }
    }
}
