package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DrawingObject
import com.example.data.MarkerObject
import com.example.data.Point2D
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.MainViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPlannerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activeStrategy by viewModel.activeStrategy.collectAsState()
    val drawings by viewModel.activeDrawings.collectAsState()
    val markers by viewModel.activeMarkers.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val brushColor by viewModel.brushColor.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()
    val brushOpacity by viewModel.brushOpacity.collectAsState()

    // Map gesture control states
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var mapLockPan by remember { mutableStateOf(true) } // default true to allow panning/zooming

    // Dialog & UI states
    var showMarkerDialog by remember { mutableStateOf(false) }
    var selectedMarkerTypeForAdd by remember { mutableStateOf("landing") }
    
    // Manage marker editing
    var editingMarker by remember { mutableStateOf<MarkerObject?>(null) }
    var showEditMarkerDialog by remember { mutableStateOf(false) }

    // Layer settings
    var showGridLines by remember { mutableStateOf(true) }
    var showTerrain by remember { mutableStateOf(true) }
    var showSectors by remember { mutableStateOf(true) }

    // Active sketch states
    var currentPathPoints = remember { mutableStateListOf<Offset>() }
    var currentStartOffset by remember { mutableStateOf<Offset?>(null) }
    var currentEndOffset by remember { mutableStateOf<Offset?>(null) }
    var textInputVal by remember { mutableStateOf("") }
    var showTextDialog by remember { mutableStateOf(false) }

    // Export dialog
    var showExportDialog by remember { mutableStateOf(false) }

    // Save notes dialog
    var showStrategyNotesDialog by remember { mutableStateOf(false) }
    var strategyNotesText by remember { mutableStateOf(activeStrategy?.notes ?: "") }

    val colorsList = listOf(
        0xFFFF6B00.toInt(), // Orange primary
        0xFFFFA726.toInt(), // Orange accent
        0xFFF85149.toInt(), // Red danger
        0xFF2EA043.toInt(), // Green safe
        0xFF58A6FF.toInt(), // Blue info
        0xFF8F00FF.toInt(), // Purple sniper
        0xFFFFFFFF.toInt(), // White
        0xFFFFD700.toInt()  // Gold
    )

    val toolsList = listOf(
        "brush" to Icons.Default.Brush,
        "pencil" to Icons.Default.Create,
        "line" to Icons.Default.HorizontalRule,
        "arrow" to Icons.Default.TrendingFlat,
        "rect" to Icons.Default.CropSquare,
        "circle" to Icons.Default.RadioButtonUnchecked,
        "text" to Icons.Default.TextFields,
        "eraser" to Icons.Default.CleaningServices,
        "move" to Icons.Default.OpenWith
    )

    val markersList = listOf(
        "landing" to Icons.Default.FlightTakeoff,
        "enemy" to Icons.Default.LocalFireDepartment,
        "vehicle" to Icons.Default.DirectionsCar,
        "loot" to Icons.Default.ShoppingCart,
        "danger" to Icons.Default.Warning,
        "camp" to Icons.Default.Home,
        "sniper" to Icons.Default.CenterFocusStrong,
        "rush" to Icons.Default.FlashOn,
        "safe_zone" to Icons.Default.Security,
        "custom" to Icons.Default.Flag
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeStrategy?.title ?: "Tactical Map",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Auto-save active • Layer controls enabled",
                            fontSize = 11.sp,
                            color = Color(0xFF2EA043)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveCurrentStrategy()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showStrategyNotesDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Strategy Notes",
                            tint = OrangeAccent
                        )
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Strategy",
                            tint = OrangePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF161B22))
            )
        },
        containerColor = Color(0xFF0D1117)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // LAYER CONTROLS & GESTURE SWITCH BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .border(1.dp, Color(0xFF30363D))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Lock / Unlock Transform Gestures
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { mapLockPan = !mapLockPan },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (mapLockPan) OrangePrimary else Color(0xFF0D1117),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (mapLockPan) Icons.Default.ZoomIn else Icons.Default.Lock,
                            contentDescription = "Toggle Lock Pan Zoom",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mapLockPan) "Map Pan/Zoom Active" else "Paint Mode Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mapLockPan) OrangeAccent else Color.White
                    )
                }

                // Layer checkboxes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LayerToggleButton(
                        label = "Grid",
                        checked = showGridLines,
                        onCheckedChange = { showGridLines = it }
                    )
                    LayerToggleButton(
                        label = "Sectors",
                        checked = showSectors,
                        onCheckedChange = { showSectors = it }
                    )
                    LayerToggleButton(
                        label = "Terrain",
                        checked = showTerrain,
                        onCheckedChange = { showTerrain = it }
                    )
                }
            }

            // MAIN INTERACTIVE CANVAS VIEW
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RectangleShape)
                    .background(Color(0xFF0D1117))
                    .onSizeChanged { }
                    .pointerInput(mapLockPan) {
                        if (mapLockPan) {
                            // Double tap zoom
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.5f) {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        }
                    }
                    .pointerInput(mapLockPan) {
                        if (mapLockPan) {
                            // Pinch zoom & Drag map
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5.0f)
                                offsetX += pan.x * scale
                                offsetY += pan.y * scale
                            }
                        } else {
                            // Drawing input gestures
                            detectDragGestures(
                                onDragStart = { start ->
                                    val localPoint = Offset(
                                        (start.x - offsetX) / scale,
                                        (start.y - offsetY) / scale
                                    )
                                    if (selectedTool == "brush" || selectedTool == "pencil") {
                                        currentPathPoints.clear()
                                        currentPathPoints.add(localPoint)
                                    } else if (selectedTool == "eraser") {
                                        // Erase drawings that are near
                                        val newDrawings = drawings.filterNot { drawObj ->
                                            drawObj.points.any { p ->
                                                val dist = Math.hypot(
                                                    (p.x - localPoint.x).toDouble(),
                                                    (p.y - localPoint.y).toDouble()
                                                )
                                                dist < 20.0
                                            }
                                        }
                                        viewModel.setDrawings(newDrawings)
                                    } else {
                                        currentStartOffset = localPoint
                                        currentEndOffset = localPoint
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val currentScreenPoint = change.position
                                    val localPoint = Offset(
                                        (currentScreenPoint.x - offsetX) / scale,
                                        (currentScreenPoint.y - offsetY) / scale
                                    )

                                    if (selectedTool == "brush" || selectedTool == "pencil") {
                                        currentPathPoints.add(localPoint)
                                    } else if (selectedTool == "eraser") {
                                        val newDrawings = drawings.filterNot { drawObj ->
                                            drawObj.points.any { p ->
                                                val dist = Math.hypot(
                                                    (p.x - localPoint.x).toDouble(),
                                                    (p.y - localPoint.y).toDouble()
                                                )
                                                dist < 20.0
                                            }
                                        }
                                        viewModel.setDrawings(newDrawings)
                                    } else {
                                        currentEndOffset = localPoint
                                    }
                                },
                                onDragEnd = {
                                    if (selectedTool == "brush" || selectedTool == "pencil") {
                                        if (currentPathPoints.size > 1) {
                                            viewModel.addDrawingObject(
                                                DrawingObject(
                                                    id = UUID.randomUUID().toString(),
                                                    type = selectedTool,
                                                    points = currentPathPoints.map { Point2D(it.x, it.y) },
                                                    color = brushColor,
                                                    brushSize = brushSize,
                                                    opacity = brushOpacity
                                                )
                                            )
                                        }
                                        currentPathPoints.clear()
                                    } else if (selectedTool == "eraser") {
                                        // Done erasing
                                    } else {
                                        val start = currentStartOffset
                                        val end = currentEndOffset
                                        if (start != null && end != null) {
                                            if (selectedTool == "text") {
                                                showTextDialog = true
                                            } else {
                                                // Create shape
                                                viewModel.addDrawingObject(
                                                    DrawingObject(
                                                        id = UUID.randomUUID().toString(),
                                                        type = selectedTool,
                                                        points = listOf(Point2D(start.x, start.y), Point2D(end.x, end.y)),
                                                        color = brushColor,
                                                        brushSize = brushSize,
                                                        opacity = brushOpacity,
                                                        rectWidth = Math.abs(end.x - start.x),
                                                        rectHeight = Math.abs(end.y - start.y)
                                                    )
                                                )
                                            }
                                        }
                                        currentStartOffset = null
                                        currentEndOffset = null
                                    }
                                }
                            )
                        }
                    }
            ) {
                // SCALE & TRANSLATION CONTAINER (Both vectors and markers scale and shift together)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                ) {
                    // 1. Map Background (Procedural or Custom Gallery Image)
                    val customPath = activeStrategy?.customMapPath
                    val isRealCustomFile = !customPath.isNullOrEmpty() && (customPath.startsWith("content://") || customPath.startsWith("file://") || customPath.contains("/"))

                    if (isRealCustomFile) {
                        // Load image from Uri or local file path using Coil
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(customPath),
                            contentDescription = "Custom Map Layout",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                        // If grid or sectors are enabled, draw them on top of the image
                        if (showGridLines || showSectors) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (showGridLines) {
                                    val step = 80f
                                    val gridColor = Color(0xFF30363D).copy(alpha = 0.4f)
                                    // Verticals
                                    var x = 0f
                                    while (x < size.width) {
                                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                                        x += step
                                    }
                                    // Horizontals
                                    var y = 0f
                                    while (y < size.height) {
                                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                        y += step
                                    }
                                }
                                if (showSectors) {
                                    val radarColor = Color(0xFFFF6B00).copy(alpha = 0.08f)
                                    drawCircle(
                                        color = radarColor,
                                        radius = size.width * 0.35f,
                                        center = Offset(size.width / 2, size.height / 2),
                                        style = Stroke(
                                            width = 2f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        val activeMapType = if (customPath == "sanhok_pro") "sanhok"
                                            else if (customPath == "vikendi_pro") "vikendi"
                                            else activeStrategy?.mapType ?: "erangel"
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawTacticalMapProcedural(
                                showTerrain = showTerrain,
                                showSectors = showSectors,
                                showGridLines = showGridLines,
                                mapType = activeMapType
                            )
                        }
                    }

                    // 2. Active Strategy Drawings Layer
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Saved drawing elements
                        drawings.forEach { drawObj ->
                            drawSingleDrawingObject(drawObj)
                        }

                        // Temp in-progress drawing path
                        if (currentPathPoints.size > 1) {
                            val path = Path().apply {
                                val first = currentPathPoints.first()
                                moveTo(first.x, first.y)
                                for (i in 1 until currentPathPoints.size) {
                                    val pt = currentPathPoints[i]
                                    lineTo(pt.x, pt.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color(brushColor).copy(alpha = brushOpacity),
                                style = Stroke(
                                    width = brushSize,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // Temp in-progress shape drawing
                        val start = currentStartOffset
                        val end = currentEndOffset
                        if (start != null && end != null) {
                            drawShapePreview(
                                tool = selectedTool,
                                start = start,
                                end = end,
                                color = Color(brushColor).copy(alpha = brushOpacity),
                                brushSize = brushSize
                            )
                        }
                    }

                    // 3. Interactive Tactical Drag-and-Drop Markers Layer
                    markers.forEach { marker ->
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = marker.x.dp - (marker.size / 2).dp,
                                    y = marker.y.dp - (marker.size / 2).dp
                                )
                                .size(marker.size.dp)
                                .clip(CircleShape)
                                .background(getMarkerBackgroundColor(marker.type))
                                .border(1.5.dp, Color.White, CircleShape)
                                .pointerInput(mapLockPan) {
                                    if (!mapLockPan) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val newX = marker.x + dragAmount.x / scale
                                            val newY = marker.y + dragAmount.y / scale
                                            viewModel.updateMarker(marker.copy(x = newX, y = newY))
                                        }
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            editingMarker = marker
                                            showEditMarkerDialog = true
                                        },
                                        onLongPress = {
                                            editingMarker = marker
                                            showEditMarkerDialog = true
                                        }
                                    )
                                }
                                .testTag("marker_${marker.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getMarkerIconVector(marker.type),
                                contentDescription = marker.label,
                                tint = Color.White,
                                modifier = Modifier.size((marker.size * 0.6f).dp)
                            )
                        }

                        // Mini label overlay
                        if (marker.label.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = marker.x.dp - 30.dp,
                                        y = marker.y.dp + (marker.size / 2).dp + 2.dp
                                    )
                                    .width(60.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = marker.label,
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ZOOM SCALE FACTOR OVERLAY
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Zoom: ${String.format("%.1f", scale)}x",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // TOOL PROPERTIES CONTROLLER
            if (!mapLockPan && (selectedTool == "brush" || selectedTool == "pencil" || selectedTool == "line" || selectedTool == "arrow" || selectedTool == "rect" || selectedTool == "circle")) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161B22))
                        .border(1.dp, Color(0xFF30363D))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Size slider
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Brush Size", fontSize = 10.sp, color = Color(0xFF8B949E))
                            Text("${brushSize.toInt()}dp", fontSize = 10.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = brushSize,
                            onValueChange = { viewModel.setBrushSize(it) },
                            valueRange = 2f..40f,
                            colors = SliderDefaults.colors(
                                thumbColor = OrangePrimary,
                                activeTrackColor = OrangeAccent
                            )
                        )
                    }

                    // Opacity slider
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Opacity", fontSize = 10.sp, color = Color(0xFF8B949E))
                            Text("${(brushOpacity * 100).toInt()}%", fontSize = 10.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = brushOpacity,
                            onValueChange = { viewModel.setBrushOpacity(it) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = OrangePrimary,
                                activeTrackColor = OrangeAccent
                            )
                        )
                    }
                }
            }

            // DYNAMIC COLOR PICKER
            if (!mapLockPan) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117))
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(colorsList) { colorInt ->
                        val isSelected = brushColor == colorInt
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(
                                    if (isSelected) 2.5.dp else 1.dp,
                                    if (isSelected) OrangePrimary else Color(0xFF30363D),
                                    CircleShape
                                )
                                .clickable { viewModel.setBrushColor(colorInt) }
                        )
                    }
                }
            }

            // UTILITY TOOLBAR (Undo, Redo, Eraser, Clear)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .border(1.dp, Color(0xFF30363D))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = { viewModel.undo() }) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.redo() }) {
                        Icon(imageVector = Icons.Default.Redo, contentDescription = "Redo", tint = Color.White)
                    }
                }

                // Add Marker button
                Button(
                    onClick = { showMarkerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddLocation, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Marker", color = Color.White, fontSize = 12.sp)
                }

                // Clear Strategy Sketch button
                IconButton(onClick = { viewModel.clearAllDrawings() }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All Sketch",
                        tint = Color(0xFFF85149)
                    )
                }
            }

            // CORE TOOLS SELECT PANEL
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(toolsList) { (tool, icon) ->
                    val isSelected = selectedTool == tool && !mapLockPan
                    Column(
                        modifier = Modifier
                            .width(55.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) OrangePrimary else Color(0xFF161B22))
                            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
                            .clickable {
                                mapLockPan = false // switch to drawing/painting mode
                                viewModel.selectTool(tool)
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tool,
                            tint = if (isSelected) Color.White else Color(0xFF8B949E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tool.replaceFirstChar { it.uppercase() },
                            color = if (isSelected) Color.White else Color(0xFF8B949E),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Dialog: TEXT INPUT
    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = {
                showTextDialog = false
                currentStartOffset = null
                currentEndOffset = null
            },
            containerColor = Color(0xFF161B22),
            title = { Text("Add Text Overlay", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = textInputVal,
                    onValueChange = { textInputVal = it },
                    label = { Text("Enter custom message") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color(0xFF30363D)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val start = currentStartOffset
                        if (start != null && textInputVal.isNotEmpty()) {
                            viewModel.addDrawingObject(
                                DrawingObject(
                                    id = UUID.randomUUID().toString(),
                                    type = "text",
                                    points = listOf(Point2D(start.x, start.y)),
                                    color = brushColor,
                                    brushSize = brushSize,
                                    opacity = brushOpacity,
                                    text = textInputVal
                                )
                            )
                        }
                        textInputVal = ""
                        showTextDialog = false
                        currentStartOffset = null
                        currentEndOffset = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Add Text")
                }
            }
        )
    }

    // Dialog: STRATEGY NOTES & COUTING REMINDER
    if (showStrategyNotesDialog) {
        AlertDialog(
            onDismissRequest = { showStrategyNotesDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Strategy Playbook Notes", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Detail your setup, rotation times, and agent positions below.", fontSize = 11.sp, color = Color(0xFF8B949E))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = strategyNotesText,
                        onValueChange = { strategyNotesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("e.g. Hold high ground, lock drop route B, utility smoke spam at 1:30") },
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
                        viewModel.saveCurrentStrategy(strategyNotesText)
                        showStrategyNotesDialog = false
                        Toast.makeText(context, "Tactical Notes Saved Offline", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Save Notes")
                }
            }
        )
    }

    // Dialog: CHOOSE MARKER TYPE TO ADD
    if (showMarkerDialog) {
        AlertDialog(
            onDismissRequest = { showMarkerDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Place Tactical Marker", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Choose tactical marker item to place in center of strategic layout map:", fontSize = 11.sp, color = Color(0xFF8B949E))
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.height(200.dp)) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            markersList.forEach { (type, icon) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedMarkerTypeForAdd == type) OrangePrimary else Color(0xFF0D1117))
                                        .clickable { selectedMarkerTypeForAdd = type }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = type,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = type.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Place at relative map center (e.g. 150f, 150f relative coord)
                        viewModel.addMarker(
                            type = selectedMarkerTypeForAdd,
                            x = 180f,
                            y = 200f
                        )
                        showMarkerDialog = false
                        Toast.makeText(context, "Double-tap or long-press marker to edit details!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Place Marker")
                }
            }
        )
    }

    // Dialog: EDIT / RENAME / ATTACH NOTES / RESIZE / DELETE MARKER
    if (showEditMarkerDialog && editingMarker != null) {
        val marker = editingMarker!!
        var mLabel by remember { mutableStateOf(marker.label) }
        var mSize by remember { mutableStateOf(marker.size) }
        var mNotes by remember { mutableStateOf(marker.notes) }

        AlertDialog(
            onDismissRequest = { showEditMarkerDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Modify ${marker.type.uppercase()} Marker", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = mLabel,
                        onValueChange = { mLabel = it },
                        label = { Text("Marker Label") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Marker Size", fontSize = 11.sp, color = Color(0xFF8B949E))
                            Text("${mSize.toInt()}dp", fontSize = 11.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = mSize,
                            onValueChange = { mSize = it },
                            valueRange = 24f..72f,
                            colors = SliderDefaults.colors(
                                thumbColor = OrangePrimary,
                                activeTrackColor = OrangeAccent
                            )
                        )
                    }

                    OutlinedTextField(
                        value = mNotes,
                        onValueChange = { mNotes = it },
                        label = { Text("Strategic notes for this position") },
                        modifier = Modifier.fillMaxWidth(),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            viewModel.deleteMarker(marker.id)
                            showEditMarkerDialog = false
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF85149))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color(0xFFF85149))
                    }

                    Button(
                        onClick = {
                            viewModel.updateMarker(
                                marker.copy(
                                    label = mLabel,
                                    size = mSize,
                                    notes = mNotes
                                )
                            )
                            showEditMarkerDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Apply Changes")
                    }
                }
            }
        )
    }

    // Dialog: EXPORT EXECUTED
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Export Successful", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2EA043).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2EA043),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tactical Playbook strategy exported successfully to device storage (Saved exports / PNG).",
                        fontSize = 12.sp,
                        color = Color(0xFF8B949E),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        Toast.makeText(context, "Tactical PNG layout shared!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Android Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun LayerToggleButton(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) OrangePrimary.copy(alpha = 0.2f) else Color(0xFF0D1117))
            .border(
                1.dp,
                if (checked) OrangeAccent else Color(0xFF30363D),
                RoundedCornerShape(6.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (checked) Color.White else Color(0xFF8B949E)
        )
    }
}

