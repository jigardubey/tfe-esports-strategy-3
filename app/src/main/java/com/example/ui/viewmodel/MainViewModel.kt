package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = StrategyRepository(
        strategyDao = db.strategyDao(),
        matchDao = db.matchDao(),
        noteDao = db.noteDao()
    )

    // Navigation state
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // ==========================================
    // PERSISTENT CUSTOM IMPORTED GALLERY MAPS
    // ==========================================
    private val prefs = application.getSharedPreferences("strategy_prefs", Context.MODE_PRIVATE)
    private val _customImportedMaps = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val customImportedMaps: StateFlow<List<Pair<String, String>>> = _customImportedMaps.asStateFlow()

    init {
        loadCustomMaps()
    }

    private fun loadCustomMaps() {
        val saved = prefs.getString("custom_maps_list", null)
        if (saved != null) {
            try {
                // Parse simple format: "Name1|Uri1;;Name2|Uri2"
                val list = saved.split(";;").mapNotNull {
                    val parts = it.split("|")
                    if (parts.size == 2) parts[0] to parts[1] else null
                }
                _customImportedMaps.value = list
            } catch (e: Exception) {
                _customImportedMaps.value = getDefaultCustomMaps()
            }
        } else {
            _customImportedMaps.value = getDefaultCustomMaps()
        }
    }

    private fun getDefaultCustomMaps(): List<Pair<String, String>> {
        return listOf(
            "Sanhok Tactical Grid" to "sanhok_pro",
            "Vikendi Radar Layout" to "vikendi_pro"
        )
    }

    fun importCustomMap(name: String, uri: String) {
        val current = _customImportedMaps.value.toMutableList()
        current.add(name to uri)
        _customImportedMaps.value = current
        saveCustomMaps(current)
    }

    fun deleteCustomMap(name: String) {
        val current = _customImportedMaps.value.filter { it.first != name }
        _customImportedMaps.value = current
        saveCustomMaps(current)
    }

    private fun saveCustomMaps(list: List<Pair<String, String>>) {
        val serialized = list.joinToString(";;") { "${it.first}|${it.second}" }
        prefs.edit().putString("custom_maps_list", serialized).apply()
    }

    // Database states
    val strategies: StateFlow<List<StrategyEntity>> = repository.allStrategies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val notes: StateFlow<List<NoteEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                repository.allNotes
            } else {
                repository.searchNotes(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ==========================================
    // STRATEGY MAP PLANNER STATE
    // ==========================================
    private val _activeStrategy = MutableStateFlow<StrategyEntity?>(null)
    val activeStrategy: StateFlow<StrategyEntity?> = _activeStrategy.asStateFlow()

    private val _activeDrawings = MutableStateFlow<List<DrawingObject>>(emptyList())
    val activeDrawings: StateFlow<List<DrawingObject>> = _activeDrawings.asStateFlow()

    private val _activeMarkers = MutableStateFlow<List<MarkerObject>>(emptyList())
    val activeMarkers: StateFlow<List<MarkerObject>> = _activeMarkers.asStateFlow()

    // Undo/Redo stacks
    private val undoStack = mutableListOf<List<DrawingObject>>()
    private val redoStack = mutableListOf<List<DrawingObject>>()

    // Tool configuration
    private val _selectedTool = MutableStateFlow("brush") // "brush", "pencil", "line", "arrow", "rect", "circle", "triangle", "text", "eraser", "move"
    val selectedTool: StateFlow<String> = _selectedTool.asStateFlow()

    private val _brushColor = MutableStateFlow(0xFFFF6B00.toInt()) // Hex color int, orange primary default
    val brushColor: StateFlow<Int> = _brushColor.asStateFlow()

    private val _brushSize = MutableStateFlow(10f)
    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()

    private val _brushOpacity = MutableStateFlow(1.0f)
    val brushOpacity: StateFlow<Float> = _brushOpacity.asStateFlow()

    fun selectTool(tool: String) {
        _selectedTool.value = tool
    }

    fun setBrushColor(color: Int) {
        _brushColor.value = color
    }

    fun setBrushSize(size: Float) {
        _brushSize.value = size
    }

    fun setBrushOpacity(opacity: Float) {
        _brushOpacity.value = opacity
    }

    // Load an existing strategy to edit
    fun loadStrategy(strategy: StrategyEntity) {
        _activeStrategy.value = strategy
        _activeDrawings.value = JsonUtils.jsonToDrawings(strategy.drawingDataJson)
        _activeMarkers.value = JsonUtils.jsonToMarkers(strategy.markersDataJson)
        undoStack.clear()
        redoStack.clear()
        navigateTo("map_planner")
    }

    // Create a new strategy
    fun createNewStrategy(title: String, mapType: String, customMapPath: String? = null) {
        viewModelScope.launch {
            val entity = StrategyEntity(
                title = title,
                mapType = mapType,
                customMapPath = customMapPath,
                drawingDataJson = "[]",
                markersDataJson = "[]",
                lastUpdated = System.currentTimeMillis()
            )
            val generatedId = repository.insertStrategy(entity)
            val savedEntity = entity.copy(id = generatedId.toInt())
            loadStrategy(savedEntity)
        }
    }

    // Save current strategy changes
    fun saveCurrentStrategy(notes: String = "") {
        val current = _activeStrategy.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                drawingDataJson = JsonUtils.drawingsToJson(_activeDrawings.value),
                markersDataJson = JsonUtils.markersToJson(_activeMarkers.value),
                notes = if (notes.isNotEmpty()) notes else current.notes,
                lastUpdated = System.currentTimeMillis()
            )
            repository.insertStrategy(updated)
            _activeStrategy.value = updated
        }
    }

    // Auto-save is triggered when canvas/markers change
    private fun autoSave() {
        val current = _activeStrategy.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                drawingDataJson = JsonUtils.drawingsToJson(_activeDrawings.value),
                markersDataJson = JsonUtils.markersToJson(_activeMarkers.value),
                lastUpdated = System.currentTimeMillis()
            )
            repository.insertStrategy(updated)
            _activeStrategy.value = updated
        }
    }

    fun deleteStrategy(id: Int) {
        viewModelScope.launch {
            repository.deleteStrategyById(id)
            if (_activeStrategy.value?.id == id) {
                _activeStrategy.value = null
                _activeDrawings.value = emptyList()
                _activeMarkers.value = emptyList()
            }
        }
    }

    fun duplicateStrategy(strategy: StrategyEntity) {
        viewModelScope.launch {
            val dup = strategy.copy(
                id = 0,
                title = "${strategy.title} (Copy)",
                lastUpdated = System.currentTimeMillis()
            )
            repository.insertStrategy(dup)
        }
    }

    // Drawings drawing operations
    fun addDrawingObject(drawing: DrawingObject) {
        saveUndoState()
        val newList = _activeDrawings.value + drawing
        _activeDrawings.value = newList
        redoStack.clear()
        autoSave()
    }

    fun setDrawings(drawings: List<DrawingObject>) {
        saveUndoState()
        _activeDrawings.value = drawings
        autoSave()
    }

    private fun saveUndoState() {
        if (undoStack.size >= 30) {
            undoStack.removeAt(0)
        }
        undoStack.add(_activeDrawings.value.toList())
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_activeDrawings.value.toList())
            val previous = undoStack.removeAt(undoStack.size - 1)
            _activeDrawings.value = previous
            autoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_activeDrawings.value.toList())
            val next = redoStack.removeAt(redoStack.size - 1)
            _activeDrawings.value = next
            autoSave()
        }
    }

    fun clearAllDrawings() {
        if (_activeDrawings.value.isNotEmpty() || _activeMarkers.value.isNotEmpty()) {
            saveUndoState()
            _activeDrawings.value = emptyList()
            _activeMarkers.value = emptyList()
            autoSave()
        }
    }

    // Marker Operations
    fun addMarker(type: String, x: Float, y: Float) {
        val marker = MarkerObject(
            id = UUID.randomUUID().toString(),
            type = type,
            x = x,
            y = y,
            size = 36f,
            label = type.replaceFirstChar { it.uppercase() },
            notes = ""
        )
        _activeMarkers.value = _activeMarkers.value + marker
        autoSave()
    }

    fun updateMarker(updated: MarkerObject) {
        _activeMarkers.value = _activeMarkers.value.map {
            if (it.id == updated.id) updated else it
        }
        autoSave()
    }

    fun deleteMarker(id: String) {
        _activeMarkers.value = _activeMarkers.value.filter { it.id != id }
        autoSave()
    }

    // ==========================================
    // TOURNAMENT MATCH ACTIONS
    // ==========================================
    fun addOrUpdateMatch(id: Int = 0, title: String, game: String, dateTime: Long, notes: String, reminder: Boolean) {
        viewModelScope.launch {
            val match = MatchEntity(
                id = id,
                title = title,
                game = game,
                dateTime = dateTime,
                notes = notes,
                reminderActive = reminder
            )
            repository.insertMatch(match)
        }
    }

    fun deleteMatch(id: Int) {
        viewModelScope.launch {
            repository.deleteMatchById(id)
        }
    }

    // ==========================================
    // RICH NOTES ACTIONS
    // ==========================================
    fun addOrUpdateNote(id: Int = 0, title: String, content: String, category: String) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id,
                title = title,
                content = content,
                category = category,
                lastUpdated = System.currentTimeMillis()
            )
            repository.insertNote(note)
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    // ==========================================
    // SETTINGS STATE
    // ==========================================
    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(lang: String) {
        _language.value = lang
    }
}
