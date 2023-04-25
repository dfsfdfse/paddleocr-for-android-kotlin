package realcool.ocr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class FileUtils {
    companion object{
        fun getAssetsByFilename(ctx: Context, filename: String): Bitmap{
            val open = ctx.resources.assets.open(filename)
            return BitmapFactory.decodeStream(open)
        }
    }
}