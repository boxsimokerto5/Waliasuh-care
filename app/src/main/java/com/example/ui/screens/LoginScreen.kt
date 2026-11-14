package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.dialogs.SchoolRegistrationDialog
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    viewModel: HealthViewModel,
    onLoginSuccessAdmin: () -> Unit,
    onLoginSuccessWali: (waliName: String) -> Unit,
    onLoginSuccessSuperAdmin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showRegistrationDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            val nextLang = if (appLanguage == AppLanguage.ENGLISH) {
                                AppLanguage.INDONESIAN
                            } else {
                                AppLanguage.ENGLISH
                            }
                            viewModel.setAppLanguage(nextLang)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, WaliPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_login_toggle_language")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = if (isEn) "Switch Language" else "Ganti Bahasa",
                                tint = WaliPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (appLanguage == AppLanguage.ENGLISH) "EN • English" else "ID • Indonesia",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WaliPrimary
                            )
                        }
                    }
                }

                // Hero Logo & Title
                ElevatedCard(
                    shape = CircleShape,
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.size(86.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_sr_care),
                        contentDescription = if (isEn) "Wali Asuh Care Logo" else "Logo Wali Asuh Care",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Wali Asuh Care",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isEn) {
                            "Integrated School & Asrama Health Platform"
                        } else {
                            "Platform Kesehatan Sekolah & Asrama Terpadu"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WaliPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isEn) {
                            "Referral & Student Health Monitoring System"
                        } else {
                            "Sistem Rujukan & Pemantauan Kesehatan Anak Asuh"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Login Card Form
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isEn) "Sign In to Account" else "Masuk ke Akun",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Please enter the username and password provided by the Administrator."
                            } else {
                                "Silakan masukkan username dan password yang diberikan oleh Administrator."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        // Error Banner
                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Input Username
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            label = { Text("Username") },
                            placeholder = { Text(if (isEn) "Wali Asuh or Admin name" else "Nama Wali atau Admin") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = WaliPrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_input_username")
                        )

                        // Input Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = WaliPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) {
                                            if (isEn) "Hide password" else "Sembunyikan password"
                                        } else {
                                            if (isEn) "Show password" else "Lihat password"
                                        },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_input_password")
                        )

                        // Ingat Saya Checkbox
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                modifier = Modifier.testTag("login_checkbox_remember")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Remember my account on this device" else "Ingat akun saya di perangkat ini",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Tombol Masuk
                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                viewModel.login(username, password, rememberMe) { result ->
                                    isLoading = false
                                    when (result) {
                                        is LoginResult.SuccessSuperAdmin -> {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Welcome to the Superadmin Portal!" else "Selamat datang di Portal Superadmin!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onLoginSuccessSuperAdmin?.invoke()
                                        }
                                        is LoginResult.SuccessAdmin -> {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Welcome, Administrator!" else "Selamat datang, Administrator!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onLoginSuccessAdmin()
                                        }
                                        is LoginResult.SuccessWali -> {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Welcome aboard, ${result.account.name}!" else "Selamat bertugas, ${result.account.name}!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onLoginSuccessWali(result.account.name)
                                        }
                                        is LoginResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_login_submit")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isLoading) {
                                        if (isEn) "Signing in..." else "Memproses..."
                                    } else {
                                        if (isEn) "Sign In" else "Masuk ke Aplikasi"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Tombol Pendaftaran Sekolah Baru (Sesuai Permintaan User)
                OutlinedCard(
                    onClick = { showRegistrationDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_open_school_registration"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, WaliPrimary.copy(alpha = 0.5f)),
                    colors = CardDefaults.outlinedCardColors(containerColor = WaliPrimary.copy(alpha = 0.06f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEn) "School not registered yet?" else "Sekolah belum terdaftar?",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) {
                                        "Tap to fill out the registration form"
                                    } else {
                                        "Silakan klik untuk mengisi formulir pendaftaran"
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = WaliPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = if (isEn) {
                        "Integrated School & Asrama Health Application\nPlease use official credentials issued by the Administrator."
                    } else {
                        "Aplikasi Kesehatan Sekolah & Asrama Terpadu\nSilakan gunakan kredensial resmi yang diterbitkan oleh Administrator."
                    },
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                        .testTag("login_independent_disclaimer")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = if (isEn) "Application status information" else "Informasi status aplikasi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = stringResource(R.string.disclaimer_short),
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showRegistrationDialog) {
                    SchoolRegistrationDialog(
                        onDismiss = { showRegistrationDialog = false },
                        onSubmit = { schoolName, address, city, estimatedWali, picName, picEmail, picPhone, onSubmitted ->
                            viewModel.submitSchoolRegistration(
                                schoolName = schoolName,
                                address = address,
                                city = city,
                                estimatedWaliCount = estimatedWali,
                                picName = picName,
                                picEmail = picEmail,
                                picPhone = picPhone,
                                onComplete = onSubmitted
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
