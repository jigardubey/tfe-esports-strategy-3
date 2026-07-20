package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonUtils {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val drawingListType = Types.newParameterizedType(List::class.java, DrawingObject::class.java)
    private val markerListType = Types.newParameterizedType(List::class.java, MarkerObject::class.java)

    private val drawingAdapter = moshi.adapter<List<DrawingObject>>(drawingListType)
    private val markerAdapter = moshi.adapter<List<MarkerObject>>(markerListType)

    fun drawingsToJson(drawings: List<DrawingObject>): String {
        return try {
            drawingAdapter.toJson(drawings)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun jsonToDrawings(json: String?): List<DrawingObject> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            drawingAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun markersToJson(markers: List<MarkerObject>): String {
        return try {
            markerAdapter.toJson(markers)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun jsonToMarkers(json: String?): List<MarkerObject> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            markerAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
