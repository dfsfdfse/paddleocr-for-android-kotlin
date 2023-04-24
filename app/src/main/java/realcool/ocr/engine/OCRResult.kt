package realcool.ocr.engine

import android.graphics.Bitmap
import realcool.ocr.OCRResultModel

data class OCRResult (
    val outputImg: Bitmap,
    val result: List<OCRResultModel>
)