package realcool.ocr.engine

interface OCRExecCallback {
    fun onSuccess(result: OCRResult)
    fun onFail(e: Throwable)
}