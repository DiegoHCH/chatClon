package `in`.co.madhur.chatbubblesdemo

import `in`.co.madhur.chatbubblesdemo.model.ChatMessage
import `in`.co.madhur.chatbubblesdemo.model.Status
import `in`.co.madhur.chatbubblesdemo.model.UserType
import `in`.co.madhur.chatbubblesdemo.widgets.Emoji
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by madhur on 17/01/15.
 */
class ChatListAdapter(chatMessages: ArrayList<ChatMessage>, context: MainActivity) : BaseAdapter() {
    private val chatMessages: ArrayList<ChatMessage>
    private val context: Context
    override fun getCount(): Int {
        return chatMessages.size
    }

    override fun getItem(position: Int): Any {
        return chatMessages[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var v: View? = null
        val message: ChatMessage = chatMessages[position]
        val holder1: ViewHolder1
        val holder2: ViewHolder2
        if (message.userType == UserType.SELF) {
            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user1_item, null, false)
                holder1 = ViewHolder1()
                holder1.messageTextView = v.findViewById<View>(R.id.message_text) as TextView
                holder1.timeTextView = v.findViewById<View>(R.id.time_text) as TextView
                v.tag = holder1
            } else {
                v = convertView
                holder1 = v.tag as ViewHolder1
            }
            holder1.messageTextView?.text = Emoji.replaceEmoji(
                message.messageText,
                    holder1.messageTextView!!.paint.fontMetricsInt,
                    AndroidUtilities.dp(16f)
            )
            holder1.timeTextView!!.text = SIMPLE_DATE_FORMAT.format(message.messageTime)
        } else if (message.userType == UserType.OTHER) {
            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user2_item, null, false)
                holder2 = ViewHolder2()
                holder2.messageTextView = v.findViewById<View>(R.id.message_text) as TextView
                holder2.timeTextView = v.findViewById<View>(R.id.time_text) as TextView
                holder2.messageStatus = v.findViewById<View>(R.id.user_reply_status) as ImageView
                v.tag = holder2
            } else {
                v = convertView
                holder2 = v.tag as ViewHolder2
            }
            holder2.messageTextView?.text = Emoji.replaceEmoji(
                message.messageText,
                holder2.messageTextView!!.paint.fontMetricsInt,
                AndroidUtilities.dp(16f)
            )
            //holder2.messageTextView.setText(message.getMessageText());
            holder2.timeTextView!!.text = SIMPLE_DATE_FORMAT.format(message.messageTime)
            if (message.messageStatus == Status.DELIVERED) {
                holder2.messageStatus!!.setImageDrawable(context.resources.getDrawable(R.drawable.ic_double_tick))
            } else if (message.messageStatus == Status.SENT) {
                holder2.messageStatus!!.setImageDrawable(context.resources.getDrawable(R.drawable.ic_single_tick))
            }
        }
        return v!!
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        val message: ChatMessage = chatMessages[position]
        return message.userType?.ordinal!!
    }

    private inner class ViewHolder1 {
        var messageTextView: TextView? = null
        var timeTextView: TextView? = null
    }

    private inner class ViewHolder2 {
        var messageStatus: ImageView? = null
        var messageTextView: TextView? = null
        var timeTextView: TextView? = null
    }

    companion object {
        val SIMPLE_DATE_FORMAT = SimpleDateFormat("HH:mm")
    }

    init {
        this.chatMessages = chatMessages
        this.context = context
    }
}


