package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class DashboardCategoryHub {
    BEROBAT,
    PUASA,
    HAID,
    PESAN
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
    var showDaftarPuasaSheet by mutableStateOf(false)
    var showChecklistPuasaSheet by mutableStateOf(false)
    var showRankingPuasaSheet by mutableStateOf(false)
}

@Composable
fun rememberDashboardModalState(): DashboardModalState = remember { DashboardModalState() }
