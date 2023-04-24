package realcool.android

import java.util.*

class OCRConfig {
    var modelPath: String = "models"
    var useOpencl: Boolean = false
    var cpuThreadNum: Int = 1
    var cpuPowerMode: String = PowerMode.LITE_POWER_FULL.name
    var detModelName: String = "det_db.nb"
    var clsModelName: String = "cls.nb"
    var recModelName: String = "rec_crnn.nb"
    var labelPath: String = "labels/ppocr_keys_v1.txt"
    var runDet: Boolean = true
    var runCls: Boolean = true
    var runRec: Boolean = true
    var drawPosBox: Boolean = true
    var detLongSize: Int = 960
    val wordLabels: Vector<String> by lazy { Vector() }

    enum class PowerMode {
        LITE_POWER_HIGH,
        LITE_POWER_LOW,
        LITE_POWER_FULL,
        LITE_POWER_NO_BIND,
        LITE_POWER_RAND_HIGH,
        LITE_POWER_RAND_LOW
    }
}