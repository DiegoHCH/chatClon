package `in`.co.madhur.chatbubblesdemo.widgets

import `in`.co.madhur.chatbubblesdemo.AndroidUtilities
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.RelativeLayout

class SizeNotifierRelativeLayout : RelativeLayout {
    private val rect = Rect()
    var delegate: SizeNotifierRelativeLayoutDelegate? = null

    interface SizeNotifierRelativeLayoutDelegate {
        fun onSizeChanged(keyboardHeight: Int)
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    /**
     * Calculate the soft keyboard height and report back to listener
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (delegate != null) {
            val rootView = this.rootView
            val usableViewHeight: Int =
                rootView.height - AndroidUtilities.statusBarHeight - AndroidUtilities.getViewInset(
                    rootView
                )
            getWindowVisibleDisplayFrame(rect)
            val keyboardHeight = usableViewHeight - (rect.bottom - rect.top)
            delegate!!.onSizeChanged(keyboardHeight)
        }
    }
}