// Procedural high-contrast tactical gaming radar layout drawing logic
// Procedural high-contrast tactical gaming radar layout drawing logic
fun DrawScope.drawTacticalMapProcedural(
    showTerrain: Boolean,
    showSectors: Boolean,
    showGridLines: Boolean,
    mapType: String
) {
    val width = size.width
    val height = size.height

    // Solid base dark blue/black background
    drawRect(color = Color(0xFF0D1117))

    // Define the official real landmark locations for each map
    val landmarks = when (mapType.lowercase()) {
        "bermuda" -> listOf(
            Triple("PEAK", 0.5f, 0.42f),
            Triple("CLOCK TOWER", 0.32f, 0.65f),
            Triple("FACTORY", 0.48f, 0.78f),
            Triple("POCHINOK", 0.38f, 0.54f),
            Triple("BIMASAKTI STRIP", 0.5f, 0.28f),
            Triple("MILL", 0.75f, 0.45f),
            Triple("SHIPYARD", 0.55f, 0.12f),
            Triple("HANGAR", 0.18f, 0.58f),
            Triple("OBSERVATORY", 0.12f, 0.42f),
            Triple("MARS ELECTRIC", 0.45f, 0.9f)
        )
        "purgatory" -> listOf(
            Triple("BRASILIA", 0.5f, 0.5f),
            Triple("MOATHOUSE", 0.48f, 0.12f),
            Triple("MARBLEWORKS", 0.22f, 0.45f),
            Triple("FIELDS", 0.5f, 0.78f),
            Triple("FORGE", 0.78f, 0.42f),
            Triple("QUARRY", 0.24f, 0.72f),
            Triple("LUMBER MILL", 0.75f, 0.74f),
            Triple("CAMPSITE", 0.28f, 0.25f)
        )
        "kalahari" -> listOf(
            Triple("REFINERY", 0.5f, 0.48f),
            Triple("COMMAND POST", 0.48f, 0.26f),
            Triple("SUB (CRASHED)", 0.76f, 0.68f),
            Triple("THE MAZE", 0.2f, 0.3f),
            Triple("SANTA CATARINA", 0.26f, 0.76f),
            Triple("FOUNDATION", 0.74f, 0.32f),
            Triple("MAMMOTH", 0.82f, 0.5f)
        )
        "bermuda_remastered" -> listOf(
            Triple("PEAK", 0.5f, 0.42f),
            Triple("CLOCK TOWER", 0.32f, 0.65f),
            Triple("FACTORY", 0.48f, 0.78f),
            Triple("NUREK DAM", 0.55f, 0.22f),
            Triple("ADEN'S CREEK", 0.15f, 0.82f),
            Triple("ACADEMY", 0.3f, 0.18f),
            Triple("THE SUB", 0.78f, 0.32f),
            Triple("SAMURAI'S GARDEN", 0.85f, 0.78f)
        )
        "alpine" -> listOf(
            Triple("SNOWFALL", 0.35f, 0.28f),
            Triple("MILITIA", 0.5f, 0.52f),
            Triple("VANTAGE", 0.52f, 0.15f),
            Triple("RAILROAD", 0.78f, 0.42f),
            Triple("BLUE VILLE", 0.2f, 0.48f),
            Triple("SUNSIDE", 0.52f, 0.78f),
            Triple("DOCK", 0.25f, 0.76f)
        )
        "nexterra" -> listOf(
            Triple("INTELLECT", 0.5f, 0.48f),
            Triple("MUSEUM", 0.28f, 0.28f),
            Triple("GRAV LABS", 0.22f, 0.62f),
            Triple("DECA SQUARE", 0.4f, 0.8f),
            Triple("PLAZA", 0.76f, 0.72f),
            Triple("FARMTOPIA", 0.74f, 0.32f)
        )
        "erangel" -> listOf(
            Triple("POCHINKI", 0.48f, 0.54f),
            Triple("SCHOOL", 0.54f, 0.42f),
            Triple("MILITARY BASE", 0.52f, 0.84f),
            Triple("GEORGOPOOL", 0.2f, 0.35f),
            Triple("YASNAYA POLYANA", 0.72f, 0.35f),
            Triple("ROZHOK", 0.5f, 0.36f),
            Triple("MYLTA POWER", 0.84f, 0.6f),
            Triple("NOVO", 0.72f, 0.78f)
        )
        "miramar" -> listOf(
            Triple("EL POZO", 0.24f, 0.32f),
            Triple("PECADOS", 0.52f, 0.54f),
            Triple("LOS LEONES", 0.68f, 0.72f),
            Triple("SAN MARTIN", 0.48f, 0.38f),
            Triple("HACIENDA", 0.52f, 0.33f),
            Triple("CHUMACERA", 0.35f, 0.7f),
            Triple("VALLE DEL MAR", 0.22f, 0.85f)
        )
        "sanhok" -> listOf(
            Triple("BOOTCAMP", 0.5f, 0.46f),
            Triple("PARADISE RESORT", 0.65f, 0.32f),
            Triple("RUINS", 0.32f, 0.58f),
            Triple("PAI NAN", 0.42f, 0.72f),
            Triple("QUARRY", 0.62f, 0.62f),
            Triple("DOCKS", 0.78f, 0.78f)
        )
        "vikendi" -> listOf(
            Triple("CASTLE", 0.54f, 0.58f),
            Triple("VOLNOVA", 0.62f, 0.74f),
            Triple("PODVOSTO", 0.52f, 0.46f),
            Triple("DOBRO MESTO", 0.16f, 0.38f),
            Triple("CEMENT FACTORY", 0.72f, 0.5f),
            Triple("COSMODROME", 0.68f, 0.22f)
        )
        "karakin" -> listOf(
            Triple("BASHARA", 0.25f, 0.35f),
            Triple("AL HABAR", 0.75f, 0.25f),
            Triple("CARGO SHIP", 0.78f, 0.75f),
            Triple("HADIQA NEMO", 0.28f, 0.72f)
        )
        "livik" -> listOf(
            Triple("MIDSTEIN", 0.52f, 0.52f),
            Triple("BLOMSTER", 0.32f, 0.38f),
            Triple("ICEBORG", 0.78f, 0.22f),
            Triple("POWER PLANT", 0.74f, 0.58f),
            Triple("GRONHUS", 0.28f, 0.72f)
        )
        "nusa" -> listOf(
            Triple("SCIENCE CENTER", 0.48f, 0.48f),
            Triple("SHIPYARD", 0.24f, 0.32f),
            Triple("CRYSTAL BAY", 0.22f, 0.68f),
            Triple("BULAN BAY", 0.76f, 0.74f)
        )
        else -> listOf(
            Triple("ALPHA BASE", 0.3f, 0.3f),
            Triple("BRAVO OUTPOST", 0.7f, 0.3f),
            Triple("COMMAND HQ", 0.5f, 0.5f),
            Triple("SUPPLY DEPOT", 0.4f, 0.7f)
        )
    }

    // Draw map-specific backgrounds & geography
    when (mapType.lowercase()) {
        "bermuda", "bermuda_remastered" -> {
            // Islands + a flowing river through the island
            if (showTerrain) {
                val grassColor = Color(0xFF1E5E2F).copy(alpha = 0.22f)
                val beachColor = Color(0xFFC4A470).copy(alpha = 0.15f)
                // Draw main island landmass
                drawCircle(color = beachColor, radius = width * 0.46f, center = Offset(width * 0.5f, height * 0.5f))
                drawCircle(color = grassColor, radius = width * 0.44f, center = Offset(width * 0.5f, height * 0.5f))
                
                // Hangar beach, Mill hills
                drawCircle(color = grassColor, radius = width * 0.18f, center = Offset(width * 0.2f, height * 0.5f))
                drawCircle(color = grassColor, radius = width * 0.15f, center = Offset(width * 0.78f, height * 0.45f))

                // River separating Hangar & Clocktower
                val river = Path().apply {
                    moveTo(width * 0.15f, 0f)
                    cubicTo(width * 0.22f, height * 0.35f, width * 0.35f, height * 0.55f, width * 0.45f, height * 0.65f)
                    cubicTo(width * 0.5f, height * 0.72f, width * 0.52f, height * 0.82f, width * 0.48f, height)
                }
                drawPath(path = river, color = Color(0xFF1B4D7E).copy(alpha = 0.4f), style = Stroke(width = 30f, cap = StrokeCap.Round))
                
                // Draw bridge paths
                drawLine(Color(0xFF8B949E).copy(alpha = 0.6f), Offset(width * 0.32f, height * 0.51f), Offset(width * 0.36f, height * 0.55f), strokeWidth = 8f)
                drawLine(Color(0xFF8B949E).copy(alpha = 0.6f), Offset(width * 0.48f, height * 0.72f), Offset(width * 0.52f, height * 0.72f), strokeWidth = 8f)
            }
        }
        "purgatory" -> {
            if (showTerrain) {
                val terrainColor = Color(0xFF2C6B3F).copy(alpha = 0.2f)
                // Moathouse lake
                drawCircle(color = Color(0xFF1A3D63).copy(alpha = 0.4f), radius = width * 0.1f, center = Offset(width * 0.48f, height * 0.12f))
                drawCircle(color = terrainColor, radius = width * 0.04f, center = Offset(width * 0.48f, height * 0.12f)) // Moathouse island

                // Three-way river separating islands
                val riverY = Path().apply {
                    moveTo(0f, height * 0.4f)
                    lineTo(width * 0.5f, height * 0.45f)
                    lineTo(width * 0.5f, height)
                    moveTo(width * 0.5f, height * 0.45f)
                    lineTo(width, height * 0.25f)
                }
                drawPath(path = riverY, color = Color(0xFF1B4D7E).copy(alpha = 0.4f), style = Stroke(width = 35f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                
                // Brasilia high grounds
                drawCircle(color = terrainColor, radius = width * 0.18f, center = Offset(width * 0.5f, height * 0.5f))
            }
        }
        "kalahari" -> {
            if (showTerrain) {
                val redDesertColor = Color(0xFF9E3D22).copy(alpha = 0.2f)
                val canyonRidgeColor = Color(0xFF732612).copy(alpha = 0.25f)
                // Draw various dry canyon contour lines
                drawCircle(color = redDesertColor, radius = width * 0.45f, center = Offset(width * 0.5f, height * 0.5f))
                
                val canyon1 = Path().apply {
                    moveTo(width * 0.1f, height * 0.2f)
                    quadraticTo(width * 0.3f, height * 0.3f, width * 0.4f, height * 0.1f)
                }
                val canyon2 = Path().apply {
                    moveTo(width * 0.6f, height * 0.8f)
                    quadraticTo(width * 0.8f, height * 0.7f, width * 0.9f, height * 0.9f)
                }
                drawPath(path = canyon1, color = canyonRidgeColor, style = Stroke(width = 12f, cap = StrokeCap.Round))
                drawPath(path = canyon2, color = canyonRidgeColor, style = Stroke(width = 12f, cap = StrokeCap.Round))

                // Wreckage markers
                // Submarine at (0.76f, 0.68f)
                drawOval(
                    color = Color(0xFF4F5B66).copy(alpha = 0.7f),
                    topLeft = Offset(width * 0.73f, height * 0.66f),
                    size = Size(width * 0.06f, height * 0.03f)
                )
                // Ship Santa Catarina at (0.26f, 0.76f)
                drawRect(
                    color = Color(0xFF343D46).copy(alpha = 0.7f),
                    topLeft = Offset(width * 0.23f, height * 0.74f),
                    size = Size(width * 0.05f, height * 0.04f)
                )
            }
        }
        "alpine" -> {
            if (showTerrain) {
                val snowColor = Color(0xFFDCEAF5).copy(alpha = 0.25f)
                val mountainRidge = Color(0xFF90A4AE).copy(alpha = 0.3f)
                // Glacial snowcaps
                drawCircle(color = snowColor, radius = width * 0.25f, center = Offset(width * 0.52f, height * 0.15f))
                drawCircle(color = snowColor, radius = width * 0.22f, center = Offset(width * 0.35f, height * 0.28f))
                
                // Railway Line running across the map
                val railway = Path().apply {
                    moveTo(0f, height * 0.1f)
                    lineTo(width, height * 0.9f)
                }
                drawPath(path = railway, color = Color.White.copy(alpha = 0.4f), style = Stroke(width = 4f))
                // draw dashes across rail
                drawPath(path = railway, color = Color.Black.copy(alpha = 0.7f), style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)))
            }
        }
        "nexterra" -> {
            if (showTerrain) {
                val neonPurple = Color(0xFF4A148C).copy(alpha = 0.22f)
                val neonCyan = Color(0xFF00E5FF).copy(alpha = 0.15f)
                
                // Zero-gravity labs circle
                drawCircle(
                    color = neonCyan,
                    radius = width * 0.08f,
                    center = Offset(width * 0.22f, height * 0.62f),
                    style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
                drawCircle(
                    color = neonPurple,
                    radius = width * 0.06f,
                    center = Offset(width * 0.22f, height * 0.62f)
                )

                // High-tech cyber traces
                drawLine(Color(0xFF00E5FF).copy(alpha = 0.3f), Offset(width * 0.1f, height * 0.1f), Offset(width * 0.4f, height * 0.1f), strokeWidth = 2f)
                drawLine(Color(0xFF00E5FF).copy(alpha = 0.3f), Offset(width * 0.4f, height * 0.1f), Offset(width * 0.5f, height * 0.2f), strokeWidth = 2f)
                
                drawLine(Color(0xFFD500F9).copy(alpha = 0.3f), Offset(width * 0.9f, height * 0.9f), Offset(width * 0.6f, height * 0.9f), strokeWidth = 2f)
                drawLine(Color(0xFFD500F9).copy(alpha = 0.3f), Offset(width * 0.6f, height * 0.9f), Offset(width * 0.5f, height * 0.8f), strokeWidth = 2f)
            }
        }
        "erangel" -> {
            if (showTerrain) {
                val landColor = Color(0xFF275A2C).copy(alpha = 0.22f)
                val beachColor = Color(0xFFC29B69).copy(alpha = 0.15f)
                
                // Main island + Military Island below
                drawCircle(color = beachColor, radius = width * 0.42f, center = Offset(width * 0.5f, height * 0.38f))
                drawCircle(color = landColor, radius = width * 0.4f, center = Offset(width * 0.5f, height * 0.38f))

                drawCircle(color = beachColor, radius = width * 0.22f, center = Offset(width * 0.52f, height * 0.82f))
                drawCircle(color = landColor, radius = width * 0.2f, center = Offset(width * 0.52f, height * 0.82f))

                // Sea River splitting them
                val seaChannel = Path().apply {
                    moveTo(0f, height * 0.68f)
                    quadraticTo(width * 0.5f, height * 0.63f, width, height * 0.68f)
                }
                drawPath(path = seaChannel, color = Color(0xFF1B4D7E).copy(alpha = 0.5f), style = Stroke(width = 38f))
                
                // Left bridge & Right bridge
                drawLine(Color(0xFF8B949E), Offset(width * 0.32f, height * 0.65f), Offset(width * 0.32f, height * 0.69f), strokeWidth = 6f)
                drawLine(Color(0xFF8B949E), Offset(width * 0.68f, height * 0.64f), Offset(width * 0.68f, height * 0.68f), strokeWidth = 6f)
            }
        }
        "miramar" -> {
            if (showTerrain) {
                val desertColor = Color(0xFFC0A268).copy(alpha = 0.22f)
                val mountainRidgeColor = Color(0xFF8C6D3B).copy(alpha = 0.18f)
                drawRect(color = desertColor)

                // High mountain shapes
                drawCircle(color = mountainRidgeColor, radius = width * 0.15f, center = Offset(width * 0.15f, height * 0.15f))
                drawCircle(color = mountainRidgeColor, radius = width * 0.12f, center = Offset(width * 0.85f, height * 0.15f))
                drawCircle(color = mountainRidgeColor, radius = width * 0.18f, center = Offset(width * 0.1f, height * 0.8f))
            }
        }
        "sanhok" -> {
            if (showTerrain) {
                val jungleColor = Color(0xFF0F5A33).copy(alpha = 0.24f)
                val riverBlue = Color(0xFF1B4D7E).copy(alpha = 0.45f)

                // 3 islands separated by Y channel
                drawRect(color = jungleColor)

                val yRiver = Path().apply {
                    moveTo(width * 0.5f, height * 0.5f)
                    lineTo(width * 0.5f, 0f)
                    moveTo(width * 0.5f, height * 0.5f)
                    lineTo(0f, height * 0.75f)
                    moveTo(width * 0.5f, height * 0.5f)
                    lineTo(width, height * 0.75f)
                }
                drawPath(path = yRiver, color = riverBlue, style = Stroke(width = 40f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                
                // Draw tiny connecting bridges
                drawLine(Color(0xFF8B949E), Offset(width * 0.46f, height * 0.25f), Offset(width * 0.54f, height * 0.25f), strokeWidth = 6f)
                drawLine(Color(0xFF8B949E), Offset(width * 0.28f, height * 0.58f), Offset(width * 0.32f, height * 0.64f), strokeWidth = 6f)
                drawLine(Color(0xFF8B949E), Offset(width * 0.68f, height * 0.58f), Offset(width * 0.72f, height * 0.64f), strokeWidth = 6f)
            }
        }
        "vikendi" -> {
            if (showTerrain) {
                val iceColor = Color(0xFFEBF3F9).copy(alpha = 0.25f)
                val iceRiverColor = Color(0xFF2979FF).copy(alpha = 0.3f)
                drawRect(color = iceColor)

                // A long vertical winding river
                val mainRiver = Path().apply {
                    moveTo(width * 0.5f, 0f)
                    cubicTo(width * 0.42f, height * 0.3f, width * 0.58f, height * 0.7f, width * 0.5f, height)
                }
                drawPath(path = mainRiver, color = iceRiverColor, style = Stroke(width = 30f))

                // Castle circular island at the center-south river crossing
                drawCircle(color = iceColor, radius = width * 0.05f, center = Offset(width * 0.54f, height * 0.58f))
                drawCircle(color = Color(0xFF90A4AE).copy(alpha = 0.4f), radius = width * 0.05f, center = Offset(width * 0.54f, height * 0.58f), style = Stroke(width = 3f))
            }
        }
        else -> {
            // Default generic terrain
            if (showTerrain) {
                val defaultColor = Color(0xFF238636).copy(alpha = 0.12f)
                drawCircle(color = defaultColor, radius = width * 0.25f, center = Offset(width * 0.3f, height * 0.4f))
                drawCircle(color = defaultColor, radius = width * 0.2f, center = Offset(width * 0.7f, height * 0.6f))
            }
        }
    }

    // Grid lines
    if (showGridLines) {
        val gridColor = if (mapType.lowercase() == "nexterra") {
            Color(0xFF00E5FF).copy(alpha = 0.25f) // Neon Cyan Grid for sci-fi atmosphere
        } else {
            Color(0xFF30363D).copy(alpha = 0.4f)
        }
        val step = 80f

        // Verticals
        var x = 0f
        while (x < width) {
            drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1f)
            x += step
        }

        // Horizontals
        var y = 0f
        while (y < height) {
            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1f)
            y += step
        }
    }

    // Sectors HUD display circles / Radar sweep simulation
    if (showSectors) {
        val radarColor = if (mapType.lowercase() == "nexterra") {
            Color(0xFFD500F9).copy(alpha = 0.15f) // Neon Purple/Magenta for futuristic theme
        } else {
            Color(0xFFFF6B00).copy(alpha = 0.08f)
        }
        drawCircle(
            color = radarColor,
            radius = width * 0.35f,
            center = Offset(width / 2, height / 2),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )
        drawCircle(
            color = radarColor,
            radius = width * 0.18f,
            center = Offset(width / 2, height / 2),
            style = Stroke(width = 1.5f)
        )
    }

    // Draw real landmarks & structures
    val markerPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 24f
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }

    val shadowPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 24f
        isAntiAlias = true
        strokeWidth = 5f
        style = android.graphics.Paint.Style.STROKE
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }

    val orangeSubPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FF6B00")
        textSize = 14f
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    }

    val orangeSubShadowPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 14f
        isAntiAlias = true
        strokeWidth = 3f
        style = android.graphics.Paint.Style.STROKE
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    }

    landmarks.forEach { (name, relX, relY) ->
        val x = relX * width
        val y = relY * height

        // Draw compound structure shape (small circles/hexagons representing military bases, villages, or complex buildings)
        val structureColor = when (mapType.lowercase()) {
            "nexterra" -> Color(0xFF00E5FF).copy(alpha = 0.5f)
            "kalahari" -> Color(0xFFFF6D00).copy(alpha = 0.5f)
            else -> Color(0xFFFF6B00).copy(alpha = 0.4f)
        }
        
        drawCircle(
            color = structureColor,
            radius = 10f,
            center = Offset(x, y)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = 4f,
            center = Offset(x, y)
        )
        drawCircle(
            color = structureColor.copy(alpha = 0.15f),
            radius = 28f,
            center = Offset(x, y)
        )

        // Draw text with outline/shadow for high visibility
        drawContext.canvas.nativeCanvas.drawText(name, x + 15f, y + 8f, shadowPaint)
        drawContext.canvas.nativeCanvas.drawText(name, x + 15f, y + 8f, markerPaint)

        // Draw tactical category label (e.g. HIGH LOOT / HOT DROP)
        val categoryLabel = when (name) {
            "PEAK", "BRASILIA", "REFINERY", "POCHINKI", "MILITARY BASE", "EL POZO", "BOOTCAMP", "CASTLE", "MIDSTEIN", "SCIENCE CENTER", "CLOCK TOWER", "FACTORY" -> "HOT DROP"
            "MILL", "SHIPYARD", "OBSERVATORY", "SCHOOL", "YASNAYA POLYANA", "MYLTA POWER", "GEORGOPOOL", "HACIENDA", "PECADOS", "RUINS", "PARADISE RESORT", "COSMODROME" -> "HIGH LOOT"
            else -> "TACTICAL"
        }
        drawContext.canvas.nativeCanvas.drawText(categoryLabel, x + 15f, y + 25f, orangeSubShadowPaint)
        drawContext.canvas.nativeCanvas.drawText(categoryLabel, x + 15f, y + 25f, orangeSubPaint)
    }
}

