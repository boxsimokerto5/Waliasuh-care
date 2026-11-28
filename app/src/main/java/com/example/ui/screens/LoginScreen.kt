package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
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
    val view = LocalView.current
    val isEn = LocalAppStrings.current.isEnglish
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    if (!view.isInEditMode) {
        DisposableEffect(view) {
            val window = (view.context as? Activity)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            val prevLightStatus = controller?.isAppearanceLightStatusBars
            controller?.isAppearanceLightStatusBars = false
            onDispose {
                if (prevLightStatus != null) {
                    controller.isAppearanceLightStatusBars = prevLightStatus
                }
            }
        }
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showRegistrationDialog by remember { mutableStateOf(false) }

    val pageBg = if (isLight) Color(0xFFF3FAF8) else MaterialTheme.colorScheme.background
    val heroBrush = if (isLight) {
        Brush.verticalGradient(
            listOf(Color(0xFF064E78), Color(0xFF0A6C82), Color(0xFF0F766E))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFF042B47), Color(0xFF064B5E), Color(0xFF0B534D))
        )
    }
    val cardBg = if (isLight) Color.White else MaterialTheme.colorScheme.surface
    val cardBorderColor = if (isLight) Color(0xFFBDE8E2) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val accentPrimary = if (isLight) Color(0xFF0A6C82) else WaliPrimary

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = pageBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Soft Atmospheric Ocean-Emerald Header Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(heroBrush)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft ambient glow orbs
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-30).dp, y = (-20).dp)
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF5EEAD4).copy(alpha = 0.22f), Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 24.dp, y = 24.dp)
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFDE68A).copy(alpha = 0.18f), Color.Transparent)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF99F6E4),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isEn) "Care Portal" else "Portal Asuh",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

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
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
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
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (appLanguage == AppLanguage.ENGLISH) "EN • English" else "ID • Indonesia",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Luminous Crest
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF084C82),
                        shadowElevation = 8.dp,
                        border = BorderStroke(2.5.dp, Color.White.copy(alpha = 0.9f)),
                        modifier = Modifier.size(88.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_sr_care),
                            contentDescription = if (isEn) "Wali Asuh Care Logo" else "Logo Wali Asuh Care",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp)
                                .clip(CircleShape)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Wali Asuh Care",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = if (isEn) {
                                "Integrated School & Asrama Care Platform"
                            } else {
                                "Platform Kesehatan, Ibadah & Asrama Terpadu"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCCFBF1),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isEn) {
                                "Referral & Student Health Monitoring System"
                            } else {
                                "Sistem Rujukan & Pemantauan Anak Asuh"
                            },
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.80f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Form & Action Cards Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Login Card Form Bernuansa Lembut
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = cardBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isLight) Color(0xFFE6FFFA) else accentPrimary.copy(alpha = 0.16f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = accentPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Sign In to Account" else "Masuk ke Akun",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) {
                                        "Wali Asuh • Admin • Parent Portal"
                                    } else {
                                        "Wali Asuh • Admin • Portal Orang Tua"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = accentPrimary
                                )
                            }
                        }

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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentPrimary,
                            unfocusedBorderColor = if (isLight) Color(0xFFBAE6FD) else MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = if (isLight) Color(0xFFF8FEFD) else Color.Transparent,
                            unfocusedContainerColor = if (isLight) Color(0xFFF9FCFB) else Color.Transparent,
                            focusedLabelColor = accentPrimary
                        )

                        // Input Username
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            label = { Text("Username") },
                            placeholder = { Text(if (isEn) "Wali, Admin, or Parent username" else "Username Wali, Admin, atau Orang Tua") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = accentPrimary)
                            },
                            colors = fieldColors,
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
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
                                Icon(Icons.Default.Lock, contentDescription = null, tint = accentPrimary)
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
                            colors = fieldColors,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
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
                                colors = CheckboxDefaults.colors(checkedColor = accentPrimary),
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
                                        is LoginResult.SuccessGuruNgaji -> {
                                            Toast.makeText(
                                                context,
                                                if (isEn) {
                                                    "Welcome, Ustadz/ah ${result.account.name} (Mengaji Portal)!"
                                                } else {
                                                    "Selamat bertugas, ${result.account.name} (Portal Mengaji)!"
                                                },
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onLoginSuccessWali(result.account.name)
                                        }
                                        is LoginResult.SuccessPetugasKesehatan -> {
                                            Toast.makeText(
                                                context,
                                                if (isEn) {
                                                    "Welcome, ${result.account.name} (Health Officer Portal)!"
                                                } else {
                                                    "Selamat bertugas, ${result.account.name} (Portal Kesehatan)!"
                                                },
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onLoginSuccessWali(result.account.name)
                                        }
                                        is LoginResult.SuccessParent -> {
                                            Toast.makeText(
                                                context,
                                                if (isEn) {
                                                    "Welcome, ${result.link.parentName.ifBlank { "Parent" }}!"
                                                } else {
                                                    "Selamat datang, ${result.link.parentName.ifBlank { "Orang Tua" }}!"
                                                },
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        is LoginResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
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
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Kartu Pendaftaran Sekolah Baru Bernuansa Krem-Amber Hangat
                OutlinedCard(
                    onClick = { showRegistrationDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_open_school_registration"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isLight) Color(0xFFFDE68A) else Color(0xFFD97706).copy(alpha = 0.45f)
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isLight) Color(0xFFFFFBEB) else Color(0xFF282115)
                    )
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
                            Surface(
                                shape = CircleShape,
                                color = if (isLight) Color(0xFFFEF3C7) else Color(0xFFD97706).copy(alpha = 0.25f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = if (isLight) Color(0xFFB45309) else Color(0xFFFBBF24),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Sekolah Rakyat not registered yet?" else "Sekolah Rakyat belum terdaftar?",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) {
                                        "Tap to register your Sekolah Rakyat unit & admin account"
                                    } else {
                                        "Klik untuk mengisi formulir pendaftaran Sekolah Rakyat baru"
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isLight) Color(0xFFB45309) else Color(0xFFFBBF24),
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLight) Color(0xFFEAF6F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        0.8.dp,
                        if (isLight) Color(0xFFBDE8E2) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                        .testTag("login_independent_disclaimer")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = if (isEn) "Application status information" else "Informasi status aplikasi",
                            tint = accentPrimary,
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

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
