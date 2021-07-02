package `in`.co.madhur.chatbubblesdemo

import `in`.co.madhur.chatbubblesdemo.NativeLoader.initNativeLibs
import android.app.Application
import android.os.Handler

/**
 * Created by madhur on 3/1/15.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        applicationHandler = Handler(instance!!.mainLooper)
        initNativeLibs(instance!!)
    }

    companion object {
        var instance: App? = null
            private set

        @JvmField
        @Volatile
        var applicationHandler: Handler? = null
    }
}