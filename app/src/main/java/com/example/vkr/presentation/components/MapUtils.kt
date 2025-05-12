package com.example.vkr.ui.components

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.geometry.LinearRing

data class TeamArea(
    val teamId: String,
    val teamName: String,
    val points: List<Point>,
    val color: Int
)

fun parsePoints(json: String): List<Point> {
    val gson = Gson()
    val type = object : TypeToken<List<Map<String, Double>>>() {}.type
    val rawPoints: List<Map<String, Double>> = gson.fromJson(json, type)
    return rawPoints.map { Point(it["lat"]!!, it["lon"]!!) }
}

fun getPolygonCenter(points: List<Point>): Point {
    val avgLatitude = points.map { it.latitude }.average()
    val avgLongitude = points.map { it.longitude }.average()
    return Point(avgLatitude, avgLongitude)
}

fun isPointInsidePolygon(polygon: List<Point>, point: Point): Boolean {
    var crossings = 0
    for (i in polygon.indices) {
        val a = polygon[i]
        val j = (i + 1) % polygon.size
        val b = polygon[j]
        if (((a.latitude > point.latitude) != (b.latitude > point.latitude)) &&
            (point.longitude < (b.longitude - a.longitude) * (point.latitude - a.latitude) / (b.latitude - a.latitude) + a.longitude)
        ) {
            crossings++
        }
    }
    return (crossings % 2 == 1)
}