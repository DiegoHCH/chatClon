package `in`.co.madhur.chatbubblesdemo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipFile

object NativeLoader {
    private const val LIB_VERSION = 4
    private const val LIB_NAME = "chat." + LIB_VERSION
    private const val LIB_SO_NAME = "lib" + LIB_NAME + ".so"
    private const val LOCALE_LIB_SO_NAME = "lib" + LIB_NAME + "loc.so"

    @Volatile
    private var nativeLoaded = false
    private fun getNativeLibraryDir(context: Context?): File? {
        var f: File? = null
        if (context != null) {
            try {
                f =
                    File(ApplicationInfo::class.java.getField("nativeLibraryDir")[context.applicationInfo] as String)
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }
        if (f == null) {
            f = File(context!!.applicationInfo.dataDir, "lib")
        }
        return if (f != null && f.isDirectory) {
            f
        } else null
    }

    private fun loadFromZip(
        context: Context,
        destDir: File,
        destLocalFile: File?,
        folder: String?
    ): Boolean {
        try {
            for (file in destDir.listFiles()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, e.message!!)
        }
        var zipFile: ZipFile? = null
        var stream: InputStream? = null
        try {
            zipFile = ZipFile(context.applicationInfo.sourceDir)
            val entry = zipFile.getEntry("lib/" + folder + "/" + LIB_SO_NAME)
                ?: throw Exception("Unable to find file in apk:" + "lib/" + folder + "/" + LIB_NAME)
            stream = zipFile.getInputStream(entry)
            val out: OutputStream = FileOutputStream(destLocalFile)
            val buf = ByteArray(4096)
            var len: Int
            while (stream.read(buf).also { len = it } > 0) {
                Thread.yield()
                out.write(buf, 0, len)
            }
            out.close()
            if (Build.VERSION.SDK_INT >= 9) {
                destLocalFile!!.setReadable(true, false)
                destLocalFile.setExecutable(true, false)
                destLocalFile.setWritable(true)
            }
            try {
                System.load(destLocalFile!!.absolutePath)
                nativeLoaded = true
            } catch (e: Error) {
                Log.e(Constants.TAG, e.message!!)
            }
            return true
        } catch (e: Exception) {
            Log.e(Constants.TAG, e.message!!)
        } finally {
            if (stream != null) {
                try {
                    stream.close()
                } catch (e: Exception) {
                    Log.e(Constants.TAG, e.message!!)
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (e: Exception) {
                    Log.e(Constants.TAG, e.message!!)
                }
            }
        }
        return false
    }

    @JvmStatic
    @Synchronized
    fun initNativeLibs(context: Context) {
        if (nativeLoaded) {
            return
        }
        try {
            var folder: String? = null
            folder = try {
                if (Build.CPU_ABI.equals("armeabi-v7a", ignoreCase = true)) {
                    "armeabi-v7a"
                } else if (Build.CPU_ABI.equals("armeabi", ignoreCase = true)) {
                    "armeabi"
                } else if (Build.CPU_ABI.equals("x86", ignoreCase = true)) {
                    "x86"
                } else if (Build.CPU_ABI.equals("mips", ignoreCase = true)) {
                    "mips"
                } else {
                    "armeabi"
                    //FileLog.e("tmessages", "Unsupported arch: " + Build.CPU_ABI);
                }
            } catch (e: Exception) {
                //  FileLog.e("tmessages", e);
                Log.e(Constants.TAG, e.message!!)
                "armeabi"
            }
            val javaArch = System.getProperty("os.arch")
            if (javaArch != null && javaArch.contains("686")) {
                folder = "x86"
            }
            var destFile = getNativeLibraryDir(context)
            if (destFile != null) {
                destFile = File(destFile, LIB_SO_NAME)
                if (destFile.exists()) {
                    try {
                        System.loadLibrary(LIB_NAME)
                        nativeLoaded = true
                        return
                    } catch (e: Error) {
                        Log.e(Constants.TAG, e.message!!)
                    }
                }
            }
            val destDir = File(context.filesDir, "lib")
            destDir.mkdirs()
            val destLocalFile = File(destDir, LOCALE_LIB_SO_NAME)
            if (destLocalFile != null && destLocalFile.exists()) {
                try {
                    System.load(destLocalFile.absolutePath)
                    nativeLoaded = true
                    return
                } catch (e: Error) {
                    Log.e(Constants.TAG, e.message!!)
                }
                destLocalFile.delete()
            }
            if (loadFromZip(context, destDir, destLocalFile, folder)) {
                return
            }

            /*
            folder = "x86";
                destLocalFile = new File(context.getFilesDir().getAbsolutePath() + "/libtmessages86.so");
                if (!loadFromZip(context, destLocalFile, folder)) {
                    destLocalFile = new File(context.getFilesDir().getAbsolutePath() + "/libtmessagesarm.so");
                    folder = "armeabi";
                    loadFromZip(context, destLocalFile, folder);
                }
             */
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            System.loadLibrary(LIB_NAME)
            nativeLoaded = true
        } catch (e: Error) {
            Log.e(Constants.TAG, e.message!!)
        }
    }
}