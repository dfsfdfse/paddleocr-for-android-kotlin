package realcool.ocr

import android.content.Context
import realcool.ocr.exception.OCRException
import java.io.File

class OCREngine(_modelPath: String) {
    private lateinit var ocr: OCRNative
    var modelPath: String = ""
    var useOpencl: Boolean = false
    var detLongSize: Int = 960
    var cpuThreadNum: Int = 1
    var runDet: Boolean = true
    var runCls: Boolean = true
    var runRec: Boolean = true
    var cpuPowerMode: String = "LITE_POWER_HIGH"
    var detModelName: String = "det_db.nb"
    var clsModelName: String = "cls.nb"
    var recModelName: String = "rec_crnn.nb"

    init {
        modelPath = _modelPath
    }

    constructor(
        _modelPath: String,
        _detModelName: String,
        _clsModelName: String,
        _recModelName: String
    ) : this(_modelPath) {
        detModelName = _detModelName
        clsModelName = _clsModelName
        recModelName = _recModelName
    }

    fun init(ctx: Context) {
        release()
        if (modelPath.isEmpty()) {
            throw OCRException("训练模板文件夹为空")
        }
        var realPath = ""
        if (modelPath[0] != '/') {
            realPath = ctx.cacheDir.path + "/" + modelPath
            Utils.copyDirectoryFromAssets(ctx, modelPath, realPath)
        }
        val sp = realPath + File.separator
        val det = sp + detModelName
        val cls = sp + clsModelName
        val rec = sp + recModelName
        ocr = OCRNative(det, rec, cls, if (useOpencl) 1 else 0, cpuThreadNum, cpuPowerMode)
    }

    fun release() {
        if (this::ocr.isInitialized) {
            ocr.destroy()
        }
    }
}