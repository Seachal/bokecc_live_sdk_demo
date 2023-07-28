package com.bokecc.livemodule.popup;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.popup.callback.SettingPopupInterface;
import com.bokecc.livemodule.popup.callback.SettingPopupWindowCallBack;
import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.livemodule.view.ChangeLineTextView;
import com.bokecc.livemodule.view.ChangeSpeedTextView;
import com.bokecc.livemodule.view.PortraitQualityTextView;
import com.bokecc.livemodule.view.WarpLinearLayout;
import com.bokecc.sdk.mobile.live.listener.LiveChangeSourceListener;
import com.bokecc.sdk.mobile.live.pojo.LiveLineInfo;
import com.bokecc.sdk.mobile.live.pojo.LiveQualityInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLineInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQualityinfo;

import java.util.List;

/**
 * 竖屏更多设置弹出框
 */
public class SettingPopupWindow extends BasePopupWindow {
    private final static String TAG = SettingPopupWindow.class.getSimpleName();
    //弹框内容的跟布局
    private LinearLayout root;
    //模式切换需要的view
    private View modeChange;
    private Switch audioSwitch;

    //线路切换中需要的view
    private View lineRoot;
    private WarpLinearLayout lineContentRoot;
    private ChangeLineTextView lineCheckedView;

    //清晰度切换需要的view
    private View qualityRoot;
    private WarpLinearLayout qualityContentRoot;
    private PortraitQualityTextView qualityCheckedView;

    //倍速需要用到的view
    private View speedRoot;
    private WarpLinearLayout speedContentRoot;
    private float currentSpeed = 1.0f;

    private SettingPopupInterface settingPopupInterface;


    public SettingPopupWindow(Context context) {
        super(context);
        init();
    }

    @Override
    public int getContentView() {
        return R.layout.popup_window_setting;
    }

