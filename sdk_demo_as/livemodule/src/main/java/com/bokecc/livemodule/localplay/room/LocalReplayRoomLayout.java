package com.bokecc.livemodule.localplay.room;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.bokecc.livemodule.base.BaseReplayRoomLayout;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.localplay.DWLocalReplayRoomListener;
import com.bokecc.livemodule.popup.callback.SettingPopupWindowCallBack;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.room.rightview.ReplayRightView;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;


/**
 * 回放直播间信息组件
 */
public class LocalReplayRoomLayout extends BaseReplayRoomLayout implements DWLocalReplayRoomListener {

    private final static String TAG = LocalReplayRoomLayout.class.getSimpleName();


    public LocalReplayRoomLayout(Context context) {
        this(context, null);
    }

    public LocalReplayRoomLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public LocalReplayRoomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(boolean isVideoMain) {
        super.init(isVideoMain);
        //右侧 切换音频/视频  线路 清晰度 倍速回调
        rightView.setRightCallBack(new ReplayRightView.RightCallBack() {
            @Override
            public void onChangePlayMode(DWLiveReplay.PlayMode playMode) {

            }

            @Override
            public void onChangeQuality(int quality, String qualityDesc) {

            }

            @Override
            public void onSpeedChange(float speed) {
                DWLiveLocalReplay.getInstance().setSpeed(speed);
                setSpeed(2,speed);
            }

            @Override
            public void onChangeLine(int line) {

            }

            @Override
            public void onClose() {
                removeCallbacks(hideRunnable);
                if (!mTopLayout.isShown()) {
                    show();
                }
                postDelayed(hideRunnable, HIDE_TIME);
            }
        });
//更多 以及弹出框初始化
        mVideoDocMore.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    settingPopupWindow.show(activity,getRootView(), Gravity.BOTTOM,0,0);
                }
            }
        });
    }
    //****************************** 回放直播间监听 用于CoreHandler 触发相关逻辑 ***************************/

    /**
     * 更新直播间标题
     */
    @Override
    public void updateRoomTitle(final String title) {
        post(new Runnable() {
            @Override
            public void run() {
                mTitle.setText(title);
            }
        });
    }

    /**
     * 回放播放初始化已经完成
     */
    @Override
    public void videoPrepared() {
        startTimerTask();
        setPlaySeekBarCanSeek(true);

        DWLocalReplayCoreHandler replayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (replayCoreHandler == null) {
            return;
        }
        if (!replayCoreHandler.hasDocView()) {
            mVideoDocSwitch.setVisibility(GONE);
            mLandVideoDocSwitch.setVisibility(GONE);
        } else {
            mVideoDocSwitch.setVisibility(VISIBLE);
            mLandVideoDocSwitch.setVisibility(VISIBLE);
        }
        // 开始播放重置播放狀態
        onVideoPrepared();

    }

    /**
     * 更新缓冲进度
     *
     * @param percent 缓冲百分比
     */
    @Override
    public void updateBufferPercent(final int percent) {
        post(new Runnable() {
            @Override
            public void run() {
                int result = (int) ((double) getPlaySeekBar().getMax() * percent / 100);
                setPlayBarSecondaryProgress(result);
            }
        });
    }

    /**
     * 展示播放的视频时长
     */
    @Override
    public void showVideoDuration(final long playerDuration) {
        post(new Runnable() {
            @Override
            public void run() {
                long playSecond = Math.round((double) playerDuration / 1000) * 1000;
                String formatTime = TimeUtil.getFormatTime(playSecond);
                mDurationView.setText(formatTime);
                mLandDurationView.setText(formatTime);
                setSeekBarMax((int) playSecond);
            }
        });
    }

    @Override
    public void onPlayComplete() {
        post(new Runnable() {
            @Override
            public void run() {
                stopTimerTask();
                mTopLayout.setVisibility(INVISIBLE);
                mBottomLayout.setVisibility(INVISIBLE);
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText("播放结束");
                mTryBtn.setText("重新播放");
                mPlayIcon.setSelected(false);
                mLandPlayIcon.setSelected(false);
                DWLocalReplayCoreHandler.getInstance().getPlayer().seekTo(0);
                setPlayBarProgress(0);
                setSpeedText(1);
                Log.d(TAG, "onPlayComplete");
                DWLocalReplayCoreHandler.getInstance().stop();

            }
        });
    }

    @Override
    public void onPlayError(int code) {
        stopTimerTask();
        post(new Runnable() {
            @Override
            public void run() {
                mTopLayout.setVisibility(INVISIBLE);
                mBottomLayout.setVisibility(INVISIBLE);
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText("播放失败");
                mTryBtn.setText("点击重试");
                //将倍速初始化
                if (DWLocalReplayCoreHandler.getInstance().getPlayer() != null)
                    DWLocalReplayCoreHandler.getInstance().getPlayer().setSpeed(1.0f);
                setSpeedText(1);
                DWLocalReplayCoreHandler.getInstance().stop();
            }
        });
    }

    @Override
    public void onSeekComplete() {
        setPlaySeekBarCanSeek(true);
    }

    //****************************************** 重写实现方法 *************************************/

    @Override
    protected void playInit() {
        DWLocalReplayCoreHandler replayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (replayCoreHandler == null) {
            return;
        }
        // 设置回调监听事件
        replayCoreHandler.setLocalDwReplayRoomListener(this);
        // 设置进度记忆控件
        replayCoreHandler.setProRecordJumpTextView(progress_record);


    }

    @Override
    protected void playRetry(boolean updateStream) {
        DWLocalReplayCoreHandler instance = DWLocalReplayCoreHandler.getInstance();
        if (instance != null) {
            int progress = getPlaySeekBar().getProgress();
            instance.retryReplay(progress);
        }
    }

    @Override
    protected void playChangeState(long progress) {
        DWLocalReplayCoreHandler localReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        // 判断是否为空
        if (localReplayCoreHandler == null || localReplayCoreHandler.getPlayer() == null) {
            return;
        }
        // 修改播放状态
        if (mPlayIcon.isSelected()) {
            mPlayIcon.setSelected(false);
            // 暂停播放
            DWLiveLocalReplay.getInstance().pause();
        } else {
            mPlayIcon.setSelected(true);
            // 开始播放
            DWLiveLocalReplay.getInstance().start();
        }
    }


    @Override
    protected void playChangeSpeed() {
        DWLocalReplayCoreHandler localReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        float speed = localReplayCoreHandler.getPlayer().getSpeed(0f);
        if (speed == 0.5f) {
            localReplayCoreHandler.getPlayer().setSpeed(1.0f);
        } else if (speed == 1.0f) {
            localReplayCoreHandler.getPlayer().setSpeed(1.25f);
        } else if (speed == 1.25f) {
            localReplayCoreHandler.getPlayer().setSpeed(1.5f);
        } else {
            localReplayCoreHandler.getPlayer().setSpeed(0.5f);
        }
        float speed1 = localReplayCoreHandler.getPlayer().getSpeed(1.0f);
        rightView.setSpeed(speed1);
        setSpeedText(speed1);
    }

    @Override
    protected DWReplayPlayer getPlayer() {
        DWLocalReplayCoreHandler replayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (replayCoreHandler == null) {
            return null;
        }
        return replayCoreHandler.getPlayer();
    }

    @Override
    protected int getDocumentDisplayMode() {
        DWLocalReplayCoreHandler replayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            return replayCoreHandler.getDocumentDisplayMode();
        }
        return 1;
    }

    @Override
    public void onSpeed(float speed) {
        DWLiveLocalReplay.getInstance().setSpeed(speed);
        setSpeed(1,speed);
    }

    /**
     * 设置播放模式的界面
     * @param type  1为竖屏切换倍速成功之后   2为横屏切换倍速之后
     * @param speed 倍速
     */
    private void setSpeed(int type,float speed){
        if (type == 1){
            rightView.setSpeed(speed);
        }else if (type == 2){
            settingPopupWindow.setSpeed(speed);
        }
        setSpeedText(speed);
    }
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mVideoDocMore.setVisibility(GONE);
        } else {
            // 竖屏
            mVideoDocMore.setVisibility(VISIBLE);
        }
    }
    @Override
    protected boolean isHasDoc() {
        DWLocalReplayCoreHandler replayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            return replayCoreHandler.hasDocView();
        }
        return false;
    }


}
