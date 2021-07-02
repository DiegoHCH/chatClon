package `in`.co.madhur.chatbubblesdemo.widgets

import `in`.co.madhur.chatbubblesdemo.AndroidUtilities
import `in`.co.madhur.chatbubblesdemo.R
import android.content.Context
import android.database.DataSetObserver
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*

class EmojiView : LinearLayout {
    private val adapters = ArrayList<EmojiGridAdapter>()
    private val icons = intArrayOf(
        R.drawable.ic_emoji_recent,
        R.drawable.ic_emoji_smile,
        R.drawable.ic_emoji_flower,
        R.drawable.ic_emoji_bell,
        R.drawable.ic_emoji_car,
        R.drawable.ic_emoji_symbol
    )
    private var listener: Listener? = null
    private var pager: ViewPager? = null
    private var recentsWrap: FrameLayout? = null
    private val views = ArrayList<GridView>()

    constructor(paramContext: Context?) : super(paramContext) {
        init()
    }

    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?) : super(
        paramContext,
        paramAttributeSet
    ) {
        init()
    }

    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?, paramInt: Int) : super(
        paramContext,
        paramAttributeSet,
        paramInt
    ) {
        init()
    }

    private fun addToRecent(paramLong: Long) {
        if (pager!!.currentItem == 0) {
            return
        }
        val localArrayList = ArrayList<Long>()
        val currentRecent = Emoji.data[0]
        var was = false
        for (aCurrentRecent in currentRecent) {
            if (paramLong == aCurrentRecent) {
                localArrayList.add(0, paramLong)
                was = true
            } else {
                localArrayList.add(aCurrentRecent)
            }
        }
        if (!was) {
            localArrayList.add(0, paramLong)
        }
        Emoji.data[0] = LongArray(Math.min(localArrayList.size, 50))
        for (q in 0 until Emoji.data[0].size) {
            Emoji.data[0][q] = localArrayList[q]
        }
        adapters[0].data = Emoji.data[0]
        adapters[0].notifyDataSetChanged()
        saveRecents()
    }

    private fun convert(paramLong: Long): String {
        var str = ""
        var i = 0
        while (true) {
            if (i >= 4) {
                return str
            }
            val j = (0xFFFF and paramLong.toInt() shr 16 * (3 - i))
            if (j != 0) {
                str = str + j.toChar()
            }
            i++
        }
    }

    private fun init() {
        orientation = VERTICAL
        for (i in Emoji.data.indices) {
            val gridView = GridView(context)
            //  if (AndroidUtilities.isTablet()) {
            //     gridView.setColumnWidth(AndroidUtilities.dp(60));
            // } else {
            gridView.columnWidth = AndroidUtilities.dp(45f)
            // }
            gridView.numColumns = -1
            views.add(gridView)
            val localEmojiGridAdapter = EmojiGridAdapter(Emoji.data[i])
            gridView.adapter = localEmojiGridAdapter
            //  AndroidUtilities.setListViewEdgeEffectColor(gridView, 0xff999999);
            adapters.add(localEmojiGridAdapter)
        }
        setBackgroundColor(-0xddddde)
        pager = ViewPager(context)
        pager!!.adapter = EmojiPagesAdapter()
        val tabs = PagerSlidingTabStrip(context)
        tabs.setViewPager(pager!!)
        tabs.setShouldExpand(true)
        tabs.setIndicatorColor(-0xcc4a1b)
        tabs.setIndicatorHeight(AndroidUtilities.dp(2.0f))
        tabs.setUnderlineHeight(AndroidUtilities.dp(2.0f))
        tabs.setUnderlineColor(0x66000000)
        tabs.tabBackground = 0
        val localLinearLayout = LinearLayout(context)
        localLinearLayout.orientation = HORIZONTAL
        localLinearLayout.addView(
            tabs,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f)
        )
        val localImageView = ImageView(context)
        localImageView.setImageResource(R.drawable.ic_emoji_backspace)
        localImageView.scaleType = ImageView.ScaleType.CENTER
        localImageView.setBackgroundResource(R.drawable.bg_emoji_bs)
        localImageView.setOnClickListener {
            if (listener != null) {
                listener!!.onBackspace()
            }
        }
        localLinearLayout.addView(
            localImageView,
            LayoutParams(AndroidUtilities.dp(61f), LayoutParams.MATCH_PARENT)
        )
        recentsWrap = FrameLayout(context)
        recentsWrap!!.addView(views[0])
        val localTextView = TextView(context)
        localTextView.text = context.getString(R.string.NoRecent)
        localTextView.textSize = 18.0f
        localTextView.setTextColor(-7829368)
        localTextView.gravity = 17
        recentsWrap!!.addView(localTextView)
        views[0].emptyView = localTextView
        addView(localLinearLayout, LayoutParams(-1, AndroidUtilities.dp(48.0f)))
        addView(pager)
        loadRecents()
        if (Emoji.data[0] == null || Emoji.data[0].size == 0) {
            pager!!.currentItem = 1
        }
    }

    private fun saveRecents() {
        val localArrayList = ArrayList<Long?>()
        val arrayOfLong = Emoji.data[0]
        val i = arrayOfLong.size
        var j = 0
        while (true) {
            if (j >= i) {
                context.getSharedPreferences("emoji", 0).edit()
                    .putString("recents", TextUtils.join(",", localArrayList)).commit()
                return
            }
            localArrayList.add(arrayOfLong[j])
            j++
        }
    }

    fun loadRecents() {
        val str = context.getSharedPreferences("emoji", 0).getString("recents", "")
        var arrayOfString: Array<String>? = null
        if (str != null && str.length > 0) {
            arrayOfString = str.split(",").toTypedArray()
            Emoji.data[0] = LongArray(arrayOfString.size)
        }
        if (arrayOfString != null) {
            for (i in arrayOfString.indices) {
                Emoji.data[0][i] = arrayOfString[i].toLong()
            }
            adapters[0].data = Emoji.data[0]
            adapters[0].notifyDataSetChanged()
        }
    }

    public override fun onMeasure(paramInt1: Int, paramInt2: Int) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(paramInt1), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(paramInt2), MeasureSpec.EXACTLY
            )
        )
    }

    fun setListener(paramListener: Listener?) {
        listener = paramListener
    }

    fun invalidateViews() {
        for (gridView in views) {
            gridView?.invalidateViews()
        }
    }

    private inner class EmojiGridAdapter(var data: LongArray) : BaseAdapter() {
        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(i: Int): Any? {
            return null
        }

        override fun getItemId(i: Int): Long {
            return data[i]
        }

        override fun getView(i: Int, view: View, paramViewGroup: ViewGroup): View {
            var imageView = view as ImageView
            if (imageView == null) {
                imageView = object : AppCompatImageView(this@EmojiView.context) {
                    public override fun onMeasure(
                        paramAnonymousInt1: Int,
                        paramAnonymousInt2: Int
                    ) {
                        setMeasuredDimension(
                            MeasureSpec.getSize(paramAnonymousInt1),
                            MeasureSpec.getSize(paramAnonymousInt1)
                        )
                    }
                }
                imageView.setOnClickListener { view ->
                    if (listener != null) {
                        listener!!.onEmojiSelected(convert(view.tag as Long))
                    }
                    addToRecent(view.tag as Long)
                }
                imageView.setBackgroundResource(R.drawable.list_selector)
                imageView.setScaleType(ImageView.ScaleType.CENTER)
            }
            imageView.setImageDrawable(Emoji.getEmojiBigDrawable(data[i]))
            imageView.tag = data[i]
            return imageView
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer)
            }
        }
    }

    private inner class EmojiPagesAdapter : PagerAdapter(), PagerSlidingTabStrip.IconTabProvider {
        override fun destroyItem(paramViewGroup: ViewGroup, paramInt: Int, paramObject: Any) {
            val localObject: View?
            localObject = if (paramInt == 0) {
                recentsWrap
            } else {
                views[paramInt]
            }
            paramViewGroup.removeView(localObject)
        }

        override fun getCount(): Int {
            return views.size
        }

        override fun getPageIconResId(paramInt: Int): Int {
            return icons[paramInt]
        }

        override fun instantiateItem(paramViewGroup: ViewGroup, paramInt: Int): Any {
            val localObject: View
            localObject = if (paramInt == 0) {
                recentsWrap!!
            } else {
                views[paramInt]
            }
            paramViewGroup.addView(localObject)
            return localObject
        }

        override fun isViewFromObject(paramView: View, paramObject: Any): Boolean {
            return paramView === paramObject
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer)
            }
        }
    }

    interface Listener {
        fun onBackspace()
        fun onEmojiSelected(paramString: String?)
    }
}