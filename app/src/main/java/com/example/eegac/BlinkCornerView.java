// BlinkCornerView.java
package com.example.eegac;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

public class BlinkCornerView extends View {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Paint squarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hzPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightedHzPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int highlightedFreq = -1;

    private boolean tlOn = false; // Top-Left (前进) @ 6Hz
    private boolean trOn = false; // Top-Right (右转) @ 8Hz
    private boolean blOn = false; // Bottom-Left (左转) @ 11Hz
    private boolean brOn = false; // Bottom-Right (后退) @ 13Hz

    private static final long HALF_PERIOD_MS_TL = Math.round(1000.0 / (2.0 * 6.0));  // 6Hz
    private static final long HALF_PERIOD_MS_TR = Math.round(1000.0 / (2.0 * 8.0));  // 8Hz
    private static final long HALF_PERIOD_MS_BL = Math.round(1000.0 / (2.0 * 11.0)); // 11Hz
    private static final long HALF_PERIOD_MS_BR = Math.round(1000.0 / (2.0 * 13.0)); // 13Hz

    private final Runnable toggleTL = () -> { tlOn = !tlOn; postInvalidate(); handler.postDelayed(this.toggleTL, HALF_PERIOD_MS_TL); };
    private final Runnable toggleTR = () -> { trOn = !trOn; postInvalidate(); handler.postDelayed(this.toggleTR, HALF_PERIOD_MS_TR); };
    private final Runnable toggleBL = () -> { blOn = !blOn; postInvalidate(); handler.postDelayed(this.toggleBL, HALF_PERIOD_MS_BL); };
    private final Runnable toggleBR = () -> { brOn = !brOn; postInvalidate(); handler.postDelayed(this.toggleBR, HALF_PERIOD_MS_BR); };


    public BlinkCornerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        squarePaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(sp(24)); // 字体也相应缩小一点
        textPaint.setTextAlign(Paint.Align.CENTER);

        hzPaint.setColor(Color.LTGRAY);
        hzPaint.setTextSize(sp(12));
        hzPaint.setTextAlign(Paint.Align.CENTER);

        highlightedHzPaint.setColor(Color.GREEN);
        highlightedHzPaint.setTextSize(sp(14));
        highlightedHzPaint.setTextAlign(Paint.Align.CENTER);
        highlightedHzPaint.setFakeBoldText(true);

        setFocusable(true);
    }

    public void highlightFrequency(int freq) {
        this.highlightedFreq = freq;
        postInvalidate();
        handler.removeCallbacks(resetHighlight);
        if (freq > 0) {
            handler.postDelayed(resetHighlight, 500);
        }
    }

    private final Runnable resetHighlight = () -> {
        highlightedFreq = -1;
        postInvalidate();
    };


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.removeCallbacksAndMessages(null);
        handler.post(toggleTL);
        handler.post(toggleTR);
        handler.post(toggleBL);
        handler.post(toggleBR);
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
        drawFourCornerLayout(canvas);
    }

    private void drawFourCornerLayout(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        // --- 修改点：进一步调整边距和方块大小 ---
        float margin = dp(20); // 边距调整为更贴近角落
        float squareSize = (Math.min(w, h) / 4.0f); // 将尺寸除数增加到4.0，使方块更小

        // --- 绘制左上角 (前进) ---
        float tl_centerX = margin + squareSize / 2;
        float tl_centerY = margin + squareSize / 2;
        squarePaint.setColor(tlOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(margin, margin, margin + squareSize, margin + squareSize, squarePaint);
        canvas.drawText("前进", tl_centerX, tl_centerY + dp(8), textPaint);
        canvas.drawText("6 Hz", tl_centerX, tl_centerY + dp(30), (highlightedFreq == 6) ? highlightedHzPaint : hzPaint);


        // --- 绘制右上角 (右转) ---
        float tr_centerX = w - margin - squareSize / 2;
        float tr_centerY = margin + squareSize / 2;
        squarePaint.setColor(trOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(w - margin - squareSize, margin, w - margin, margin + squareSize, squarePaint);
        canvas.drawText("右转", tr_centerX, tr_centerY + dp(8), textPaint);
        canvas.drawText("8 Hz", tr_centerX, tr_centerY + dp(30), (highlightedFreq == 8) ? highlightedHzPaint : hzPaint);


        // --- 绘制左下角 (左转) ---
        float bl_centerX = margin + squareSize / 2;
        float bl_centerY = h - margin - squareSize / 2;
        squarePaint.setColor(blOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(margin, h - margin - squareSize, margin + squareSize, h - margin, squarePaint);
        canvas.drawText("左转", bl_centerX, bl_centerY + dp(8), textPaint);
        canvas.drawText("11 Hz", bl_centerX, bl_centerY - dp(20), (highlightedFreq == 11) ? highlightedHzPaint : hzPaint);

        // --- 绘制右下角 (后退) ---
        float br_centerX = w - margin - squareSize / 2;
        float br_centerY = h - margin - squareSize / 2;
        squarePaint.setColor(brOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(w - margin - squareSize, h - margin - squareSize, w - margin, h - margin, squarePaint);
        canvas.drawText("后退", br_centerX, br_centerY + dp(8), textPaint);
        canvas.drawText("13 Hz", br_centerX, br_centerY - dp(20), (highlightedFreq == 13) ? highlightedHzPaint : hzPaint);
    }


    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}