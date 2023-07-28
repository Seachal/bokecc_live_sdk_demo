package com.bokecc.livemodule.view;

import android.content.Context;
import android.view.Gravity;

public class ChangeSpeedTextView extends android.support.v7.widget.AppCompatTextView {
    /**
     * 倍速  目前有 0.5  1.0  1.5
     */
    private final float speed;

    public ChangeSpeedTextView(Context context, float speed) {
        super(context);
        this.speed=speed;
        initView();
    }

    private void initView() {
        setGravity(Gravity.CENTER);
        setTextSize(16);
    }

    public float getSpeed() {
        return speed;
    }
}
