package realcool.android

import android.content.Context
import android.graphics.*
import android.util.Log
import realcool.android.exception.OCRException
import java.io.File

class OCREngine() {
    private lateinit var ocr: OCRNative
    var config: OCRConfig = OCRConfig()
    lateinit var outputImg: Bitmap
    val outputTexts: ArrayList<String> by lazy { ArrayList() }

    constructor(
        modelPath: String,
        detModelName: String,
        clsModelName: String,
        recModelName: String
    ) : this() {
        config.modelPath = modelPath
        config.detModelName = detModelName
        config.clsModelName = clsModelName
        config.recModelName = recModelName
    }

    fun isLoaded() = this::ocr.isInitialized && ocr.isLoaded()

    fun init(ctx: Context): Boolean {
        release()
        if (config.modelPath.isEmpty()) {
            throw OCRException("训练模板文件夹为空")
        }
        var realPath = ""
        if (config.modelPath[0] != '/') {
            realPath = ctx.cacheDir.path + "/" + config.modelPath
            Utils.copyDirectoryFromAssets(ctx, config.modelPath, realPath)
        }
        val sp = realPath + File.separator
        val det = sp + config.detModelName
        val cls = sp + config.clsModelName
        val rec = sp + config.recModelName
        ocr = OCRNative(
            det,
            rec,
            cls,
            if (config.useOpencl) 1 else 0,
            config.cpuThreadNum,
            config.cpuPowerMode
        )
        loadLabel(ctx, config.labelPath)
        return true
    }

    fun exec(input: Bitmap): ArrayList<OCRResultModel> {
        val exec = ocr.exec(input, config.detLongSize, config.runDet, config.runCls, config.runRec)
        pickWords(exec)
        outputImg = input.copy(Bitmap.Config.ARGB_8888, true)
        if (config.drawPosBox) drawBox(outputImg, exec)
        return exec
    }

    fun detect(temp: Bitmap, origin: Bitmap): String {
        val start = System.currentTimeMillis()
        val detect = ocr.detect(temp, origin)
        val end = System.currentTimeMillis()
        Log.e("耗时", "${end - start}")
        var res = "detect为空"
        if (detect.isNotEmpty()) {
            res =
                "1:x:${detect[0]},y:${detect[1]},2:x:${detect[2]},y:${detect[3]},3:x:${detect[4]},y:${detect[5]},4:x:${detect[6]},y:${detect[7]}"
        }
        Log.e("检测匹配", res);
        return res
    }

    private fun pickWords(results: ArrayList<OCRResultModel>): ArrayList<OCRResultModel> {
        val wordLabels = config.wordLabels
        for (r in results) {
            val word = StringBuilder()
            for (i in r.wordIndex) {
                if (i >= 0 && i < wordLabels.size) {
                    word.append(wordLabels[i])
                } else {
                    word.append("×")
                }
            }
            r.label = word.toString()
            r.clsLabel = if (r.clsIdx == 1f) "180" else "0"
        }
        return results
    }

    private fun loadLabel(ctx: Context, labelPath: String? = null) {
        val wordLabels = config.wordLabels
        wordLabels.clear()
        wordLabels.add("black")
        try {
            if (labelPath == null) {
                wordLabels.clear()
                return
            }
            val open = ctx.assets.open(labelPath)
            val available = open.available()
            val lines = ByteArray(available)
            open.read(lines)
            open.close()
            val words = String(lines)
            val split = words.split("\n")
            for (w in split) wordLabels.add(w)
            wordLabels.add(" ")
        } catch (e: Exception) {
            throw OCRException("ocr 加载 labels 失败")
        }
    }

    private fun drawBox(output: Bitmap, results: ArrayList<OCRResultModel>) {
        val canvas = Canvas(output)
        val paintAlpha = Paint()
        paintAlpha.style = Paint.Style.FILL
        paintAlpha.color = Color.parseColor("#3B85F5")
        paintAlpha.alpha = 50

        val paint = Paint()
        paint.color = Color.parseColor("#3B85F5")
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE

        for (result in results) {
            val path = Path()
            val points = result.points
            if (points.size == 0) {
                continue
            }
            path.moveTo(points[0].x.toFloat(), points[0].y.toFloat())
            for (i in points.size - 1 downTo 0) {
                path.lineTo(points[i].x.toFloat(), points[i].y.toFloat())
            }
            canvas.drawPath(path, paint)
            canvas.drawPath(path, paintAlpha)
        }
    }

    fun release() {
        if (this::ocr.isInitialized) {
            ocr.destroy()
        }
    }
}