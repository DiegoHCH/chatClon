package `in`.co.madhur.chatbubblesdemo

import java.util.*

class NotificationCenter {
    private val observers = HashMap<Int, ArrayList<Any>>()
    private val removeAfterBroadcast = HashMap<Int, Any>()
    private val addAfterBroadcast = HashMap<Int, Any>()
    private var broadcasting = 0

    interface NotificationCenterDelegate {
        fun didReceivedNotification(id: Int, vararg args: Any?)
    }

    fun postNotificationName(id: Int, vararg args: Any?) {
        synchronized(observers) {
            broadcasting++
            val objects = observers[id]
            if (objects != null) {
                for (obj in objects) {
                    (obj as NotificationCenterDelegate).didReceivedNotification(id, *args)
                }
            }
            broadcasting--
            if (broadcasting == 0) {
                if (!removeAfterBroadcast.isEmpty()) {
                    for ((key, value) in removeAfterBroadcast) {
                        removeObserver(value, key)
                    }
                    removeAfterBroadcast.clear()
                }
                if (!addAfterBroadcast.isEmpty()) {
                    for ((key, value) in addAfterBroadcast) {
                        addObserver(value, key)
                    }
                    addAfterBroadcast.clear()
                }
            }
        }
    }

    fun addObserver(observer: Any, id: Int) {
        synchronized(observers) {
            if (broadcasting != 0) {
                addAfterBroadcast[id] = observer
                return
            }
            var objects = observers[id]
            if (objects == null) {
                observers[id] = ArrayList<Any>().also { objects = it }
            }
            if (objects!!.contains(observer)) {
                return
            }
            objects!!.add(observer)
        }
    }

    fun removeObserver(observer: Any, id: Int) {
        synchronized(observers) {
            if (broadcasting != 0) {
                removeAfterBroadcast[id] = observer
                return
            }
            val objects = observers[id]
            if (objects != null) {
                objects.remove(observer)
                if (objects.size == 0) {
                    observers.remove(id)
                }
            }
        }
    }

    companion object {
        const val emojiDidLoaded = 999

        @Volatile
        var instance: NotificationCenter? = null
            get() {
                var localInstance = field
                if (localInstance == null) {
                    synchronized(NotificationCenter::class.java) {
                        localInstance = field
                        if (localInstance == null) {
                            localInstance = NotificationCenter()
                            field = localInstance
                        }
                    }
                }
                return localInstance
            }
            private set
    }
}