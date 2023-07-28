package com.bokecc.livemodule.replay.room.rightview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.livemodule.view.ChangeSpeedTextView;
import com.bokecc.livemodule.view.RightBaseView;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;

/**
 * 倍速切换右侧弹出列表
 */
@SuppressLint("ViewConstructor")
public class ReplaySpeedView extends RightBaseView {
    //默认为正常倍速播放
    private float currentSpeed = 1.0f;
    private LinearLayout llSpeed;
    private SpeedCallBack speedCallBack;

    protected ReplaySpeedView(Context context) {
        super(context);
    }


    /**
     * 初始化
     */
    public void initViews() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.right_speed_view, null, false);
        llSpeed = inflate.findViewById(R.id.ll_speed);
        View root = inflate.findViewById(R.id.root);
        this.addView(inflate);
        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void setQualityCallBack(SpeedCallBack speedCallBack) {
        this.speedCallBack = speedCallBack;
    }

    public void setCurrentSpeed(float speed) {
        currentSpeed = speed;
        int num = llSpeed.getChildCount();
        for (int i = 0; i < num; i++) {
            View child = llSpeed.getChildAt(i);
            if (child instanceof ChangeSpeedTextView) {
                ChangeSpeedTextView changeSpeedTextView = (ChangeSpeedTextView) child;
                if (currentSpeed == changeSpeedTextView.getSpeed()) {
                    changeSpeedTextView.setTextColor(Color.parseColor("#F89E0F"));
                } else {
                    changeSpeedTextView.setTextColor(Color.parseColor("#ffffff"));
                }
            }
        }
    }

    public void setData(float[] speeds) {
        llSpeed.removeAllViews();
        if (speeds != null && speeds.length > 0) {
            for (int i = 0; i < speeds.length; i++) {
                float speed = speeds[i];
                ChangeSpeedTextView speedTextView = new ChangeSpeedTextView(getContext(), speed);
                if (speed == 0.5f) {
                    speedTextView.setText("0.5x");
                } else if (speed == 1.0f) {
                    speedTextView.setText("1.0x");
                } else if (speed == 1.25f) {
                    speedTextView.setText("1.25x");
                } else if (speed == 1.5f) {
                    speedTextView.setText("1.5x");
                }
                speedTextView.setTextSize(16);
                llSpeed.addView(speedTextView);
                if (i != 0) {
                    LayoutParams layoutParams = (LayoutParams) speedTextView.getLayoutParams();
                    layoutParams.topMargin = DensityUtil.dp2px(getContext(), 36);
                    speedTextView.setLayoutParams(layoutParams);
                }

                speedTextView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleItemClick(v);
                    }
                });
                if (currentSpeed == speed) {
                    speedTextView.setTextColor(Color.parseColor("#F89E0F"));
                } else {
                    speedTextView.setTextColor(Color.parseColor("#ffffff"));
                }
            }
        }
    }

    //切换倍速播放的点击事件
    private void handleItemClick(View v) {
        if (v instanceof ChangeSpeedTextView) {
            ChangeSpeedTextView changeSpeedTextView = (ChangeSpeedTextView) v;
            if (currentSpeed == changeSpeedTextView.getSpeed()) {
                return;
            }
            currentSpeed = changeSpeedTextView.getSpeed();
        }
        int num = llSpeed.getChildCount();
        for (int i = 0; i < num; i++) {
            View child = llSpeed.getChildAt(i);
            if (child instanceof ChangeSpeedTextView) {
                ChangeSpeedTextView changeSpeedTextView = (ChangeSpeedTextView) child;
                if (currentSpeed == changeSpeedTextView.getSpeed()) {
                    changeSpeedTextView.setTextColor(Color.parseColor("#F89E0F"));
                } else {
                    changeSpeedTextView.setTextColor(Color.parseColor("#ffffff"));
                }
            }
        }
        if (speedCallBack != null) {
            speedCallBack.speedChange(currentSpeed);
        }
    }


    public interface SpeedCallBack {
        void speedChange(float speed);
    }
}
