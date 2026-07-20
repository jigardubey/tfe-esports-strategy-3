package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strategies")
data class StrategyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val mapType: String, // "erangel", "miramar", "custom"
    val customMapPath: String? = null,
    val drawingDataJson: String = "[]",
    val markersDataJson: String = "[]",
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val game: String,
    val dateTime: Long,
    val notes: String = "",
    val reminderActive: Boolean = false
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "General", "Tactical", "Scouting", "Lineups"
    val lastUpdated: Long = System.currentTimeMillis()
)
