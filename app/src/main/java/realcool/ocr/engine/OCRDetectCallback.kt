package realcool.ocr.engine

interface OCRDetectCallback {
    fun onSuccess(result: String)

    fun onFail(e: Throwable)
}