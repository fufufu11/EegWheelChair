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

/**
 * 黑色背景六方块闪烁视图。////
 * 上方: F, +, B
 * 下方: L, -, R
 * 频率:
 * F (上左): 6Hz
 * + (上中): 7Hz
 * B (上右): 8Hz
 * L (下左): 9Hz
 * - (下中): 11Hz
 * R (下右): 13Hz
 */
public class BlinkCornerView extends View {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Paint squarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 6个方块的亮灭状态
    private boolean tlOn = false; // Top-Left (F) @ 6Hz
    private boolean tcOn = false; // Top-Center (+) @ 7Hz
    private boolean trOn = false; // Top-Right (B) @ 8Hz
    private boolean blOn = false; // Bottom-Left (L) @ 9Hz
    private boolean bcOn = false; // Bottom-Center (-) @ 11Hz
    private boolean brOn = false; // Bottom-Right (R) @ 13Hz

    // 6个频率对应的半周期 (毫秒)
    private static final long HALF_PERIOD_MS_TL = Math.round(1000.0 / (2.0 * 6.0));
    private static final long HALF_PERIOD_MS_TC = Math.round(1000.0 / (2.0 * 7.0));
    private static final long HALF_PERIOD_MS_TR = Math.round(1000.0 / (2.0 * 8.0));
    private static final long HALF_PERIOD_MS_BL = Math.round(1000.0 / (2.0 * 9.0));
    private static final long HALF_PERIOD_MS_BC = Math.round(1000.0 / (2.0 * 11.0));
    private static final long HALF_PERIOD_MS_BR = Math.round(1000.0 / (2.0 * 13.0));


    // 6个独立的Runnable来控制闪烁
    private final Runnable toggleTL = () -> { tlOn = !tlOn; postInvalidate(); handler.postDelayed(this.toggleTL, HALF_PERIOD_MS_TL); };
    private final Runnable toggleTC = () -> { tcOn = !tcOn; postInvalidate(); handler.postDelayed(this.toggleTC, HALF_PERIOD_MS_TC); };
    private final Runnable toggleTR = () -> { trOn = !trOn; postInvalidate(); handler.postDelayed(this.toggleTR, HALF_PERIOD_MS_TR); };
    private final Runnable toggleBL = () -> { blOn = !blOn; postInvalidate(); handler.postDelayed(this.toggleBL, HALF_PERIOD_MS_BL); };
    private final Runnable toggleBC = () -> { bcOn = !bcOn; postInvalidate(); handler.postDelayed(this.toggleBC, HALF_PERIOD_MS_BC); };
    private final Runnable toggleBR = () -> { brOn = !brOn; postInvalidate(); handler.postDelayed(this.toggleBR, HALF_PERIOD_MS_BR); };


    public BlinkCornerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        squarePaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(sp(28));
        textPaint.setTextAlign(Paint.Align.CENTER);
        setFocusable(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.removeCallbacksAndMessages(null);
        // 启动所有闪烁任务
        handler.post(toggleTL);
        handler.post(toggleTC);
        handler.post(toggleTR);
        handler.post(toggleBL);
        handler.post(toggleBC);
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
        drawSixSquareLayout(canvas);
    }

    private void drawSixSquareLayout(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        float margin = dp(16);
        float squareSize = (Math.min(w, h) / 3f) - (margin * 1.5f);

        // --- 绘制上方三个方块 ---
        float topY = margin;
        // 上左 (F)
        float tl_left = margin;
        squarePaint.setColor(tlOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(tl_left, topY, tl_left + squareSize, topY + squareSize, squarePaint);
        canvas.drawText("F", tl_left + squareSize / 2, topY + squareSize / 2 + dp(10), textPaint);

        // 上中 (+)
        float tc_left = (w - squareSize) / 2f;
        squarePaint.setColor(tcOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(tc_left, topY, tc_left + squareSize, topY + squareSize, squarePaint);
        canvas.drawText("+", tc_left + squareSize / 2, topY + squareSize / 2 + dp(10), textPaint);

        // 上右 (B)
        float tr_left = w - margin - squareSize;
        squarePaint.setColor(trOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(tr_left, topY, tr_left + squareSize, topY + squareSize, squarePaint);
        canvas.drawText("B", tr_left + squareSize / 2, topY + squareSize / 2 + dp(10), textPaint);


        // --- 绘制下方三个方块 ---
        float bottomY = h - margin - squareSize;
        // 下左 (L)
        float bl_left = margin;
        squarePaint.setColor(blOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(bl_left, bottomY, bl_left + squareSize, bottomY + squareSize, squarePaint);
        canvas.drawText("L", bl_left + squareSize / 2, bottomY + squareSize / 2 + dp(10), textPaint);

        // 下中 (-)
        float bc_left = (w - squareSize) / 2f;
        squarePaint.setColor(bcOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(bc_left, bottomY, bc_left + squareSize, bottomY + squareSize, squarePaint);
        canvas.drawText("-", bc_left + squareSize / 2, bottomY + squareSize / 2 + dp(10), textPaint);

        // 下右 (R)
        float br_left = w - margin - squareSize;
        squarePaint.setColor(brOn ? Color.WHITE : Color.BLACK);
        canvas.drawRect(br_left, bottomY, br_left + squareSize, bottomY + squareSize, squarePaint);
        canvas.drawText("R", br_left + squareSize / 2, bottomY + squareSize / 2 + dp(10), textPaint);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}