package cpf.marqueeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

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
public class MarqueeView extends SurfaceView implements SurfaceHolder.Callback {

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

    private int fps, maxFps;

    private boolean fadingEdge;

    /**
     * The currently displayed Marquee
     */
    private int position;

    private String[] entries;

    private boolean isRunning = true;

    private boolean isPause = false;

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
        start();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setFps(int fps) {
        if (fps < 30) {
            fps = 30;
        } else if (fps > maxFps) {
            fps = maxFps;
        }
        this.fps = fps;
    }

    public int getFps() {
        return fps;
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public MarqueeView(Context context) {
        super(context);
        init(true);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.obtainStyledAttributes(attrs, cpf.marqueeview.R.styleable.MarqueeView);
        textSize = arr.getDimension(cpf.marqueeview.R.styleable.MarqueeView_textSize, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                14f,
                getResources().getDisplayMetrics()
        ));
        textColor = arr.getColor(cpf.marqueeview.R.styleable.MarqueeView_textColor, Color.WHITE);
        speed = arr.getFloat(cpf.marqueeview.R.styleable.MarqueeView_speed, Middle);
        marqueeRepeatLimit = arr.getInt(
                cpf.marqueeview.R.styleable.MarqueeView_marqueeRepeatLimit,
                cpf.marqueeview.MarqueeView.MarqueeForever
        );
        CharSequence[] array = arr.getTextArray(cpf.marqueeview.R.styleable.MarqueeView_entries);
        if (array != null && array.length > 0) {
            entries = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                entries[i] = array[i].toString();
            }
        }
        offset = arr.getFloat(cpf.marqueeview.R.styleable.MarqueeView_offset, 1f);
        fadingEdge = arr.getBoolean(cpf.marqueeview.R.styleable.MarqueeView_fadingEdge, true);
        backgroundColor = arr.getColor(cpf.marqueeview.R.styleable.MarqueeView_backgroundColor, Color.BLACK);
        fps = arr.getInt(R.styleable.MarqueeView_fps, 60);
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
    public void surfaceCreated(SurfaceHolder holder) {
        resume();
        if (isRunning) {
            start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            clear(false);
        } else {
            clear(true);
        }
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
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                callOnClick();
            }
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
            marqueeRepeatLimit = cpf.marqueeview.MarqueeView.MarqueeForever;
            offset = 1f;
            fadingEdge = true;
            backgroundColor = Color.BLACK;
            fps = 60;
        }
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        maxFps = (int) (windowManager != null ? windowManager.getDefaultDisplay().getRefreshRate() : 60);
        if (fps < 30) {
            fps = 30;
        } else if (fps > maxFps) {
            fps = maxFps;
        }
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
        if (!isRunning || isPause) return;
        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(backgroundColor);
        canvas.clipRect(getDrawRectF());
        canvas.drawText(text, x + getPaddingStart(), y + getPaddingTop(), paint);
        getHolder().unlockCanvasAndPost(canvas);
    }

    public void clear(boolean onlyText) {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (onlyText) {
            canvas.drawColor(backgroundColor);
        }
        getHolder().unlockCanvasAndPost(canvas);
    }

    public void start() {
        if (!getHolder().getSurface().isValid()) return;
        stop();
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
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void resume() {
        isPause = false;
        clear(true);
    }

    private void pause() {
        isPause = true;
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
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
            long lastTime = System.currentTimeMillis();
            long targetTs = 1000 / fps;
            while (isRunning && !isPause) {
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
                x -= speed;
                long sleepMs = (targetTs - System.currentTimeMillis() + lastTime) / 2;
                if (sleepMs > 0) {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException ignored) {
                    }
                }
                lastTime = System.currentTimeMillis();
            }
        }
    };
}
