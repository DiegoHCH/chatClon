package `in`.co.madhur.chatbubblesdemo.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * Created by madhur on 17/01/15.
 */
class ChatLayout : RelativeLayout {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs, 0) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val adjustVal = 12.667.toFloat()
        if (childCount < 3) return
        val imageViewWidth = getChildAt(0).measuredWidth
        val timeWidth = getChildAt(1).measuredWidth
        val messageHeight = getChildAt(2).measuredHeight
        val messageWidth = getChildAt(2).measuredWidth
        val layoutWidth = (imageViewWidth + timeWidth + messageWidth + convertDpToPixel(
            adjustVal,
            context
        )).toInt()
        setMeasuredDimension(layoutWidth, messageHeight)
    }

    companion object {
        /**
         * This method converts dp unit to equivalent pixels, depending on device density.
         *
         * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent px equivalent to dp depending on device density
         */
        fun convertDpToPixel(dp: Float, context: Context): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi / 160f)
        }
    }
}