package realcool.android

import android.content.Context
import java.io.*

class Utils {
    companion object {
        fun copyFileFromAssets(ctx: Context, srcPath: String, dstPath: String) {
            if (srcPath.isEmpty() || dstPath.isEmpty()) {
                return;
            }
            lateinit var inputStream: InputStream
            lateinit var outputStream: OutputStream
            try {
                inputStream = BufferedInputStream(ctx.assets.open(srcPath))
                outputStream = BufferedOutputStream(FileOutputStream(File(dstPath)))
                val buffer = ByteArray(1024)
                var read = inputStream.read(buffer)
                while (read != -1) {
                    outputStream.write(buffer, 0, read)
                    read = inputStream.read(buffer)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    outputStream.close()
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun copyDirectoryFromAssets(ctx: Context, srcDir: String, dstDir: String) {
            if (srcDir.isEmpty() || dstDir.isEmpty()) {
                return;
            }
            try {
                if (!File(dstDir).exists()) {
                    File(dstDir).mkdirs()
                }
                for (fileName in ctx.assets.list(srcDir)!!) {
                    val srcSubPath = srcDir + File.separator + fileName
                    val dstSubPath = dstDir + File.separator + fileName
                    if (File(srcSubPath).isDirectory) {
                        copyDirectoryFromAssets(ctx, srcSubPath, dstSubPath)
                    } else {
                        copyFileFromAssets(ctx, srcSubPath, dstSubPath)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}