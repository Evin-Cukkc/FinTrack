package com.evinn.fintrack;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

import com.evinn.fintrack.utils.CurrencyUtils;

/**
 * Grafik batang sederhana: pemasukan vs pengeluaran.
 * Dilengkapi garis bantu, label nominal ringkas, dan animasi saat data berubah.
 */
public class AnalyticsChartView extends View {

    private static final int GRID_LINES = 4;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private double income = 0;
    private double expense = 0;
    private float progress = 1f;
    private ValueAnimator animator;

    private int colorIncome;
    private int colorExpense;
    private int colorGrid;
    private int colorText;
    private int colorMuted;

    public AnalyticsChartView(Context context) {
        super(context);
        init(context);
    }

    public AnalyticsChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        colorIncome = ContextCompat.getColor(context, R.color.income_green);
        colorExpense = ContextCompat.getColor(context, R.color.expense_red);
        colorGrid = ContextCompat.getColor(context, R.color.outline);
        colorText = ContextCompat.getColor(context, R.color.text_primary);
        colorMuted = ContextCompat.getColor(context, R.color.text_secondary);

        gridPaint.setColor(colorGrid);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dpToPx(1f));
    }

    public void setData(double income, double expense) {
        if (this.income == income && this.expense == expense) {
            return;
        }

        this.income = income;
        this.expense = expense;

        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
        }
        super.onDetachedFromWindow();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float chartTop = dpToPx(28f);                 // ruang untuk label nominal
        float chartBottom = height - dpToPx(30f);     // ruang untuk label kategori
        float chartHeight = chartBottom - chartTop;

        double maxValue = Math.max(income, expense);

        // Tampilan jika data belum ada
        if (maxValue <= 0) {
            paint.setColor(colorMuted);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextSize(spToPx(13f));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(getContext().getString(R.string.chart_empty),
                    width / 2f, height / 2f, paint);
            return;
        }

        // Garis bantu horizontal
        for (int i = 0; i <= GRID_LINES - 1; i++) {
            float y = chartBottom - (chartHeight * i / (GRID_LINES - 1));
            canvas.drawLine(0f, y, width, y, gridPaint);
        }

        float barWidth = Math.min(dpToPx(72f), width * 0.24f);
        float gap = Math.min(dpToPx(56f), width * 0.18f);
        float groupWidth = barWidth * 2f + gap;
        float incomeLeft = (width - groupWidth) / 2f;
        float expenseLeft = incomeLeft + barWidth + gap;
        float cornerRadius = dpToPx(8f);

        drawBar(canvas, income, maxValue, chartBottom, chartHeight,
                incomeLeft, barWidth, cornerRadius, colorIncome);
        drawBar(canvas, expense, maxValue, chartBottom, chartHeight,
                expenseLeft, barWidth, cornerRadius, colorExpense);

        // Label kategori
        paint.setColor(colorMuted);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(spToPx(12f));
        paint.setTextAlign(Paint.Align.CENTER);

        float labelY = height - dpToPx(8f);
        canvas.drawText(getContext().getString(R.string.income),
                incomeLeft + barWidth / 2f, labelY, paint);
        canvas.drawText(getContext().getString(R.string.expense),
                expenseLeft + barWidth / 2f, labelY, paint);
    }

    private void drawBar(Canvas canvas, double value, double maxValue,
                         float chartBottom, float chartHeight,
                         float left, float barWidth, float cornerRadius, int color) {

        float fullHeight = (float) ((value / maxValue) * chartHeight);
        if (value > 0) {
            fullHeight = Math.max(fullHeight, dpToPx(3f));
        }
        float barHeight = fullHeight * progress;

        paint.setColor(color);
        barRect.set(left, chartBottom - barHeight, left + barWidth, chartBottom);
        canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, paint);

        // Label nominal di atas batang
        paint.setColor(colorText);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(spToPx(12f));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(CurrencyUtils.formatCompact(value),
                left + barWidth / 2f,
                chartBottom - barHeight - dpToPx(8f),
                paint);
    }
}
