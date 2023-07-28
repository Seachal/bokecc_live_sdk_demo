package com.bokecc.livemodule.replay.room.rightview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.bokecc.livemodule.BuildConfig;
import com.bokecc.livemodule.R;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.utils.DensityUtil;
import com.bokecc.livemodule.view.ChangeLineTextView;
import com.bokecc.livemodule.view.RightBaseView;
import com.bokecc.sdk.mobile.live.listener.LiveChangeSourceListener;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.ReplayChangeSourceListener;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLineInfo;

import java.util.List;

public class ReplayLineView extends RightBaseView {
    private LinearLayout ll_line;
    private LineCallBack lineCallBack;
    private Switch mSwitch;
    private View mVideoAudioSwitchRoot;

    private ChangeLineTextView checkedView;
    private PlayModeCallBack playModeCallBack;

    public ReplayLineView(Context context) {
        super(context);
    }

    @Override
    public void initViews() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.right_line_view, null);
        mSwitch = inflate.findViewById(R.id.audio_switch);
        mVideoAudioSwitchRoot = inflate.findViewById(R.id.ll_video_audio_root);
        ll_line = inflate.findViewById(R.id.ll_line);
        this.addView(inflate);

        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handSwitchClick(mSwitch.isChecked());

            }
        });
    }

    /**
     * 设置 线路切换 的回调
     *
     * @param lineCallBack LineCallBack
     */
    public void setLineCallBack(LineCallBack lineCallBack) {
        this.lineCallBack = lineCallBack;
    }

    /**
     * 设置 播放模式 的回调
     *
     * @param playModeCallBack PlayModeCallBack
     */
    public void setPlayModeCallBack(PlayModeCallBack playModeCallBack) {
        this.playModeCallBack = playModeCallBack;
    }

    /**
     * 显示切换音频模式
     */
    public void showSwitch() {
        mVideoAudioSwitchRoot.setVisibility(VISIBLE);
    }

    /**
     * 隐藏切换音频模式
     */
    public void hideSwitch() {
        mVideoAudioSwitchRoot.setVisibility(GONE);
    }

    /**
     * 设置数据，展示线路列表
     */
    public void setData(List<ReplayLineInfo> lines, int indexNum) {
        ll_line.removeAllViews();
        if (lines == null || lines.size() <= 0) {
            return;
        }
        // todo dds_test 测试空线路
//        if (BuildConfig.DEBUG) {
//            ReplayLineInfo liveLineInfo = new ReplayLineInfo();
//            liveLineInfo.setLine("line5");
//            lines.add(liveLineInfo);
//        }
        for (int i = 0; i < lines.size(); i++) {
            ChangeLineTextView changeLineTextView = new ChangeLineTextView(getContext(), i);
            // 设置选中
            // 设置选中
            if (indexNum == i) {
                changeLineTextView.setChecked(true, false);
                checkedView = changeLineTextView;
            } else {
                changeLineTextView.setChecked(false, false);
            }
            LinearLayout.LayoutParams params = new LayoutParams(DensityUtil.dp2px(getContext(), 60), ViewGroup.LayoutParams.MATCH_PARENT);
            ll_line.addView(changeLineTextView, params);
            // 设置点击监听
            changeLineTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleItemClick(v);
                }
            });
        }
    }

    // 处理线路点击事件
    private void handleItemClick(View v) {
        final ChangeLineTextView changeLineTextView = (ChangeLineTextView) v;
        if (changeLineTextView.isChecked()) {
            return;
        }
        setCheckView(changeLineTextView);
        DWReplayCoreHandler instance = DWReplayCoreHandler.getInstance();
        if (instance != null) {
            instance.changeLine(changeLineTextView.getLine(), new ReplayChangeSourceListener() {
                @Override
                public void onChange(int success) {
                    if (success == LiveChangeSourceListener.SUCCESS) {
                        checkedView = changeLineTextView;
                        if (lineCallBack != null) {
                            lineCallBack.onLineChange(checkedView.getLine());
                        }
                    } else {
                        setCheckView(checkedView);
                        sendErrorToast(success);
                    }
                }
            });
        }

    }

    // 处理切换音频模式点击事件
    private void handSwitchClick(final boolean isCheck) {
        DWReplayCoreHandler instance = DWReplayCoreHandler.getInstance();
        if (instance != null) {
            // 切换到视频模式
            instance.changePlayMode(isCheck ? DWLiveReplay.PlayMode.PLAY_MODE_TYEP_AUDIO : DWLiveReplay.PlayMode.PLAY_MODE_TYEP_VIDEO, new ReplayChangeSourceListener() {
                @Override
                public void onChange(int success) {
                    if (success == LiveChangeSourceListener.SUCCESS) {
                        if (playModeCallBack != null) {
                            playModeCallBack.onPlayModeChange(isCheck ? DWLiveReplay.PlayMode.PLAY_MODE_TYEP_AUDIO : DWLiveReplay.PlayMode.PLAY_MODE_TYEP_VIDEO);
                        }
                    } else {
                        mSwitch.setChecked(!isCheck);
                        sendErrorToast(success);
                    }
                }
            });
        }


    }

    // 弹出错误信息
    private void sendErrorToast(int errorCode) {
        if (errorCode == LiveChangeSourceListener.INTERVAL) {
            toastOnUiThread("您切换的太频繁了");
        } else if (errorCode == LiveChangeSourceListener.FAIL) {
            toastOnUiThread("切换失败");
        }
    }

    // 设置默认选项
    private void setCheckView(ChangeLineTextView changeLineTextView) {
        if (changeLineTextView == null) return;
        for (int i = 0; i < ll_line.getChildCount(); i++) {
            ChangeLineTextView childAt = (ChangeLineTextView) ll_line.getChildAt(i);
            childAt.setChecked(childAt.getLine() == changeLineTextView.getLine(), false);
        }
    }

    public void setLine(int line) {
        for (int i = 0; i < ll_line.getChildCount(); i++) {
            ChangeLineTextView childAt = (ChangeLineTextView) ll_line.getChildAt(i);
            if (childAt.getLine() == line) {
                childAt.setChecked(true, false);
                checkedView = childAt;
            } else {
                childAt.setChecked(false, false);
            }

        }
    }

    public void setMode(boolean isAudio) {
        mSwitch.setChecked(isAudio);
    }

    // 切换线路成功的回调
    public interface LineCallBack {
        void onLineChange(int line);
    }

    // 切换播放模式成功的回调
    public interface PlayModeCallBack {
        void onPlayModeChange(DWLiveReplay.PlayMode playMode);
    }
}
