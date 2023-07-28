package com.bokecc.livemodule.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;

/**
 * 线路ItemView
 */
public class PortraitQualityTextView extends android.support.v7.widget.AppCompatTextView {

    private final static char[] cs = "零一二三四五六七八九".toCharArray();

    /**
     * 清晰度
     */
    private int quality;
    // 是否选中
    private boolean isChecked;
    private String desc;
    public PortraitQualityTextView(Context context, int quality, String desc) {
        this(context, null, quality,desc);
    }

    public PortraitQualityTextView(Context context, AttributeSet attrs, int quality, String desc) {
        super(context, attrs);
        this.quality = quality;
        this.desc=desc;
        initView();
        setText(desc);
    }

    private void initView() {
        setGravity(Gravity.CENTER);
        setTextSize(15);
    }

    /**
     * 是否选中
     *
     * @param isChecked isChecked
     */
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        if (isChecked) {
            setTextColor(Color.parseColor("#F89E0F"));
        } else {
            setTextColor(Color.parseColor("#333333"));
        }
    }

    /**
     * 是否选中
     *
     * @return isChecked
     */
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * 获取当前清晰度
     *
     * @return true
     */
    public int getQuality() {
        return quality;
    }

    public String getDesc() {
        return desc;
    }
}
