package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Point2D(val x: Float, val y: Float)

@JsonClass(generateAdapter = true)
data class DrawingObject(
    val id: String,
    val type: String, // "pencil", "brush", "line", "arrow", "rect", "circle", "triangle", "text"
    val points: List<Point2D> = emptyList(),
    val color: Int, // Hex Color Int
    val brushSize: Float,
    val opacity: Float,
    val text: String = "",
    val rectWidth: Float = 0f,
    val rectHeight: Float = 0f
)

@JsonClass(generateAdapter = true)
data class MarkerObject(
    val id: String,
    val type: String, // "landing", "enemy", "vehicle", "loot", "danger", "camp", "sniper", "rush", "safe_zone", "custom"
    val x: Float,
    val y: Float,
    val size: Float,
    val label: String,
    val notes: String = ""
)
