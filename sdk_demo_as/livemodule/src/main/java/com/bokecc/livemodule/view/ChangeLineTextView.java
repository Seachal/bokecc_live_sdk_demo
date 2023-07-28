package com.bokecc.livemodule.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;

/**
 * 线路ItemView
 */
public class ChangeLineTextView extends android.support.v7.widget.AppCompatTextView {

    private final static char[] cs = "零一二三四五六七八九".toCharArray();

    /**
     * 线路索引
     */
    private int line;
    // 是否选中
    private boolean isChecked;

    public ChangeLineTextView(Context context, int line) {
        this(context, null, line);
    }

    public ChangeLineTextView(Context context, AttributeSet attrs, int line) {
        super(context, attrs);
        this.line = line;
        initView();
        setText(String.format("线路%s", (line + 1)));
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
    public void setChecked(boolean isChecked,boolean isPortrait) {
        this.isChecked = isChecked;
        if (isChecked) {
            setTextColor(Color.parseColor("#F89E0F"));
        } else {
            setTextColor(isPortrait?Color.parseColor("#333333"):Color.parseColor("#ffffff"));
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
     * 获取当前线路
     *
     * @return true
     */
    public int getLine() {
        return line;
    }

    /**
     * @param number 数字
     * @return 字符串
     */
    private String NumberToString(int number) {
        StringBuilder temp = new StringBuilder();
        while (number > 0) {
            temp.append(cs[number % 10]);
            number /= 10;
        }
        return temp.toString();

    }
}