    @Override
    public void init() {
        setOutsideTouchable(true);
        setAnimationStyle(R.style.showPopupAnimation);
        setFocusable(true);
        setFocusableInTouchMode(true);
        root = findviewById(R.id.content_root);
        findviewById(R.id.pop_bg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        findviewById(R.id.tv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private LinearLayout.LayoutParams createLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = DensityUtil.dp2px(mContext, 10);
        layoutParams.topMargin = DensityUtil.dp2px(mContext, 22);
        return layoutParams;
    }

    //region --------------------- 模式切换功能 ----------------
    //添加音频模式
    public void addModeChange() {
        if (modeChange != null) {
            root.removeView(modeChange);
        }
        modeChange = LayoutInflater.from(mContext).inflate(R.layout.setting_mode_change, null);
        audioSwitch = modeChange.findViewById(R.id.audio_switch);
        audioSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingPopupInterface != null) {
                    settingPopupInterface.onPlayMode(audioSwitch.isChecked(), new SettingPopupWindowCallBack() {
                        @Override
                        public void onResult(int isSuccess) {
                            if (isSuccess == LiveChangeSourceListener.SUCCESS) {
                                toastOnUiThread("切换成功");
                            } else {
                                audioSwitch.setChecked(!audioSwitch.isChecked());
                                sendErrorToast(isSuccess);
                            }
                        }
                    });
                }
                dismiss();
            }
        });
        root.addView(modeChange, 1, createLayoutParams());
    }

    //动态设置模式
    public void setMode(boolean isAudio) {
        audioSwitch.setChecked(isAudio);
    }
    // endregion

    //region --------------------- 线路切换功能 ----------------
    //创建线路的view
    private void createLineView() {
        if (lineRoot != null) {
            root.removeView(lineRoot);
        }
        lineRoot = LayoutInflater.from(mContext).inflate(R.layout.setting_item, null);
        TextView title = lineRoot.findViewById(R.id.tv_setting_title);
        title.setText("线路切换:");
        lineContentRoot = lineRoot.findViewById(R.id.ll_setting_content);
        lineContentRoot.removeAllViews();
    }

    //添加线路
    private void addLineView(int length, int indexNum) {
        if (length <= 0) {
            return;
        }
        for (int i = 0; i < length; i++) {
            ChangeLineTextView changeLineTextView = new ChangeLineTextView(mContext, i);
            // 设置选中
            if (indexNum == i) {
                changeLineTextView.setChecked(true, true);
                lineCheckedView = changeLineTextView;
            } else {
                changeLineTextView.setChecked(false, true);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lineContentRoot.addView(changeLineTextView, params);
            // 设置点击监听
            changeLineTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLineItemClick(v);
                }
            });
        }

        if (modeChange != null) {
            root.addView(lineRoot, 2, createLayoutParams());
        } else {
            root.addView(lineRoot, 1, createLayoutParams());
        }
    }

    //处理线路点击事件
    private void handleLineItemClick(View v) {
        final ChangeLineTextView changeLineTextView = (ChangeLineTextView) v;
        if (changeLineTextView.isChecked()) {
            return;
        }
        setLineCheckView(changeLineTextView);
        if (settingPopupInterface != null) {
            settingPopupInterface.onLine(changeLineTextView.getLine(), new SettingPopupWindowCallBack() {
                @Override
                public void onResult(int isSuccess) {
                    if (isSuccess == LiveChangeSourceListener.SUCCESS) {
                        lineCheckedView = changeLineTextView;
                        toastOnUiThread("切换成功");
                    } else {
                        setLineCheckView(lineCheckedView);
                        sendErrorToast(isSuccess);
                    }
                }
            });
        }
        dismiss();
    }

    // 设置默认选项
    private void setLineCheckView(ChangeLineTextView changeLineTextView) {
        if (changeLineTextView == null) return;
        for (int i = 0; i < lineContentRoot.getChildCount(); i++) {
            ChangeLineTextView childAt = (ChangeLineTextView) lineContentRoot.getChildAt(i);
            childAt.setChecked(childAt.getLine() == changeLineTextView.getLine(), true);
        }
    }

    //添加直播线路
    public void addLines(List<LiveLineInfo> lines, int indexNum) {
        if (lines == null){
            return;
        }
        createLineView();
        addLineView(lines.size(), indexNum);
    }

    //添加回放线路
    public void addReplayLines(List<ReplayLineInfo> lines, int indexNum) {
        createLineView();
        addLineView(lines.size(), indexNum);
    }

    //设置当前线路
    public void setLine(int line) {
        for (int i = 0; i < lineContentRoot.getChildCount(); i++) {
            ChangeLineTextView childAt = (ChangeLineTextView) lineContentRoot.getChildAt(i);
            if (childAt.getLine() == line) {
                childAt.setChecked(true, true);
                lineCheckedView = childAt;
            } else {
                childAt.setChecked(false, true);
            }
        }
    }
    // endregion

    //region --------------------- 倍速切换功能 ----------------
    //添加倍速
    public void addSpeed(float[] speeds) {
        if (speeds == null || speeds.length <= 0) {
            Log.e(TAG, "speeds addSpeed param is null");
            return;
        }
        if (speedRoot != null) {
            root.removeView(speedRoot);
        }
        speedRoot = LayoutInflater.from(mContext).inflate(R.layout.setting_item, null);
        TextView title = speedRoot.findViewById(R.id.tv_setting_title);
        title.setText("倍速:");
        speedContentRoot = speedRoot.findViewById(R.id.ll_setting_content);
        speedContentRoot.removeAllViews();
        if (speeds != null && speeds.length > 0) {
            for (int i = 0; i < speeds.length; i++) {
                float speed = speeds[i];
                ChangeSpeedTextView speedTextView = new ChangeSpeedTextView(mContext, speed);
                speedTextView.setTextSize(15);
                if (speed == 0.5f) {
                    speedTextView.setText("0.5x");
                } else if (speed == 1.0f) {
                    speedTextView.setText("1x");
                } else if (speed == 1.25f) {
                    speedTextView.setText("1.25x");
                } else if (speed == 1.5f) {
                    speedTextView.setText("1.5x");
                }
                speedContentRoot.addView(speedTextView);
                if (i != 0) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    speedTextView.setLayoutParams(layoutParams);
                }

                speedTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleSpeedItemClick(v);
                    }
                });
                if (currentSpeed == speed) {
                    speedTextView.setTextColor(Color.parseColor("#FF842F"));
                } else {
                    speedTextView.setTextColor(Color.parseColor("#333333"));
                }
            }
        }

        int i = 1;
        if (modeChange != null) {
            i++;
        }
        if (lineRoot != null) {
            i++;
        }
        if (qualityRoot != null) {
            i++;
        }
        root.addView(speedRoot, i, createLayoutParams());
    }

    //切换倍速播放的点击事件
    private void handleSpeedItemClick(View v) {
        if (v instanceof ChangeSpeedTextView) {
            ChangeSpeedTextView changeSpeedTextView = (ChangeSpeedTextView) v;
            if (currentSpeed == changeSpeedTextView.getSpeed()) {
                return;
            }
            currentSpeed = changeSpeedTextView.getSpeed();
        }
        int num = speedContentRoot.getChildCount();
        for (int i = 0; i < num; i++) {
            View child = speedContentRoot.getChildAt(i);
            if (child instanceof ChangeSpeedTextView) {
                ChangeSpeedTextView changeSpeedTextView = (ChangeSpeedTextView) child;
                if (currentSpeed == changeSpeedTextView.getSpeed()) {
                    changeSpeedTextView.setTextColor(Color.parseColor("#FF842F"));
                } else {
                    changeSpeedTextView.setTextColor(Color.parseColor("#333333"));
                }
            }
        }
        if (settingPopupInterface != null) {
            settingPopupInterface.onSpeed(currentSpeed);
        }
        dismiss();
    }

    //动态设置倍速
    public void setSpeed(float speed) {
        currentSpeed = speed;
        int num = speedContentRoot.getChildCount();
        for (int i = 0; i < num; i++) {
            View child = speedContentRoot.getChildAt(i);
            if (child instanceof ChangeSpeedTextView) {
                ChangeSpeedTextView changeSpeedTextView = (ChangeSpeedTextView) child;
                if (currentSpeed == changeSpeedTextView.getSpeed()) {
                    changeSpeedTextView.setTextColor(Color.parseColor("#FF842F"));
                } else {
                    changeSpeedTextView.setTextColor(Color.parseColor("#333333"));
                }
            }
        }
    }
    // endregion

    //region --------------------- 清晰度切换功能 --------------
    //创建清晰度的view
    private void createQualityView() {
        if (qualityRoot != null) {
            root.removeView(qualityRoot);
        }
        qualityRoot = LayoutInflater.from(mContext).inflate(R.layout.setting_item, null);
        TextView title = qualityRoot.findViewById(R.id.tv_setting_title);
        title.setText("清晰度:");
        qualityContentRoot = qualityRoot.findViewById(R.id.ll_setting_content);
    }

    //添加清晰度textview
    private void addQualityTextView(PortraitQualityTextView qualityTextView) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qualityContentRoot.addView(qualityTextView, params);

        // 设置Item点击监听
        qualityTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleQualityItemClick(v);
            }
        });
    }

    //添加清晰度到根view
    private void addQuality() {
        int i = 1;
        if (modeChange != null) {
            i++;
        }
        if (lineRoot != null) {
            i++;
        }
        root.addView(qualityRoot, i, createLayoutParams());
    }

    // 处理清晰度的点击事件
    private void handleQualityItemClick(View v) {
        final PortraitQualityTextView qualityTextView = (PortraitQualityTextView) v;
        if (qualityTextView.isChecked()) {
            return;
        }
        setQualityCheckView(qualityTextView);
        if (settingPopupInterface != null) {
            settingPopupInterface.onQuality(qualityTextView.getQuality(), qualityTextView.getDesc(), new SettingPopupWindowCallBack() {
                @Override
                public void onResult(int code) {
                    if (code == 0) {
                        qualityCheckedView = qualityTextView;
                    } else {
                        setQualityCheckView(qualityCheckedView);
                        sendErrorToast(code);
                    }
                }
            });
        }
        dismiss();
    }

    // 设置默认选项
    private void setQualityCheckView(PortraitQualityTextView qualityTextView) {
        for (int i = 0; i < qualityContentRoot.getChildCount(); i++) {
            PortraitQualityTextView childAt = (PortraitQualityTextView) qualityContentRoot.getChildAt(i);
            childAt.setChecked(childAt.getQuality() == qualityTextView.getQuality());
        }
    }

    //添加直播清晰度数据
    public void addQuality(List<LiveQualityInfo> videoQuality, LiveQualityInfo currentQuality) {
        createQualityView();
        if (videoQuality == null || currentQuality == null) {
            Log.e(TAG, "LiveQualityView setData param is null");
            return;
        }
        int size = videoQuality.size();
        for (int i = 0; i < size; i++) {
            LiveQualityInfo liveLineParams = videoQuality.get(i);
            PortraitQualityTextView qualityTextView = new PortraitQualityTextView(mContext,
                    liveLineParams.getQuality(), liveLineParams.getDesc());
            // 默认显示第一条数据：原画
            if (qualityCheckedView == null) {
                if (i == 0) {
                    qualityTextView.setChecked(true);
                    qualityCheckedView = qualityTextView;
                }
            } else {
                if (qualityCheckedView.getQuality() == liveLineParams.getQuality()) {
                    qualityTextView.setChecked(true);
                }
            }
            addQualityTextView(qualityTextView);
        }
        addQuality();
    }

    //添加回放清晰度
    public void addQuality(List<ReplayQualityinfo> videoQuality, ReplayQualityinfo currentQuality) {
        createQualityView();
        if (videoQuality == null || currentQuality == null) {
            Log.e(TAG, "LiveQualityView setData param is null");
            return;
        }
        int size = videoQuality.size();
        for (int i = 0; i < size; i++) {
            ReplayQualityinfo replayLineParams = videoQuality.get(i);
            PortraitQualityTextView qualityTextView = new PortraitQualityTextView(mContext,
                    replayLineParams.getQuality(), replayLineParams.getDesc());
            // 默认显示第一条数据：原画
            if (qualityCheckedView == null) {
                if (i == 0) {
                    qualityTextView.setChecked(true);
                    qualityCheckedView = qualityTextView;
                }
            } else {
                if (qualityCheckedView.getQuality() == replayLineParams.getQuality()) {
                    qualityTextView.setChecked(true);
                }
            }
            addQualityTextView(qualityTextView);
        }
        addQuality();
    }

    //动态设置清晰度
    public void setQuality(int quality) {
        for (int i = 0; i < qualityContentRoot.getChildCount(); i++) {
            PortraitQualityTextView childAt = (PortraitQualityTextView) qualityContentRoot.getChildAt(i);
            if (childAt.getQuality() == quality) {
                childAt.setChecked(true);
                qualityCheckedView = childAt;
            } else {
                childAt.setChecked(false);
            }

        }
    }
    // endregion


    // 弹出错误提示
    private void sendErrorToast(int errorCode) {
        if (errorCode == LiveChangeSourceListener.INTERVAL) {
            toastOnUiThread("您切换的太频繁了");
        } else if (errorCode == LiveChangeSourceListener.FAIL) {
            toastOnUiThread("切换失败");
        }
    }

    //设置回调
    public void setSettingPopupInterface(SettingPopupInterface settingPopupInterface) {
        this.settingPopupInterface = settingPopupInterface;
    }
}
