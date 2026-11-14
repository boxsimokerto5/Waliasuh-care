package com.example.util.i18n

import java.util.Locale

data class AppStrings(
    val language: AppLanguage,
    val nav: CommonNavStrings,
    val medical: MedicalStrings,
    val student: StudentShiftStrings,
    val haidFasting: HaidFastingStrings,
    val report: ReportPdfStrings
) {
    val locale: Locale get() = language.locale
    val isEnglish: Boolean get() = language == AppLanguage.ENGLISH
    val close: String get() = nav.close
    val cancel: String get() = nav.cancel
    val search: String get() = nav.search
    val delete: String get() = nav.delete
    val all: String get() = nav.all
    val today: String get() = nav.today
    val tomorrow: String get() = nav.tomorrow
    val dayAfterTomorrow: String get() = nav.dayAfterTomorrow
}

val IdStrings = AppStrings(
    language = AppLanguage.INDONESIAN,
    nav = IdCommonNavStrings,
    medical = IdMedicalStrings,
    student = IdStudentShiftStrings,
    haidFasting = IdHaidFastingStrings,
    report = IdReportPdfStrings
)

val EnStrings = AppStrings(
    language = AppLanguage.ENGLISH,
    nav = EnCommonNavStrings,
    medical = EnMedicalStrings,
    student = EnStudentShiftStrings,
    haidFasting = EnHaidFastingStrings,
    report = EnReportPdfStrings
)
