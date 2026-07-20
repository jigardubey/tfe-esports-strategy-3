package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MatchEntity
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TournamentPlannerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val matches by viewModel.matches.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMatchForEdit by remember { mutableStateOf<MatchEntity?>(null) }

    // Form inputs
    var matchTitle by remember { mutableStateOf("") }
    var matchGame by remember { mutableStateOf("PUBG Mobile") }
    var matchDateStr by remember { mutableStateOf("") } // simple text date
    var matchNotes by remember { mutableStateOf("") }
    var matchReminderActive by remember { mutableStateOf(true) }

    val gameOptions = listOf("PUBG Mobile", "BGMI", "Valorant", "Free Fire", "Apex Legends", "CS2")

    fun resetForm() {
        matchTitle = ""
        matchGame = "PUBG Mobile"
        matchDateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        matchNotes = ""
        matchReminderActive = true
    }

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
                                text = "Tournament Planner",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Schedules & training matches",
                                fontSize = 11.sp,
                                color = Color(0xFF8B949E)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            resetForm()
                            showAddDialog = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(OrangePrimary, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Match",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "UPCOMING MATCH TIMELINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B949E),
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (matches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF30363D),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tournament matches scheduled.",
                            fontSize = 14.sp,
                            color = Color(0xFF8B949E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                resetForm()
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Schedule First Match", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(matches) { match ->
                        MatchCardItem(
                            match = match,
                            onClick = {
                                selectedMatchForEdit = match
                                matchTitle = match.title
                                matchGame = match.game
                                matchDateStr = try {
                                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(match.dateTime))
                                } catch (e: Exception) {
                                    ""
                                }
                                matchNotes = match.notes
                                matchReminderActive = match.reminderActive
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }

    // Dialog: ADD MATCH
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Schedule Match Grid", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = matchTitle,
                        onValueChange = { matchTitle = it },
                        label = { Text("Match Title") },
                        placeholder = { Text("e.g. Grand Finals Round 1") },
                        modifier = Modifier.fillMaxWidth().testTag("match_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    // Game category spinner simulator
                    Column {
                        Text("Select Game Engine:", fontSize = 11.sp, color = Color(0xFF8B949E))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.height(36.dp)) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(gameOptions) { game ->
                                        val isSelected = matchGame == game
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) OrangePrimary else Color(0xFF0D1117))
                                                .border(1.dp, Color(0xFF30363D), RoundedCornerShape(6.dp))
                                                .clickable { matchGame = game }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = game,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else Color(0xFF8B949E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = matchDateStr,
                        onValueChange = { matchDateStr = it },
                        label = { Text("Date & Time (YYYY-MM-DD HH:MM)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    OutlinedTextField(
                        value = matchNotes,
                        onValueChange = { matchNotes = it },
                        label = { Text("Tactical notes / rosters") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Calendar Reminder", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = matchReminderActive,
                            onCheckedChange = { matchReminderActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OrangePrimary,
                                checkedTrackColor = OrangeAccent.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (matchTitle.trim().isNotEmpty()) {
                            val parsedDate = try {
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(matchDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }

                            viewModel.addOrUpdateMatch(
                                title = matchTitle,
                                game = matchGame,
                                dateTime = parsedDate,
                                notes = matchNotes,
                                reminder = matchReminderActive
                            )
                            showAddDialog = false
                            Toast.makeText(context, "Esports match scheduled successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color(0xFF8B949E))
                }
            }
        )
    }

    // Dialog: EDIT / DELETE MATCH
    if (selectedMatchForEdit != null) {
        val currentMatch = selectedMatchForEdit!!
        AlertDialog(
            onDismissRequest = { selectedMatchForEdit = null },
            containerColor = Color(0xFF161B22),
            title = { Text("Edit Match Schedule", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = matchTitle,
                        onValueChange = { matchTitle = it },
                        label = { Text("Match Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    OutlinedTextField(
                        value = matchDateStr,
                        onValueChange = { matchDateStr = it },
                        label = { Text("Date & Time") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    OutlinedTextField(
                        value = matchNotes,
                        onValueChange = { matchNotes = it },
                        label = { Text("Match details & tactics") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Calendar Reminder", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = matchReminderActive,
                            onCheckedChange = { matchReminderActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OrangePrimary,
                                checkedTrackColor = OrangeAccent.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            viewModel.deleteMatch(currentMatch.id)
                            selectedMatchForEdit = null
                            Toast.makeText(context, "Match deleted from timeline", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF85149))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color(0xFFF85149))
                    }

                    Button(
                        onClick = {
                            if (matchTitle.trim().isNotEmpty()) {
                                val parsedDate = try {
                                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(matchDateStr)?.time ?: currentMatch.dateTime
                                } catch (e: Exception) {
                                    currentMatch.dateTime
                                }

                                viewModel.addOrUpdateMatch(
                                    id = currentMatch.id,
                                    title = matchTitle,
                                    game = matchGame,
                                    dateTime = parsedDate,
                                    notes = matchNotes,
                                    reminder = matchReminderActive
                                )
                                selectedMatchForEdit = null
                                Toast.makeText(context, "Schedule updated offline", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Apply Changes")
                    }
                }
            }
        )
    }
}

@Composable
fun MatchCardItem(
    match: MatchEntity,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("EEEE, MMM dd • HH:mm", Locale.getDefault())
    val formattedDate = formatter.format(Date(match.dateTime))

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Game tag badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(getGameTagColor(match.game).copy(alpha = 0.12f))
                        .border(1.dp, getGameTagColor(match.game).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = match.game.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = getGameTagColor(match.game)
                    )
                }

                if (match.reminderActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Active Reminder",
                            tint = OrangeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("Reminder set", fontSize = 10.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = match.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF8B949E),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color(0xFF8B949E)
                )
            }

            if (match.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D1117))
                        .padding(10.dp)
                ) {
                    Text(
                        text = match.notes,
                        fontSize = 11.sp,
                        color = Color(0xFFF0F6FC),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

fun getGameTagColor(game: String): Color {
    return when (game.uppercase()) {
        "PUBG MOBILE", "BGMI" -> Color(0xFFFF6B00) // Orange
        "VALORANT" -> Color(0xFFF85149)          // Red
        "FREE FIRE" -> Color(0xFFFFA726)         // Orange/Gold accent
        else -> Color(0xFF58A6FF)                // Blue info
    }
}
