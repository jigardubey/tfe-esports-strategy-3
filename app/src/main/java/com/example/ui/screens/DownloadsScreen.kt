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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val customImportedMaps by viewModel.customImportedMaps.collectAsState()

    // Local simulated list of exported maps & imported templates
    var exportedPngs by remember {
        mutableStateOf(
            listOf(
                "Final_Grand_Finals_Setup.png" to "Saved July 19, 246kb",
                "Miramar_Hold_West.png" to "Saved July 18, 192kb",
                "Military_Base_Push.pdf" to "Saved July 15, 1.2mb"
            )
        )
    }

    var showImportDialog by remember { mutableStateOf(false) }
    var importedMapName by remember { mutableStateOf("") }
    var tempSelectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher for selecting custom blueprint images from actual mobile gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            tempSelectedImageUri = uri
            Toast.makeText(context, "Blueprint image loaded! Enter a name to save.", Toast.LENGTH_SHORT).show()
        }
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
                                text = "Strategy Vault",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Exported PNGs & custom map templates",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // CUSTOM MAPS HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IMPORTED CUSTOM MAP TEMPLATES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B949E),
                        letterSpacing = 1.sp
                    )
                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161B22)),
                        modifier = Modifier.border(1.dp, OrangePrimary, RoundedCornerShape(12.dp)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Import New",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // LIST CUSTOM MAPS
            if (customImportedMaps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .background(Color(0xFF161B22), RoundedCornerShape(24.dp))
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No custom maps uploaded from gallery yet.", fontSize = 12.sp, color = Color(0xFF8B949E))
                    }
                }
            } else {
                items(customImportedMaps) { (name, path) ->
                    val subtext = if (path.startsWith("content://") || path.startsWith("file://") || path.contains("/")) {
                        "Imported from Device Gallery • Standard 1:1"
                    } else {
                        "High Quality Vector Blueprint"
                    }
                    Card(
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(OrangePrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = OrangeAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = subtext,
                                        fontSize = 11.sp,
                                        color = Color(0xFF8B949E),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteCustomMap(name)
                                    Toast.makeText(context, "Custom template removed", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFF85149),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SAVED EXPORTS HEADER
            item {
                Text(
                    text = "SAVED TACTICAL EXPORTS (PNG/PDF)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 1.sp
                )
            }

            // EXPORTS LIST
            if (exportedPngs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .background(Color(0xFF161B22), RoundedCornerShape(24.dp))
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No strategy exports saved yet.", fontSize = 12.sp, color = Color(0xFF8B949E))
                    }
                }
            } else {
                items(exportedPngs) { (filename, info) ->
                    Card(
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2EA043).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (filename.endsWith(".pdf")) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color(0xFF2EA043),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = filename,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = info,
                                        fontSize = 11.sp,
                                        color = Color(0xFF8B949E),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "Sharing $filename with squad roster...", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        exportedPngs = exportedPngs.filter { it.first != filename }
                                        Toast.makeText(context, "Export deleted", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFF85149),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialog: IMPORT CUSTOM MAP
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { 
                showImportDialog = false
                tempSelectedImageUri = null
                importedMapName = ""
            },
            containerColor = Color(0xFF161B22),
            title = { Text("Import Map from Gallery", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select a top-down blueprint layout image from your device gallery to use as a strategic planning canvas.",
                        fontSize = 11.sp,
                        color = Color(0xFF8B949E)
                    )
                    
                    OutlinedTextField(
                        value = importedMapName,
                        onValueChange = { importedMapName = it },
                        label = { Text("Blueprint Label Name") },
                        placeholder = { Text("e.g. Erangel Squad House") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (tempSelectedImageUri != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2EA043).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF2EA043), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2EA043))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Image Selected Successfully",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161B22)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OrangePrimary, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = OrangeAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image from Gallery", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = tempSelectedImageUri
                        if (importedMapName.trim().isNotEmpty() && uri != null) {
                            viewModel.importCustomMap(importedMapName, uri.toString())
                            showImportDialog = false
                            importedMapName = ""
                            tempSelectedImageUri = null
                            Toast.makeText(context, "Custom terrain layout imported successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Please enter a name and select an image!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = importedMapName.trim().isNotEmpty() && tempSelectedImageUri != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        disabledContainerColor = Color(0xFF30363D)
                    )
                ) {
                    Text("Import to Vault", color = if (importedMapName.trim().isNotEmpty() && tempSelectedImageUri != null) Color.White else Color(0xFF8B949E))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showImportDialog = false
                    tempSelectedImageUri = null
                    importedMapName = ""
                }) {
                    Text("Cancel", color = Color(0xFF8B949E))
                }
            }
        )
    }
}
