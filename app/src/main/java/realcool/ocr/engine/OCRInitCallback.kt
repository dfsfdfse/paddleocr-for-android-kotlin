package realcool.ocr.engine

interface OCRInitCallback {
    fun onSuccess()

    fun onFail(e: Throwable)
}