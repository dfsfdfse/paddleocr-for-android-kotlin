package realcool.ocr

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import realcool.ocr.engine.OCR
import realcool.ocr.engine.OCRExecCallback
import realcool.ocr.engine.OCRInitCallback
import realcool.ocr.engine.OCRResult

class MainActivity : AppCompatActivity() {
    private lateinit var ocr: OCR
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ocr = OCR(this)

        val initBtn = findViewById<Button>(R.id.init_model)
        val startBtn = findViewById<Button>(R.id.start_model)
        val resultImg = findViewById<ImageView>(R.id.result_img)
        val resultText = findViewById<TextView>(R.id.result_text)

        initBtn.setOnClickListener {
            resultText.text = "开始加载模型"
            ocr.init(null, object : OCRInitCallback {
                override fun onSuccess() {
                    resultText.text = "加载模型成功"
                }

                @SuppressLint("SetTextI18n")
                override fun onFail(e: Throwable) {
                    resultText.text = "加载模型失败: ${e.message}"
                }
            })
        }
        startBtn.setOnClickListener {
            resultText.text = "开始识别"
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test4)
            ocr.exec(bitmap, object : OCRExecCallback {
                override fun onSuccess(result: OCRResult) {
                    var text = ""
                    result.result.forEachIndexed { index, res ->
                        Log.e("输出", "文字: ${res.label}")
                        text += "$index: 文字:${res.label} 文字方向: ${res.clsLabel}; 文字方向置信度: ${res.clsConfidence}; 识别置信度 ${res.confidence};文字索引:${res.wordIndex} 文字位置:${res.points}\n"
                    }
                    resultText.text = text
                    resultImg.setImageBitmap(result.outputImg)
                }

                @SuppressLint("SetTextI18n")
                override fun onFail(e: Throwable) {
                    resultText.text = "识别失败 $e"
                }

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ocr.release()
    }
}