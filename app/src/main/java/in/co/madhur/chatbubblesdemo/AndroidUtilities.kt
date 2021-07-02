package `in`.co.madhur.chatbubblesdemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowManager
import java.io.*
import java.nio.channels.FileChannel

/**
 * Created by madhur on 3/1/15.
 */
object AndroidUtilities {
    @JvmField
    var density = 1f
    @JvmField
    var statusBarHeight = 0
    var displaySize = Point()
    @JvmStatic
    fun dp(value: Float): Int {
        return Math.ceil((density * value).toDouble()).toInt()
    }

    @JvmOverloads
    fun runOnUIThread(runnable: Runnable?, delay: Long = 0) {
        if (delay == 0L) {
            App.applicationHandler!!.post(runnable!!)
        } else {
            App.applicationHandler!!.postDelayed(runnable!!, delay)
        }
    }

    @JvmStatic
    external fun loadBitmap(
        path: String?,
        bitmap: Bitmap?,
        scale: Int,
        width: Int,
        height: Int,
        stride: Int
    )

    @Throws(IOException::class)
    fun copyFile(sourceFile: InputStream, destFile: File?): Boolean {
        val out: OutputStream = FileOutputStream(destFile)
        val buf = ByteArray(4096)
        var len: Int
        while (sourceFile.read(buf).also { len = it } > 0) {
            Thread.yield()
            out.write(buf, 0, len)
        }
        out.close()
        return true
    }

    @Throws(IOException::class)
    fun copyFile(sourceFile: File?, destFile: File): Boolean {
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        var source: FileChannel? = null
        var destination: FileChannel? = null
        try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination.transferFrom(source, 0, source.size())
        } catch (e: Exception) {
            //FileLog.e("tmessages", e);
            return false
        } finally {
            source?.close()
            destination?.close()
        }
        return true
    }

    fun checkDisplaySize() {
        try {
            val manager =
                App.instance?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (manager != null) {
                val display = manager.defaultDisplay
                if (display != null) {
                    if (Build.VERSION.SDK_INT < 13) {
                        displaySize[display.width] = display.height
                    } else {
                        display.getSize(displaySize)
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    @JvmStatic
    fun getViewInset(view: View?): Int {
        if (view == null || Build.VERSION.SDK_INT < 21) {
            return 0
        }
        try {
            val mAttachInfoField = View::class.java.getDeclaredField("mAttachInfo")
            mAttachInfoField.isAccessible = true
            val mAttachInfo = mAttachInfoField[view]
            if (mAttachInfo != null) {
                val mStableInsetsField = mAttachInfo.javaClass.getDeclaredField("mStableInsets")
                mStableInsetsField.isAccessible = true
                val insets = mStableInsetsField[mAttachInfo] as Rect
                return insets.bottom
            }
        } catch (e: Exception) {
            // FileLog.e("tmessages", e);
        }
        return 0
    }

    init {
        density = App.instance?.getResources()?.getDisplayMetrics()?.density!!
        checkDisplaySize()
    }
}