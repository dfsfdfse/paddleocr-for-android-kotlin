package realcool.ocr.engine

import android.graphics.Bitmap
import realcool.android.OCRResultModel

data class OCRResult (
    val outputImg: Bitmap,
    val result: List<OCRResultModel>
)