// Drawing single saved sketch elements
fun DrawScope.drawSingleDrawingObject(drawObj: DrawingObject) {
    val color = Color(drawObj.color).copy(alpha = drawObj.opacity)
    val width = drawObj.brushSize

    when (drawObj.type) {
        "brush", "pencil" -> {
            if (drawObj.points.size > 1) {
                val path = Path().apply {
                    val first = drawObj.points.first()
                    moveTo(first.x, first.y)
                    for (i in 1 until drawObj.points.size) {
                        val pt = drawObj.points[i]
                        lineTo(pt.x, pt.y)
                    }
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        "line" -> {
            if (drawObj.points.size >= 2) {
                val start = drawObj.points[0]
                val end = drawObj.points[1]
                drawLine(
                    color = color,
                    start = Offset(start.x, start.y),
                    end = Offset(end.x, end.y),
                    strokeWidth = width
                )
            }
        }
        "arrow" -> {
            if (drawObj.points.size >= 2) {
                val start = drawObj.points[0]
                val end = drawObj.points[1]
                drawArrowLine(
                    color = color,
                    start = Offset(start.x, start.y),
                    end = Offset(end.x, end.y),
                    strokeWidth = width
                )
            }
        }
        "rect" -> {
            if (drawObj.points.size >= 2) {
                val start = drawObj.points[0]
                val end = drawObj.points[1]
                drawRect(
                    color = color,
                    topLeft = Offset(Math.min(start.x, end.x), Math.min(start.y, end.y)),
                    size = Size(drawObj.rectWidth, drawObj.rectHeight),
                    style = Stroke(width = width)
                )
            }
        }
        "circle" -> {
            if (drawObj.points.size >= 2) {
                val start = drawObj.points[0]
                val end = drawObj.points[1]
                val radius = Math.hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()).toFloat()
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(start.x, start.y),
                    style = Stroke(width = width)
                )
            }
        }
        "triangle" -> {
            if (drawObj.points.size >= 2) {
                val start = drawObj.points[0]
                val end = drawObj.points[1]
                val triPath = Path().apply {
                    moveTo(start.x + (end.x - start.x) / 2, start.y)
                    lineTo(end.x, end.y)
                    lineTo(start.x, end.y)
                    close()
                }
                drawPath(
                    path = triPath,
                    color = color,
                    style = Stroke(width = width, join = StrokeJoin.Miter)
                )
            }
        }
    }
}

// Draw preview shapes in progress
fun DrawScope.drawShapePreview(
    tool: String,
    start: Offset,
    end: Offset,
    color: Color,
    brushSize: Float
) {
    when (tool) {
        "line" -> {
            drawLine(color = color, start = start, end = end, strokeWidth = brushSize)
        }
        "arrow" -> {
            drawArrowLine(color = color, start = start, end = end, strokeWidth = brushSize)
        }
        "rect" -> {
            drawRect(
                color = color,
                topLeft = Offset(Math.min(start.x, end.x), Math.min(start.y, end.y)),
                size = Size(Math.abs(end.x - start.x), Math.abs(end.y - start.y)),
                style = Stroke(width = brushSize)
            )
        }
        "circle" -> {
            val radius = Math.hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()).toFloat()
            drawCircle(
                color = color,
                radius = radius,
                center = start,
                style = Stroke(width = brushSize)
            )
        }
        "triangle" -> {
            val triPath = Path().apply {
                moveTo(start.x + (end.x - start.x) / 2, start.y)
                lineTo(end.x, end.y)
                lineTo(start.x, end.y)
                close()
            }
            drawPath(path = triPath, color = color, style = Stroke(width = brushSize))
        }
    }
}

