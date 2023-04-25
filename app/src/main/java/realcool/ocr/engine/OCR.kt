package realcool.ocr.engine

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import realcool.android.OCRConfig
import realcool.android.OCREngine
import realcool.android.exception.OCRException

class OCR(_ctx: Context) {
    private var ocr: OCREngine = OCREngine()
    private var ctx: Context

    init {
        ctx = _ctx
    }

    @MainThread
    fun init(config: OCRConfig? = null, callback: OCRInitCallback) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch(Dispatchers.IO) {
            initSync(config).fold(
                {
                    coroutineScope.launch(Dispatchers.Main) {
                        if (it) callback.onSuccess() else callback.onFail(OCRException("未知错误"))
                    }
                },
                { coroutineScope.launch(Dispatchers.Main) { callback.onFail(it) } })
        }
    }

    @WorkerThread
    fun initSync(config: OCRConfig? = null): Result<Boolean> {
        if (config != null) {
            ocr.config = config
        }
        return try {
            Result.success(ocr.init(ctx))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    @MainThread
    fun exec(bitmap: Bitmap, callback: OCRExecCallback) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch(Dispatchers.IO) {
            execSync(bitmap).fold(
                {
                    coroutineScope.launch(Dispatchers.Main) { callback.onSuccess(it) }
                },
                { coroutineScope.launch(Dispatchers.Main) { callback.onFail(it) } })
        }
    }

    @WorkerThread
    fun execSync(bitmap: Bitmap): Result<OCRResult> {
        return if (!ocr.isLoaded()) {
            Result.failure(OCRException("请先加载模型"))
        } else {
            val exec = ocr.exec(bitmap)
            Result.success(OCRResult(ocr.outputImg, exec))
        }
    }

    @MainThread
    fun detect(temp: Bitmap, origin: Bitmap, callback: OCRDetectCallback) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch(Dispatchers.IO) {
            detectSync(temp, origin).fold(
                {
                    coroutineScope.launch(Dispatchers.Main) { callback.onSuccess(it) }
                },
                { coroutineScope.launch(Dispatchers.Main) { callback.onFail(it) } })
        }
    }

    @WorkerThread
    fun detectSync(temp: Bitmap, origin: Bitmap): Result<String> {
        return if (!ocr.isLoaded()) {
            Result.failure(OCRException("请先加载模型"))
        } else {
            val detect = ocr.detect(temp, origin)
            Result.success(detect)
        }
    }

    fun release() {
        ocr.release()
    }
}