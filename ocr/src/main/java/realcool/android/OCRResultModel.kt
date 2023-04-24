package realcool.android

import android.graphics.Point

class OCRResultModel {
    val points: ArrayList<Point> by lazy { ArrayList() }
    val wordIndex: ArrayList<Int> by lazy { ArrayList() }
    var label: String = ""
    var confidence: Float = 0f
    var clsLabel: String = ""
    var clsIdx: Float = 0f
    var clsConfidence: Float = 0f
    fun addPoints(x: Int, y: Int) {
        val point = Point(x, y)
        points.add(point)
    }

    fun addWordIndex(index: Int) {
        wordIndex.add(index)
    }
}