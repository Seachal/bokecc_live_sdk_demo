package com.bokecc.livemodule.replay.room;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.base.BaseReplayRoomLayout;
import com.bokecc.livemodule.popup.callback.SettingPopupWindowCallBack;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.DWReplayRoomListener;
import com.bokecc.livemodule.replay.room.rightview.ReplayRightView;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.livemodule.view.CustomToast;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.listener.LiveChangeSourceListener;
import com.bokecc.sdk.mobile.live.pojo.PracticeRankInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.RecordInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.ReplayChangeSourceListener;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayDot;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLineInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPracticeInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQualityinfo;

import java.util.ArrayList;
import java.util.List;

import static com.bokecc.sdk.mobile.live.Exception.ErrorCode.DOC_PAGE_INFO_FAILED;
import static com.bokecc.sdk.mobile.live.Exception.ErrorCode.GET_META_DATA_FAILED;
import static com.bokecc.sdk.mobile.live.replay.DWLiveReplay.Audio.HAVE_AUDIO_LINE_TURE;

/**
 * 回放直播间信息组件
 */
public class ReplayRoomLayout extends BaseReplayRoomLayout implements DWReplayRoomListener {
    private static final String TAG = "ReplayRoomLayout";

    public ReplayRoomLayout(Context context) {
        super(context);
    }

