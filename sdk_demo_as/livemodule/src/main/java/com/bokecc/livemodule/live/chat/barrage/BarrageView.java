package com.bokecc.livemodule.live.chat.barrage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class BarrageView extends android.support.v7.widget.AppCompatTextView {

    private boolean isFinish = false;
    private TextView mOutTextView;

    private int mStrokeColor = Color.parseColor("#6E6E6E");
    private float mStrokeWidth = 5F;
    public BarrageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mOutTextView = new TextView(context, attrs,defStyle);
        TextPaint paint = mOutTextView.getPaint();
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);

        mOutTextView.setTextColor(mStrokeColor);
        mOutTextView.setGravity(getGravity());
    }

    public BarrageView(Context context) {
        super(context);
        mOutTextView = new TextView(context);
        TextPaint paint = mOutTextView.getPaint();
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);

        mOutTextView.setTextColor(mStrokeColor);
        mOutTextView.setGravity(getGravity());
    }

    public BarrageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOutTextView = new TextView(context, attrs);
        TextPaint paint = mOutTextView.getPaint();
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);

        mOutTextView.setTextColor(mStrokeColor);
        mOutTextView.setGravity(getGravity());
    }

    public boolean getFinish() {
        return isFinish;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CharSequence content = mOutTextView.getText();
        if ((content == null) || (!content.equals(getText()))) {
            mOutTextView.setText(getText());
            postInvalidate();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mOutTextView != null) {
            mOutTextView.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mOutTextView.layout(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mOutTextView.draw(canvas);
        super.onDraw(canvas);
    }
    @Override
    public void setGravity(int gravity) {
        super.setGravity(gravity);
        if (mOutTextView != null) {
            mOutTextView.setGravity(gravity);
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        if (mOutTextView != null) {
            mOutTextView.setLayoutParams(params);
        }
    }

    @Override
    public void setMinimumWidth(int minWidth) {
        super.setMinimumWidth(minWidth);
        if (mOutTextView != null) {
            mOutTextView.setMinWidth(minWidth);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (mOutTextView != null) {
            mOutTextView.setText(text);
        }
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        if (mOutTextView != null) {
            mOutTextView.setTextSize(size);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mOutTextView != null) {
            mOutTextView.setVisibility(visibility);
        }
    }
}