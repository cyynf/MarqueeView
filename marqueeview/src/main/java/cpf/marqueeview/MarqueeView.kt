package cpf.marqueeview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*

/**
 * Author: cpf
 * Date: 2020/4/7
 * Email: cpf4263@gmail.com
 *
 *
 * Scrolling marquee
 */
class MarqueeView : View {
    @Px
    var textSize = 0f
        set(value) {
            paint.textSize = textSize
            field = value
        }

    @ColorInt
    var textColor = 0
        set(value) {
            paint.color = textColor
            field = value
        }

    /**
     * Scrolling speed
     */
    @FloatRange(from = 0.0, to = 1.0)
    var speed = 0f
        set(value) {
            if (value < 0 || value > 1) {
                throw Exception("Scrolling speed is [0.0,1.0]")
            }
            field = value
        }

    /**
     * Number of rolling repeats
     */
    var marqueeRepeatLimit = 0

    /**
     * The content is initially displayed relative to the view width offset
     */
    var offset = 0f

    var isFadingEdge = false

    /**
     * The currently displayed Marquee
     */
    var position = 0
        private set

    var isPause = true

    private var mListener: ((Int) -> Unit)? = null
    private var entries: ArrayList<String> = arrayListOf()
    private var paint: Paint = Paint()
    private var mX = 0f
    private var mY = 0f
    private var mText: String = ""
    private var job: Job? = null

    init {
        paint.isAntiAlias = true
    }

    fun setText(text: List<String>) {
        stop()
        entries.clear()
        entries.addAll(text)
        start()
    }

    fun setText(text: String?) {
        stop()
        entries.clear()
        text?.let {
            entries.add(it)
        }
        start()
    }

    fun setOnItemClickListener(block: ((Int) -> Unit)?) {
        mListener = block
    }

    constructor(context: Context?) : super(context) {
        init(true)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        val arr =
            context.obtainStyledAttributes(attrs, R.styleable.MarqueeView)
        textSize = arr.getDimension(
            R.styleable.MarqueeView_textSize, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                14f,
                resources.displayMetrics
            )
        )
        textColor = arr.getColor(
            R.styleable.MarqueeView_textColor,
            Color.WHITE
        )
        speed = arr.getFloat(
            R.styleable.MarqueeView_speed,
            Middle
        )
        marqueeRepeatLimit = arr.getInt(
            R.styleable.MarqueeView_marqueeRepeatLimit,
            MarqueeForever
        )
        val array =
            arr.getTextArray(R.styleable.MarqueeView_entries)
        if (array != null && array.isNotEmpty()) {
            entries.addAll(array.toList().map { it.toString() })
        }
        offset = arr.getFloat(R.styleable.MarqueeView_offset, 1f)
        isFadingEdge = arr.getBoolean(R.styleable.MarqueeView_fadingEdge, true)
        arr.recycle()
        init(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidthSize = MeasureSpec.getSize(widthMeasureSpec)
        val specHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val specHeightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (specHeightMode == MeasureSpec.AT_MOST) {
            val fontMetrics = paint.fontMetrics
            var calcHeight =
                (fontMetrics.bottom - fontMetrics.top + paddingTop + paddingBottom).toInt()
            if (calcHeight > specHeightSize) {
                calcHeight = specHeightSize
            }
            setMeasuredDimension(specWidthSize, calcHeight)
        } else {
            setMeasuredDimension(specWidthSize, specHeightSize)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (mListener != null && position >= 0) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                mListener!!.invoke(position)
            }
            return true
        }
        if (isClickable) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                callOnClick()
            }
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    private fun init(initDefault: Boolean) {
        if (initDefault) {
            textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                14f,
                resources.displayMetrics
            )
            textColor = Color.WHITE
            speed = Middle
            marqueeRepeatLimit = MarqueeForever
            offset = 1f
            isFadingEdge = true
        }
        paint.textSize = textSize
        paint.color = textColor
        if (entries.isNotEmpty()) {
            start()
        }
    }

    private val drawRectF: RectF
        get() = RectF(
            paddingStart.toFloat(),
            paddingTop.toFloat(),
            (width - paddingEnd).toFloat(),
            (height - paddingBottom).toFloat()
        )

    private fun setFadingEdge() {
        if (isFadingEdge) {
            val rectF = drawRectF
            val rate = textSize / rectF.width()
            paint.shader = LinearGradient(
                rectF.left,
                0f,
                rectF.right,
                0f,
                intArrayOf(
                    Color.TRANSPARENT,
                    textColor,
                    textColor,
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, rate, 1 - rate, 1f),
                Shader.TileMode.CLAMP
            )
        } else {
            paint.shader = null
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.clipRect(drawRectF)
        canvas.drawText(mText, mX + paddingStart, mY + paddingTop, paint)
    }

    fun bindToLifeCycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(
                source: LifecycleOwner,
                event: Lifecycle.Event
            ) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        start()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        stop()
                    }
                    Lifecycle.Event.ON_DESTROY -> lifecycle.removeObserver(this)
                    else -> {
                    }
                }
            }
        })
    }

    @Synchronized
    fun start() {
        if (!isPause || entries.isEmpty()) return
        stop()
        isPause = false
        drawTask()
    }

    @Synchronized
    fun stop() {
        if (isPause) return
        isPause = true
        runBlocking {
            job?.cancelAndJoin()
        }
    }

    private fun drawTask() {
        job = GlobalScope.launch {
            while (width == 0 || height == 0) {
                delay(16)
            }
            var repeatCount = marqueeRepeatLimit
            val rect = drawRectF
            val mWidth = rect.width()
            val mHeight = rect.height()
            val fontMetrics = paint.fontMetrics
            val baseline =
                mHeight / 2 + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
            val textWidthArray = FloatArray(entries.size)
            for (i in entries.indices) {
                textWidthArray[i] = paint.measureText(entries[i])
            }
            position = 0
            if (entries.size == 1) {
                val text = entries[0]
                val textWidth = textWidthArray[0]
                if (textWidth <= mWidth) {
                    paint.shader = null
                    mX = 0f
                    mY = baseline
                    mText = text
                    postInvalidate()
                    return@launch
                }
            }
            setFadingEdge()
            var x = offset * mWidth
            while (!isPause) {
                val textWidth = textWidthArray[position]
                if (x < 0 - textWidth) {
                    x = mWidth
                    if (++position >= entries.size) {
                        position = 0
                        if (marqueeRepeatLimit != MarqueeForever && --repeatCount < 0) {
                            position = -1
                            return@launch
                        }
                    }
                }
                val text = entries[position]
                mX = x
                mY = baseline
                mText = text
                postInvalidate()
                x -= speed * 2
                delay(5)
            }
        }
    }

    companion object {
        var Slow = 0.2f
        var Middle = 0.5f
        var Fast = 0.8f
        var MarqueeForever = -1
    }

}