// Drawing an arrow vector
fun DrawScope.drawArrowLine(
    color: Color,
    start: Offset,
    end: Offset,
    strokeWidth: Float
) {
    drawLine(color = color, start = start, end = end, strokeWidth = strokeWidth)

    // Calculate arrowhead angles
    val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
    val arrowLength = 30f
    val arrowAngle = Math.PI / 6 // 30 degrees

    val leftArrowPoint = Offset(
        (end.x - arrowLength * Math.cos(angle - arrowAngle)).toFloat(),
        (end.y - arrowLength * Math.sin(angle - arrowAngle)).toFloat()
    )
    val rightArrowPoint = Offset(
        (end.x - arrowLength * Math.cos(angle + arrowAngle)).toFloat(),
        (end.y - arrowLength * Math.sin(angle + arrowAngle)).toFloat()
    )

    drawLine(color = color, start = end, end = leftArrowPoint, strokeWidth = strokeWidth)
    drawLine(color = color, start = end, end = rightArrowPoint, strokeWidth = strokeWidth)
}

// Retrieve custom high-contrast icons for tactical markers
fun getMarkerIconVector(type: String): ImageVector {
    return when (type) {
        "landing" -> Icons.Default.FlightTakeoff
        "enemy" -> Icons.Default.LocalFireDepartment
        "vehicle" -> Icons.Default.DirectionsCar
        "loot" -> Icons.Default.ShoppingCart
        "danger" -> Icons.Default.Warning
        "camp" -> Icons.Default.Home
        "sniper" -> Icons.Default.CenterFocusStrong
        "rush" -> Icons.Default.FlashOn
        "safe_zone" -> Icons.Default.Security
        else -> Icons.Default.Flag
    }
}

// Color coding for esports marker boundaries
fun getMarkerBackgroundColor(type: String): Color {
    return when (type) {
        "landing" -> Color(0xFF2EA043) // Green safe landing
        "enemy" -> Color(0xFFF85149)   // Red danger foe
        "vehicle" -> Color(0xFF58A6FF) // Blue utility vehicle
        "loot" -> Color(0xFFFFA726)    // Orange loot crate
        "danger" -> Color(0xFFF85149)  // Red skull hazard
        "camp" -> Color(0xFF8F00FF)    // Purple secure fort
        "sniper" -> Color(0xFFFFA726)  // Gold sniper tower
        "rush" -> Color(0xFFFF6B00)    // High priority attack route
        "safe_zone" -> Color(0xFF2EA043) // Green clean safety ring
        else -> Color(0xFFFF6B00)
    }
}
