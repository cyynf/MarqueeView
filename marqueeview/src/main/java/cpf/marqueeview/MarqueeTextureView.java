package cpf.marqueeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;

/**
 * Author: cpf
 * Date: 2020/4/7
 * Email: cpf4263@gmail.com
 * <p>
 * Scrolling marquee
 */
public class MarqueeTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    public static float Slow = 2f;
    public static float Middle = 3f;
    public static float Fast = 4f;
    public static int MarqueeForever = -1;

    private float textSize;

    private int textColor;

    private int backgroundColor;

    /**
     * Scrolling speed
     */
    private float speed;

    /**
     * Number of rolling repeats
     */
    private int marqueeRepeatLimit;

    /**
     * The content is initially displayed relative to the view width offset
     */
    private float offset;

    private boolean fadingEdge;

    /**
     * The currently displayed Marquee
     */
    private int position;

    private String[] entries;

    private boolean isRunning;

    private Paint paint;

    private Thread thread;

    private OnItemClickListener onItemClickListener;

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(@Px float textSize) {
        this.textSize = textSize;
        paint.setTextSize(textSize);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        paint.setColor(textColor);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getMarqueeRepeatLimit() {
        return marqueeRepeatLimit;
    }

    public void setMarqueeRepeatLimit(int marqueeRepeatLimit) {
        this.marqueeRepeatLimit = marqueeRepeatLimit;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public boolean isFadingEdge() {
        return fadingEdge;
    }

    public void setFadingEdge(boolean fadingEdge) {
        this.fadingEdge = fadingEdge;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setText(String... text) {
        entries = text;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public MarqueeTextureView(Context context) {
        super(context);
        init(true);
    }

    public MarqueeTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextureView);
        textSize = arr.getDimension(R.styleable.MarqueeTextureView_textSize, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                14f,
                getResources().getDisplayMetrics()
        ));
        textColor = arr.getColor(R.styleable.MarqueeTextureView_textColor, Color.WHITE);
        speed = arr.getFloat(R.styleable.MarqueeTextureView_speed, Middle);
        marqueeRepeatLimit = arr.getInt(
                R.styleable.MarqueeTextureView_marqueeRepeatLimit,
                MarqueeTextureView.MarqueeForever
        );
        CharSequence[] array = arr.getTextArray(R.styleable.MarqueeTextureView_entries);
        if (array != null && array.length > 0) {
            entries = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                entries[i] = array[i].toString();
            }
        }
        offset = arr.getFloat(R.styleable.MarqueeTextureView_offset, 1f);
        fadingEdge = arr.getBoolean(R.styleable.MarqueeTextureView_fadingEdge, true);
        backgroundColor = arr.getColor(R.styleable.MarqueeTextureView_backgroundColor, Color.TRANSPARENT);
        arr.recycle();
        init(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specHeightMode == MeasureSpec.AT_MOST) {
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            int calcHeight = (int) (fontMetrics.bottom - fontMetrics.top + getPaddingTop() + getPaddingBottom());
            if (calcHeight > specHeightSize) {
                calcHeight = specHeightSize;
            }
            setMeasuredDimension(specWidthSize, calcHeight);
        } else {
            setMeasuredDimension(specWidthSize, specHeightSize);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stop();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        setVisibility(visibility);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (onItemClickListener != null && position >= 0) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onItemClickListener.onClick(position);
            }
            return true;
        }
        if (isClickable()) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    private void init(boolean initDefault) {
        if (initDefault) {
            textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    14f,
                    getResources().getDisplayMetrics()
            );
            textColor = Color.WHITE;
            speed = Middle;
            marqueeRepeatLimit = MarqueeTextureView.MarqueeForever;
            offset = 1f;
            fadingEdge = true;
            backgroundColor = Color.TRANSPARENT;
        }
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        setOpaque(false);
        setSurfaceTextureListener(this);
    }

    private RectF getDrawRectF() {
        return new RectF(
                (float) getPaddingStart(),
                (float) getPaddingTop(),
                (float) (getWidth() - getPaddingEnd()),
                (float) (getHeight() - getPaddingBottom())
        );
    }

    private void setFadingEdge() {
        if (fadingEdge) {
            RectF rectF = getDrawRectF();
            float rate = textSize / rectF.width();
            paint.setShader(new LinearGradient(
                    rectF.left,
                    0f,
                    rectF.right,
                    0f,
                    new int[]{Color.TRANSPARENT, textColor, textColor, Color.TRANSPARENT},
                    new float[]{0f, rate, 1 - rate, 1f},
                    Shader.TileMode.CLAMP
            ));
        } else {
            paint.setShader(null);
        }
    }

    private void draw(String text, float x, float y) {
        if (!isRunning) return;
        Canvas canvas = lockCanvas();
        if (canvas == null) return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(backgroundColor);
        canvas.clipRect(getDrawRectF());
        canvas.drawText(text, x + getPaddingStart(), y + getPaddingTop(), paint);
        unlockCanvasAndPost(canvas);
    }

    public void start() {
        if (getSurfaceTexture() == null) return;
        if (thread != null && thread.isAlive()) {
            isRunning = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (entries != null && entries.length != 0) {
            isRunning = true;
            thread = new Thread(drawTask);
            thread.start();
        } else {
            isRunning = false;
            draw("", 0f, 0f);
        }
    }

    public void stop() {
        isRunning = false;
    }

    private Runnable drawTask = new Runnable() {
        @Override
        public void run() {
            int repeatCount = marqueeRepeatLimit;
            RectF rect = getDrawRectF();
            float mWidth = rect.width();
            float mHeight = rect.height();
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float baseline = mHeight / 2 + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
            float[] textWidthArray = new float[entries.length];
            for (int i = 0; i < entries.length; i++) {
                textWidthArray[i] = paint.measureText(entries[i]);
            }
            position = 0;
            if (entries.length == 1) {
                String text = entries[0];
                float textWidth = textWidthArray[0];
                if (textWidth <= mWidth) {
                    paint.setShader(null);
                    draw(text, 0f, baseline);
                    return;
                }
            }
            setFadingEdge();
            float x = offset * mWidth;
            while (isRunning) {
                float textWidth = textWidthArray[position];
                if (x < 0 - textWidth) {
                    x = mWidth;
                    if (++position >= entries.length) {
                        position = 0;
                        if (marqueeRepeatLimit != MarqueeForever && --repeatCount < 0) {
                            position = -1;
                            return;
                        }
                    }
                }
                String text = entries[position];
                draw(text, x, baseline);
                x -= 1f;
                long sleepMs = (long) (15 / speed);
                if (sleepMs < 1) {
                    sleepMs = 1;
                }
                // Expect a minimum of 60 frames
                else if (sleepMs > 16) {
                    sleepMs = 16;
                }
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
