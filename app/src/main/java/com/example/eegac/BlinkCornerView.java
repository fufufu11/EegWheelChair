package com.example.eegac;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

/**
 * 黑色背景四角闪烁方块视图。
 * 新增了UI状态管理，支持主菜单、温度菜单和模式菜单之间的切换。
 * 频率要求保持不变：
 * 左上 6Hz；右上 9Hz；左下 11Hz；右下 7Hz。
 */
public class BlinkCornerView extends View {

    // --- 新增：定义UI状态，加入 MODE_MENU ---
    public enum State {
        MAIN_MENU,  // 主菜单
        TEMP_MENU,  // 温度调节菜单
        MODE_MENU   // 模式选择菜单
    }

    private State currentState = State.MAIN_MENU; // 默认是主菜单

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Paint squarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean ltOn = false; // 左上 6Hz
    private boolean rtOn = false; // 右上 9Hz
    private boolean lbOn = false; // 左下 11Hz
    private boolean rbOn = false; // 右下 7Hz

    private static final long HALF_PERIOD_MS_LT = Math.round(1000.0 / (2.0 * 6.0));
    private static final long HALF_PERIOD_MS_RT = Math.round(1000.0 / (2.0 * 9.0));
    private static final long HALF_PERIOD_MS_LB = Math.round(1000.0 / (2.0 * 11.0));
    private static final long HALF_PERIOD_MS_RB = Math.round(1000.0 / (2.0 * 7.0));

    private final Runnable toggleLT = new Runnable() {
        @Override public void run() {
            ltOn = !ltOn;
            invalidate();
            handler.postDelayed(this, HALF_PERIOD_MS_LT);
        }
    };

    private final Runnable toggleRT = new Runnable() {
        @Override public void run() {
            rtOn = !rtOn;
            invalidate();
            handler.postDelayed(this, HALF_PERIOD_MS_RT);
        }
    };

    private final Runnable toggleLB = new Runnable() {
        @Override public void run() {
            lbOn = !lbOn;
            invalidate();
            handler.postDelayed(this, HALF_PERIOD_MS_LB);
        }
    };

    private final Runnable toggleRB = new Runnable() {
        @Override public void run() {
            rbOn = !rbOn;
            invalidate();
            handler.postDelayed(this, HALF_PERIOD_MS_RB);
        }
    };

    public BlinkCornerView(Context context) {
        super(context);
        init();
    }

    public BlinkCornerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlinkCornerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        squarePaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(sp(22));
        setFocusable(true);
    }

    public void setState(State newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            invalidate();
        }
    }

    public State getCurrentState() {
        return this.currentState;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.removeCallbacksAndMessages(null);
        handler.post(toggleLT);
        handler.post(toggleRT);
        handler.post(toggleLB);
        handler.post(toggleRB);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        // --- 核心修改：根据当前状态，调用不同的绘制方法 ---
        switch (currentState) {
            case MAIN_MENU:
                drawMainMenu(canvas);
                break;
            case TEMP_MENU:
                drawTempMenu(canvas);
                break;
            // --- 新增 case ---
            case MODE_MENU:
                drawModeMenu(canvas);
                break;
        }
    }

    /**
     * 绘制主菜单界面
     */
    private void drawMainMenu(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        float margin = dp(16);
        float square = Math.min(w, h) / 3f;

        // 左上: 开
        squarePaint.setColor(ltOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(margin, margin, margin + square, margin + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("开", margin, margin + square + dp(28), textPaint);

        // 右上: 关
        squarePaint.setColor(rtOn ? Color.WHITE : Color.BLACK);
        float rtLeft = w - margin - square;
        canvas.drawRect(rtLeft, margin, rtLeft + square, margin + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("关", rtLeft + square, margin + square + dp(28), textPaint);

        // 左下: 温度
        squarePaint.setColor(lbOn ? Color.WHITE : Color.BLACK);
        float lbTop = h - margin - square;
        canvas.drawRect(margin, lbTop, margin + square, lbTop + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("温度", margin, lbTop - dp(8), textPaint);

        // 右下: 模式
        squarePaint.setColor(rbOn ? Color.WHITE : Color.BLACK);
        float rbLeft = w - margin - square;
        float rbTop = h - margin - square;
        canvas.drawRect(rbLeft, rbTop, rbLeft + square, rbTop + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("模式", rbLeft + square, rbTop - dp(8), textPaint);
    }

    /**
     * 绘制温度调节界面
     */
    private void drawTempMenu(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        float margin = dp(16);
        float square = Math.min(w, h) / 3f;

        // 左上: 温度+
        squarePaint.setColor(ltOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(margin, margin, margin + square, margin + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("温度+", margin, margin + square + dp(28), textPaint);

        // 右上: 温度-
        squarePaint.setColor(rtOn ? Color.WHITE : Color.BLACK);
        float rtLeft = w - margin - square;
        canvas.drawRect(rtLeft, margin, rtLeft + square, margin + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("温度-", rtLeft + square, margin + square + dp(28), textPaint);

        // 左下: 返回
        squarePaint.setColor(lbOn ? Color.WHITE : Color.BLACK);
        float lbTop = h - margin - square;
        canvas.drawRect(margin, lbTop, margin + square, lbTop + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("返回", margin, lbTop - dp(8), textPaint);

        // 右下: 在此菜单中不使用，但方块保持闪烁
        squarePaint.setColor(rbOn ? Color.WHITE : Color.BLACK);
        float rbLeft = w - margin - square;
        float rbTop = h - margin - square;
        canvas.drawRect(rbLeft, rbTop, rbLeft + square, rbTop + square, squarePaint);
    }

    /**
     * --- 新增方法：绘制模式选择界面 ---
     */
    private void drawModeMenu(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        float margin = dp(16);
        float square = Math.min(w, h) / 3f;

        // 左上: 制冷
        squarePaint.setColor(ltOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(margin, margin, margin + square, margin + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("制冷", margin, margin + square + dp(28), textPaint);

        // 右上: 制热
        squarePaint.setColor(rtOn ? Color.WHITE : Color.BLACK);
        float rtLeft = w - margin - square;
        canvas.drawRect(rtLeft, margin, rtLeft + square, margin + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("制热", rtLeft + square, margin + square + dp(28), textPaint);

        // 左下: 返回
        squarePaint.setColor(lbOn ? Color.WHITE : Color.BLACK);
        float lbTop = h - margin - square;
        canvas.drawRect(margin, lbTop, margin + square, lbTop + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("返回", margin, lbTop - dp(8), textPaint);

        // 右下: 除湿
        squarePaint.setColor(rbOn ? Color.WHITE : Color.BLACK);
        float rbLeft = w - margin - square;
        float rbTop = h - margin - square;
        canvas.drawRect(rbLeft, rbTop, rbLeft + square, rbTop + square, squarePaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("除湿", rbLeft + square, rbTop - dp(8), textPaint);
    }


    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}