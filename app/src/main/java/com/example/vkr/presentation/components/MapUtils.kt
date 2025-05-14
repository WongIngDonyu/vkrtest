package com.example.vkr.ui.components

import com.yandex.mapkit.geometry.Point
import org.json.JSONArray

data class TeamArea(val teamId: String, val teamName: String, val points: List<Point>, val color: Int)

fun parsePoints(json: String): List<Point> {
    val jsonArray = JSONArray(json)
    val points = mutableListOf<Point>()

    for (i in 0 until jsonArray.length()) {
        val pair = jsonArray.getJSONArray(i)
        val latitude = pair.getDouble(0)
        val longitude = pair.getDouble(1)
        points.add(Point(latitude, longitude))
    }

    return points
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