package realcool.android

import android.graphics.Bitmap
import kotlin.math.roundToInt

class OCRNative(
    detModelPath: String,
    recModelPath: String,
    clsModelPath: String,
    useOpencl: Int,
    threadNum: Int,
    cpuPowerMode: String
) {
    private var pointer: Long = 0L

    companion object {
        fun loadLibrary() {
            try {
                System.loadLibrary("Native")
            } catch (e: Throwable) {
                throw RuntimeException(
                    "Load libNative.so failed, please check it exists in apk file.",
                    e
                )
            }
        }
    }

    init {
        loadLibrary()
        pointer = init(detModelPath, recModelPath, clsModelPath, useOpencl, threadNum, cpuPowerMode)
    }

    fun isLoaded() = pointer != 0L

    fun exec(
        img: Bitmap,
        detLongSize: Int,
        runDet: Boolean,
        runCls: Boolean,
        runRec: Boolean
    ): ArrayList<OCRResultModel> {
        val results: ArrayList<OCRResultModel> = ArrayList()
        var begin = 0
        val raw = forward(
            pointer, img, detLongSize,
            if (runDet) 1 else 0,
            if (runCls) 1 else 0,
            if (runRec) 1 else 0
        )
        while (begin < raw.size) {
            val pointNum = raw[begin].roundToInt()
            val wordNum = raw[begin + 1].roundToInt()
            val res = parse(
                raw,
                begin + 2,
                pointNum,
                wordNum
            )
            begin += 5 + pointNum * 2 + wordNum
            results.add(res)
        }
        return results
    }

    fun destroy() {
        if (pointer != 0L) {
            release(pointer)
            pointer = 0
        }
    }

    private fun parse(raw: FloatArray, begin: Int, pointNum: Int, wordNum: Int): OCRResultModel {
        var current = begin
        val res = OCRResultModel()
        res.confidence = raw[current]
        current++
        for (i in 0 until pointNum) {
            res.addPoints(Math.round(raw[current + i * 2]), raw[current + i * 2 + 1].roundToInt())
        }
        current += pointNum * 2
        for (i in 0 until wordNum) {
            res.addWordIndex(raw[current + i].roundToInt())
        }
        current += wordNum
        res.clsIdx = raw[current]
        res.clsConfidence = raw[current + 1]
        return res
    }

    private external fun init(
        detModelPath: String,
        recModelPath: String,
        clsModelPath: String,
        useOpencl: Int,
        threadNum: Int,
        cpuPowerMode: String
    ): Long

    private external fun forward(
        pointer: Long,
        image: Bitmap,
        maxSizeLen: Int,
        runDet: Int,
        runCls: Int,
        runRec: Int,
    ): FloatArray

    private external fun release(pointer: Long)

    external fun detect(temp: Bitmap, origin: Bitmap): IntArray
}