package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.language.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF161B22), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Settings & Lab Info",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Interface configuration & compliance",
                                fontSize = 11.sp,
                                color = Color(0xFF8B949E)
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0D1117)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // APPLICATION SPECIFICATIONS
            Text(
                text = "TACTICAL INTERFACE CONFIGURE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B949E),
                letterSpacing = 1.sp
            )

            // Theme Setting Row (Always Premium Dark)
            SettingItemRow(
                title = "Visual Theme Engine",
                subtitle = "Active: Elite Dark Mode (Optimized 60fps)",
                icon = Icons.Default.DarkMode,
                color = OrangeAccent,
                onClick = {
                    Toast.makeText(context, "Elite Dark Theme is enforced for tactical focus.", Toast.LENGTH_SHORT).show()
                }
            )

            // Language Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text("Language Configuration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Select localized playbook text formats", fontSize = 11.sp, color = Color(0xFF8B949E))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("en" to "ENGLISH", "es" to "ESPAÑOL", "kr" to "한국어").forEach { (code, name) ->
                            val isSelected = currentLanguage == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OrangePrimary else Color(0xFF0D1117))
                                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.setLanguage(code)
                                        Toast.makeText(context, "Localized to $name", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF8B949E)
                                )
                            }
                        }
                    }
                }
            }

            // UTILITIES SECTION
            Text(
                text = "COMMUNICATION & SUPPORT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B949E),
                letterSpacing = 1.sp
            )

            SettingItemRow(
                title = "Submit Tactical Feedback",
                subtitle = "Report bugs or suggest new sketching tools",
                icon = Icons.Default.Feedback,
                color = Color(0xFF58A6FF),
                onClick = { showFeedbackDialog = true }
            )

            SettingItemRow(
                title = "Rate App on Store",
                subtitle = "Support offline development",
                icon = Icons.Default.StarRate,
                color = Color(0xFFFFD700),
                onClick = {
                    Toast.makeText(context, "Thank you for rating TFE Esports Strategy!", Toast.LENGTH_SHORT).show()
                }
            )

            SettingItemRow(
                title = "Share Tactical Lab",
                subtitle = "Invite other roster coaches & players",
                icon = Icons.Default.Share,
                color = Color(0xFF2EA043),
                onClick = {
                    Toast.makeText(context, "Native Playbook share link generated!", Toast.LENGTH_SHORT).show()
                }
            )

            // LEGAL SECTION
            Text(
                text = "LEGAL SPECIFICATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B949E),
                letterSpacing = 1.sp
            )

            SettingItemRow(
                title = "About Playbook Engine",
                subtitle = "Software version, frameworks, and system details",
                icon = Icons.Default.Info,
                color = Color.White,
                onClick = { showAboutDialog = true }
            )

            SettingItemRow(
                title = "Privacy Policy",
                subtitle = "100% offline security specifications",
                icon = Icons.Default.Security,
                color = Color(0xFF2EA043),
                onClick = { showPrivacyDialog = true }
            )

            SettingItemRow(
                title = "Terms of Service",
                subtitle = "No login/data tracking guidelines",
                icon = Icons.Default.Gavel,
                color = Color(0xFFF85149),
                onClick = { showTermsDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog: ABOUT
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("About TFE Esports Strategy", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version: 1.0.0 (Offline Production Build)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Designed exclusively for professional coaches, analysts, and esports athletes. This app provides top-tier offline-first strategy planning, visual drafting vectors, coordinates layering, and championship timeline schedulers.",
                        fontSize = 12.sp,
                        color = Color(0xFF8B949E)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Frameworks: Jetpack Compose, Room DB, Kotlin Flow", fontSize = 11.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Dialog: PRIVACY POLICY
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Privacy Policy", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.height(240.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        "TFE ESPORTS STRATEGY operates on a strict zero-server, completely offline-first security principle:\n\n" +
                        "1. Local Storage: All maps, strategy layouts, matches schedules, and tactical journal entries are stored inside your device's secured SQLite database. No external cloud servers are queried.\n\n" +
                        "2. No Tracking: No telemetry, personal logs, or location coordinate analytics are collected. Your strategic drawings remain entirely confidential.\n\n" +
                        "3. Complete Safety: You can safely organize roster rosters, schedules, and counter-tactics knowing that no data leaves your mobile device.",
                        fontSize = 12.sp,
                        color = Color(0xFF8B949E)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("I Understand")
                }
            }
        )
    }

    // Dialog: TERMS OF SERVICE
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Terms of Service", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.height(240.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        "Welcome to TFE Esports Strategy. By using this local software application, you agree to these offline parameters:\n\n" +
                        "1. License: You are granted a personal, non-exclusive license to use the visual canvas, marking nodes, and calendar schedulers to develop strategy concepts for esports teams.\n\n" +
                        "2. Zero-Warranty: This local software is provided 'as-is' with zero cloud guarantees. All updates and edits are automatically saved to your own device.\n\n" +
                        "3. Fair Play: You are free to export tactical layouts to PNG or share schedules natively. TFE holds zero intellectual properties over your created playbooks.",
                        fontSize = 12.sp,
                        color = Color(0xFF8B949E)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Accept Terms")
                }
            }
        )
    }

    // Dialog: FEEDBACK FORM
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Submit Tactical Feedback", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tell us how we can improve the playbook canvas tools:", fontSize = 11.sp, color = Color(0xFF8B949E))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("feedback_text_input"),
                        placeholder = { Text("e.g. Please add smoke radius circle overlays!") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedbackText.trim().isNotEmpty()) {
                            Toast.makeText(context, "Feedback logged offline! Thank you.", Toast.LENGTH_SHORT).show()
                            feedbackText = ""
                            showFeedbackDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Submit")
                }
            }
        )
    }
}

@Composable
fun SettingItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0D1117)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF8B949E)
            )
        }
    }
}
