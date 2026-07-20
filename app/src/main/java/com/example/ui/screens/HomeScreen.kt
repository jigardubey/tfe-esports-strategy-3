package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StrategyEntity
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val strategies by viewModel.strategies.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val customImportedMaps by viewModel.customImportedMaps.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newStrategyTitle by remember { mutableStateOf("") }
    var selectedGameCategory by remember { mutableStateOf("BGMI") }
    var selectedMapType by remember { mutableStateOf("erangel") }
    var selectedCustomMapPath by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedCustomMapPath = uri.toString()
            selectedMapType = "custom"
            Toast.makeText(context, "Gallery blueprint selected successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TFE ESPORTS",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = OrangePrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "STRATEGY CENTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B949E),
                            letterSpacing = 2.sp
                        )
                    }
                    
                    // Immersive Circular JD Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(OrangePrimary, OrangeAccent)
                                )
                            )
                            .clickable { onNavigate("settings") }
                            .padding(2.dp) // Ring border space
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF0D1117)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = OrangeAccent
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0D1117)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Next Match Promotion Banner (Immersive UI specification)
            item {
                val nextMatch = matches.firstOrNull()
                val matchTitle = nextMatch?.title ?: "Grand Finals - Lobby A"
                val matchOpponent = nextMatch?.game ?: "Elite League Scrims"
                val matchMeta = if (nextMatch != null) {
                    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                    "${nextMatch.game.uppercase()} • ${sdf.format(java.util.Date(nextMatch.dateTime))}"
                } else {
                    "Erangel • Starts in 45m"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeAccent)
                            )
                        )
                        .clickable { onNavigate("tournament") }
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NEXT MATCH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = matchTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "$matchOpponent • $matchMeta",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black.copy(alpha = 0.75f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Schedule",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Quick Actions section
            item {
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "New Strategy",
                        subtitle = "Blank sketching canvas",
                        icon = Icons.Default.Add,
                        iconBgColor = OrangePrimary.copy(alpha = 0.1f),
                        iconColor = OrangePrimary,
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Matches Hub",
                        subtitle = "Schedules & Scrims",
                        icon = Icons.Default.Event,
                        iconBgColor = OrangeAccent.copy(alpha = 0.1f),
                        iconColor = OrangeAccent,
                        onClick = { onNavigate("tournament") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Extra Actions / Notes Quick Access
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Tactical Notes",
                        subtitle = "Offline journal keys",
                        icon = Icons.Default.Edit,
                        iconBgColor = Color(0xFF2EA043).copy(alpha = 0.1f),
                        iconColor = Color(0xFF2EA043),
                        onClick = { onNavigate("notes") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Offline Vault",
                        subtitle = "Manage downloads",
                        icon = Icons.Default.Download,
                        iconBgColor = Color(0xFF58A6FF).copy(alpha = 0.1f),
                        iconColor = Color(0xFF58A6FF),
                        onClick = { onNavigate("downloads") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Stats / Info
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Strategies",
                        value = "${strategies.size}",
                        icon = Icons.Default.Map,
                        color = OrangePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Scheduled",
                        value = "${matches.size}",
                        icon = Icons.Default.Event,
                        color = Color(0xFF58A6FF),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Notes Journal",
                        value = "${notes.size}",
                        icon = Icons.Default.Notes,
                        color = Color(0xFF2EA043),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Continue Editing Section (if strategies exist)
            if (strategies.isNotEmpty()) {
                val lastStrategy = strategies.first()
                item {
                    Text(
                        text = "CONTINUE PLANNING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B949E),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        onClick = { viewModel.loadStrategy(lastStrategy) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .testTag("continue_editing_card"),
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
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0D1117))
                                    .border(1.dp, OrangePrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lastStrategy.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Map: ${getMapDisplayName(lastStrategy.mapType)} • Saved ${formatTimestamp(lastStrategy.lastUpdated)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8B949E),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Edit",
                                tint = OrangeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Recent Strategies Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT STRATEGIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B949E),
                        letterSpacing = 1.5.sp
                    )
                }
            }

            if (strategies.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .background(Color(0xFF161B22), RoundedCornerShape(24.dp))
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFF30363D),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No strategies created yet.",
                                fontSize = 14.sp,
                                color = Color(0xFF8B949E),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tap 'New Strategy' to initialize a setup.",
                                fontSize = 12.sp,
                                color = Color(0xFF8B949E).copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(strategies.take(5)) { strategy ->
                    StrategyRowItem(
                        strategy = strategy,
                        onClick = { viewModel.loadStrategy(strategy) },
                        onDelete = { viewModel.deleteStrategy(strategy.id) },
                        onDuplicate = { viewModel.duplicateStrategy(strategy) }
                    )
                }
            }

            // Bottom space
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialog to create a new strategy
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF161B22),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFF0F6FC),
            title = {
                Text(
                    text = "New Tactical Strategy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = newStrategyTitle,
                        onValueChange = { newStrategyTitle = it },
                        label = { Text("Strategy Title") },
                        placeholder = { Text("e.g. Erangel Military Hold") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("strategy_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    // Game Category Selector
                    Column {
                        Text(
                            text = "Select Game Platform:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("BGMI", "FREE FIRE", "CUSTOM").forEach { cat ->
                                val isCatSelected = selectedGameCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCatSelected) OrangePrimary.copy(alpha = 0.15f) else Color(0xFF0D1117))
                                        .border(
                                            1.dp,
                                            if (isCatSelected) OrangePrimary else Color(0xFF30363D),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedGameCategory = cat
                                            selectedCustomMapPath = null
                                            selectedMapType = when (cat) {
                                                "BGMI" -> "erangel"
                                                "FREE FIRE" -> "bermuda"
                                                else -> "custom"
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCatSelected) OrangeAccent else Color(0xFF8B949E)
                                    )
                                }
                            }
                        }
                    }

                    // Map selection list of the selected category
                    Column {
                        Text(
                            text = "Choose Battleground Map:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val mapsToShow = when (selectedGameCategory) {
                            "BGMI" -> listOf(
                                "erangel" to "Erangel",
                                "miramar" to "Miramar",
                                "sanhok" to "Sanhok",
                                "vikendi" to "Vikendi",
                                "karakin" to "Karakin",
                                "livik" to "Livik",
                                "nusa" to "Nusa"
                            )
                            "FREE FIRE" -> listOf(
                                "bermuda" to "Bermuda",
                                "purgatory" to "Purgatory",
                                "kalahari" to "Kalahari",
                                "bermuda_remastered" to "Bermuda Remast.",
                                "alpine" to "Alpine",
                                "nexterra" to "NeXTerra"
                            )
                            else -> {
                                val list = mutableListOf(
                                    "custom" to "Custom Grid"
                                )
                                customImportedMaps.forEach { (name, path) ->
                                    list.add(path to name)
                                }
                                list.add("browse_gallery" to "+ Add Gallery")
                                list
                            }
                        }

                        val chunkedMaps = mapsToShow.chunked(3)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunkedMaps.forEach { rowMaps ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowMaps.forEach { (typeId, displayName) ->
                                        val isSelected = if (selectedGameCategory != "CUSTOM") {
                                            selectedMapType == typeId
                                        } else {
                                            if (typeId == "custom") {
                                                selectedMapType == "custom" && selectedCustomMapPath == null
                                            } else if (typeId == "browse_gallery") {
                                                selectedMapType == "custom" && selectedCustomMapPath != null && !customImportedMaps.any { it.second == selectedCustomMapPath }
                                            } else {
                                                selectedMapType == "custom" && selectedCustomMapPath == typeId
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) OrangePrimary else Color(0xFF0D1117))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) OrangeAccent else Color(0xFF30363D),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    if (selectedGameCategory != "CUSTOM") {
                                                        selectedMapType = typeId
                                                        selectedCustomMapPath = null
                                                    } else {
                                                        if (typeId == "browse_gallery") {
                                                            galleryLauncher.launch("image/*")
                                                        } else {
                                                            selectedMapType = "custom"
                                                            selectedCustomMapPath = if (typeId == "custom") null else typeId
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (typeId == "browse_gallery" && selectedCustomMapPath != null && !customImportedMaps.any { it.second == selectedCustomMapPath }) {
                                                    "Gallery Selected ✓"
                                                } else {
                                                    displayName
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else Color(0xFF8B949E),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    // Fill empty slots in the last row to maintain weights
                                    if (rowMaps.size < 3) {
                                        repeat(3 - rowMaps.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStrategyTitle.trim().isNotEmpty()) {
                            viewModel.createNewStrategy(
                                title = newStrategyTitle,
                                mapType = selectedMapType,
                                customMapPath = selectedCustomMapPath
                            )
                            showCreateDialog = false
                            newStrategyTitle = ""
                            selectedCustomMapPath = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Initialize", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color(0xFF8B949E))
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF8B949E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StrategyRowItem(
    strategy: StrategyEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High fidelity Thumbnail matching HTML
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D1117))
                    .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
            ) {
                // Background icon with low opacity
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = OrangePrimary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center)
                )
                // Bottom Right mini map tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = getMapDisplayName(strategy.mapType).uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strategy.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Saved ${formatTimestamp(strategy.lastUpdated)}",
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E),
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                // Beautiful badges
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(OrangePrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFFLINE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TACTICAL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B949E)
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color(0xFF8B949E)
                    )
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(Color(0xFF21262D))
                ) {
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.CopyAll, null, tint = OrangeAccent) },
                        onClick = {
                            onDuplicate()
                            expandedMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFF85149)) },
                        onClick = {
                            onDelete()
                            expandedMenu = false
                        }
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val netDate = Date(timestamp)
        sdf.format(netDate)
    } catch (e: Exception) {
        "recently"
    }
}

fun getMapDisplayName(mapType: String): String {
    return when (mapType.lowercase()) {
        "erangel" -> "Erangel"
        "miramar" -> "Miramar"
        "sanhok" -> "Sanhok"
        "vikendi" -> "Vikendi"
        "karakin" -> "Karakin"
        "livik" -> "Livik"
        "nusa" -> "Nusa"
        "bermuda" -> "Bermuda"
        "purgatory" -> "Purgatory"
        "kalahari" -> "Kalahari"
        "bermuda_remastered" -> "Bermuda Remast."
        "alpine" -> "Alpine"
        "nexterra" -> "NeXTerra"
        "custom" -> "Custom Grid"
        else -> mapType.replaceFirstChar { it.uppercase() }
    }
}