    public ReplayRoomLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReplayRoomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(boolean isVideoMain) {
        super.init(isVideoMain);
        // 右侧弹出切换清晰度
        mLandQuality.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rightView.showRight(ReplayRightView.RIGHT_SHOW_QUALITY);
            }
        });

        //右侧 切换音频/视频  线路 清晰度 倍速回调
        rightView.setRightCallBack(new ReplayRightView.RightCallBack() {
            @Override
            public void onChangePlayMode(DWLiveReplay.PlayMode playMode) {
                mLandQuality.setVisibility(playMode == DWLiveReplay.PlayMode.PLAY_MODE_TYEP_VIDEO ? VISIBLE : GONE);
                if (replayRoomStatusListener != null) {
                    replayRoomStatusListener.onChangePlayMode(playMode);
                }
                setMode(2, playMode == DWLiveReplay.PlayMode.PLAY_MODE_TYEP_AUDIO);
            }

            @Override
            public void onChangeQuality(int quality, String qualityDesc) {
                setQuality(2, quality, qualityDesc);
            }

            @Override
            public void onSpeedChange(float speed) {
                DWLiveReplay.getInstance().setSpeed(speed);
                setSpeed(2, speed);
            }

            @Override
            public void onChangeLine(int line) {
                setLine(2, line);
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
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    rightView.showRight(ReplayRightView.RIGHT_SHOW_LINE);
                } else {
                    settingPopupWindow.show(activity, getRootView(), Gravity.BOTTOM, 0, 0);
                }

            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //  横屏
            mLandQuality.setVisibility(VISIBLE);
        } else {
            // 竖屏
            mLandQuality.setVisibility(GONE);
        }

    }

    //****************************** 回放直播间监听 用于CoreHandler 触发相关逻辑 ***************************/

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
    public void videoPrepared() {
        if (!DWLiveReplay.getInstance().isPlayVideo()) {
            startTimerTask();
        }
        setPlaySeekBarCanSeek(true);
        // 初始化打点View
        initDotView();
        // 开始播放重置播放狀態
        onVideoPrepared();
    }

    @Override
    public void startRending() {
        startTimerTask();

    }

    @Override
    public void bufferStart() {
        stopTimerTask();
    }

    @Override
    public void onPlayComplete() {
        mTopLayout.post(new Runnable() {
            @Override
            public void run() {
                mTopLayout.setVisibility(INVISIBLE);
                mBottomLayout.setVisibility(INVISIBLE);
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText("播放结束");
                mTryBtn.setText("重新播放");
                mPlayIcon.setSelected(false);
                mLandPlayIcon.setSelected(false);
                DWReplayCoreHandler.getInstance().getPlayer().seekTo(0);
                setPlayBarProgress(0);
                //将倍速初始化
                if (DWReplayCoreHandler.getInstance().getPlayer() != null)
                    DWReplayCoreHandler.getInstance().getPlayer().setSpeed(1.0f);
                settingPopupWindow.setSpeed(1.0f);
                rightView.setSpeed(1.0f);
                setSpeedText(1);
                stopTimerTask();
            }
        });
    }

    @Override
    public void onSeekComplete() {
        setPlaySeekBarCanSeek(true);
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
                if (DWReplayCoreHandler.getInstance().getPlayer() != null)
                    DWReplayCoreHandler.getInstance().getPlayer().setSpeed(1.0f);
                settingPopupWindow.setSpeed(1.0f);
                rightView.setSpeed(1.0f);
                setSpeedText(1);
            }
        });
    }

    @Override
    public void onException(final DWLiveException exception) {
        post(new Runnable() {
            @Override
            public void run() {
                if (exception.getErrorCode() != GET_META_DATA_FAILED && exception.getErrorCode() != DOC_PAGE_INFO_FAILED) {
                    CustomToast.showToast(mContext, exception.getMessage(), Toast.LENGTH_SHORT);
                } else {
                    CustomToast.showToast(mContext, "元数据信息获取失败：" + exception.getMessage(), Toast.LENGTH_SHORT);
                }
            }
        });

    }

    @Override
    public void onHDReceivedVideoQuality(final List<ReplayQualityinfo> videoQuality, final ReplayQualityinfo currentQuality) {
        post(new Runnable() {
            @Override
            public void run() {

                rightView.showQuality(videoQuality, currentQuality);
                settingPopupWindow.addQuality(videoQuality, currentQuality);
            }
        });
    }

    @Override
    public void onHDReceivedVideoAudioLines(final List<ReplayLineInfo> lines, final int currentLineIndex) {
        post(new Runnable() {
            @Override
            public void run() {
                rightView.showVideoAudioLines(lines, currentLineIndex);
                settingPopupWindow.addReplayLines(lines, currentLineIndex);
            }
        });
    }

    @Override
    public void onHDAudioMode(final DWLiveReplay.Audio hasAudio) {
        post(new Runnable() {
            @Override
            public void run() {
                rightView.showAudio(hasAudio);
                if (hasAudio == HAVE_AUDIO_LINE_TURE) {
                    settingPopupWindow.addModeChange();
                }
            }
        });
    }

    @Override
    public void onHDReceivePracticeList(List<ReplayPracticeInfo> list) {
        if (list.size() == 0) {
            CustomToast.showToast(getContext(), "本场直播无随堂测数据", Toast.LENGTH_LONG);
        } else {
            CustomToast.showToast(getContext(), "本场直播进行了" + list.size() + "次随堂测", Toast.LENGTH_LONG);
            for (int i = 0; i < list.size(); i++) {
                ReplayPracticeInfo replayPracticeInfo = list.get(i);
                PracticeRankInfo rankInfo = replayPracticeInfo.getRankInfo();
                PracticeStatisInfo statisInfo = replayPracticeInfo.getStatisInfo();
                Log.i(TAG, rankInfo.toString());
                Log.i(TAG, statisInfo.toString());
            }

        }


    }

    @Override
    public void onHDReplayDotList(List<ReplayDot> dotList) {
        this.dotBeans = dotList;
        initDotView();
    }

    @Override
    public void onPageInfoList(ArrayList<ReplayPageInfo> infoList) {
//        CustomToast.showToast(getContext(), "翻页数据onPageInfoList回调成功:翻页条数：" + infoList.size(), Toast.LENGTH_LONG);
    }


    //****************************************** 重写实现方法 *************************************/

    @Override
    protected void playInit() {
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler == null) {
            return;
        }
        // 设置事件监听
        dwReplayCoreHandler.setReplayRoomListener(this);
        // 设置进度记忆控件
        dwReplayCoreHandler.setProRecordJumpTextView(progress_record);
        mLandQuality.setVisibility(VISIBLE);
        // 是否显示文档
        if (!dwReplayCoreHandler.hasDocView()) {
            mVideoDocSwitch.setVisibility(GONE);
            mLandVideoDocSwitch.setVisibility(GONE);
        }
        // 显示跑马灯
        if (dwReplayCoreHandler.isOpenMarquee()) {
            mMarqueeView = findViewById(R.id.marquee_view);
            mMarqueeView.setVisibility(VISIBLE);
            setMarquee(DWLiveReplay.getInstance().getViewer().getMarquee());
        }
        // 显示标题
        RecordInfo recordInfo = DWLiveReplay.getInstance().getRoomRecordInfo();
        if (recordInfo != null) {
            mTitle.setText(recordInfo.getTitle());
        }
    }

    @Override
    protected void playRetry(boolean updateStream) {
        DWReplayCoreHandler instance = DWReplayCoreHandler.getInstance();
        if (instance != null) {
            int progress = getPlaySeekBar().getProgress();
            instance.retryReplay(progress, updateStream);
        }
    }

    @Override
    protected void playChangeState(long progress) {
        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        // 判断是否为空
        if (replayCoreHandler == null || replayCoreHandler.getPlayer() == null) {
            return;
        }
        // 修改播放状态
        if (mPlayIcon.isSelected()) {
            mPlayIcon.setSelected(false);
            mLandPlayIcon.setSelected(false);
            replayCoreHandler.pause();
        } else {
            mPlayIcon.setSelected(true);
            mLandPlayIcon.setSelected(true);
            if (progress > 0) {
                DWLiveReplay.getInstance().setLastPosition(progress);
            }
            mTipsLayout.setVisibility(GONE);
            replayCoreHandler.start();
        }
    }


    @Override
    protected void playChangeSpeed() {

    }

    @Override
    protected DWReplayPlayer getPlayer() {
        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        if (replayCoreHandler == null) {
            return null;
        }
        return replayCoreHandler.getPlayer();
    }

    @Override
    protected int getDocumentDisplayMode() {
        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            return replayCoreHandler.getDocumentDisplayMode();
        }
        return 1;
    }

    @Override
    protected boolean isHasDoc() {
        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            return replayCoreHandler.hasDocView();
        }
        return false;
    }


    @Override
    public void onSpeed(float speed) {
        DWLiveReplay.getInstance().setSpeed(speed);
        setSpeed(1, speed);
    }

    @Override
    public void onPlayMode(final boolean isAudio, final SettingPopupWindowCallBack callBack) {
        DWLiveReplay.getInstance().changePlayMode(isAudio ? DWLiveReplay.PlayMode.PLAY_MODE_TYEP_AUDIO : DWLiveReplay.PlayMode.PLAY_MODE_TYEP_VIDEO, new ReplayChangeSourceListener() {
            @Override
            public void onChange(int success) {
                if (replayRoomStatusListener != null) {
                    replayRoomStatusListener.onChangePlayMode(isAudio ? DWLiveReplay.PlayMode.PLAY_MODE_TYEP_AUDIO : DWLiveReplay.PlayMode.PLAY_MODE_TYEP_VIDEO);
                }
                if (success == 0) {
                    setMode(1, isAudio);
                }
                if (callBack != null) {
                    callBack.onResult(success);
                }
            }
        });
    }

    @Override
    public void onQuality(final int quality, final String desc, final SettingPopupWindowCallBack callBack) {
        DWReplayCoreHandler instance = DWReplayCoreHandler.getInstance();
        if (instance != null) {
            instance.changeQuality(quality, new ReplayChangeSourceListener() {
                @Override
                public void onChange(int success) {
                    if (success == 0) {
                        setQuality(1, quality, desc);
                    }
                    if (callBack != null) {
                        callBack.onResult(success);
                    }
                }
            });
        }

    }

    @Override
    public void onLine(final int line, final SettingPopupWindowCallBack callBack) {
        DWReplayCoreHandler instance = DWReplayCoreHandler.getInstance();
        if (instance != null) {
            instance.changeLine(line, new ReplayChangeSourceListener() {
                @Override
                public void onChange(int success) {
                    if (success == LiveChangeSourceListener.SUCCESS) {
                        setLine(1, line);
                    }
                    if (callBack != null) {
                        callBack.onResult(success);
                    }
                }
            });
        }
    }

    /**
     * 设置清晰度的界面
     *
     * @param type    1为竖屏切换清晰度成功之后   2为横屏切换清晰度之后
     * @param quality 清晰度
     */
    private void setQuality(int type, int quality, String desc) {
        if (type == 1) {
            rightView.setQuality(quality);
        } else if (type == 2) {
            settingPopupWindow.setQuality(quality);
        }
        mLandDurationView.setText(desc);
    }

    /**
     * 设置线路的界面
     *
     * @param type 1为竖屏切换线路成功之后   2为横屏切换线路之后
     * @param line 线路
     */
    private void setLine(int type, int line) {
        if (type == 1) {
            rightView.setLine(line);
        } else if (type == 2) {
            settingPopupWindow.setLine(line);
        }
    }

    /**
     * 设置播放模式的界面
     *
     * @param type    1为竖屏切换模式成功之后   2为横屏切换模式之后
     * @param isAudio 是否是音频
     */
    private void setMode(int type, boolean isAudio) {
        if (type == 1) {
            rightView.setMode(isAudio);
        } else if (type == 2) {
            settingPopupWindow.setMode(isAudio);
        }
    }

    /**
     * 设置播放模式的界面
     *
     * @param type  1为竖屏切换倍速成功之后   2为横屏切换倍速之后
     * @param speed 倍速
     */
    private void setSpeed(int type, float speed) {
        if (type == 1) {
            rightView.setSpeed(speed);
        } else if (type == 2) {
            settingPopupWindow.setSpeed(speed);
        }
        setSpeedText(speed);
    }
}