package `in`.co.madhur.chatbubblesdemo

import `in`.co.madhur.chatbubblesdemo.model.ChatMessage
import `in`.co.madhur.chatbubblesdemo.model.Status
import `in`.co.madhur.chatbubblesdemo.model.UserType
import `in`.co.madhur.chatbubblesdemo.widgets.Emoji
import `in`.co.madhur.chatbubblesdemo.widgets.EmojiView
import `in`.co.madhur.chatbubblesdemo.widgets.SizeNotifierRelativeLayout
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(),
    SizeNotifierRelativeLayout.SizeNotifierRelativeLayoutDelegate,
    NotificationCenter.NotificationCenterDelegate {
    private var chatListView: ListView? = null
    private var chatEditText1: EditText? = null
    private var chatMessages: ArrayList<ChatMessage>? = null
    private var enterChatView1: ImageView? = null
    private var emojiButton: ImageView? = null
    private val watcher1: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            if (chatEditText1!!.text.toString() == "") {
            } else {
                enterChatView1!!.setImageResource(R.drawable.ic_chat_send)
            }
        }

        override fun afterTextChanged(editable: Editable) {
            if (editable.length == 0) {
                enterChatView1!!.setImageResource(R.drawable.ic_chat_send)
            } else {
                enterChatView1!!.setImageResource(R.drawable.ic_chat_send_active)
            }
        }
    }
    private var listAdapter: ChatListAdapter? = null
    private var emojiView: EmojiView? = null
    private var sizeNotifierRelativeLayout: SizeNotifierRelativeLayout? = null

    /**
     * Check if the emoji popup is showing
     *
     * @return
     */
    var isEmojiPopupShowing = false
        private set
    private var keyboardHeight = 0
    private var keyboardVisible = false
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private val keyListener =
        View.OnKeyListener { v, keyCode, event -> // If the event is a key-down event on the "enter" button
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                // Perform action on key press
                val editText = v as EditText
                if (v === chatEditText1) {
                    sendMessage(editText.text.toString(), UserType.OTHER)
                }
                chatEditText1!!.setText("")
                return@OnKeyListener true
            }
            false
        }
    private val clickListener = View.OnClickListener { v ->
        if (v === enterChatView1) {
            sendMessage(chatEditText1!!.text.toString(), UserType.OTHER)
        }
        chatEditText1!!.setText("")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidUtilities.statusBarHeight = statusBarHeight
        chatMessages = ArrayList<ChatMessage>()
        chatListView = findViewById<ListView>(R.id.chat_list_view)
        chatEditText1 = findViewById<EditText>(R.id.chat_edit_text1)
        enterChatView1 = findViewById<ImageView>(R.id.enter_chat1)

        // Hide the emoji on click of edit text
        chatEditText1!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (isEmojiPopupShowing) hideEmojiPopup()
            }
        })
        emojiButton = findViewById<ImageView>(R.id.emojiButton)
        emojiButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                showEmojiPopup(!isEmojiPopupShowing)
            }
        })
        listAdapter = ChatListAdapter(chatMessages!!, this)
        chatListView!!.adapter = listAdapter
        chatEditText1!!.setOnKeyListener(keyListener)
        enterChatView1!!.setOnClickListener(clickListener)
        chatEditText1!!.addTextChangedListener(watcher1)
        ((R.id.chat_layout) as SizeNotifierRelativeLayout?).also { sizeNotifierRelativeLayout = it }
        sizeNotifierRelativeLayout?.delegate = this
        NotificationCenter.instance?.addObserver(this, NotificationCenter.emojiDidLoaded)
    }

    private fun sendMessage(messageText: String, userType: UserType) {
        if (messageText.trim { it <= ' ' }.isEmpty()) return
        val message = ChatMessage()
        message.messageStatus = Status.SENT
        message.messageText = messageText
        message.userType = UserType.SELF
        message.messageTime = Date().time
        chatMessages!!.add(message)
        if (listAdapter != null) listAdapter!!.notifyDataSetChanged()

        // Mark message as delivered after one second
        val exec = Executors.newScheduledThreadPool(1)
        exec.schedule({
            message.messageStatus = Status.DELIVERED
            val message = ChatMessage()
            message.messageStatus = Status.SENT
            message.messageText = messageText
            message.userType = UserType.SELF
            message.messageTime = Date().time
            chatMessages!!.add(message)
            this.runOnUiThread(Runnable { listAdapter!!.notifyDataSetChanged() })
        }, 1, TimeUnit.SECONDS)
    }

    private val activity: MainActivity
        private get() = this

    /**
     * Show or hide the emoji popup
     *
     * @param show
     */
    private fun showEmojiPopup(show: Boolean) {
        isEmojiPopupShowing = show
        if (show) {
            if (emojiView == null) {
                if (activity == null) {
                    return
                }
                emojiView = EmojiView(activity)
                emojiView!!.setListener(object : EmojiView.Listener {
                    override fun onBackspace() {
                        chatEditText1!!.dispatchKeyEvent(KeyEvent(0, 67))
                    }

                    override fun onEmojiSelected(paramString: String?) {
                        var i = chatEditText1!!.selectionEnd
                        if (i < 0) {
                            i = 0
                        }
                        val any = try {
                            val localCharSequence: CharSequence? = Emoji.replaceEmoji(
                                paramString,
                                chatEditText1!!.paint.fontMetricsInt,
                                AndroidUtilities.dp(20f)
                            )
                            chatEditText1!!.setText(
                                chatEditText1!!.text.insert(
                                    i,
                                    localCharSequence
                                )
                            )
                            val j = i + localCharSequence?.length!!
                            chatEditText1!!.setSelection(j, j)
                        } catch (e: Exception) {
                            Log.e(Constants.TAG, "Error showing emoji")
                        }
                    }

                })
                windowLayoutParams = WindowManager.LayoutParams()
                windowLayoutParams!!.gravity = Gravity.BOTTOM or Gravity.LEFT
                if (Build.VERSION.SDK_INT >= 21) {
                    windowLayoutParams!!.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
                } else {
                    windowLayoutParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                    windowLayoutParams!!.token = activity.window.decorView.windowToken
                }
                windowLayoutParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            val currentHeight: Int
            if (keyboardHeight <= 0) keyboardHeight =
                App.instance?.getSharedPreferences("emoji", 0)
                    ?.getInt("kbd_height", AndroidUtilities.dp(200f))!!
            currentHeight = keyboardHeight
            val wm = App.instance?.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
            windowLayoutParams!!.height = currentHeight
            windowLayoutParams!!.width = AndroidUtilities.displaySize.x
            try {
                if (emojiView!!.parent != null) {
                    wm.removeViewImmediate(emojiView)
                }
            } catch (e: Exception) {
                Log.e(Constants.TAG, e.message!!)
            }
            try {
                wm.addView(emojiView, windowLayoutParams)
            } catch (e: Exception) {
                Log.e(Constants.TAG, e.message!!)
                return
            }
            if (!keyboardVisible) {
                sizeNotifierRelativeLayout?.setPadding(0, 0, 0, currentHeight)
                return
            }
        } else {
            removeEmojiWindow()
            sizeNotifierRelativeLayout?.post(Runnable {
                sizeNotifierRelativeLayout?.setPadding(0, 0, 0, 0)
            })
        }
    }

    /**
     * Remove emoji window
     */
    private fun removeEmojiWindow() {
        if (emojiView == null) {
            return
        }
        try {
            if (emojiView!!.parent != null) {
                val wm = App.instance?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.removeViewImmediate(emojiView)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, e.message!!)
        }
    }

    /**
     * Hides the emoji popup
     */
    fun hideEmojiPopup() {
        if (isEmojiPopupShowing) {
            showEmojiPopup(false)
        }
    }

    /**
     * Updates emoji views when they are complete loading
     *
     * @param id
     * @param args
     */
    override fun didReceivedNotification(id: Int, vararg args: Any?) {
        if (id == NotificationCenter.emojiDidLoaded) {
            if (emojiView != null) {
                emojiView!!.invalidateViews()
            }
            if (chatListView != null) {
                chatListView!!.invalidateViews()
            }
        }
    }

    override fun onSizeChanged(height: Int) {
        val localRect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(localRect)
        val wm = App.instance?.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
        if (wm == null || wm.defaultDisplay == null) {
            return
        }
        if (height > AndroidUtilities.dp(50f) && keyboardVisible) {
            keyboardHeight = height
            App.instance?.getSharedPreferences("emoji", 0)?.edit()
                ?.putInt("kbd_height", keyboardHeight)?.commit()
        }
        if (isEmojiPopupShowing) {
            var newHeight = 0
            newHeight = keyboardHeight
            if (windowLayoutParams!!.width != AndroidUtilities.displaySize.x || windowLayoutParams!!.height != newHeight) {
                windowLayoutParams!!.width = AndroidUtilities.displaySize.x
                windowLayoutParams!!.height = newHeight
                wm.updateViewLayout(emojiView, windowLayoutParams)
                if (!keyboardVisible) {
                    sizeNotifierRelativeLayout?.post(Runnable {
                        if (sizeNotifierRelativeLayout != null) {
                            sizeNotifierRelativeLayout!!.setPadding(
                                0,
                                0,
                                0,
                                windowLayoutParams!!.height
                            )
                            sizeNotifierRelativeLayout!!.requestLayout()
                        }
                    })
                }
            }
        }
        val oldValue = keyboardVisible
        keyboardVisible = height > 0
        if (keyboardVisible && sizeNotifierRelativeLayout?.paddingBottom!! > 0) {
            showEmojiPopup(false)
        } else if (!keyboardVisible && keyboardVisible != oldValue && isEmojiPopupShowing) {
            showEmojiPopup(false)
        }
    }

     override fun onDestroy() {
         super.onDestroy()
         NotificationCenter.instance?.removeObserver(this, NotificationCenter.emojiDidLoaded)
    }

    /**
     * Get the system status bar height
     *
     * @return
     */
    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId: Int =
                resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    override fun onPause() {
        super.onPause()
        hideEmojiPopup()
    }
}