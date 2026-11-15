package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import com.example.data.model.ChildStudent
import com.example.data.model.MengajiRecord
import com.example.data.model.VisitSession

enum class DashboardCategoryHub {
    BEROBAT,
    KUNJUNGAN,
    PUASA,
    HAID,
    PESAN,
    MENGAJI
}

@Stable
class DashboardModalState {
    var activeCategoryHub by mutableStateOf<DashboardCategoryHub?>(null)
    var showCreateDialog by mutableStateOf(false)
    var showDirectTreatmentDialog by mutableStateOf(false)
    var showPuskesmasInfoSheet by mutableStateOf(false)
    var showShiftMessagesSheet by mutableStateOf(false)
    var showCreateShiftMessageDialog by mutableStateOf(false)
    var showShiftHistorySheet by mutableStateOf(false)
    var showInputHaidDialog by mutableStateOf(false)
    var showDaftarHaidSheet by mutableStateOf(false)
    var showDaftarSuciSheet by mutableStateOf(false)
    var showFiqihHaidDialog by mutableStateOf(false)
    var showInputPuasaDialog by mutableStateOf(false)
    var showCreatePuasaSessionDialog by mutableStateOf(false)
    var showDaftarPuasaSheet by mutableStateOf(false)
    var showChecklistPuasaSheet by mutableStateOf(false)
    var showRankingPuasaSheet by mutableStateOf(false)
    var showInputMengajiDialog by mutableStateOf(false)
    var showRiwayatMengajiSheet by mutableStateOf(false)
    var riwayatMengajiInitialTab by mutableStateOf(0)
    var showKartuMengajiSheet by mutableStateOf(false)
    var mengajiTargetStudent by mutableStateOf<ChildStudent?>(null)
    var mengajiRecordToEdit by mutableStateOf<MengajiRecord?>(null)

    // Modul Kunjungan Orang Tua / Wali
    var showJadwalKunjunganSheet by mutableStateOf(false)
    var showDaftarHadirKunjunganSheet by mutableStateOf(false)
    var daftarHadirKunjunganInitialTab by mutableStateOf(0)
    var showLaporanKunjunganSheet by mutableStateOf(false)
    var showSiswaPerKecamatanSheet by mutableStateOf(false)
    var showCreateVisitSessionDialog by mutableStateOf(false)
    var visitSessionToEdit by mutableStateOf<VisitSession?>(null)
    var visitStudentForDetail by mutableStateOf<ChildStudent?>(null)
}

@Composable
fun rememberDashboardModalState(): DashboardModalState = remember { DashboardModalState() }
