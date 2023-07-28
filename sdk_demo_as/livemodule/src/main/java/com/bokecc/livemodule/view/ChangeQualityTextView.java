package com.bokecc.livemodule.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;

/**
 * 自定义清晰度Item选项
 */
public class ChangeQualityTextView extends android.support.v7.widget.AppCompatTextView {

    /**
     * 直播：
     * 0-原画 200-高清 300-流畅
     * 回放：
     * 根据返回清晰度描述决定
     */
    private int quality;

    /**
     * 清晰度描述
     * 目前只有回放的清晰度有描述
     */
    private String desp;
    // 是否选中
    private boolean isChecked;

    /**
     * 从3.14.0版本开始，由服務器返回清晰度描述为准
     */
    @Deprecated
    public ChangeQualityTextView(Context context, int quality) {
        this(context, null);
        this.quality = quality;
        if (quality == 0) {
            desp = "原画";
        } else if (quality == 200) {
            desp = "流畅";
        } else if (quality == 300) {
            desp = "标清";
        }
        setText(desp);
        initView();
    }

    // 构造回放Item显示View
    public ChangeQualityTextView(Context context, int quality, String desp) {
        this(context, null);
        this.quality = quality;
        this.desp = desp;
        setText(desp);
        initView();
    }

    public ChangeQualityTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initView() {
        setGravity(Gravity.CENTER);
        setTextSize(16);
        setTextColor(Color.parseColor("#ffffff"));
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
            setTextColor(Color.parseColor("#ffffff"));
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
     * 获取清晰度id
     *
     * @return quality
     */
    public int getQuality() {
        return quality;
    }

    /**
     * 获取清晰度描述
     *
     * @return desp
     */
    public String getDesp() {
        return desp;
    }
}
