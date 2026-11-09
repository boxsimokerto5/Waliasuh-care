package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HealthViewModel
import com.example.ui.components.TopHeader
import com.example.ui.screens.CompletedHistoryScreen
import com.example.ui.screens.FaskesInfoScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.MyApplicationTheme

enum class HealthNavScreen(val title: String) {
    PLANS("Rencana"),
    HISTORY("Telah Diobati"),
    STUDENTS("Anak Asuh"),
    FASKES("Puskesmas")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Default utama adalah tema terang (false)
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                HealthAppContent(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun HealthAppContent(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: HealthViewModel = viewModel()
) {
    var currentScreen by rememberSaveable { mutableStateOf(HealthNavScreen.PLANS) }

    // Collect Reactive StateFlows
    val students by viewModel.students.collectAsStateWithLifecycle()
    val activePlans by viewModel.activePlans.collectAsStateWithLifecycle()
    val completedRecords by viewModel.filteredCompletedRecords.collectAsStateWithLifecycle()
    val pendingControls by viewModel.pendingControlRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.completedRecords.collectAsStateWithLifecycle()

    val historySearchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val studentSearchQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()
    val selectedFaskesFilter by viewModel.selectedFaskesFilter.collectAsStateWithLifecycle()

    // Handle back button for secondary screens
    if (currentScreen != HealthNavScreen.PLANS) {
        BackHandler {
            currentScreen = HealthNavScreen.PLANS
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopHeader(
                activeCount = activePlans.size,
                controlCount = pendingControls.size,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == HealthNavScreen.PLANS,
                        onClick = { currentScreen = HealthNavScreen.PLANS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = "Rencana Berobat",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Rencana", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_plans")
                    )
                    NavigationBarItem(
                        selected = currentScreen == HealthNavScreen.HISTORY,
                        onClick = { currentScreen = HealthNavScreen.HISTORY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = "Telah Diobati",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Telah Diobati", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_history")
                    )
                    NavigationBarItem(
                        selected = currentScreen == HealthNavScreen.STUDENTS,
                        onClick = { currentScreen = HealthNavScreen.STUDENTS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Data Siswa",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Anak Asuh", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_students")
                    )
                    NavigationBarItem(
                        selected = currentScreen == HealthNavScreen.FASKES,
                        onClick = { currentScreen = HealthNavScreen.FASKES },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "Faskes Mitra",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Puskesmas", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_faskes")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (currentScreen) {
                HealthNavScreen.PLANS -> {
                    PlanScreen(
                        viewModel = viewModel,
                        plans = activePlans,
                        pendingControls = pendingControls,
                        students = students
                    )
                }
                HealthNavScreen.HISTORY -> {
                    CompletedHistoryScreen(
                        viewModel = viewModel,
                        completedRecords = completedRecords,
                        students = students,
                        searchQuery = historySearchQuery,
                        selectedFaskes = selectedFaskesFilter
                    )
                }
                HealthNavScreen.STUDENTS -> {
                    StudentsScreen(
                        viewModel = viewModel,
                        students = students,
                        records = allRecords,
                        searchQuery = studentSearchQuery
                    )
                }
                HealthNavScreen.FASKES -> {
                    FaskesInfoScreen(records = allRecords)
                }
            }
        }
    }
}
